package com.pro.Facture.service;

import com.pro.Facture.Dto.UtilisateurCreateDto;
import com.pro.Facture.Dto.UtilisateurDto;
import com.pro.Facture.Entity.Utilisateur;
import com.pro.Facture.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    // 🔹 Créer un utilisateur
    public UtilisateurDto register(UtilisateurCreateDto dto) {
        // Vérifie si l'email existe déjà
        if (utilisateurRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        Utilisateur utilisateur = new Utilisateur(
                dto.getEmail(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getRole()
        );

        Utilisateur savedUser = utilisateurRepository.save(utilisateur);

        return convertToDTO(savedUser);
    }

    // 🔹 Récupérer un utilisateur par email
    public UtilisateurDto getByEmail(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        return convertToDTO(utilisateur);
    }

    // 🔹 Récupérer la liste des utilisateurs
    public List<UtilisateurDto> getAll() {
        return utilisateurRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 🔹 Convertir Entity → DTO
    private UtilisateurDto convertToDTO(Utilisateur utilisateur) {
        UtilisateurDto dto = new UtilisateurDto();
        dto.setId(utilisateur.getId());
        dto.setEmail(utilisateur.getEmail());
        dto.setRole(utilisateur.getRole());
        return dto;
    }
}
