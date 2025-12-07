package com.pro.Facture.service;

import com.pro.Facture.Dto.PlaceDto;
import com.pro.Facture.Entity.Place;
import com.pro.Facture.repository.PlaceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlaceService {

    private final PlaceRepository placeRepository;

    public PlaceService(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    // CREATE
    public Place createPlace(PlaceDto dto) {
        Place place = new Place();
        place.setNom(dto.getNom());
        place.setEmail(dto.getEmail());
        place.setTelephone(dto.getTelephone());
        place.setCel(dto.getCel());
        place.setAdresse(dto.getAdresse());
        place.setDesc(dto.getDesc());
        place.setLogo(dto.getLogo());
        return placeRepository.save(place);
    }

    // READ ALL
    public List<Place> getAllPlaces() {
        return placeRepository.findAll();
    }

    // READ ONE
    public Place getPlace(Long id) {
        return placeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Place non trouvée"));
    }

    // UPDATE
    public Place updatePlace(Long id, PlaceDto dto) {
        Place place = getPlace(id);

        place.setNom(dto.getNom());
        place.setEmail(dto.getEmail());
        place.setTelephone(dto.getTelephone());
        place.setCel(dto.getCel());
        place.setAdresse(dto.getAdresse());
        place.setDesc(dto.getDesc());
        place.setLogo(dto.getLogo());

        return placeRepository.save(place);
    }

    // DELETE
    public void deletePlace(Long id) {
        placeRepository.deleteById(id);
    }
}
