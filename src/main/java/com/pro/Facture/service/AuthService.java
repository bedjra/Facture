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

    // 🔹 Mettre à jour un utilisateur par ID
    public UtilisateurDto updateUserById(Long id, UtilisateurCreateDto dto) {
        // 1️⃣ Récupérer l'utilisateur existant
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        // 2️⃣ Mettre à jour l'email si fourni et non utilisé par un autre utilisateur
        if (dto.getEmail() != null && !dto.getEmail().equals(utilisateur.getEmail())) {
            if (utilisateurRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Email déjà utilisé");
            }
            utilisateur.setEmail(dto.getEmail());
        }

        // 3️⃣ Mettre à jour le mot de passe si fourni
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            utilisateur.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // 4️⃣ Mettre à jour le rôle si fourni
        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            try {
                Role role = Role.valueOf(dto.getRole().toUpperCase());
                utilisateur.setRole(role);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Rôle invalide");
            }
        }

        // 5️⃣ Sauvegarder les modifications
        Utilisateur updatedUser = utilisateurRepository.save(utilisateur);

        // 6️⃣ Retourner le DTO
        return convertToDTO(updatedUser);
    }

}
