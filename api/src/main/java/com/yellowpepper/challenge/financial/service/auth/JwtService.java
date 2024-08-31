package com.yellowpepper.challenge.financial.service.auth;

import com.amazonaws.auth.PEM;
import com.yellowpepper.challenge.financial.config.AppConfiguration;
import com.yellowpepper.challenge.financial.exception.ConfigurationException;
import com.yellowpepper.challenge.financial.exception.OperationException;
import com.yellowpepper.challenge.financial.exception.UnauthorizedException;
import com.yellowpepper.challenge.financial.exception.UserNotFoundException;
import com.yellowpepper.challenge.financial.model.User;
import com.yellowpepper.challenge.financial.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.InvalidJwtSignatureException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

@Slf4j
@Service
public class JwtService {

    private static final int KEYS_BYTES = 2048;
    public static final String JWT_TYPE = "JWT";
    private static final String JWT_ISSUER = "Financial YellowPepper";
    private static final Integer JWT_NOT_BEFORE_MINS = 2;
    private static final String JWT_SUBJECT = "financial-token";
    private static final String JWT_USER_CLAIM = "user-id";
    public static final String JWT_IAT_KEY = "iat";
    public static final String JWT_IAT_TTL_KEY = "iatTTL";
    public static final String JWT_TYPE_KEY = "typ";
    private static final int JWT_VALIDATION_CLOCK_SKEW_SEC = 30;
    private static final String ALGORITHM_IDENTIFIER = AlgorithmIdentifiers.RSA_USING_SHA256;
    private static final Integer MILLISECONDS = 1000;
    private final UserService userService;
    private final AppConfiguration configuration;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    public JwtService(final UserService userService, final AppConfiguration configuration)
            throws ConfigurationException {
        this.userService = userService;
        this.configuration = configuration;
        setupAuthKeys();
    }

    /**
     * Validates a JWT token
     * @param jwt Token for validation
     * @return User details if the token is valid
     * @throws UnauthorizedException Raise an exception if the token does not meet
     * with the requirements
     */
    public User validateJwtToken(final String jwt) throws UnauthorizedException {
        try {
            final Long userId = extractUserIdFromJwt(jwt);
            return userService.loadUserByUserId(userId);
        } catch (UserNotFoundException e) {
            throw new UnauthorizedException("User from token not found", e);
        }
    }

    /**
     * Builds an authentication token in JWT format
     * @param userId User owner of the token
     * @return A JWT token
     */
    public String buildJwt(final Long userId) throws OperationException {
        try {
            final JwtClaims claims = buildClaims(userId);
            final JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setKey(privateKey);
            jws.setAlgorithmHeaderValue(ALGORITHM_IDENTIFIER);
            jws.setHeader(JWT_TYPE_KEY, JWT_TYPE);

            return jws.getCompactSerialization();
        } catch (JoseException | MalformedClaimException e) {
            throw new OperationException(String.format("Unable to generate the authentication token %s",
                    e.getMessage()), e);
        }
    }

    private Long extractUserIdFromJwt(final String jwt) {
        try {
            final JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setRequireExpirationTime()
                    .setAllowedClockSkewInSeconds(JWT_VALIDATION_CLOCK_SKEW_SEC)
                    .setRequireSubject()
                    .setExpectedIssuer(JWT_ISSUER)
                    .setVerificationKey(publicKey)
                    .build();

            final JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
            return jwtClaims.getClaimValue(JWT_USER_CLAIM, Long.class);
        } catch (InvalidJwtSignatureException e) {
            throw new UnauthorizedException("Invalid signature validating the token", e);
        } catch (InvalidJwtException | MalformedClaimException e) {
            throw new UnauthorizedException(String.format("Current token is invalid %s", e.getMessage()), e);
        }
    }

    private JwtClaims buildClaims(final Long userId) throws MalformedClaimException {
        final JwtClaims claims = new JwtClaims();
        claims.setIssuer(JWT_ISSUER);
        claims.setIssuedAtToNow();
        claims.setGeneratedJwtId();
        claims.setNotBeforeMinutesInThePast(JWT_NOT_BEFORE_MINS);
        claims.setSubject(JWT_SUBJECT);
        claims.setClaim(JWT_USER_CLAIM, userId);
        final Long issuedAt = claims.getIssuedAt().getValueInMillis() / MILLISECONDS;
        claims.setClaim(JWT_IAT_KEY, issuedAt);
        claims.setClaim(JWT_TYPE_KEY, JWT_TYPE);
        claims.setExpirationTimeMinutesInTheFuture(configuration.getOauthTokenExpirationTimeMins());
        final Long expiredAt = claims.getExpirationTime().getValueInMillis() / MILLISECONDS;
        claims.setClaim(JWT_IAT_TTL_KEY, expiredAt);

        return claims;
    }

    private void setupAuthKeys() throws ConfigurationException {
        try {
            if (StringUtils.isNotEmpty(configuration.getOauthPrivateKeyPath())) {
                log.info("Using authentication keys provided from configuration {}",
                        configuration.getOauthPrivateKeyPath());
                InputStream privateKeyInputStream = new FileInputStream(configuration.getOauthPrivateKeyPath());
                InputStream publicKeyInputStream = new FileInputStream(configuration.getOauthPublicKeyPath());
                privateKey = (RSAPrivateKey) PEM.readPrivateKey(privateKeyInputStream);
                publicKey = (RSAPublicKey) PEM.readPublicKey(publicKeyInputStream);

                return;
            }

            log.info("Using temporal keys for authentication");
            final RsaJsonWebKey keys = RsaJwkGenerator.generateJwk(KEYS_BYTES);
            privateKey = keys.getRsaPrivateKey();
            publicKey = keys.getRsaPublicKey();
        } catch (FileNotFoundException e) {
            throw new ConfigurationException("Unable to retrieve configuration keys from files", e);
        } catch (JoseException | InvalidKeySpecException | IOException | IllegalArgumentException e) {
            throw new ConfigurationException("Unable to initialize OAuth keys", e);
        }
    }
}
