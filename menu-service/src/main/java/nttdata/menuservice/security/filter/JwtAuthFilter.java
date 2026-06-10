package nttdata.menuservice.security.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    // ACELAȘI SECRET CA ÎN USER-SERVICE
    private static final String SECRET = "5367566859703373367639792F423F452848284D6251655468576D5A71347437";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // Decodeaza token-ul
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(io.jsonwebtoken.io.Decoders.BASE64.decode(SECRET))
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String username = claims.getSubject();

                // Extrage lista de roluri pe care tocmai am adaugat-o in user-service
                List<String> roles = claims.get("roles", List.class);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null && roles != null) {

                    // Transforma textele (ex: "ROLE_ADMIN") in autorizatii intelese de Spring Security
                    var authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                System.out.println("Eroare la procesarea token-ului JWT în Menu Service: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}