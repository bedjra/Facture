package com.pro.Facture.service;

import com.pro.Facture.Dto.UtilisateurCreateDto;
import com.pro.Facture.Dto.UtilisateurDto;
import com.pro.Facture.Entity.Utilisateur;
import com.pro.Facture.enums.Role;
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
        if (utilisateurRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        // 🔹 Convertit la String en Enum
        Role role;
        try {
            role = Role.valueOf(dto.getRole().toUpperCase());
            // toUpperCase pour éviter problème "cptr" vs "CPTR"
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rôle invalide");
        }

        Utilisateur utilisateur = new Utilisateur(
                dto.getEmail(),
                passwordEncoder.encode(dto.getPassword()),
                role
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
        dto.setRole(Role.valueOf(String.valueOf(utilisateur.getRole())));
        return dto;

    }

    public void deleteUser(Long id) {
        utilisateurRepository.deleteById(id);
    }

}
