package com.pro.Facture.service;

import com.pro.Facture.Entity.Commande;
import com.pro.Facture.Entity.Place;
import com.pro.Facture.repository.CommandeRepository;
import com.pro.Facture.repository.PlaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommandeService {

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private PlaceRepository placeRepository;

    // 🔥 Générer ref automatique 0001, 0002, 0003...
    private String generateRef() {
        Long count = commandeRepository.count() + 1;
        return String.format("%04d", count); // 0001, 0002, ...
    }

    public Commande createCommande(Commande dto, Long placeId) {

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("Place introuvable"));

        Commande cmd = new Commande();

        cmd.setPlace(place);

        // ⭐ REF AUTO
        cmd.setRef(generateRef());

        cmd.setDesign(dto.getDesign());
        cmd.setHt(dto.getHt());
        cmd.setCode(dto.getCode());
        cmd.setRetenue(dto.getRetenue());
        cmd.setMt(dto.getMt());
        cmd.setAvance(dto.getAvance());

        // ⭐ BASE = HT
        cmd.setBase(dto.getHt());

        // ⭐ TVA depuis Place
        cmd.setTva(place.getTva());

        // ⭐ TTC = HT + valeur TVA
        // valeur tva = ht * (tva/100)
        Double valeurTva = dto.getHt() * (cmd.getTva() / 100);
        Double ttc = dto.getHt() + valeurTva;

        cmd.setMtTtc(ttc);

        // ⭐ NET = TTC - AVANCE
        cmd.setNet(ttc - dto.getAvance());

        return commandeRepository.save(cmd);
    }
}
