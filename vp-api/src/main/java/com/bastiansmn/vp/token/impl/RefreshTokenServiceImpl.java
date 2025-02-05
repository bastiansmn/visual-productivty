package com.bastiansmn.vp.token.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.bastiansmn.vp.config.SecurityConstant;
import com.bastiansmn.vp.config.properties.JwtProperties;
import com.bastiansmn.vp.config.properties.SpringProperties;
import com.bastiansmn.vp.exception.FunctionalException;
import com.bastiansmn.vp.exception.FunctionalRule;
import com.bastiansmn.vp.token.RefreshTokenService;
import com.bastiansmn.vp.token.TokenService;
import com.bastiansmn.vp.user.UserDAO;
import com.bastiansmn.vp.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final UserService userService;
    private final JwtProperties jwtProperties;
    private final SpringProperties springProperties;
    private final TokenService tokenService;

    @Override
    public UserDAO refresh(HttpServletRequest request, HttpServletResponse response) throws FunctionalException {
        // Read http cookie to get refresh token
        Cookie refreshTokenCookie = WebUtils.getCookie(request, SecurityConstant.REFRESH_TOKEN_COOKIE_NAME);
        log.debug("Refreshing token with: {}", refreshTokenCookie);
        if (refreshTokenCookie == null)
            throw new FunctionalException(FunctionalRule.TOKEN_0001);
        String refreshToken = refreshTokenCookie.getValue();
        try {
            UserDAO user = this.verifyUser(refreshToken);
            tokenService.createJWTAndAddInHeaders(user, response, springProperties.getProfile().equals("prod"));
            return user;
        } catch (FunctionalException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    @Override
    public Boolean validate(HttpServletRequest request, HttpServletResponse response) throws FunctionalException {
        if (request.getCookies() == null)
            return false;
        // Get refresh token
        Cookie refreshTokenCookie = WebUtils.getCookie(request, SecurityConstant.REFRESH_TOKEN_COOKIE_NAME);
        log.debug("Refreshing token with: {}", refreshTokenCookie);
        if (refreshTokenCookie == null)
            throw new FunctionalException(FunctionalRule.TOKEN_0002);
        String refreshToken = refreshTokenCookie.getValue();

        // Get access token
        Cookie accessTokenCookie = WebUtils.getCookie(request, SecurityConstant.ACCESS_TOKEN_COOKIE_NAME);
        if (accessTokenCookie == null) {
            // Refresh the token
            this.refresh(request, response);
            return true;
        }
        String accessToken = accessTokenCookie.getValue();

        // Check expiration date of both access and refresh token
        try {

            this.verifyUser(accessToken);
            this.verifyUser(refreshToken);

        } catch (TokenExpiredException e) {
            return false;
        } catch (JWTVerificationException e) {
            throw new FunctionalException(FunctionalRule.TOKEN_0001);
        }
        return true;
    }

    private UserDAO verifyUser(String token) throws FunctionalException {
        Algorithm algorithm = Algorithm.HMAC256(jwtProperties.getSecret().getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        String email = decodedJWT.getSubject();
        if (!this.userService.emailExists(email))
            throw new FunctionalException(FunctionalRule.USER_0004);

        if (!this.userService.isEnabled(email))
            throw new FunctionalException(
                    FunctionalRule.USER_0006,
                    FORBIDDEN
            );

        if (!this.userService.isNotLocked(email))
            throw new FunctionalException(
                    FunctionalRule.USER_0005,
                    FORBIDDEN
            );

        return this.userService.fetchByEmail(email);
    }

}
