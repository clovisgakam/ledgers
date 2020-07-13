package de.adorsys.ledgers.idp.service.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import de.adorsys.ledgers.idp.core.domain.IdpRole;
import de.adorsys.ledgers.idp.core.domain.TokenUsage;
import de.adorsys.ledgers.idp.core.domain.UserStatus;
import de.adorsys.ledgers.idp.core.exception.IdpErrorCode;
import de.adorsys.ledgers.idp.core.exception.IdpException;
import de.adorsys.ledgers.idp.service.api.domain.IdpUser;
import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;
import net.minidev.json.reader.JsonWriterI;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.RandomStringUtils.random;

public class TokenValidation {
    private static final String MISSING_ROLE = "Missing field for claim role";
    private static final String MISSING_LOGIN = "Missing field for claim login";
    private static final String MISSING_USAGE = "Missing field for claim token_usage";
    private static final String USAGE = "tokenUsage";
    private static final String ROLE = "role";
    private static final String LOGIN = "login";
    private static final String ACT = "act";

    public static SignedJWT parse(String token) {
        try {
            return SignedJWT.parse(token);
        } catch (ParseException e) {
            throw IdpException.builder()
                          .devMsg("")//TODO
                          .errorCode(IdpErrorCode.TOKEN_INVALID)
                          .build();
        }
    }

    public static JWTClaimsSet parse(SignedJWT jwt) {
        try {
            return jwt.getJWTClaimsSet();
        } catch (ParseException e) {
            throw IdpException.builder()
                          .devMsg("")//TODO Supplier!!!!
                          .errorCode(IdpErrorCode.TOKEN_INVALID)
                          .build();
        }
    }

    public static long validateJWT(JWSHeader header, JWTClaimsSet claimsSet, SignedJWT jwt, String hmacSecret) {
        try {
            checkAlgorithm(header);
            verifySecret(hmacSecret, jwt);
            return checkExpiration(new Date(), claimsSet);
        } catch (JOSEException e) {
            throw IdpException.builder()
                          .devMsg("Error parsing token")
                          .errorCode(IdpErrorCode.TOKEN_INVALID)
                          .build();
        }
    }

    public static void checkUserNotBlocked(IdpUser user) {
        if (user.getUserStatus() == UserStatus.BLOCKED) {
            throw IdpException.blockedSupplier(IdpErrorCode.USER_BLOCKED, user.getLogin()).get();
        }
    }

    public static JWTClaimsSet genJWT(String userId, String userLogin, IdpRole userRole, Date issueTime, Date expires, TokenUsage usage, Map<String, String> act) {
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                                               .subject(Objects.requireNonNull(userId, "Missing userId"))
                                               .jwtID(random(10, true, true))
                                               .issueTime(issueTime)
                                               .expirationTime(expires)
                                               .claim(LOGIN, Objects.requireNonNull(userLogin, MISSING_LOGIN))
                                               .claim(ROLE, Objects.requireNonNull(userRole, MISSING_ROLE))
                                               .claim(USAGE, Objects.requireNonNull(usage, MISSING_USAGE).name());

        if (act != null) {
            builder = builder.claim(ACT, act);
        }

        return builder.build();
    }

    public static String signJWT(JWTClaimsSet claimsSet, ObjectMapper mapper, String hmacSecret) {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256).keyID(random(10, true, true)).build();
        JSONValue.registerWriter(LocalDate.class, new JsonWriterI<LocalDate>() {
            @Override
            public void writeJSONString(LocalDate value, Appendable out, JSONStyle compression) throws IOException {
                if (value == null) {
                    out.append("null");
                } else {
                    out.append(mapper.writeValueAsString(value));
                }
            }
        });
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        try {
            signedJWT.sign(new MACSigner(hmacSecret));
        } catch (JOSEException e) {
            throw IdpException.builder()
                          .errorCode(IdpErrorCode.FAILED_SIGNING)
                          .devMsg(String.format("Error signing user token %s", e))
                          .build();
        }
        return signedJWT.serialize();
    }

    private static void verifySecret(String hmacSecret, SignedJWT jwt) throws JOSEException {
        if (!jwt.verify(new MACVerifier(hmacSecret))) {
            throw IdpException.tokenSupplier(IdpErrorCode.TOKEN_SIGNATURE_INVALID).get();
        }
    }

    private static void checkAlgorithm(JWSHeader header) {
        if (!JWSAlgorithm.HS256.equals(header.getAlgorithm())) {
            throw IdpException.tokenSupplier(IdpErrorCode.WRONG_ALGORITHM).get();
        }
    }

    private static long checkExpiration(Date refTime, JWTClaimsSet jwtClaimsSet) {
        long expiresIn = jwtClaimsSet.getExpirationTime() == null
                                 ? -1
                                 : (jwtClaimsSet.getExpirationTime().getTime() - refTime.getTime()) / 1000;
        if (expiresIn <= 0) {
            throw IdpException.tokenSupplier(IdpErrorCode.TOKEN_EXPIRED).get();
        }
        return expiresIn;
    }
}
