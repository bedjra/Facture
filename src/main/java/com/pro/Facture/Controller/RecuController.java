package com.pro.Facture.Controller;

import com.pro.Facture.Dto.RecuDto;
import com.pro.Facture.service.Pdf.RecuPdfService;
import com.pro.Facture.service.RecuService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recu")
@CrossOrigin("*")
public class RecuController {

    private final RecuService recuService;
    private final RecuPdfService recuPdfService;

    public RecuController(RecuService recuService,
                          RecuPdfService recuPdfService) {

        this.recuService = recuService;
        this.recuPdfService = recuPdfService;
    }

    // =========================
    // CREATE + PDF
    // =========================

    @PostMapping
    public ResponseEntity<byte[]> createRecuAndGeneratePdf(
            @RequestBody RecuDto dto) {

        // 1️⃣ Sauvegarder le reçu
        RecuDto savedRecu = recuService.create(dto);

        // 2️⃣ Générer le PDF
        byte[] pdf = recuPdfService.generatePdf(savedRecu.getId());

        // 3️⃣ Retourner le PDF
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=recu_" +
                                savedRecu.getNumeroPieceAffichage() +
                                ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // =========================
    // GET PDF
    // =========================

    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> getPdf(@PathVariable Long id) {

        byte[] pdf = recuPdfService.generatePdf(id);

        RecuDto recu = recuService.getById(id);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=recu_" +
                                recu.getNumeroPieceAffichage() +
                                ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // =========================
    // READ ALL
    // =========================

    @GetMapping
    public ResponseEntity<List<RecuDto>> getAll() {

        return ResponseEntity.ok(recuService.getAll());
    }

    // =========================
    // READ BY ID
    // =========================

    @GetMapping("/{id}")
    public ResponseEntity<RecuDto> getById(@PathVariable Long id) {

        return ResponseEntity.ok(recuService.getById(id));
    }

    // =========================
    // UPDATE
    // =========================

    @PutMapping("/{id}")
    public ResponseEntity<RecuDto> update(
            @PathVariable Long id,
            @RequestBody RecuDto dto) {

        return ResponseEntity.ok(recuService.update(id, dto));
    }

    // =========================
    // DELETE
    // =========================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {

        recuService.delete(id);

        return ResponseEntity.ok("Reçu supprimé avec succès");
    }
}