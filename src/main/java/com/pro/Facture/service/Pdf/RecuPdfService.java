package com.pro.Facture.service.Pdf;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.pro.Facture.Entity.Recu;
import com.pro.Facture.Entity.Place;
import com.pro.Facture.repository.RecuRepository;
import com.pro.Facture.repository.PlaceRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class RecuPdfService {

    private final RecuRepository recuRepository;
    private final PlaceRepository placeRepository;

    public RecuPdfService(RecuRepository recuRepository,
                          PlaceRepository placeRepository) {
        this.recuRepository = recuRepository;
        this.placeRepository = placeRepository;
    }

    public byte[] generatePdf(Long id) {

        // Récupération du reçu
        Recu recu = recuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reçu non trouvé"));

        // Récupération du cabinet (premier en base)
        Place place = placeRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("Cabinet non configuré"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // =========================
            // INFOS CABINET
            // =========================
            document.add(new Paragraph(place.getNom())
                    .setBold()
                    .setFontSize(16));

            document.add(new Paragraph("Activité : " + place.getDesc()));
            document.add(new Paragraph("Siège : " + place.getAdresse()));
            document.add(new Paragraph("Tél : " + place.getTelephone()));
            document.add(new Paragraph("Cel : " + place.getCel()));
            document.add(new Paragraph("Email : " + place.getEmail()));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("===================================="));
            document.add(new Paragraph(" "));

            // =========================
            // INFOS REÇU
            // =========================
            document.add(new Paragraph("N° Pièce : " + recu.getNumeroPiece()));
            document.add(new Paragraph("Date : " + recu.getDate()));
            document.add(new Paragraph("Montant : " + recu.getMontantEncaisse() + " FCFA"));
            document.add(new Paragraph("Bénéficiaire : " + recu.getBeneficiaire()));
            document.add(new Paragraph("Motif : " + recu.getMotif()));
            document.add(new Paragraph("Mode : " + recu.getMode()));

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF", e);
        }

        return out.toByteArray();
    }
}
