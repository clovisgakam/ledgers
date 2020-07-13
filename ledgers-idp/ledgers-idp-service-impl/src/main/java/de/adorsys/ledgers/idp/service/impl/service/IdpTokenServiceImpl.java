package de.adorsys.ledgers.idp.service.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import de.adorsys.ledgers.idp.core.domain.IdpAccessToken;
import de.adorsys.ledgers.idp.core.domain.IdpRole;
import de.adorsys.ledgers.idp.core.domain.IdpToken;
import de.adorsys.ledgers.idp.core.domain.TokenUsage;
import de.adorsys.ledgers.idp.core.exception.IdpErrorCode;
import de.adorsys.ledgers.idp.core.exception.IdpException;
import de.adorsys.ledgers.idp.service.api.domain.IdpUser;
import de.adorsys.ledgers.idp.service.api.service.IdpTokenService;
import de.adorsys.ledgers.idp.service.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

import static de.adorsys.ledgers.idp.service.impl.service.TokenValidation.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdpTokenServiceImpl implements IdpTokenService {
    private static final String COULD_NOT_VERIFY_SIGNATURE_OF_TOKEN_WITH_SUBJECT = "Could not verify signature of token with subject : {}";
    private static final String TOKEN_WITH_SUBJECT_EXPIRED = "Token with subject {} is expired at {} and reference time is {}";
    private static final String WRONG_JWS_ALGO_FOR_TOKEN_WITH_SUBJECT = "Wrong jws algo for token with subject : {}";
    private static final String USER_DOES_NOT_HAVE_THE_ROLE_S = "User with id %s and login %s does not have the role %s";

    @Value("${idp.default.token.lifetime.seconds:600}")
    private int defaultLoginTokenExpireInSeconds;

    private final HashMacSecretSource secretSource;
    private final UserService userService;
    private final PasswordEncService passwordEnc;
    private final ObjectMapper objectMapper;

    @Override
    public IdpToken validate(String token) {
        SignedJWT jwt = parse(token);
        JWTClaimsSet claimsSet = parse(jwt);
        long expiresIn = validateJWT(jwt.getHeader(), claimsSet, jwt, secretSource.getHmacSecret());

        IdpUser user = userService.getById(Long.parseLong(claimsSet.getSubject()));
        checkUserNotBlocked(user);

        IdpAccessToken accessTokenJWT = toAccessTokenObject(claimsSet);
        return bearerToken(token, expiresIn, accessTokenJWT);
    }

    @Override
    public IdpToken login(String login, String password) {
        IdpUser user = userService.findByLogin(login);
        boolean success = passwordEnc.verify(String.valueOf(user.getUserId()), password, user.getPassword());
        if (!success) {
            throw IdpException.builder()
                          .devMsg("Wrong credentials!")
                          .errorCode(IdpErrorCode.WRONG_CREDENTIALS)
                          .build();
        }
        Date issueDate = new Date();
        Date expires = DateUtils.addSeconds(issueDate, defaultLoginTokenExpireInSeconds);
        return bearerToken(user, issueDate, expires, TokenUsage.DIRECT_ACCESS, null);
    }

    public IdpToken bearerToken(IdpUser user, Date issueTime, Date expires, TokenUsage usage, Map<String, String> act) {
        JWTClaimsSet claimsSet = genJWT(String.valueOf(user.getUserId()), user.getLogin(), user.getRole(), issueTime, expires, usage, act);
        IdpAccessToken accessToken = toAccessTokenObject(claimsSet);
        String accessTokenString = signJWT(claimsSet, objectMapper, secretSource.getHmacSecret());
        long expiresInSeconds = (expires.getTime() - issueTime.getTime()) / 1000;
        return bearerToken(accessTokenString, expiresInSeconds, accessToken);
    }

    public IdpToken bearerToken(String userId, String userLogin, IdpRole userRole, Date issueTime, Date expires, TokenUsage usage, Map<String, String> act) {
        JWTClaimsSet claimsSet = genJWT(userId, userLogin, userRole, issueTime, expires, usage, act);
        IdpAccessToken accessToken = toAccessTokenObject(claimsSet);
        String accessTokenString = signJWT(claimsSet, objectMapper, secretSource.getHmacSecret());
        long expiresInSeconds = (expires.getTime() - issueTime.getTime()) / 1000;
        return bearerToken(accessTokenString, expiresInSeconds, accessToken);
    }

    private IdpToken bearerToken(String accessToken, long expiresIn, IdpAccessToken accessTokenJWT) {
        IdpToken bt = new IdpToken();
        bt.setAccess_token(accessToken);
        bt.setAccessTokenObject(accessTokenJWT);
        bt.setExpires_in(expiresIn);
        return bt;
    }

    private IdpAccessToken toAccessTokenObject(JWTClaimsSet jwtClaimsSet) {
        return objectMapper.convertValue(jwtClaimsSet.toJSONObject(false), IdpAccessToken.class);
    }
}
