package com.pro.Facture.service.Pdf;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.DashedBorder;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.pro.Facture.Entity.Place;
import com.pro.Facture.Entity.Recu;
import com.pro.Facture.repository.PlaceRepository;
import com.pro.Facture.repository.RecuRepository;
import com.pro.Facture.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.time.format.DateTimeFormatter;

@Service
public class RecuPdfService {

    private final UtilisateurRepository utilisateurRepository;  // ← ajouter
    private final RecuRepository recuRepository;
    private final PlaceRepository placeRepository;

    public RecuPdfService(UtilisateurRepository utilisateurRepository, RecuRepository recuRepository,
                          PlaceRepository placeRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.recuRepository = recuRepository;
        this.placeRepository = placeRepository;
    }

    public byte[] generatePdf(Long id) {
        Recu recu = recuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reçu non trouvé"));

        Place place = placeRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("Cabinet non configuré"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            String folderName = "RecuPdf";
            Path folderPath = Paths.get(folderName);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            String fileName = "Recu_" + (recu.getNumeroPiece() != null
                    ? recu.getNumeroPiece().replace("/", "-") : id) + ".pdf";
            File destinationFile = new File(folderName, fileName);

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(10, 35, 20, 35);

            buildRecuBlock(document, place, recu, "EXEMPLAIRE CLIENT");
            addSeparator(document);
            buildRecuBlock(document, place, recu, "EXEMPLAIRE CABINET");

            document.close();

            byte[] pdfBytes = out.toByteArray();
            try (FileOutputStream fos = new FileOutputStream(destinationFile)) {
                fos.write(pdfBytes);
            }

            return pdfBytes;

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération ou stockage PDF : " + e.getMessage(), e);
        }
    }

