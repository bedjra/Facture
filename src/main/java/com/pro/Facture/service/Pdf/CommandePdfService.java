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
        Rectangle size = new Rectangle(250, 700);
        Document doc = new Document(size, 10, 10, 10, 10);

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            // FONTS
            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 8);
            Font small = FontFactory.getFont(FontFactory.HELVETICA, 7);

            // HEADER
            PdfPTable head = new PdfPTable(1);
            head.setWidthPercentage(100);
            head.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            head.addCell(new Paragraph(place.getNom(), bold));
            if (place.getAdresse() != null) head.addCell(new Paragraph(place.getAdresse(), small));
            if (place.getTelephone() != null)
                head.addCell(new Paragraph("Tel : " + place.getTelephone(), small));

            doc.add(head);
            doc.add(Chunk.NEWLINE);

            // TITRE
            Paragraph titre = new Paragraph("FACTURE", title);
            titre.setAlignment(Element.ALIGN_CENTER);
            doc.add(titre);

            doc.add(Chunk.NEWLINE);

            // INFO FACTURE
            PdfPTable info = new PdfPTable(1);
            info.setWidthPercentage(100);
            info.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            info.addCell(new Paragraph("Réf : " + dto.getRef(), normal));
            info.addCell(new Paragraph("Date : " + dto.getDateFacture(), normal));
            info.addCell(new Paragraph("Client : " + dto.getClient().getNom(), normal));

            doc.add(info);

            doc.add(Chunk.NEWLINE);

            // TABLEAU DES LIGNES
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 1});

            PdfPCell c1 = new PdfPCell(new Phrase("Désignation", bold));
            PdfPCell c2 = new PdfPCell(new Phrase("Montant HT", bold));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(c1);
            table.addCell(c2);

            for (LigneCommandeResponseDto l : dto.getLignes()) {
                table.addCell(new Phrase(l.getDesign(), normal));
                table.addCell(new Phrase(format(l.getBaseHT()), normal));
            }

            doc.add(table);
            doc.add(Chunk.NEWLINE);

            // TOTALS
            PdfPTable tot = new PdfPTable(2);
            tot.setWidthPercentage(100);
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

            Paragraph msg = new Paragraph("Merci pour votre confiance !", small);
            msg.setAlignment(Element.ALIGN_CENTER);
            doc.add(msg);

            doc.close();

        } catch (Exception e) {
            throw new RuntimeException("Erreur PDF : " + e.getMessage());
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
