package com.pro.Facture.service.Pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.pro.Facture.Dto.CommandeResponseDto;
import com.pro.Facture.Dto.LigneCommandeResponseDto;
import com.pro.Facture.Entity.Place;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Service
public class CommandePdfService {

    private static final String PDF_BASE_FOLDER = "pdfFactures/";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] genererPdf(CommandeResponseDto dto, Place place) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 30, 30, 35, 35);

        try {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            doc.open();

            Font fCompanyName = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Font fSubtitle   = new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC);
            Font fTitle      = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font fBold       = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            Font fNormal     = new Font(Font.FontFamily.HELVETICA, 9,  Font.NORMAL);
            Font fSmall      = new Font(Font.FontFamily.HELVETICA, 8,  Font.NORMAL);
            Font fHighlight  = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

            // ============================================================
            // EN-TÊTE : Logo | Nom société + sous-titre
            // ============================================================
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{1.2f, 2.8f});
            header.setSpacingAfter(10);

            // Cellule logo
            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setPadding(20);
            logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            if (place.getLogo() != null) {
                Image logo = Image.getInstance(place.getLogo());
                logo.scaleToFit(100, 100);
                logoCell.addElement(logo);
            }

            header.addCell(logoCell);

            // Cellule nom société
            // ================== CELLULE INFO SOCIÉTÉ ==================
            PdfPCell infoCell = new PdfPCell();
            infoCell.setBorder(Rectangle.BOTTOM);
            infoCell.setBorderWidthBottom(1f);
            infoCell.setBorderColorBottom(BaseColor.BLACK);
            infoCell.setPaddingTop(5f);
            infoCell.setPaddingBottom(4f);      // Espace minimal entre le texte et le trait
            infoCell.setPaddingLeft(0f);
            infoCell.setPaddingRight(0f);
            infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            infoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            infoCell.setUseDescender(true);     // ← Clé : supprime l'espace résiduel sous les glyphes

// --------- Titre principal ----------
            Font fTitre = new Font(Font.FontFamily.HELVETICA, 26, Font.BOLD, new BaseColor(54, 54, 54));
            Paragraph titrePrincipal = new Paragraph("CFACI GROUP CONSULTING", fTitre);
            titrePrincipal.setAlignment(Element.ALIGN_CENTER);
            titrePrincipal.setSpacingBefore(0f);
            titrePrincipal.setSpacingAfter(2f);   // Petit espace entre titre et sous-titre
            infoCell.addElement(titrePrincipal);

// --------- Sous-titre en gras ----------
            Font fDesc = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.BLACK);
            Paragraph description = new Paragraph("Cabinet d'expertise comptable et d'audit", fDesc);
            description.setAlignment(Element.ALIGN_CENTER);
            description.setSpacingBefore(0f);
            description.setSpacingAfter(0f);
            infoCell.addElement(description);

// ====== Ajouter la cellule au header ======
            header.addCell(infoCell);
            doc.add(header);


            // ============================================================
            // DATE à droite
            // ============================================================
            Paragraph dateParagraph = new Paragraph(
                    "Date:" + formatDate(dto.getDateFacture()), fNormal);
            dateParagraph.setAlignment(Element.ALIGN_RIGHT);
            dateParagraph.setSpacingAfter(12);
            doc.add(dateParagraph);

            // ============================================================
            // BLOC CLIENT (gauche) + FACTURE N° (droite)
            // ============================================================
            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);
            info.setWidths(new float[]{1.2f, 1f});
            info.setSpacingAfter(20);


// ================== TABLE CLIENT ==================
            PdfPTable clientTable = new PdfPTable(1);
            clientTable.setWidthPercentage(100);

// IMPORTANT : les cellules ne doivent PAS avoir de bordures
            PdfPCell c1 = createClientRow("Client", dto.getClient().getNom(), fBold, fNormal);
            c1.setBorder(Rectangle.NO_BORDER);
            clientTable.addCell(c1);

            PdfPCell c2 = createClientRow("NIF", n(dto.getClient().getNIF()), fBold, fNormal);
            c2.setBorder(Rectangle.NO_BORDER);
            clientTable.addCell(c2);

            PdfPCell c3 = createClientRow("Tel", n(dto.getClient().getTelephone()), fBold, fNormal);
            c3.setBorder(Rectangle.NO_BORDER);
            clientTable.addCell(c3);

            PdfPCell c4 = createClientRow("Adresse", n(dto.getClient().getAdresse()), fBold, fNormal);
            c4.setBorder(Rectangle.NO_BORDER);
            clientTable.addCell(c4);


