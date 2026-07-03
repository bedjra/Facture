package com.pro.Facture.Controller;

import com.pro.Facture.Dto.ChargeDTO;
import com.pro.Facture.service.ChargeService;
import com.pro.Facture.service.Pdf.ChargePdfService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/charge")
@CrossOrigin("*")
public class ChargeController {

    private final ChargeService chargeService;
    private final ChargePdfService chargePdfService;

    public ChargeController(ChargeService chargeService, ChargePdfService chargePdfService) {
        this.chargeService = chargeService;
        this.chargePdfService = chargePdfService;
    }

    // CREATE + retourne directement le PDF (fiche de la charge)
    @PostMapping
    public ResponseEntity<byte[]> create(@RequestBody ChargeDTO dto) {

        ChargeDTO saved = chargeService.create(dto);

        byte[] pdf = chargePdfService.generateChargePdf(saved.getId());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=charge_" + saved.getId() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // READ ALL
    @GetMapping
    public List<ChargeDTO> getAll() {
        return chargeService.getAll();
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ChargeDTO getById(@PathVariable Long id) {
        return chargeService.getById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ChargeDTO update(@PathVariable Long id,
                            @RequestBody ChargeDTO dto) {
        return chargeService.update(id, dto);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        chargeService.delete(id);
    }

    // Total des charges
    @GetMapping("/total")
    public Double getTotalCharges() {
        return chargeService.getTotalCharges();
    }

    // PDF d'une charge existante (fiche individuelle)
    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> getChargePdf(@PathVariable Long id) {
        byte[] pdf = chargePdfService.generateChargePdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=charge_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // PDF - Bordereau complet (toutes les charges)
    @GetMapping("/pdf/bordereau")
    public ResponseEntity<byte[]> getBordereauPdf() {
        byte[] pdf = chargePdfService.generateBordereauPdf();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=bordereau_depenses.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // PDF - Bordereau filtré par période (debut/fin au format yyyy-MM-dd)
    @GetMapping("/pdf/bordereau/periode")
    public ResponseEntity<byte[]> getBordereauPdfParPeriode(
            @RequestParam("debut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam("fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        byte[] pdf = chargePdfService.generateBordereauPdf(debut, fin);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=bordereau_depenses_" + debut + "_" + fin + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}