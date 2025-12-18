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

            // FONTS
            Font fTitle = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font fBold = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
            Font fNormal = new Font(Font.FontFamily.HELVETICA, 11);
            Font fSmall = new Font(Font.FontFamily.HELVETICA, 9);

            // ===============================
            // 🔵 ENTÊTE COMME LE PDF
            // ===============================
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{1, 2});

            // Logo (optionnel)
            PdfPCell logo = new PdfPCell();
            logo.setBorder(Rectangle.NO_BORDER);
            logo.setPadding(0);

            // Si tu as un logo → décommente :
            // Image img = Image.getInstance("classpath:logo.png");
            // img.scaleAbsolute(80, 60);
            // logo.addElement(img);

            header.addCell(logo);

            // Texte du cabinet
            PdfPCell textCab = new PdfPCell();
            textCab.setBorder(Rectangle.NO_BORDER);
            textCab.addElement(new Phrase(place.getNom(), fTitle));
            textCab.addElement(new Phrase("Cabinet d'expertise comptable et d’audit", fNormal));
            header.addCell(textCab);

            doc.add(header);
            doc.add(space(10));

            // ===============================
            // 🔵 DETAILS CLIENT + FACTURE
            // ===============================

            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);
            info.setWidths(new float[]{1, 1});

            // CLIENT
            PdfPTable client = new PdfPTable(1);
            client.setWidthPercentage(100);
            client.addCell(block("Client : " + dto.getClient().getNom(), fBold));
            client.addCell(block("NIF : " + n(dto.getClient().getNIF()), fNormal));
            client.addCell(block("Téléphone : " + n(dto.getClient().getTelephone()), fNormal));
            client.addCell(block("Adresse : " + n(dto.getClient().getAdresse()), fNormal));

            info.addCell(wrapper(client));

            // FACTURE
            PdfPTable blocFact = new PdfPTable(1);
            blocFact.setWidthPercentage(100);
            blocFact.addCell(block("Facture N° : " + dto.getRef(), fBold));
            blocFact.addCell(block("Date : " + dto.getDateFacture().toString(), fNormal));

            info.addCell(wrapper(blocFact));

            doc.add(info);
            doc.add(space(15));

            // ===============================
            // 🔵 TABLEAU PRINCIPAL
            // ===============================

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 3, 1});

            table.addCell(headerCell("Réf", fBold));
            table.addCell(headerCell("Désignation", fBold));
            table.addCell(headerCell("MT HT", fBold));

            for (LigneCommandeResponseDto l : dto.getLignes()) {
                table.addCell(normalCell("00001", fNormal));
                table.addCell(normalCell(l.getDesign(), fNormal));
                table.addCell(rightCell(format(l.getBaseHT()), fNormal));
            }

            doc.add(table);
            doc.add(space(15));

            // ===============================
            // 🔵 TABLEAU RÉCAPITULATIF
            // ===============================
            PdfPTable recap = new PdfPTable(6);
            recap.setWidthPercentage(100);
            recap.setWidths(new float[]{1,1,1,1,1,1});

            recap.addCell(headerCell("Base", fBold));
            recap.addCell(headerCell("Retenue 5%", fBold));
            recap.addCell(headerCell("MT", fBold));
            recap.addCell(headerCell("TVA 18%", fBold));
            recap.addCell(headerCell("MT TTC", fBold));
            recap.addCell(headerCell("NET À PAYER", fBold));

            recap.addCell(normalCell(format(dto.getTotalBaseHT()), fNormal));
            recap.addCell(normalCell(format(dto.getTotalRetenue()), fNormal));
            recap.addCell(normalCell(format(dto.getTotalHTNet()), fNormal));
            recap.addCell(normalCell(format(dto.getTotalTva()), fNormal));
            recap.addCell(normalCell(format(dto.getTotalTTC()), fNormal));
            recap.addCell(normalCell(format(dto.getTotalNetAPayer()), fBold));

            doc.add(recap);
            doc.add(space(10));

            // ===============================
            // 🔵 ARRETE À LA SOMME
            // ===============================
            Paragraph arr = new Paragraph(
                    "Arrêté la présente facture à la somme de : " +
//                            valeurEnLettre((int) dto.getTotalNetAPayer()) + " FCFA",
                    fNormal
            );
            doc.add(arr);
            doc.add(space(20));

            // ===============================
            // 🔵 SIGNATURE
            // ===============================

            Paragraph sign = new Paragraph("DIRECTEUR GENERAL\n\n\n" + place.getEmail(), fBold);
            sign.setAlignment(Element.ALIGN_RIGHT);
            doc.add(sign);

            doc.close();

        } catch (Exception e) {
            throw new RuntimeException("Erreur PDF : " + e.getMessage());
        }

        return out.toByteArray();
    }


    // ================================
    // 🔧 HELPERS
    // ================================

    private PdfPCell block(String s, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(s, f));
        c.setHorizontalAlignment(Element.ALIGN_LEFT);
        c.setBorder(Rectangle.NO_BORDER);
        c.setPaddingBottom(3);
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
        PdfPCell c = new PdfPCell(new Phrase(s, f));
        c.setHorizontalAlignment(Element.ALIGN_LEFT);
        c.setPadding(6);
        return c;
    }

    private PdfPCell rightCell(String s, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(s, f));
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

    private String n(String str) {
        return str == null ? "" : str;
    }

    // Convertir en lettres simplifié
    private String valeurEnLettre(int n) {
        return n + ""; // si tu veux je peux te générer une vraie fonction complète
    }
}