// ================== WRAPPER GLOBAL ==================
            PdfPCell clientWrapper = new PdfPCell(clientTable);
            clientWrapper.setBorder(Rectangle.BOX);   // Encadrement global uniquement ici
            clientWrapper.setPadding(8);              // Petit espace interne pour joli rendu

            info.addCell(clientWrapper);

            // --- Numéro de facture centré à droite ---
            PdfPCell numCell = new PdfPCell();
            numCell.setBorder(Rectangle.NO_BORDER);
            numCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            numCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            Paragraph numFacture = new Paragraph("Facture N°: FA/" + dto.getRef(), fHighlight);
            numFacture.setAlignment(Element.ALIGN_CENTER);
            numCell.addElement(numFacture);
            info.addCell(numCell);

            doc.add(info);

            // ============================================================
            // TABLE DES LIGNES : Réf | Désignation | MT HT
            // ============================================================
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.8f, 4.5f, 1.5f});
            table.setSpacingAfter(15);

            table.addCell(createTableHeader("Réf",         fBold));
            table.addCell(createTableHeader("Désignation", fBold));
            table.addCell(createTableHeader("MT HT",       fBold));

            int i = 1;
            for (LigneCommandeResponseDto l : dto.getLignes()) {
                String ref = String.format("%06d", i++);
                table.addCell(createTableCell(ref,               fNormal, Element.ALIGN_CENTER));
                table.addCell(createTableCell(l.getDesign(),     fNormal, Element.ALIGN_LEFT));
                table.addCell(createTableCell(format(l.getBaseHT()), fNormal, Element.ALIGN_RIGHT));
            }

            // Lignes vides pour remplissage visuel (optionnel, comme dans le modèle)
            for (int k = 0; k < 3; k++) {
                table.addCell(createTableCell("", fNormal, Element.ALIGN_CENTER));
                table.addCell(createTableCell("", fNormal, Element.ALIGN_LEFT));
                table.addCell(createTableCell("", fNormal, Element.ALIGN_RIGHT));
            }

            doc.add(table);

            // ============================================================
            // RÉCAPITULATIF : 8 colonnes (avec "Code")
            // ============================================================
            PdfPTable recap = new PdfPTable(8);
            recap.setWidthPercentage(100);
            recap.setWidths(new float[]{0.8f, 1f, 1f, 1f, 1f, 1f, 1f, 1.2f});
            recap.setSpacingAfter(20);

            recap.addCell(createTableHeader("Code",        fBold));
            recap.addCell(createTableHeader("BASE",        fBold));
            recap.addCell(createTableHeader("Retenue 5%",  fBold));
            recap.addCell(createTableHeader("MT",          fBold));
            recap.addCell(createTableHeader("TVA 18%",     fBold));
            recap.addCell(createTableHeader("MT TTC",      fBold));
            recap.addCell(createTableHeader("AVANCE",      fBold));

            PdfPCell resteHeader = createTableHeader("NET A PAYER", fBold);
            recap.addCell(resteHeader);

            // Valeurs
            recap.addCell(createTableCell("",                                      fNormal, Element.ALIGN_CENTER));
            recap.addCell(createTableCell(format(dto.getTotalBaseHT()),            fNormal, Element.ALIGN_CENTER));
            recap.addCell(createTableCell(format(dto.getTotalRetenue()),           fNormal, Element.ALIGN_CENTER));
            recap.addCell(createTableCell(format(dto.getTotalHTNet()),             fNormal, Element.ALIGN_CENTER));
            recap.addCell(createTableCell(format(dto.getTotalTva()),               fNormal, Element.ALIGN_CENTER));

            PdfPCell ttcCell = new PdfPCell(new Phrase(format(dto.getTotalTTC()), fBold));
            ttcCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            ttcCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            ttcCell.setPadding(8);
            recap.addCell(ttcCell);

            recap.addCell(createTableCell(format(dto.getTotalAvance()),            fNormal, Element.ALIGN_CENTER));

            PdfPCell resteValue = new PdfPCell(new Phrase(format(dto.getTotalNetAPayer()), fBold));
            resteValue.setHorizontalAlignment(Element.ALIGN_CENTER);
            resteValue.setVerticalAlignment(Element.ALIGN_MIDDLE);
            resteValue.setPadding(8);
            recap.addCell(resteValue);

            doc.add(recap);

            // ============================================================
            // CONDITIONS DE RÈGLEMENT
            // ============================================================
            PdfPTable conditions = new PdfPTable(1);
            conditions.setWidthPercentage(100);
            conditions.setSpacingAfter(12);

            // Ligne principale avec bordure
            PdfPTable lineTable = new PdfPTable(2);
            lineTable.setWidthPercentage(100);
            lineTable.setWidths(new float[]{3f, 2f});

            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setPadding(0);
            Paragraph left = new Paragraph();
            left.add(new Chunk("Conditions de règlement:   ", fBold));
            left.add(new Chunk(format(dto.getTotalNetAPayer()), fHighlight));
            leftCell.addElement(left);
            lineTable.addCell(leftCell);

            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setPadding(0);
            Paragraph right = new Paragraph("e-mail:" + place.getEmail(), fNormal);
            right.setAlignment(Element.ALIGN_RIGHT);
            rightCell.addElement(right);
            lineTable.addCell(rightCell);

            PdfPCell mainLine = new PdfPCell(lineTable);
            mainLine.setBorder(Rectangle.BOX);
            mainLine.setPadding(8);
            conditions.addCell(mainLine);

            // Montant en lettres
            PdfPCell amountLine = new PdfPCell();
            amountLine.setBorder(Rectangle.NO_BORDER);
            amountLine.setPaddingTop(6);
            long montant = Math.round(dto.getTotalNetAPayer());
            Paragraph amountText = new Paragraph(
                    "Arrêté la présente facture à la somme de : "
                            + nombreEnLettres(montant).toUpperCase() + " FCFA",
                    fBold
            );
            amountLine.addElement(amountText);
            conditions.addCell(amountLine);

            doc.add(conditions);

            doc.add(space(20));

            // ============================================================
            // SIGNATURE
            // ============================================================
            PdfPTable signTable = new PdfPTable(1);
            signTable.setWidthPercentage(40);
            signTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            PdfPCell signCell = new PdfPCell();
            signCell.setBorder(Rectangle.NO_BORDER);
            signCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            Paragraph signTitle = new Paragraph("DIRECTEUR GENERAL", fBold);
            signTitle.setAlignment(Element.ALIGN_CENTER);
            signCell.addElement(signTitle);

            signCell.addElement(space(120));

            Font fSmallBold = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            Paragraph signName = new Paragraph("MOVIA Kodzo A E", fSmallBold);
            signName.setAlignment(Element.ALIGN_CENTER);
            signCell.addElement(signName);

            signTable.addCell(signCell);
            doc.add(signTable);

            addFooter(writer, place, fSmall);

            doc.close();

            // Sauvegarde fichier
            String folder = PDF_BASE_FOLDER + place.getNom().replaceAll("[^a-zA-Z0-9]", "_") + "/";
            Files.createDirectories(Paths.get(folder));
            String fileName = "FACTURE_" + dto.getRef() + ".pdf";
            try (FileOutputStream fos = new FileOutputStream(folder + fileName)) {
                fos.write(out.toByteArray());
            }

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF : " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    // =====================================================================
    // HELPERS
    // =====================================================================

    /** Ligne client avec bordure BOX (comme dans le modèle papier) */
    private PdfPCell createClientRow(String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setPaddingTop(4);
        cell.setPaddingBottom(4);
        cell.setPaddingLeft(6);
        cell.setPaddingRight(6);

        Paragraph p = new Paragraph();
        p.setLeading(12);
        p.add(new Chunk(label + ":  ", labelFont));
        p.add(new Chunk(value, valueFont));
        cell.addElement(p);
        return cell;
    }

    private PdfPCell createTableHeader(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        return cell;
    }

    private PdfPCell createTableCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        return cell;
    }

    private Paragraph space(float height) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingBefore(height);
        return p;
    }

    private void addFooter(PdfWriter writer, Place place, Font font) throws DocumentException {
        PdfPTable footer = new PdfPTable(1);
        footer.setTotalWidth(writer.getPageSize().getWidth() - 60);
        footer.setLockedWidth(true);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.TOP);
        cell.setPaddingTop(6);
        cell.setPaddingBottom(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph l1 = new Paragraph(
                "Audit, Assistance Comptable, fiscale et Sociale-Travaux d'inventaire, "
                        + "Gestion des Salaires-Conseils et Formations", font);
        l1.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(l1);

        Paragraph l2 = new Paragraph(
                " Zanguera, Attilamounou (LOME-TOGO)", font);
        l2.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(l2);

        Paragraph l3 = new Paragraph(
                "Tel: 97 82 28 28 ;  "
                        + place.getEmail()
                        + "  NIF: 1001727149  •  N°CNSS: 474195", font);
        l3.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(l3);

        footer.addCell(cell);
        footer.writeSelectedRows(0, -1, 30, 55, writer.getDirectContent());
    }

    private String format(double d) {
        return String.format("%,.0f", d);
    }

    private String formatDate(java.time.LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "N/A";
    }

    private String n(String s) {
        return s == null || s.isEmpty() ? "" : s;
    }

    private String nombreEnLettres(long number) {
        if (number == 0) return "zéro";

        String[] units = {
                "", "un", "deux", "trois", "quatre", "cinq",
                "six", "sept", "huit", "neuf", "dix",
                "onze", "douze", "treize", "quatorze", "quinze",
                "seize", "dix-sept", "dix-huit", "dix-neuf"
        };
        String[] tens = {
                "", "", "vingt", "trente", "quarante",
                "cinquante", "soixante", "soixante-dix",
                "quatre-vingt", "quatre-vingt-dix"
        };

        if (number < 20) return units[(int) number];
        if (number < 100) {
            int t = (int) number / 10;
            int u = (int) number % 10;
            return tens[t] + (u != 0 ? "-" + units[u] : "");
        }
        if (number < 1000) {
            int h = (int) number / 100;
            int r = (int) number % 100;
            return (h > 1 ? units[h] + " " : "") + "cent" + (r != 0 ? " " + nombreEnLettres(r) : "");
        }
        if (number < 1_000_000) {
            long m = number / 1000;
            long r = number % 1000;
            return (m > 1 ? nombreEnLettres(m) + " " : "") + "mille" + (r != 0 ? " " + nombreEnLettres(r) : "");
        }
        return String.valueOf(number);
    }
}