package com.pro.Facture.Dto;


import com.pro.Facture.enums.Role;
import lombok.Data;

@Data
public class UtilisateurDto {
    private String email;
    private String password;
    private Role role;
    private Long id;

    // Getters & Setters


}
