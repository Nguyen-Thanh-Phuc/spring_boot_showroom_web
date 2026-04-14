package com.nguyenthanhphuc.showroom.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class Auth {
    private final Algorithm algo;

    public Auth(@Value("${secretKey}") String secretKey) {
        algo = Algorithm.HMAC256(secretKey);
    }

    private String createToken(String username) {
        return JWT.create()
                .withClaim("username", username)
                .withExpiresAt(Instant.now().plusSeconds(3600 * 3))
                .sign(algo);
    }

    private boolean verifyToken(String token) {
        var verifier = JWT.require(algo).build();
        try {
            var jwt = verifier.verify(token);
            var username = jwt.getClaim("username").asString();
            return username != null;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public void setUserLoggedIn(HttpServletResponse response, String username) {
        var token = createToken(username);
        var cookie = ResponseCookie.from("token", token).path("/").httpOnly(true).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public boolean isUserLoggedIn(HttpServletRequest request) {
        var cookies = request.getCookies();
        if (cookies == null) {
            return false;
        }

        for (var cookie : cookies) {
            if ("token".equals(cookie.getName())) {
                return verifyToken(cookie.getValue());
            }
        }

        return false;
    }
}
