package nttdata.apigateway.support;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
import java.util.List;

public final class JwtTestTokens {

    private static final String SECRET = "5367566859703373367639792F423F452848284D6251655468576D5A71347437";

    private JwtTestTokens() {
    }

    public static String customerToken() {
        return token("customer1", List.of("ROLE_CUSTOMER"));
    }

    public static String adminToken() {
        return token("admin1", List.of("ROLE_ADMIN"));
    }

    public static String bearerCustomerToken() {
        return "Bearer " + customerToken();
    }

    public static String bearerAdminToken() {
        return "Bearer " + adminToken();
    }

    private static String token(String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)), SignatureAlgorithm.HS256)
                .compact();
    }
}
