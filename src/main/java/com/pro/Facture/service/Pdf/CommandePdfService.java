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
        Document doc = new Document(PageSize.A4, 25, 25, 28, 28);

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            // ===============================
            // 🔤 FONTS
            // ===============================
            Font fTitle = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font fBold = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
            Font fNormal = new Font(Font.FontFamily.HELVETICA, 11);

            // ===============================
// 🔵 ENTÊTE
// ===============================
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{1, 3});

// ===== LOGO =====
            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);

            if (place.getLogo() != null) {
                Image logo = Image.getInstance(place.getLogo());
                logo.scaleToFit(80, 80);
                logoCell.addElement(logo);
            }

            header.addCell(logoCell);

// ===== INFOS DU CABINET =====
            PdfPCell infoCell = new PdfPCell();
            infoCell.setBorder(Rectangle.NO_BORDER);

            infoCell.addElement(new Phrase(place.getNom(), fTitle));
            infoCell.addElement(new Phrase(place.getDesc(), fNormal));
            infoCell.addElement(new Phrase("📍 " + place.getAdresse(), fNormal));
            infoCell.addElement(new Phrase("📞 " + place.getTelephone() + " / " + place.getCel(), fNormal));
            infoCell.addElement(new Phrase("✉ " + place.getEmail(), fNormal));

            header.addCell(infoCell);

            doc.add(header);
            doc.add(space(15));

            // ===============================
            // 🔵 INFOS CLIENT & FACTURE
            // ===============================
            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);

            PdfPTable client = new PdfPTable(1);
            client.addCell(block("Client : " + dto.getClient().getNom(), fBold));
            client.addCell(block("NIF : " + n(dto.getClient().getNIF()), fNormal));
            client.addCell(block("Téléphone : " + n(dto.getClient().getTelephone()), fNormal));
            client.addCell(block("Adresse : " + n(dto.getClient().getAdresse()), fNormal));

            PdfPTable facture = new PdfPTable(1);
            facture.addCell(block("Facture N° : " + dto.getRef(), fBold));
            facture.addCell(block("Date : " + dto.getDateFacture(), fNormal));

            info.addCell(wrapper(client));
            info.addCell(wrapper(facture));

            doc.add(info);
            doc.add(space(15));

            // ===============================
            // 🔵 TABLE DES LIGNES
            // ===============================
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 4, 2});

            table.addCell(headerCell("Réf", fBold));
            table.addCell(headerCell("Désignation", fBold));
            table.addCell(headerCell("MT HT", fBold));

            int i = 1;
            for (LigneCommandeResponseDto l : dto.getLignes()) {
                table.addCell(normalCell(String.valueOf(i++), fNormal));
                table.addCell(normalCell(l.getDesign(), fNormal));
                table.addCell(rightCell(format(l.getBaseHT()), fNormal));
            }

            doc.add(table);
            doc.add(space(15));

            // ===============================
            // 🔵 TABLEAU RÉCAPITULATIF (HORIZONTAL)
            // ===============================
            PdfPTable recap = new PdfPTable(7);
            recap.setWidthPercentage(100);
            recap.setWidths(new float[]{1,1,1,1,1,1,1});

            recap.addCell(headerCell("Base", fBold));
            recap.addCell(headerCell("Retenue", fBold));
            recap.addCell(headerCell("MT", fBold));
            recap.addCell(headerCell("TVA 18%", fBold));
            recap.addCell(headerCell("MT TTC", fBold));
            recap.addCell(headerCell("Avance", fBold));
            recap.addCell(headerCell("RESTE À PAYER", fBold));

            recap.addCell(normalCell(format(dto.getTotalBaseHT()), fNormal));
            recap.addCell(normalCell(format(dto.getTotalRetenue()), fNormal));
            recap.addCell(normalCell(format(dto.getTotalHTNet()), fNormal));
            recap.addCell(normalCell(format(dto.getTotalTva()), fNormal));
            recap.addCell(normalCell(format(dto.getTotalTTC()), fNormal));
            recap.addCell(normalCell(format(dto.getTotalAvance()), fNormal));
            recap.addCell(normalCell(format(dto.getTotalNetAPayer()), fBold));

            doc.add(recap);
            doc.add(space(15));

            // ===============================
            // 🔵 TABLEAU CONDITIONS
            // ===============================
            PdfPTable conditions = new PdfPTable(2);
            conditions.setWidthPercentage(60);
            conditions.setHorizontalAlignment(Element.ALIGN_LEFT);
            conditions.setWidths(new float[]{2, 2});

            conditions.addCell(headerCell("Condition de règlement", fBold));
            conditions.addCell(normalCell(
                    "Reste à payer : " + format(dto.getTotalNetAPayer()) + " FCFA",
                    fNormal
            ));

            conditions.addCell(headerCell("Émis par", fBold));
            conditions.addCell(normalCell("Utilisateur", fNormal));

            conditions.addCell(headerCell("Arrêté la présente facture à la somme de", fBold));
            conditions.addCell(normalCell(
                    format(dto.getTotalNetAPayer()) + " FCFA",
                    fBold
            ));

            doc.add(conditions);
            doc.add(space(25));

            // ===============================
            // 🔵 SIGNATURE
            // ===============================
            Paragraph sign = new Paragraph(
                    "DIRECTEUR GENERAL\n\n\n" + place.getEmail(),
                    fBold
            );
            sign.setAlignment(Element.ALIGN_RIGHT);
            doc.add(sign);

            doc.close();

        } catch (Exception e) {
            throw new RuntimeException("Erreur PDF : " + e.getMessage());
        }

        return out.toByteArray();
    }

    // ===============================
    // 🔧 HELPERS
    // ===============================
    private PdfPCell block(String s, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(s, f));
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(4);
        return c;
    }

    private PdfPCell headerCell(String s, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(s, f));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setBackgroundColor(new BaseColor(230, 230, 230));
        c.setPadding(6);
        return c;
    }

    private PdfPCell normalCell(String s, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(s + " FCFA", f));
        c.setPadding(6);
        return c;
    }

    private PdfPCell rightCell(String s, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(s + " FCFA", f));
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c.setPadding(6);
        return c;
    }

    private PdfPCell wrapper(PdfPTable tb) {
        PdfPCell c = new PdfPCell(tb);
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    private Paragraph space(float h) {
        Paragraph p = new Paragraph("");
        p.setSpacingBefore(h);
        return p;
    }

    private String format(double d) {
        return String.format("%,.0f", d);
    }

    private String n(String s) {
        return s == null ? "" : s;
    }
}
