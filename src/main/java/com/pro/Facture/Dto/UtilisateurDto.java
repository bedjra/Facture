package com.pro.Facture.Dto;

import com.pro.Facture.enums.Role;
import lombok.Data;

@Data
public class UtilisateurDto {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String numeroTelephone;
    private Role role;
}