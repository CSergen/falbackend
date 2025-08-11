package com.reisfal.falbackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
<<<<<<< HEAD
=======
import org.springframework.http.HttpMethod;
>>>>>>> recover-2157
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
<<<<<<< HEAD
=======
import java.util.Set;
>>>>>>> recover-2157

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

<<<<<<< HEAD
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
=======
    // Filtre dışında bırakılacak path'ler
    private static final Set<String> WHITELIST = Set.of(
            "/auth/register",
            "/auth/login",
            "/auth/refresh",
            "/hello"           // istersen kaldır
    );

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   UserDetailsService userDetailsService) {
>>>>>>> recover-2157
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

<<<<<<< HEAD
        String path = request.getServletPath();

        // Sadece register ve login'i hariç tut
        if (path.equals("/auth/register") || path.equals("/auth/login")) {
=======
        final String path = request.getServletPath();

        // 1) CORS preflight isteği ise bırak
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
>>>>>>> recover-2157
            filterChain.doFilter(request, response);
            return;
        }

<<<<<<< HEAD
=======
        // 2) Whitelist veya public static uploads ise bırak
        if (isWhitelisted(path) || isPublicUpload(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3) Authorization header'dan Bearer token çek
>>>>>>> recover-2157
        String header = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (header != null && header.startsWith("Bearer ")) {
<<<<<<< HEAD
            token = header.substring(7);
            username = jwtTokenProvider.getUsername(token);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtTokenProvider.validateToken(token)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }


=======
            token = header.substring(7).trim();
            try {
                username = jwtTokenProvider.getUsername(token);
            } catch (Exception ignored) {
                // parse edilemeyen token akışı kırmasın
            }
        }

        // 4) SecurityContext boşsa ve token geçerli gibi görünüyorsa authenticate et
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception ignored) {
                // validate veya loadUser'da hata olsa bile request akışını kesmeyelim
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isWhitelisted(String path) {
        if (WHITELIST.contains(path)) return true;
        // İleride /auth/** tamamını açmak istersen:
        // if (path.startsWith("/auth/")) return true;
        return false;
    }

    private boolean isPublicUpload(String path) {
        // /uploads/** herkese açık (SecurityConfig’te de permitAll verdin)
        return path != null && (path.equals("/uploads") || path.startsWith("/uploads/"));
    }
>>>>>>> recover-2157
}
