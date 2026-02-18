package com.pro.Facture.config;

import com.pro.Facture.repository.UtilisateurRepository;
import com.pro.Facture.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        String email;

        try {
            email = jwtService.extractEmail(jwt); // ✅ JJWT vérifie déjà l'expiration ici
        } catch (Exception e) {
            // Token invalide ou expiré → on passe sans authentifier
            filterChain.doFilter(request, response);
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var utilisateur = utilisateurRepository.findByEmail(email).orElse(null);

            if (utilisateur != null) {
                var authToken = new UsernamePasswordAuthenticationToken(
                        utilisateur, null, utilisateur.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
    private boolean isTokenExpired(String token) {
        try {
            var claims = io.jsonwebtoken.Jwts.parser()
                    .setSigningKey(jwtService.getSecretKey())
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new java.util.Date());
        } catch (Exception e) {
            return true;
        }
    }
}