    // =========================================================
    //  SÉPARATEUR POINTILLÉS
    // =========================================================
    private void addSeparator(Document document) {
        Table sep = new Table(UnitValue.createPercentArray(new float[]{1f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(4)
                .setMarginBottom(4);

        Cell sepCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderTop(new DashedBorder(ColorConstants.DARK_GRAY, 1.2f))
                .setPaddingTop(4)
                .setPaddingBottom(4);

//        Paragraph cutLine = new Paragraph("✂  Découper ici  ✂")
//                .setFontSize(7)
//                .setFontColor(new DeviceGray(0.5f))
//                .setTextAlignment(TextAlignment.CENTER)
//                .setMarginTop(2)
//                .setMarginBottom(0);
//
//        sepCell.add(cutLine);
        sep.addCell(sepCell);
        document.add(sep);
    }

    // =========================================================
    //  BLOC DU REÇU (réutilisé 2 fois)
    // =========================================================
    private void buildRecuBlock(Document document, Place place, Recu recu, String exemplaire) throws Exception {

        // ── Bandeau exemplaire (coin droit) ──
        document.add(new Paragraph(exemplaire)
                .setFontSize(7)
                .setBold()
                .setFontColor(new DeviceGray(0.45f))
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(2));

        // ============================================================
        // EN-TÊTE : [LOGO à gauche] | [TITRE + SOUS-TITRE à droite]
        // Les deux cellules partagent la même ligne de fond (bordure bas)
        // ============================================================
        Table headerTitre = new Table(UnitValue.createPercentArray(new float[]{22, 78}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(3);

        // Cellule logo
        Cell logoCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(ColorConstants.BLACK, 1f))
                .setPadding(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        if (place.getLogo() != null && place.getLogo().length > 0) {
            try {

                Image logo = new Image(ImageDataFactory.create(place.getLogo()));

                // équivalent de scaleToFit(100,100)
                logo.setAutoScale(true);
                logo.setMaxWidth(100);
                logo.setMaxHeight(100);

                logoCell.add(logo);

            } catch (Exception ignored) {
            }
        }

        headerTitre.addCell(logoCell);

        // Cellule titre
        Cell titreCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(ColorConstants.BLACK, 1f))
                .setPaddingLeft(8)
                .setPaddingBottom(5)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        titreCell.add(new Paragraph("CFACI GROUP CONSULTING")
                .setBold()
                .setFontSize(20)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(2));

        titreCell.add(new Paragraph("Cabinet d'expertise comptable et d'audit")
                .setFontSize(10)
                .setFontColor(ColorConstants.BLACK)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(0));

        headerTitre.addCell(titreCell);
        document.add(headerTitre);

        // ============================================================
        // SECTION INFOS : [infos cabinet gauche] | [infos pièce droite]
        // ============================================================
        float s = 8f;
        float lineSpacing = 1.5f;

        String telephone = place.getTelephone() != null ? place.getTelephone() : "";
        String cel       = place.getCel() != null && !place.getCel().isEmpty() ? place.getCel() : "";
        String tels      = !telephone.isEmpty() && !cel.isEmpty()
                ? telephone + " / " + cel : telephone + cel;

        int compteur = Math.toIntExact(recu.getId() != null ? recu.getId() : 1);
        String annee = String.valueOf(Year.now().getValue());
        String numeroPieceFormat = String.format("%03d/CFACI/%s", compteur, annee);
        String dateFormatee = recu.getDate() != null
                ? recu.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";

        Table infoHeader = new Table(UnitValue.createPercentArray(new float[]{55, 45}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(8);

        // Colonne gauche : infos cabinet
        Cell cabinetCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingRight(8)
                .setVerticalAlignment(VerticalAlignment.TOP);

        cabinetCell.add(new Paragraph()
                .add(new Text("ACTIVITE").setBold().setUnderline().setFontSize(s))
                .add(new Text(" : ").setBold().setFontSize(s))
                .add(new Text("Audit, Assistance comptable, fiscale et sociale").setFontSize(s))
                .setMultipliedLeading(1.2f).setMarginBottom(0));

        cabinetCell.add(new Paragraph()
                .add(new Text("Travaux d'inventaire, Recrutement et Formation").setFontSize(s))
                .setMarginTop(0).setMarginBottom(lineSpacing));

        cabinetCell.add(new Paragraph()
                .add(new Text("SIEGE").setBold().setUnderline().setFontSize(s))
                .add(new Text(" : ").setBold().setFontSize(s))
                .add(new Text(place.getAdresse() != null ? place.getAdresse() : "-").setFontSize(s))
                .setMarginBottom(lineSpacing));

        cabinetCell.add(new Paragraph()
                .add(new Text("Tel").setBold().setUnderline().setFontSize(s))
                .add(new Text(" : ").setBold().setFontSize(s))
                .add(new Text(!tels.isEmpty() ? tels : "-").setFontSize(s))
                .setMarginBottom(lineSpacing));

        cabinetCell.add(new Paragraph()
                .add(new Text("E-mail").setBold().setUnderline().setFontSize(s))
                .add(new Text(" : ").setBold().setFontSize(s))
                .add(new Text(place.getEmail() != null && !place.getEmail().isEmpty()
                        ? place.getEmail() : "-").setFontSize(s))
                .setMarginBottom(lineSpacing));

        cabinetCell.add(new Paragraph()
                .add(new Text("NIF").setBold().setUnderline().setFontSize(s))
                .add(new Text(" : ").setBold().setFontSize(s))
                .add(new Text("1 001 727 149").setFontSize(s))
                .setMarginBottom(0));

        infoHeader.addCell(cabinetCell);

        // Colonne droite : infos pièce
        Cell pieceCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.TOP);

        pieceCell.add(labelValueRow("DATE :", dateFormatee));
        pieceCell.add(labelValueRow("PIÈCE DE CAISSE N° :", numeroPieceFormat));
//        pieceCell.add(labelValueRow("MONTANT ENCAISSÉ :", recu.getMontantEncaisse() != null
//                ? format(recu.getMontantEncaisse().doubleValue()) + " FCFA" : ""));
//        pieceCell.add(labelValueRow("MONTANT TOTAL :", recu.getMontantTotal() != null
//                ? format(recu.getMontantTotal().doubleValue()) + " FCFA" : ""));
//        pieceCell.add(labelValueRow("RESTE À PAYER :", recu.getReste() != null
//                ? format(recu.getReste().doubleValue()) + " FCFA" : ""));
//        pieceCell.add(labelValueRow("MODE DE PAIEMENT :", recu.getMode() != null
//                ? recu.getMode() : ""));

        infoHeader.addCell(pieceCell);
        document.add(infoHeader);

        // ── TABLEAU BÉNÉFICIAIRE / SOMME / MOTIF ──
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(new SolidBorder(ColorConstants.BLACK, 1))
                .setMarginBottom(8);

//        infoTable.addCell(new Cell()
//                .setBorder(Border.NO_BORDER)
//                .setBorderBottom(new SolidBorder(ColorConstants.BLACK, 0.5f))
//                .setPadding(6)
//                .add(new Paragraph()
//                        .add(new Text("Bénéficiaire : ").setBold().setFontSize(9))
//                        .add(new Text(recu.getBeneficiaire() != null ? recu.getBeneficiaire() : "").setFontSize(9))));

        infoTable.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(ColorConstants.BLACK, 0.5f))
                .setPadding(6)
                .add(new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                        .setWidth(UnitValue.createPercentValue(100))
                        .addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0)
                                .add(new Paragraph()
                                        .add(new Text("Bénéficiaire : ").setBold().setFontSize(9))
                                        .add(new Text(recu.getBeneficiaire() != null ? recu.getBeneficiaire() : "").setFontSize(9))))
                        .addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(0)
                                .setTextAlignment(TextAlignment.RIGHT)
                                .add(new Paragraph()
                                        .add(new Text("Num : ").setBold().setFontSize(9))
                                        .add(new Text(recu.getNumBenef() != null ? recu.getNumBenef() : "").setFontSize(9))))));;

        String montantLettre = convertirEnLettres(recu.getMontantEncaisse());
        infoTable.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(ColorConstants.BLACK, 0.5f))
                .setPadding(6)
                .add(new Paragraph()
                        .add(new Text("La somme de ( en lettre ) : ").setBold().setFontSize(9))
                        .add(new Text(montantLettre).setFontSize(9))));

