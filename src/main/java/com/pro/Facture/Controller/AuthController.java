package com.pro.Facture.Controller;


import com.pro.Facture.Dto.AuthRequestDto;
import com.pro.Facture.Dto.AuthResponseDto;
import com.pro.Facture.Dto.UtilisateurCreateDto;
import com.pro.Facture.Dto.UtilisateurDto;
import com.pro.Facture.Entity.Utilisateur;
import com.pro.Facture.repository.UtilisateurRepository;
import com.pro.Facture.service.AuthService;
import com.pro.Facture.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UtilisateurRepository utilisateurRepository;

    // 🔹 Inscription
    @PostMapping("/save")
    public UtilisateurDto register(@RequestBody UtilisateurCreateDto dto) {
        return authService.register(dto);
    }

    // 🔹 Login
    @PostMapping("/login")
    public String login(@RequestBody AuthRequestDto dto) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(dto.getPassword(), utilisateur.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        return jwtService.generateToken(utilisateur);
    }


}
