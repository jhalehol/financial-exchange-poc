package com.yellowpepper.challenge.financial.service.auth;

import com.amazonaws.auth.PEM;
import com.amazonaws.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

public class OAuthTestUtils {

    private static final String JWT_FILE = "token.jwt";
    private static final String PUBLIC_INVALID_SECRET_KEY = "key_invalid.pub";
    private static final String PRIVATE_SECRET_KEY = "key";
    private static final String PUBLIC_SECRET_KEY = "key_.pub";

    // force non-instantiable through the `private` constructor
    private OAuthTestUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static String getJwtContent() throws IOException {
        return getKeyContent(JWT_FILE);
    }

    public static String getPublicKeyFilePath() {
        return getKeyFilePath(PUBLIC_SECRET_KEY);
    }

    public static String getPrivateKeyFilePath() {
        return getKeyFilePath(PRIVATE_SECRET_KEY);
    }

    public static String getPublicSecretKeyContent() throws IOException {
        return getKeyContent(PUBLIC_SECRET_KEY);
    }

    public static String getPrivateSecretKeyContent() throws IOException {
        return getKeyContent(PRIVATE_SECRET_KEY);
    }

    public static String getPublicInvalidKeyPath() {
        return getKeyFilePath(PUBLIC_INVALID_SECRET_KEY);
    }

    public static RSAPublicKey getPublicSecretKey() throws IOException, InvalidKeySpecException {
        final InputStream publicStream = getKeyContentStream(PUBLIC_SECRET_KEY);
        return (RSAPublicKey) PEM.readPublicKey(publicStream);
    }

    public static RSAPrivateKey getPrivateSecretKey() throws IOException, InvalidKeySpecException {
        final InputStream privateStream = getKeyContentStream(PRIVATE_SECRET_KEY);
        return (RSAPrivateKey) PEM.readPrivateKey(privateStream);
    }

    public static String getPublicInvalidSecretKey() throws IOException {
        return getKeyContent(PUBLIC_INVALID_SECRET_KEY);
    }

    private static String getKeyContent(final String keyFile) throws IOException {
        return IOUtils.toString(getKeyContentStream(keyFile));
    }

    private static String getKeyFilePath(final String keyFile) {
        final String directory = OAuthTestUtils.class.getPackage().getName().replace(".", "/");
        final String resourcePath = "src/test/resources/" + directory + "/" + keyFile;
        return new File(resourcePath).getAbsolutePath();
    }

    private static String getResourceKeyFilePath(final String keyFile) {
        final String directory = OAuthTestUtils.class.getPackage().getName().replace(".", "/");
        return directory + "/" + keyFile;
    }

    private static InputStream getKeyContentStream(final String keyFile) throws IOException {
        final String keyFilePath = getResourceKeyFilePath(keyFile);
        final String keyContent = IOUtils.toString(Objects.requireNonNull(OAuthTestUtils.class.getClassLoader()
                .getResourceAsStream(keyFilePath)));
        return new ByteArrayInputStream(keyContent.getBytes(StandardCharsets.UTF_8));
    }
}
