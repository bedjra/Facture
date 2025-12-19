package com.pro.Facture.service.Pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.pro.Facture.Dto.CommandeResponseDto;
import com.pro.Facture.Dto.LigneCommandeResponseDto;
import com.pro.Facture.Entity.Place;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class CommandePdfService {

    // ===============================
    // 🎨 COULEURS PROFESSIONNELLES
    // ===============================
    private static final BaseColor PRIMARY_COLOR = new BaseColor(41, 128, 185);      // Bleu professionnel
    private static final BaseColor SECONDARY_COLOR = new BaseColor(52, 73, 94);      // Gris foncé
    private static final BaseColor HEADER_BG = new BaseColor(236, 240, 241);         // Gris clair
    private static final BaseColor ACCENT_COLOR = new BaseColor(231, 76, 60);        // Rouge accent
    private static final BaseColor SUCCESS_COLOR = new BaseColor(39, 174, 96);       // Vert
    private static final BaseColor TABLE_BORDER = new BaseColor(189, 195, 199);      // Bordure grise

    // Format de date français
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] genererPdf(CommandeResponseDto dto, Place place) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 30, 30, 35, 35);

        try {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            doc.open();

            // ===============================
            // 🔤 FONTS AMÉLIORÉES
            // ===============================
            Font fTitle = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, PRIMARY_COLOR);
            Font fSubtitle = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, SECONDARY_COLOR);
            Font fBold = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, SECONDARY_COLOR);
            Font fNormal = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, SECONDARY_COLOR);
            Font fSmall = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.GRAY);
            Font fHighlight = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, PRIMARY_COLOR);

            // ===============================
            // 🔵 ENTÊTE AVEC DESIGN MODERNE
            // ===============================
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{1.2f, 2.8f});
            header.setSpacingAfter(20);

            // ===== LOGO AVEC BORDURE =====
            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setPadding(10);
            logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            if (place.getLogo() != null) {
                Image logo = Image.getInstance(place.getLogo());
                logo.scaleToFit(100, 100);
                logo.setBorder(Rectangle.BOX);
                logo.setBorderWidth(1);
                logo.setBorderColor(TABLE_BORDER);
                logoCell.addElement(logo);
            }

            header.addCell(logoCell);

            // ===== INFOS DU CABINET STYLISÉES =====
            PdfPCell infoCell = new PdfPCell();
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setPaddingLeft(15);
            infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            Paragraph nomPlace = new Paragraph(place.getNom(), fTitle);
            nomPlace.setSpacingAfter(5);
            infoCell.addElement(nomPlace);

            if (place.getDesc() != null && !place.getDesc().isEmpty()) {
                Paragraph desc = new Paragraph(place.getDesc(), fNormal);
                desc.setSpacingAfter(8);
                infoCell.addElement(desc);
            }

            infoCell.addElement(createInfoLine("📍", place.getAdresse(), fNormal));
            infoCell.addElement(createInfoLine("📞", place.getTelephone() + " / " + place.getCel(), fNormal));
            infoCell.addElement(createInfoLine("✉", place.getEmail(), fNormal));

            header.addCell(infoCell);
            doc.add(header);

            // ===============================
            // 🔵 TITRE FACTURE
            // ===============================
            Paragraph factureTitle = new Paragraph("FACTURE", fSubtitle);
            factureTitle.setAlignment(Element.ALIGN_CENTER);
            factureTitle.setSpacingAfter(20);
            doc.add(factureTitle);

            // ===============================
            // 🔵 INFOS CLIENT & FACTURE DANS DES BOÎTES
            // ===============================
            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);
            info.setWidths(new float[]{1, 1});
            info.setSpacingAfter(20);

            // CLIENT
            PdfPTable clientTable = new PdfPTable(1);
            clientTable.addCell(createSectionHeader("INFORMATIONS CLIENT", fBold));
            clientTable.addCell(createInfoRow("Client", dto.getClient().getNom(), fBold, fNormal));
            clientTable.addCell(createInfoRow("NIF", n(dto.getClient().getNIF()), fSmall, fNormal));
            clientTable.addCell(createInfoRow("Téléphone", n(dto.getClient().getTelephone()), fSmall, fNormal));
            clientTable.addCell(createInfoRow("Adresse", n(dto.getClient().getAdresse()), fSmall, fNormal));

            // FACTURE
            PdfPTable factureTable = new PdfPTable(1);
            factureTable.addCell(createSectionHeader("DÉTAILS FACTURE", fBold));
            factureTable.addCell(createInfoRow("Numéro", dto.getRef(), fBold, fHighlight));
            factureTable.addCell(createInfoRow("Date", formatDate(dto.getDateFacture()), fSmall, fNormal));

            info.addCell(wrapperWithBorder(clientTable));
            info.addCell(wrapperWithBorder(factureTable));

            doc.add(info);

            // ===============================
            // 🔵 TABLE DES LIGNES AMÉLIORÉE
            // ===============================
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.5f, 3f, 1f, 1.2f});
            table.setSpacingAfter(15);

            table.addCell(createTableHeader("N°", fBold));
            table.addCell(createTableHeader("DÉSIGNATION", fBold));
            table.addCell(createTableHeader("QUANTITÉ", fBold));
            table.addCell(createTableHeader("MONTANT HT", fBold));

            int i = 1;
            for (LigneCommandeResponseDto l : dto.getLignes()) {
                table.addCell(createTableCell(String.valueOf(i++), fNormal, Element.ALIGN_CENTER));
                table.addCell(createTableCell(l.getDesign(), fNormal, Element.ALIGN_LEFT));
                table.addCell(createTableCell("1", fNormal, Element.ALIGN_CENTER));
                table.addCell(createTableCell(format(l.getBaseHT()) + " FCFA", fNormal, Element.ALIGN_RIGHT));
            }

            doc.add(table);

            // ===============================
            // 🔵 RÉCAPITULATIF HORIZONTAL
            // ===============================
            PdfPTable recap = new PdfPTable(7);
            recap.setWidthPercentage(100);
            recap.setWidths(new float[]{1f, 1f, 1f, 1f, 1f, 1f, 1.2f});
            recap.setSpacingAfter(20);

            // En-têtes
            recap.addCell(createTableHeader("Base HT", fBold));
            recap.addCell(createTableHeader("Retenue", fBold));
            recap.addCell(createTableHeader("MT HT Net", fBold));
            recap.addCell(createTableHeader("TVA 18%", fBold));
            recap.addCell(createTableHeader("Total TTC", fBold));
            recap.addCell(createTableHeader("Avance", fBold));

            PdfPCell resteHeader = createTableHeader("RESTE À PAYER", fBold);
            resteHeader.setBackgroundColor(ACCENT_COLOR);
            recap.addCell(resteHeader);

            // Valeurs
            recap.addCell(createTableCell(format(dto.getTotalBaseHT()) + " FCFA", fNormal, Element.ALIGN_CENTER));
            recap.addCell(createTableCell(format(dto.getTotalRetenue()) + " FCFA", fNormal, Element.ALIGN_CENTER));
            recap.addCell(createTableCell(format(dto.getTotalHTNet()) + " FCFA", fNormal, Element.ALIGN_CENTER));
            recap.addCell(createTableCell(format(dto.getTotalTva()) + " FCFA", fNormal, Element.ALIGN_CENTER));
            recap.addCell(createTableCell(format(dto.getTotalTTC()) + " FCFA", fNormal, Element.ALIGN_CENTER));
            recap.addCell(createTableCell(format(dto.getTotalAvance()) + " FCFA", fNormal, Element.ALIGN_CENTER));

            PdfPCell resteValue = new PdfPCell(new Phrase(format(dto.getTotalNetAPayer()) + " FCFA", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE)));
            resteValue.setBackgroundColor(ACCENT_COLOR);
            resteValue.setHorizontalAlignment(Element.ALIGN_CENTER);
            resteValue.setVerticalAlignment(Element.ALIGN_MIDDLE);
            resteValue.setPadding(8);
            resteValue.setBorderColor(TABLE_BORDER);
            recap.addCell(resteValue);

            doc.add(recap);

            // ===============================
            // 🔵 CONDITIONS DE RÈGLEMENT
            // ===============================
            PdfPTable conditions = new PdfPTable(1);
            conditions.setWidthPercentage(100);
            conditions.setSpacingAfter(15);

            PdfPCell condHeader = new PdfPCell(new Phrase("CONDITIONS DE RÈGLEMENT", fBold));
            condHeader.setBackgroundColor(HEADER_BG);
            condHeader.setPadding(8);
            condHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            conditions.addCell(condHeader);

            PdfPCell condContent = new PdfPCell();
            condContent.setPadding(10);
            condContent.addElement(new Phrase("Émis par : Utilisateur", fNormal));
            condContent.addElement(new Phrase("\nArrêté la présente facture à la somme de : " + format(dto.getTotalNetAPayer()) + " FCFA", fBold));
            conditions.addCell(condContent);

            doc.add(conditions);

            // ===============================
            // 🔵 SIGNATURE
            // ===============================
            doc.add(space(20));

            PdfPTable signTable = new PdfPTable(1);
            signTable.setWidthPercentage(40);
            signTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            PdfPCell signCell = new PdfPCell();
            signCell.setBorder(Rectangle.NO_BORDER);
            signCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            Paragraph signTitle = new Paragraph("DIRECTEUR GÉNÉRAL", fBold);
            signTitle.setAlignment(Element.ALIGN_CENTER);
            signCell.addElement(signTitle);

            signCell.addElement(space(30));

            Paragraph signEmail = new Paragraph(place.getEmail(), fSmall);
            signEmail.setAlignment(Element.ALIGN_CENTER);
            signCell.addElement(signEmail);

            signTable.addCell(signCell);
            doc.add(signTable);

            // ===============================
            // 🔵 FOOTER
            // ===============================
            addFooter(writer, place, fSmall);

            doc.close();

        } catch (Exception e) {
            throw new RuntimeException("Erreur PDF : " + e.getMessage());
        }

        return out.toByteArray();
    }

    // ===============================
    // 🔧 HELPERS AMÉLIORÉS
    // ===============================

    private Paragraph createInfoLine(String icon, String text, Font font) {
        Paragraph p = new Paragraph(icon + "  " + text, font);
        p.setSpacingAfter(3);
        return p;
    }

    private PdfPCell createSectionHeader(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(PRIMARY_COLOR);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        Font whiteFont = new Font(font.getFamily(), font.getSize(), font.getStyle(), BaseColor.WHITE);
        cell.setPhrase(new Phrase(text, whiteFont));
        return cell;
    }

    private PdfPCell createInfoRow(String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);

        Paragraph p = new Paragraph();
        p.add(new Chunk(label + ": ", labelFont));
        p.add(new Chunk(value, valueFont));
        cell.addElement(p);

        return cell;
    }

    private PdfPCell createTableHeader(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(SECONDARY_COLOR);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        cell.setBorderColor(TABLE_BORDER);
        Font whiteFont = new Font(font.getFamily(), font.getSize(), font.getStyle(), BaseColor.WHITE);
        cell.setPhrase(new Phrase(text, whiteFont));
        return cell;
    }

    private PdfPCell createTableCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        cell.setBorderColor(TABLE_BORDER);
        return cell;
    }

    private PdfPCell wrapperWithBorder(PdfPTable table) {
        PdfPCell cell = new PdfPCell(table);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(TABLE_BORDER);
        cell.setBorderWidth(1);
        cell.setPadding(0);
        return cell;
    }

    private Paragraph space(float height) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingBefore(height);
        return p;
    }

    private void addFooter(PdfWriter writer, Place place, Font font) throws DocumentException {

        Font boldSmall = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.DARK_GRAY);
        Font normalSmall = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.DARK_GRAY);
        Font blueSmall = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, new BaseColor(0, 0, 180));

        PdfPTable footer = new PdfPTable(1);
        footer.setTotalWidth(writer.getPageSize().getWidth() - 60);
        footer.setLockedWidth(true);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.TOP);
        cell.setBorderColor(TABLE_BORDER);
        cell.setPaddingTop(6);
        cell.setPaddingBottom(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        // 🔹 Ligne 1 – Activités (GRAS)
        Paragraph l1 = new Paragraph(
                "Audit, Assistance Comptable, fiscale et Sociale - Travaux d’inventaire, "
                        + "Gestion des Salaires - Conseils et Formations",
                boldSmall
        );
        l1.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(l1);

        // 🔹 Ligne 2 – Adresse
        Paragraph l2 = new Paragraph(
                "Non loin du carrefour Aïsed, juste à 50m de la pharmacie Adouni (LOME-TOGO)",
                normalSmall
        );
        l2.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(l2);

        // 🔹 Ligne 3 – Contacts (BLEU)
        Paragraph l3 = new Paragraph(
                "Tel: 90 88 94 60 / 99 70 42 88 / 97 82 28 28  •  "
                        + "cfacigroup@gmail.com  •  NIF: 1001727149  •  N°CNSS: 474195",
                blueSmall
        );
        l3.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(l3);

        footer.addCell(cell);

        footer.writeSelectedRows(
                0,
                -1,
                30,
                55,
                writer.getDirectContent()
        );
    }


    private String format(double d) {
        return String.format("%,.0f", d);
    }

    private String formatDate(java.time.LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "N/A";
    }

    private String n(String s) {
        return s == null || s.isEmpty() ? "N/A" : s;
    }
}