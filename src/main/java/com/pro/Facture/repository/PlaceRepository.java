package com.pro.Facture.repository;

import com.pro.Facture.Entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    Optional<Place> findFirstByOrderByIdAsc();
}
