package qbit.microservice.warehouse_service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import qbit.microservice.warehouse_service.config.JwtAuthentication;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;


    private SecretKey getSignInKey() {
        try {
            byte[] bytes = Base64.getDecoder().decode(secretKey.getBytes());
            return new SecretKeySpec(bytes, "HmacSHA256");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid Base64 input for secretKey", e);
        }
    }


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token, String username) {
        return (extractUsername(token).equals(username) && !isTokenExpired(token));
    }

    public String getJwtFromContext() {
        // Lấy Authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra nếu Authentication là một đối tượng JwtAuthentication
        if (authentication instanceof JwtAuthentication jwtAuthentication) {
            return jwtAuthentication.getJwt(); // Trả về JWT đã lưu trong JwtAuthentication
        }
        return null; // Nếu không tìm thấy JWT trong context
    }
}