//        infoTable.addCell(new Cell()
//                .setBorder(Border.NO_BORDER)
//                .setBorderBottom(new SolidBorder(ColorConstants.BLACK, 0.5f))
//                .setPadding(6)
//                .setMinHeight(25)
//                .add(new Paragraph()
//                        .add(new Text("Motif : ").setBold().setFontSize(9))
//                        .add(new Text(couperTexte(recu.getMotif(), 95)).setFontSize(9))));

        infoTable.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(ColorConstants.BLACK, 0.5f))
                .setPadding(6)
                .setMinHeight(25)
                .add(new Paragraph()
                        .add(new Text("Motif : ").setBold().setFontSize(9))
                        .add(new Text(recu.getMotif() != null ? recu.getMotif() : "").setFontSize(9))
                )
        );

        // Ligne récapitulatif montants
        Table montantsRow = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100));

        montantsRow.addCell(new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph()
                        .add(new Text("Montant total : ").setBold().setFontSize(9))
                        .add(new Text(recu.getMontantTotal() != null
                                ? format(recu.getMontantTotal().doubleValue()) + " FCFA" : "-").setFontSize(9))));

        montantsRow.addCell(new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph()
                        .add(new Text("Encaissé : ").setBold().setFontSize(9))
                        .add(new Text(recu.getMontantEncaisse() != null
                                ? format(recu.getMontantEncaisse().doubleValue()) + " FCFA" : "-").setFontSize(9))));

        montantsRow.addCell(new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph()
                        .add(new Text("Reste : ").setBold().setFontSize(9))
                        .add(new Text(recu.getReste() != null
                                ? format(recu.getReste().doubleValue()) + " FCFA" : "-").setFontSize(9))));

        infoTable.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(4).add(montantsRow));
        document.add(infoTable);

        // ── ZONES DE SIGNATURE ──
        Table sigTable = new Table(UnitValue.createPercentArray(new float[]{28, 28, 44}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(4);

        sigTable.addCell(signatureCell("ACCORD DE LA DIRECTION"));
        sigTable.addCell(signatureCell("SIGNATURE CAISSE"));
        sigTable.addCell(signatureCell("SIGNATURE BÉNÉFICIAIRE"));

        document.add(sigTable);

        // Utilisateur créateur
        if (recu.getUtilisateur() != null) {
            String userEmail = recu.getUtilisateur().getEmail();
            String userName = utilisateurRepository.findByEmail(userEmail)
                    .map(u -> u.getNom() + " " + u.getPrenom())
                    .orElse(userEmail);

            document.add(new Paragraph("Établi par : " + userName)
                    .setFontSize(7)
                    .setFontColor(new DeviceGray(0.5f))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginTop(2));
        }
    }

    // =========================================================
    //  HELPERS
    // =========================================================

    private Paragraph labelValueRow(String label, String value) {
        return new Paragraph()
                .add(new Text(label + "  ").setBold().setFontSize(8))
                .add(new Text(value != null ? value : "").setFontSize(8))
                .setBackgroundColor(new DeviceGray(0.82f))
                .setPadding(3)
                .setMarginBottom(0);
    }

    private Cell signatureCell(String label) {
        Table inner = new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(100));
        inner.addCell(new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph(label).setBold().setFontSize(7)
                        .setTextAlignment(TextAlignment.CENTER)));
        inner.addCell(new Cell().setHeight(28)
                .setBackgroundColor(new DeviceGray(0.82f))
                .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f)));
        return new Cell().setBorder(Border.NO_BORDER).setPadding(3).add(inner);
    }

    private String format(double d) {
        return String.format("%,.0f", d);
    }

    // ── CONVERSION EN LETTRES ──
    private static final String[] UNITES  = {"", "un", "deux", "trois", "quatre", "cinq", "six",
            "sept", "huit", "neuf", "dix", "onze", "douze", "treize", "quatorze", "quinze",
            "seize", "dix-sept", "dix-huit", "dix-neuf"};
    private static final String[] DIZAINES = {"", "", "vingt", "trente", "quarante",
            "cinquante", "soixante", "soixante", "quatre-vingt", "quatre-vingt"};

    private String convertirEnLettres(java.math.BigDecimal montant) {
        if (montant == null) return "";
        long valeur = montant.longValue();
        if (valeur == 0) return "zéro franc CFA";
        return centainesEnLettres(valeur) + " franc" + (valeur > 1 ? "s" : "") + " CFA";
    }

    private String centainesEnLettres(long n) {
        if (n < 0)  return "moins " + centainesEnLettres(-n);
        if (n == 0) return "";
        if (n < 20) return UNITES[(int) n];
        if (n < 100) {
            int d = (int)(n / 10), u = (int)(n % 10);
            if (d == 7 || d == 9) return DIZAINES[d] + "-" + UNITES[(int)(10 + n % 10)];
            String lien = (d == 8 && u == 0) ? "s"
                    : (u == 1 && d != 8) ? "-et-un"
                    : (u > 0) ? "-" + UNITES[u] : "";
            return DIZAINES[d] + lien;
        }
        if (n < 1_000) {
            long c = n / 100, r = n % 100;
            String cent = (c == 1 ? "" : centainesEnLettres(c) + "-") + "cent";
            return cent + (r == 0 ? (c > 1 ? "s" : "") : "-" + centainesEnLettres(r));
        }
        if (n < 1_000_000) {
            long m = n / 1_000, r = n % 1_000;
            String mille = (m == 1 ? "" : centainesEnLettres(m) + "-") + "mille";
            return mille + (r > 0 ? "-" + centainesEnLettres(r) : "");
        }
        if (n < 1_000_000_000) {
            long m = n / 1_000_000, r = n % 1_000_000;
            return centainesEnLettres(m) + "-million" + (m > 1 ? "s" : "")
                    + (r > 0 ? "-" + centainesEnLettres(r) : "");
        }
        long m = n / 1_000_000_000, r = n % 1_000_000_000;
        return centainesEnLettres(m) + "-milliard" + (m > 1 ? "s" : "")
                + (r > 0 ? "-" + centainesEnLettres(r) : "");
    }

}