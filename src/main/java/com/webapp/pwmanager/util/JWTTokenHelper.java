package com.webapp.pwmanager.util;

import com.webapp.pwmanager.domain.AppUser;
import com.webapp.pwmanager.domain.RefreshToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.SignatureAlgorithm;

import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JWTTokenHelper {


    private final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;
    @Value("${jwt.auth.app}")
    private String appName;
    @Value("${jwt.auth.secret_key}")
    private String secretKey;
    @Value("${jwt.auth.refresh_secret_key}")
    private String refreshKey;
    @Value("${jwt.auth.refresh_expires_in}")
    private int refreshExpiresIn;
    @Value("${jwt.auth.expires_in}")
    private int expiresIn;

    private Claims getAllClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    private Claims getAllClaimsFromRefreshToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(refreshKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }


    public String getUsernameFromToken(String token) {
        String username;
        try {
            final Claims claims = this.getAllClaimsFromToken(token);
            username = claims.getSubject();
        } catch (Exception e) {
            username = null;
        }
        return username;
    }

    public String getUsernameFromRefreshToken(String token) {
        String username;
        try {
            final Claims claims = this.getAllClaimsFromRefreshToken(token);
            username = claims.getSubject();
        } catch (Exception e) {
            username = null;
        }
        return username;
    }


    public String generateToken(String username) throws InvalidKeySpecException, NoSuchAlgorithmException {

        return Jwts.builder()
                .setIssuer(appName)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(generateExpirationDate())
                .signWith(SIGNATURE_ALGORITHM, secretKey)
                .compact();
    }

    public String generateRefreshToken(AppUser user, RefreshToken newRefreshToken) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenId", newRefreshToken.getId());
        return Jwts.builder()
                .setIssuer(appName)
                .setSubject(user.getEmail())
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(generateRefreshExpirationDate())
                .signWith(SIGNATURE_ALGORITHM, refreshKey)
                .compact();
    }

    public Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + (long) expiresIn * 60 * 1000);
    }

    public Date generateRefreshExpirationDate() {
        return new Date(System.currentTimeMillis() + (long) refreshExpiresIn * 60 * 1000);
    }


    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (
                username != null &&
                        username.equals(userDetails.getUsername()) &&
                        !isTokenExpired(token)
        );
    }

    public Boolean validateRefreshToken(String token, AppUser userDetails) {
        final String username = getUsernameFromRefreshToken(token);
        return (
                username != null
                        && username.equals(userDetails.getEmail())
                        && !isRefreshTokenExpired(token)
        );
    }

    public Object getRefreshTokenClaim(String token) {
        Object claim;
        try {
            final Claims claims = this.getAllClaimsFromRefreshToken(token);
            return claims.get("tokenId");
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        Date expireDate = getExpirationDate(token);
        return expireDate.before(new Date());
    }

    public boolean isRefreshTokenExpired(String token) {
        Date expireDate = getRefreshExpirationDate(token);
        return expireDate.before(new Date());
    }

    private Date getRefreshExpirationDate(String token) {
        Date expireDate;
        try {
            final Claims claims = this.getAllClaimsFromRefreshToken(token);
            expireDate = claims.getExpiration();
        } catch (Exception e) {
            expireDate = null;
        }
        return expireDate;
    }

    private Date getExpirationDate(String token) {
        Date expireDate;
        try {
            final Claims claims = this.getAllClaimsFromToken(token);
            expireDate = claims.getExpiration();
        } catch (Exception e) {
            expireDate = null;
        }
        return expireDate;
    }


    public Date getIssuedAtDateFromToken(String token) {
        Date issueAt;
        try {
            final Claims claims = this.getAllClaimsFromToken(token);
            issueAt = claims.getIssuedAt();
        } catch (Exception e) {
            issueAt = null;
        }
        return issueAt;
    }

    public String getToken(HttpServletRequest request) {

        String authHeader = getAuthHeaderFromHeader(request);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    public String getAuthHeaderFromHeader(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }
}
