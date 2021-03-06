package it.gov.pagopa.tkm.service;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.*;
import org.bouncycastle.util.io.Streams;

import java.io.*;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Iterator;

public class PgpStaticUtils {
    private PgpStaticUtils() {
    }

    private static final BouncyCastleProvider provider = new BouncyCastleProvider();

    public static void decryptToFile(String fileInput, String privateKey, String passphrase, String fileOutput) throws PGPException {
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(fileInput))) {
            InputStream literalDataStream = getInputStreamInClear(passphrase, inputStream, privateKey);
            int ch;
            try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fileOutput))) {
                while ((ch = literalDataStream.read()) >= 0) {
                    outputStream.write(ch);
                }
            }
        } catch (Exception e) {
            throw new PGPException(e.getMessage(), e);
        }
    }

    public static String decrypt(String encryptedMessage, String privateKey, String passphrase) throws PGPException {
        if (StringUtils.isBlank(encryptedMessage)) {
            return null;
        }
        try {
            byte[] encryptedMessageBytes = encryptedMessage.getBytes();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(encryptedMessageBytes);
            InputStream literalDataStream = getInputStreamInClear(passphrase, inputStream, privateKey);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int ch;
            while ((ch = literalDataStream.read()) >= 0) {
                outputStream.write(ch);
            }
            outputStream.close();
            return outputStream.toString();
        } catch (Exception e) {
            throw new PGPException(e.getMessage(), e);
        }
    }

    public static String encrypt(String message, String publicKey) throws PGPException {
        if (StringUtils.isBlank(message)) {
            return null;
        }
        try {
            final ByteArrayInputStream in = new ByteArrayInputStream(message.getBytes());
            final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            final PGPLiteralDataGenerator literal = new PGPLiteralDataGenerator();
            final PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(CompressionAlgorithmTags.ZIP);
            try (final OutputStream pOut = literal.open(comData.open(bOut), PGPLiteralData.BINARY, "filename", in.available(), new Date())) {
                Streams.pipeAll(in, pOut);
                comData.close();
            }
            final byte[] bytes = bOut.toByteArray();
            final PGPEncryptedDataGenerator generator = new PGPEncryptedDataGenerator(
                    new JcePGPDataEncryptorBuilder(SymmetricKeyAlgorithmTags.AES_256).setWithIntegrityPacket(true)
                            .setSecureRandom(
                                    new SecureRandom())

            );
            generator.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(readPublicKey(publicKey.getBytes())));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputStream theOut = new ArmoredOutputStream(out);
            try (OutputStream cOut = generator.open(theOut, bytes.length)) {
                cOut.write(bytes);
            }
            theOut.close();
            return out.toString();
        } catch (Exception e) {
            throw new PGPException("Error in encrypt", e);
        }
    }

    private static PGPPublicKey readPublicKey(byte[] publicKeyByte) throws IOException, PGPException {
        InputStream decoderStream = PGPUtil.getDecoderStream(new ByteArrayInputStream(publicKeyByte));
        PGPPublicKeyRingCollection keyRingCollection = new PGPPublicKeyRingCollection(decoderStream, new JcaKeyFingerprintCalculator());
        PGPPublicKey publicKey = null;
        Iterator<PGPPublicKeyRing> rIt = keyRingCollection.getKeyRings();
        while (publicKey == null && rIt.hasNext()) {
            PGPPublicKeyRing kRing = rIt.next();
            Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
            while (publicKey == null && kIt.hasNext()) {
                PGPPublicKey key = kIt.next();
                if (key.isEncryptionKey()) {
                    publicKey = key;
                }
            }
        }
        if (publicKey == null) {
            throw new IllegalArgumentException("Can't find public key in the key ring.");
        }
        return publicKey;
    }


    private static InputStream getInputStreamInClear(String passphrase, InputStream inputStream, String privateKey) throws IOException, PGPException {
        PGPObjectFactory encryptedObjectFactory = new PGPObjectFactory(PGPUtil.getDecoderStream(inputStream), new JcaKeyFingerprintCalculator());
        PGPEncryptedDataList pgpEncryptedDataList;
        Object object = encryptedObjectFactory.nextObject();
        if (object instanceof PGPEncryptedDataList) {
            pgpEncryptedDataList = (PGPEncryptedDataList) object;
        } else {
            pgpEncryptedDataList = (PGPEncryptedDataList) encryptedObjectFactory.nextObject();
        }
        Iterator iterator = pgpEncryptedDataList.getEncryptedDataObjects();
        PGPPrivateKey pgpPrivateKey = null;
        PGPPublicKeyEncryptedData publicKeyEncryptedData = null;
        PGPSecretKeyRingCollection secretKeyRingCollection = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(new ByteArrayInputStream(privateKey.getBytes())), new JcaKeyFingerprintCalculator());
        while (pgpPrivateKey == null && iterator.hasNext()) {
            publicKeyEncryptedData = (PGPPublicKeyEncryptedData) iterator.next();
            PGPSecretKey secretKey = secretKeyRingCollection.getSecretKey(publicKeyEncryptedData.getKeyID());
            if (secretKey != null) {
                pgpPrivateKey = secretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider(provider).build(passphrase.toCharArray()));
            }
        }
        if (pgpPrivateKey == null) {
            throw new IllegalArgumentException("Secret key for message not found.");
        }
        InputStream decryptedMessageStream = publicKeyEncryptedData.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider(provider).build(pgpPrivateKey));
        PGPObjectFactory decryptedObjectFactory = new PGPObjectFactory(decryptedMessageStream, new JcaKeyFingerprintCalculator());
        Object nextObject = decryptedObjectFactory.nextObject();
        if (nextObject instanceof PGPCompressedData) {
            PGPCompressedData compressedData = (PGPCompressedData) nextObject;
            nextObject = new PGPObjectFactory(PGPUtil.getDecoderStream(compressedData.getDataStream()), new JcaKeyFingerprintCalculator()).nextObject();
        } else if (!(nextObject instanceof PGPLiteralData)) {
            throw new IllegalArgumentException("Object cannot be cast to PGPCompressedData or PGPLiteralData");
        }
        PGPLiteralData literalData = (PGPLiteralData) nextObject;
        return literalData.getInputStream();
    }
}