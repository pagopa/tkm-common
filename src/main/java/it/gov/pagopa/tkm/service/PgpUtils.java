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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Iterator;

@Service
public class PgpUtils {

    @Value("${keyvault.readQueuePrvPgpKey}")
    private String privateKey;

    @Value("${keyvault.readQueuePubPgpKey}")
    private String publicKeyFromKeyVault;

    @Value("${keyvault.readQueuePrvPgpKeyPassphrase:null}")
    private char[] passphrase;

    private PGPPublicKey publicKey;

    private final BouncyCastleProvider provider = new BouncyCastleProvider();

    @PostConstruct
    public void init() throws IOException, PGPException {
        publicKey = readPublicKey(new ByteArrayInputStream(publicKeyFromKeyVault.getBytes()));
    }

    public String encrypt(String message) throws PGPException {

        final ByteArrayInputStream in = new ByteArrayInputStream(message.getBytes());
        final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        final PGPLiteralDataGenerator literal = new PGPLiteralDataGenerator();
        final PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(CompressionAlgorithmTags.ZIP);
        try (final OutputStream pOut =
                     literal.open(comData.open(bOut), PGPLiteralData.BINARY, "filename", in.available(), new Date())) {
            Streams.pipeAll(in, pOut);
        } catch (Exception e) {
            throw new PGPException("Error in encrypt", e);
        }
        final byte[] bytes = bOut.toByteArray();
        final PGPEncryptedDataGenerator generator = new PGPEncryptedDataGenerator(
                new JcePGPDataEncryptorBuilder(SymmetricKeyAlgorithmTags.AES_256).setWithIntegrityPacket(true)
                        .setSecureRandom(
                                new SecureRandom())

        );
        generator.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(publicKey));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStream theOut = new ArmoredOutputStream(out);
        try (OutputStream cOut = generator.open(theOut, bytes.length)) {
            cOut.write(bytes);
            theOut.close();
            return out.toString();
        } catch (Exception e) {
            throw new PGPException("Error in encrypt", e);
        }

    }

    private PGPPublicKey readPublicKey(InputStream in) throws IOException, PGPException {
        PGPPublicKeyRingCollection keyRingCollection = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(in), new JcaKeyFingerprintCalculator());
        PGPPublicKey pgpPublicKey = null;
        Iterator<PGPPublicKeyRing> rIt = keyRingCollection.getKeyRings();
        while (pgpPublicKey == null && rIt.hasNext()) {
            PGPPublicKeyRing kRing = rIt.next();
            Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
            while (pgpPublicKey == null && kIt.hasNext()) {
                PGPPublicKey key = kIt.next();
                if (key.isEncryptionKey()) {
                    pgpPublicKey = key;
                }
            }
        }
        if (pgpPublicKey == null) {
            throw new IllegalArgumentException("Can't find public key in the key ring.");
        }
        return pgpPublicKey;
    }

    public String decrypt(String encryptedMessage) throws IOException, PGPException {
        if (StringUtils.isBlank(encryptedMessage)) {
            return null;
        }
        byte[] encryptedMessageBytes = encryptedMessage.getBytes();
        byte[] privateKeyBytes = privateKey.getBytes();
        PGPObjectFactory encryptedObjectFactory = new PGPObjectFactory(PGPUtil.getDecoderStream(new ByteArrayInputStream(encryptedMessageBytes)), new JcaKeyFingerprintCalculator());
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
        PGPSecretKeyRingCollection secretKeyRingCollection = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(new ByteArrayInputStream(privateKeyBytes)), new JcaKeyFingerprintCalculator());
        while (pgpPrivateKey == null && iterator.hasNext()) {
            publicKeyEncryptedData = (PGPPublicKeyEncryptedData) iterator.next();
            PGPSecretKey secretKey = secretKeyRingCollection.getSecretKey(publicKeyEncryptedData.getKeyID());
            if (secretKey != null) {
                pgpPrivateKey = secretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider(provider).build(passphrase));
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
        InputStream literalDataStream = literalData.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int ch;
        while ((ch = literalDataStream.read()) >= 0) {
            outputStream.write(ch);
        }
        outputStream.close();
        return outputStream.toString();
    }

}
