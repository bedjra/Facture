package com.pro.Facture.Controller;


import com.pro.Facture.Dto.AuthRequestDto;
 import com.pro.Facture.Dto.UtilisateurCreateDto;
import com.pro.Facture.Dto.UtilisateurDto;
import com.pro.Facture.Entity.Utilisateur;
import com.pro.Facture.repository.UtilisateurRepository;
import com.pro.Facture.service.AuthService;
import com.pro.Facture.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


    @GetMapping("/info")
    public UtilisateurDto getUserInfo(@RequestHeader("Authorization") String authHeader) {

        // 1️⃣ Enlever "Bearer "
        String token = authHeader.substring(7);

        // 2️⃣ Extraire l'email depuis le token
        String email = jwtService.extractEmail(token);

        // 3️⃣ Récupérer l'utilisateur dans la base
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // 4️⃣ Remplir le DTO avec ID, email et rôle
        UtilisateurDto dto = new UtilisateurDto();
        dto.setId(utilisateur.getId());
        dto.setEmail(utilisateur.getEmail());
        dto.setRole(utilisateur.getRole());

        return dto;
    }

    // 🔹 Récupérer tous les utilisateurs
    @GetMapping("/all")
    public List<UtilisateurDto> getAllUsers() {
        return authService.getAll();
    }


    // 🔹 Supprimer un utilisateur par ID
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        authService.deleteUser(id);
        return "Utilisateur supprimé avec succès !";
    }

    // 🔹 Mettre à jour un utilisateur par ID
    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurDto> updateUser(
            @PathVariable Long id,
            @RequestBody UtilisateurCreateDto dto) {
        UtilisateurDto updatedUser = authService.updateUserById(id, dto);
        return ResponseEntity.ok(updatedUser);
    }
}
