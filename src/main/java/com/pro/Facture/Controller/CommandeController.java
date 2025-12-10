package com.pro.Facture.Controller;

import com.pro.Facture.Dto.CommandeRequestDto;
import com.pro.Facture.Dto.CommandeResponseDto;
import com.pro.Facture.Entity.Place;
import com.pro.Facture.service.CommandeService;
import com.pro.Facture.service.Pdf.CommandePdfService;
import com.pro.Facture.service.PlaceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/commande")
public class CommandeController {

    private final CommandeService commandeService;
    private final PlaceService placeService;
    private final CommandePdfService pdfService;

    public CommandeController(CommandeService commandeService,
                              PlaceService placeService,
                              CommandePdfService pdfService) {
        this.commandeService = commandeService;
        this.placeService = placeService;
        this.pdfService = pdfService;
    }

    // ----------------------------
    // POST - CREER UNE COMMANDE
    // ----------------------------
    @PostMapping
    public ResponseEntity<CommandeResponseDto> createCommande(@RequestBody CommandeRequestDto dto) {
        CommandeResponseDto response = commandeService.createCommande(dto);
        return ResponseEntity.ok(response);
    }

    // ----------------------------
    // POST - GENERER FACTURE PDF
    // ----------------------------
    @PostMapping("/pdf")
    public ResponseEntity<byte[]> generatePdf(@RequestBody CommandeRequestDto dto) {

        CommandeResponseDto saved = commandeService.createCommande(dto);

        Place place = placeService.getCurrent(); // exemple
        byte[] pdf = pdfService.genererPdf(saved, place);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=facture.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}
