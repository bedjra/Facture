package com.pro.Facture.Dto;

import lombok.Data;

@Data
public class UtilisateurCreateDto {

    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String numeroTelephone;
    private String role;
}
