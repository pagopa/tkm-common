package it.gov.pagopa.tkm.service;

import it.gov.pagopa.tkm.service.*;
import org.apache.commons.io.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.test.util.*;

import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.*;

@ExtendWith(MockitoExtension.class)
public class TestPgpUtils {

    @InjectMocks
    private PgpUtils pgpUtils;

    @BeforeEach
    public void init() throws Exception {
        String privateKey = FileUtils.readFileToString(new File("src/test/resources/pgp_private_key_test.asc"), StandardCharsets.UTF_8);
        String publicKey = FileUtils.readFileToString(new File("src/test/resources/pgp_public_key_test.asc"), StandardCharsets.UTF_8);
        PgpUtils.class.getDeclaredField("privateKey").setAccessible(true);
        PgpUtils.class.getDeclaredField("publicKeyFromKeyVault").setAccessible(true);
        ReflectionTestUtils.setField(pgpUtils, "privateKey", privateKey);
        ReflectionTestUtils.setField(pgpUtils, "publicKeyFromKeyVault", publicKey);
        Method postConstruct = PgpUtils.class.getDeclaredMethod("init");
        postConstruct.setAccessible(true);
        postConstruct.invoke(pgpUtils);
    }

    @Test
    public void givenMessage_encryptAndDecryptMessage() throws Exception {
        String encryptedMessage = pgpUtils.encrypt("MESSAGE");
        String decryptedMessage = pgpUtils.decrypt(encryptedMessage);
        Assertions.assertEquals("MESSAGE", decryptedMessage);
    }

}