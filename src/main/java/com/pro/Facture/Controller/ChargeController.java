package com.pro.Facture.Controller;

import com.pro.Facture.Dto.ChargeDTO;
import com.pro.Facture.service.ChargeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/charge")
@CrossOrigin("*")
public class ChargeController {

    private final ChargeService chargeService;

    public ChargeController(ChargeService chargeService) {
        this.chargeService = chargeService;
    }

    // CREATE
    @PostMapping
    public ChargeDTO create(@RequestBody ChargeDTO dto) {
        return chargeService.create(dto);
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

    // 🔹 Total des charges
    @GetMapping("/total")
    public Double getTotalCharges() {
        return chargeService.getTotalCharges();
    }
}