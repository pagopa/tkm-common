package it.gov.pagopa.tkm.service;


import com.google.common.io.ByteStreams;
import org.bouncycastle.openpgp.PGPException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class PgpStaticUtilsTest {

    private static final String PASSPHRASE = "passphrase";
    private static final String DECRYPTED_MESSAGE = "message";
    private static final String ENCRYPTED_MESSAGE_PGP = "-----BEGIN PGP MESSAGE-----\n" +
            "\n" +
            "hQIMA9/2VjHdvNtGAQ//T1Xh4uuPMyqLjzBt1sicAbBNrRW3+JhQGVTG31P0kyTJ\n" +
            "w8d+p5UsMXPSojU5K0cR4H/MAu5XydIYdozdzYJXuXhWDc0YpGDYCHyl98iBB3Ff\n" +
            "WhSKj8T7TqdKgT3jqorK2ZPCanaXbwmyj+CWTYoOtgMQje2TW3hQy9UIkdDi7Qjw\n" +
            "fpljkmEbdygWrL6OAQTIhE4Unt4ljYXdpGD3dIkYjSphqIzzgb5nK2FZQNnBn4By\n" +
            "iC3yup+DxI/yituR6Tepguaqe7npnUEMrtoXGRN9nvbzgHfTKiWly0LRSVafDMgE\n" +
            "cHYliF48QkFSPYV4Q4yGrt9RtYgOJcdPabXYKhcvfKPDeH2N2svrKEkgXeq3I5EQ\n" +
            "HiQzXiko5Zq88F11mHHm9oumZKdRN2zZDSaiIg1BX92Pyh4VmGJj+gkZAqneR1GG\n" +
            "lJRuMKoy5B6rDEeJpjrDtmtjNRkKx7RB+d1g3jI3xtqOdVjpX1Om4oFSzk/i/dEB\n" +
            "lQdCG9FkCf6dsypHgHyc3NDY0hxE46hsDkovqGXNEuAvcEvfiWKqjA6weDbK1YL6\n" +
            "o6xy/SxogFEYfxW8/LGDeq7olIjA90VDgET0oztnh5brEjCPIzzSwyEX2a6xazwU\n" +
            "Z9aih1R1FIz3n1VZZP1Y62pqgI2WI+S/DepYyGL4/p2e0YyA1vxvObZVw1LoQCjS\n" +
            "QgEBsuC3pyF6jzfxI+GZtUDtEXqSl6xt1b2+181gx04h9HQ7Y1A10ZamFMc5OJPs\n" +
            "JHhzVSm8+LgaObtkhBn4RkxKgA==\n" +
            "=Z+Zy\n" +
            "-----END PGP MESSAGE-----";
    private String privateKey;
    private String publicKey;
    private final Random random = new Random();

    @TempDir
    static Path tempDir;

    @BeforeAll
    void init() throws IOException {
        privateKey = new String(ByteStreams.toByteArray(new ClassPathResource("junit_pgp_private.asc").getInputStream()));
        publicKey = new String(ByteStreams.toByteArray(new ClassPathResource("junit_pgp_public.asc").getInputStream()));
    }

    @Test
    void decrypt_success() throws IOException, PGPException {
        decryptSuccess(ENCRYPTED_MESSAGE_PGP);
    }

    @Test
    void decrypt_noInputfile() throws IOException {
        String fileOut = Files.createFile(tempDir.resolve(String.valueOf(random.nextInt()))).toFile().getAbsolutePath();
        PGPException pgpException = assertThrows(PGPException.class, () -> PgpStaticUtils.decryptToFile("", privateKey, PASSPHRASE, fileOut));
        Assertions.assertTrue(pgpException.getUnderlyingException() instanceof FileNotFoundException);
    }

    @Test
    void decrypt_invalidPassphrase() throws IOException {
        Path tempFileWithMessagePgp = createTempFileWithMessage(ENCRYPTED_MESSAGE_PGP);
        String fileOut = Files.createFile(tempDir.resolve(String.valueOf(random.nextInt()))).toFile().getAbsolutePath();
        assertThrows(PGPException.class, () -> PgpStaticUtils.decryptToFile(tempFileWithMessagePgp.toFile().getAbsolutePath(), privateKey, "pwd", fileOut));
    }

    @Test
    void decrypt_blankPrivateKey() throws IOException {
        Path tempFileWithMessagePgp = createTempFileWithMessage(ENCRYPTED_MESSAGE_PGP);
        String fileOut = Files.createFile(tempDir.resolve(String.valueOf(random.nextInt()))).toFile().getAbsolutePath();
        String absolutePath = tempFileWithMessagePgp.toFile().getAbsolutePath();
        PGPException pgpException = assertThrows(PGPException.class, () -> PgpStaticUtils.decryptToFile(absolutePath, "", PASSPHRASE, fileOut));
        Assertions.assertTrue(pgpException.getUnderlyingException() instanceof IllegalArgumentException);
    }

    @Test
    void encrypt_success() throws PGPException, IOException {
        String encrypt = PgpStaticUtils.encrypt(DECRYPTED_MESSAGE, publicKey);
        assertTrue((encrypt != null ? encrypt.length() : 0) > 0);
        decryptSuccess(encrypt);
    }

    private void decryptSuccess(String encrypt) throws IOException, PGPException {
        Path tempFileWithMessagePgp = createTempFileWithMessage(encrypt);
        String fileOut = Files.createFile(tempDir.resolve(String.valueOf(random.nextInt()))).toFile().getAbsolutePath();
        PgpStaticUtils.decryptToFile(tempFileWithMessagePgp.toFile().getAbsolutePath(), privateKey, PASSPHRASE, fileOut);
        String messageDecrypted = Files.lines(Paths.get(fileOut), StandardCharsets.UTF_8).collect(Collectors.joining(System.lineSeparator()));
        assertTrue(new File(fileOut).length() > 0);
        assertEquals(DECRYPTED_MESSAGE, messageDecrypted);
    }

    @Test
    void encrypt_invalidKeyPub() {
        assertThrows(PGPException.class, () -> PgpStaticUtils.encrypt(DECRYPTED_MESSAGE, ""));
    }

    private Path createTempFileWithMessage(String message) throws IOException {
        Path tempFile = Files.createFile(tempDir.resolve(String.valueOf(random.nextInt())));
        try (FileWriter myWriter = new FileWriter(tempFile.toFile())) {
            myWriter.write(message);
        }
        return tempFile;
    }

    @Test
    void decryptMessage_success() throws Exception {
        String messageDecrypted = PgpStaticUtils.decrypt(ENCRYPTED_MESSAGE_PGP, privateKey, PASSPHRASE);
        assertEquals(DECRYPTED_MESSAGE, messageDecrypted);
    }

}

