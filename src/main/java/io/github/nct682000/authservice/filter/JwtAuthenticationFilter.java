package io.github.nct682000.authservice.filter;

import io.github.nct682000.authservice.entity.AuthUserDetails;
import io.github.nct682000.authservice.service.AuthClaims;
import io.github.nct682000.authservice.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.util.ObjectUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (ObjectUtils.isEmpty(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.info("No Bearer token found — skipping JWT authentication");
            filterChain.doFilter(request, response);
            return;
        }

        // Parse once — signature verified exactly one time for this request
        AuthClaims authClaims = jwtService.parseToken(authHeader.substring(7)).orElse(null);
        if (ObjectUtils.isEmpty(authClaims)) {
            log.info("Authentication failed — token is malformed, expired, or has an invalid signature");
            filterChain.doFilter(request, response);
            return;
        }

        log.info("Token parsed successfully for user: {}", authClaims.username());

        if (!ObjectUtils.isEmpty(authClaims.username()) && ObjectUtils.isEmpty(SecurityContextHolder.getContext().getAuthentication())) {
            UserDetails user = userDetailsService.loadUserByUsername(authClaims.username());

            if (!user.isEnabled()) {
                log.info("Authentication failed — account is disabled for user: {}", authClaims.username());
                filterChain.doFilter(request, response);
                return;
            }

            if (!user.isAccountNonLocked()) {
                log.info("Authentication failed — account is locked for user: {}", authClaims.username());
                filterChain.doFilter(request, response);
                return;
            }

            if (!user.isAccountNonExpired()) {
                log.info("Authentication failed — account has expired for user: {}", authClaims.username());
                filterChain.doFilter(request, response);
                return;
            }

            if (!user.isCredentialsNonExpired()) {
                log.info("Authentication failed — credentials have expired for user: {}", authClaims.username());
                filterChain.doFilter(request, response);
                return;
            }

            if (!jwtService.isTokenValid(authClaims, user)) {
                log.info("Authentication failed — token validation failed for user: {}", authClaims.username());
                filterChain.doFilter(request, response);
                return;
            }

            // Reject tokens whose version no longer matches the user's current token_version.
            // This covers logout-all and password-change scenarios without a blocklist.
            Integer dbVersion = ((AuthUserDetails) user).getTokenVersion();
            if (ObjectUtils.isEmpty(authClaims.tokenVersion())
                    || !authClaims.tokenVersion().equals(dbVersion)) {
                log.info("Authentication failed — token version mismatch for user: {} (token={}, db={})",
                        authClaims.username(), authClaims.tokenVersion(), dbVersion);
                filterChain.doFilter(request, response);
                return;
            }

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            log.info("Authentication successful for user: {}", authClaims.username());
        }

        filterChain.doFilter(request, response);
    }
}
