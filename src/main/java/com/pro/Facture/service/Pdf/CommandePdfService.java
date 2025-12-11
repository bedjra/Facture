package com.pro.Facture.service.Pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.pro.Facture.Dto.CommandeResponseDto;
import com.pro.Facture.Dto.LigneCommandeResponseDto;
import com.pro.Facture.Entity.Place;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class CommandePdfService {

    public byte[] genererPdf(CommandeResponseDto dto, Place place) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 📄 Format A4 + marges propres
        Document doc = new Document(PageSize.A4, 20, 20, 20, 20);

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            // FONTS
            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font small = FontFactory.getFont(FontFactory.HELVETICA, 9);

            // ===============================
            // 📌 HEADER PLACE
            // ===============================
            PdfPTable head = new PdfPTable(1);
            head.setWidthPercentage(100);
            head.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            head.addCell(new Phrase(place.getNom(), bold));
            if (place.getAdresse() != null)
                head.addCell(new Phrase(place.getAdresse(), small));
            if (place.getTelephone() != null)
                head.addCell(new Phrase("Téléphone : " + place.getTelephone(), small));

            doc.add(head);
            doc.add(Chunk.NEWLINE);

            // ===============================
            // 📌 TITRE FACTURE
            // ===============================
            Paragraph titre = new Paragraph("FACTURE", title);
            titre.setAlignment(Element.ALIGN_CENTER);
            doc.add(titre);

            doc.add(Chunk.NEWLINE);

            // ===============================
            // 📌 INFORMATIONS FACTURE
            // ===============================
            PdfPTable info = new PdfPTable(1);
            info.setWidthPercentage(100);
            info.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            info.addCell(new Phrase("Référence : " + dto.getRef(), normal));
            info.addCell(new Phrase("Date : " + dto.getDateFacture(), normal));
            info.addCell(new Phrase("Client : " + dto.getClient().getNom(), normal));

            doc.add(info);

            doc.add(Chunk.NEWLINE);

            // ===============================
            // 📌 TABLEAU LIGNES DE COMMANDE
            // ===============================
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1.5f});

            // En-têtes
            PdfPCell c1 = new PdfPCell(new Phrase("Désignation", bold));
            PdfPCell c2 = new PdfPCell(new Phrase("Montant HT", bold));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(c1);
            table.addCell(c2);

            // Contenu
            for (LigneCommandeResponseDto l : dto.getLignes()) {
                table.addCell(new Phrase(l.getDesign(), normal));
                table.addCell(new Phrase(format(l.getBaseHT()), normal));
            }

            doc.add(table);

            doc.add(Chunk.NEWLINE);

            // ===============================
            // 📌 TOTALS
            // ===============================
            PdfPTable tot = new PdfPTable(2);
            tot.setWidthPercentage(50);
            tot.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tot.setWidths(new float[]{2, 1});
            tot.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            add(tot, "Total HT", dto.getTotalBaseHT());
            add(tot, "Retenue", dto.getTotalRetenue());
            add(tot, "Total HT Net", dto.getTotalHTNet());
            add(tot, "TVA (18%)", dto.getTotalTva());
            add(tot, "Total TTC", dto.getTotalTTC());
            add(tot, "Avance", dto.getTotalAvance());
            add(tot, "Net à payer", dto.getTotalNetAPayer());

            doc.add(tot);

            doc.add(Chunk.NEWLINE);

            // ===============================
            // 📌 FOOTER
            // ===============================
            Paragraph msg = new Paragraph("Merci pour votre confiance !", small);
            msg.setAlignment(Element.ALIGN_CENTER);
            doc.add(msg);

            doc.close();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF : " + e.getMessage());
        }

        return out.toByteArray();
    }

    private void add(PdfPTable tab, String label, double value) {
        PdfPCell c1 = new PdfPCell(new Phrase(label));
        c1.setBorder(Rectangle.NO_BORDER);

        PdfPCell c2 = new PdfPCell(new Phrase(format(value)));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);

        tab.addCell(c1);
        tab.addCell(c2);
    }

    private String format(double d) {
        return String.format("%.0f F", d);
    }
}
