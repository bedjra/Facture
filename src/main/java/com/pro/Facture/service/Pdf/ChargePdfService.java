package com.pro.Facture.service.Pdf;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.pro.Facture.Entity.Charge;
import com.pro.Facture.Entity.Place;
import com.pro.Facture.repository.ChargeRepository;
import com.pro.Facture.repository.PlaceRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class ChargePdfService {

    private final ChargeRepository chargeRepository;
    private final PlaceRepository placeRepository;

    private static final Color BIC_BLEU = new DeviceRgb(20, 45, 150);

    public ChargePdfService(ChargeRepository chargeRepository, PlaceRepository placeRepository) {
        this.chargeRepository = chargeRepository;
        this.placeRepository = placeRepository;
    }

    // =========================================================
    //  GÉNÉRATION DU BORDEREAU (TOUTES LES CHARGES)
    // =========================================================
    public byte[] generateBordereauPdf() {
        List<Charge> charges = chargeRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Charge::getDateCharge,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        return buildBordereau(charges);
    }

    public byte[] generateBordereauPdf(LocalDate debut, LocalDate fin) {
        List<Charge> charges = chargeRepository.findAll()
                .stream()
                .filter(c -> c.getDateCharge() != null
                        && !c.getDateCharge().isBefore(debut)
                        && !c.getDateCharge().isAfter(fin))
                .sorted(Comparator.comparing(Charge::getDateCharge))
                .toList();

        return buildBordereau(charges);
    }

    private byte[] buildBordereau(List<Charge> charges) {
        Place place = placeRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("Cabinet non configuré"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            String folderName = "BordereauPdf";
            Path folderPath = Paths.get(folderName);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            String fileName = "Bordereau_Depenses_"
                    + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    + ".pdf";
            File destinationFile = new File(folderName, fileName);

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(10, 35, 20, 35);

            buildEnTete(document, place);
            buildTableauCharges(document, charges);
            buildSignatures(document);

            document.close();

            byte[] pdfBytes = out.toByteArray();
            try (FileOutputStream fos = new FileOutputStream(destinationFile)) {
                fos.write(pdfBytes);
            }

            return pdfBytes;

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération ou stockage du bordereau : " + e.getMessage(), e);
        }
    }

    // =========================================================
    //  GÉNÉRATION DU PDF POUR UNE SEULE CHARGE (FICHE)
    // =========================================================
    public byte[] generateChargePdf(Long id) {
        Charge charge = chargeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charge non trouvée"));

        Place place = placeRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("Cabinet non configuré"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            String folderName = "ChargePdf";
            Path folderPath = Paths.get(folderName);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            String fileName = "Charge_" + charge.getId() + ".pdf";
            File destinationFile = new File(folderName, fileName);

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(10, 35, 20, 35);

            buildEnTete(document, place);
            buildDetailCharge(document, charge);
            buildSignatures(document);

            document.close();

            byte[] pdfBytes = out.toByteArray();
            try (FileOutputStream fos = new FileOutputStream(destinationFile)) {
                fos.write(pdfBytes);
            }

            return pdfBytes;

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF de la charge : " + e.getMessage(), e);
        }
    }

    // =========================================================
    //  EN-TÊTE STYLE "REÇU"
    // =========================================================
    private void buildEnTete(Document document, Place place) {

        Table headerTitre = new Table(UnitValue.createPercentArray(new float[]{22, 78}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(3);

        Cell logoCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(ColorConstants.BLACK, 1f))
                .setPadding(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        if (place.getLogo() != null && place.getLogo().length > 0) {
            try {
                Image logo = new Image(ImageDataFactory.create(place.getLogo()));
                logo.setAutoScale(true);
                logo.setMaxWidth(100);
                logo.setMaxHeight(100);
                logoCell.add(logo);
            } catch (Exception ignored) {}
        }

        headerTitre.addCell(logoCell);

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

        float s = 8f;
        float lineSpacing = 1.5f;

        String telephone = place.getTelephone() != null ? place.getTelephone() : "";
        String cel       = place.getCel() != null && !place.getCel().isEmpty() ? place.getCel() : "";
        String tels      = !telephone.isEmpty() && !cel.isEmpty()
                ? telephone + " / " + cel : telephone + cel;

        Table infoHeader = new Table(UnitValue.createPercentArray(new float[]{55, 45}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(8);

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

        Cell dateCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.TOP);

        int anneeActuelle = LocalDate.now().getYear();

        dateCell.add(new Paragraph()
                .add(new Text("DATE D'ÉDITION : ").setBold().setFontSize(8))
                .add(new Text(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).setFontSize(8))
                .setBackgroundColor(new DeviceGray(0.85f))
                .setTextAlignment(TextAlignment.LEFT)
                .setPadding(3)
                .setMarginBottom(2));

        dateCell.add(new Paragraph()
                .add(new Text("N° PIÈCE : ").setBold().setFontSize(8))
                .add(new Text("......../CFACI/" + anneeActuelle).setFontSize(8))
                .setBackgroundColor(new DeviceGray(0.85f))
                .setTextAlignment(TextAlignment.LEFT)
                .setPadding(3)
                .setMarginBottom(0));

        infoHeader.addCell(dateCell);
        document.add(infoHeader);




        document.add(new Paragraph("BORDEREAU DE DÉPENSES")
                .setBold()
                .setFontSize(13)
                .setFontColor(ColorConstants.BLACK)
                .setTextAlignment(TextAlignment.CENTER)
                .setUnderline()
                .setMarginTop(4)
                .setMarginBottom(10));
    }

    // =========================================================
    //  TABLEAU DES CHARGES (BORDEREAU) — avec Bénéficiaire + Motif
    // =========================================================
    private void buildTableauCharges(Document document, List<Charge> charges) {

        Table table = new Table(UnitValue.createPercentArray(new float[]{6, 20, 22, 22, 12, 18}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(10);

        table.addHeaderCell(enteteCell("N°"));
        table.addHeaderCell(enteteCell("BÉNÉFICIAIRE"));
//        table.addHeaderCell(enteteCell("DESCRIPTION"));
        table.addHeaderCell(enteteCell("MOTIF"));
        table.addHeaderCell(enteteCell("DATE"));
        table.addHeaderCell(enteteCell("MONTANT (FCFA)"));

        double total = 0;
        int n = 1;

        for (Charge charge : charges) {
            double montant = charge.getMontant() != null ? charge.getMontant().doubleValue() : 0;
            total += montant;

            table.addCell(ligneCell(String.valueOf(n++), TextAlignment.CENTER, false));
            table.addCell(ligneCell(charge.getBeneficiaire() != null ? charge.getBeneficiaire() : "-",
                    TextAlignment.LEFT, false));
//            table.addCell(ligneCell(charge.getDescription() != null ? charge.getDescription() : "-",
//                    TextAlignment.LEFT, false));
            table.addCell(ligneCell(charge.getMotif() != null ? charge.getMotif() : "-",
                    TextAlignment.LEFT, false));
            table.addCell(ligneCell(charge.getDateCharge() != null
                            ? charge.getDateCharge().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-",
                    TextAlignment.CENTER, false));
            table.addCell(ligneCell(format(montant), TextAlignment.RIGHT, true));
        }

        if (charges.isEmpty()) {
            Cell empty = new Cell(1, 6)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f))
                    .setPadding(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .add(new Paragraph("Aucune charge enregistrée").setFontSize(9).setItalic());
            table.addCell(empty);
        }

        Cell totalLabel = new Cell(1, 5)
                .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f))
                .setPadding(6)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph("TOTAL DES DÉPENSES").setBold().setFontSize(9));

        Cell totalValue = new Cell()
                .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f))
                .setBackgroundColor(new DeviceGray(0.85f))
                .setPadding(6)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(format(total) + " FCFA").setBold().setFontSize(9).setFontColor(BIC_BLEU));

        table.addCell(totalLabel);
        table.addCell(totalValue);

        document.add(table);

        String enLettres = NumberToWordsFr.convert((long) total);
        document.add(new Paragraph()
                .add(new Text("Arrêté le présent bordereau à la somme de : ").setBold().setFontSize(9))
                .add(new Text(enLettres + " francs CFA").setFontSize(9).setFontColor(BIC_BLEU).setItalic())
                .setMarginTop(4)
                .setMarginBottom(10));
    }

    // =========================================================
    //  DÉTAIL D'UNE CHARGE (FICHE) — comme sur le reçu papier
    // =========================================================
    private void buildDetailCharge(Document document, Charge charge) {

        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(new SolidBorder(ColorConstants.BLACK, 1))
                .setMarginBottom(10);

        infoTable.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(ColorConstants.BLACK, 0.5f))
                .setPadding(6)
                .add(new Paragraph()
                        .add(new Text("Bénéficiaire : ").setBold().setFontSize(10))
                        .add(new Text(charge.getBeneficiaire() != null ? charge.getBeneficiaire() : "-").setFontSize(10))));

        double montant = charge.getMontant() != null ? charge.getMontant().doubleValue() : 0;
        String enLettres = NumberToWordsFr.convert((long) montant);

        infoTable.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(ColorConstants.BLACK, 0.5f))
                .setPadding(6)
                .add(new Paragraph()
                        .add(new Text("La somme de (en lettre) : ").setBold().setFontSize(10))
                        .add(new Text(enLettres + " francs CFA").setFontSize(10))));

        infoTable.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(ColorConstants.BLACK, 0.5f))
                .setPadding(6)
                .add(new Paragraph()
                        .add(new Text("Motif : ").setBold().setFontSize(10))
                        .add(new Text(charge.getMotif() != null ? charge.getMotif() : "-").setFontSize(10))));

        infoTable.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(6)
                .setBackgroundColor(new DeviceGray(0.85f))
                .add(new Paragraph()
                        .add(new Text("Montant : ").setBold().setFontSize(11))
                        .add(new Text(format(montant) + " FCFA").setBold().setFontSize(11).setFontColor(BIC_BLEU))));

        document.add(infoTable);
    }

    // =========================================================
    //  ZONES DE SIGNATURE
    // =========================================================
    private void buildSignatures(Document document) {
        Table sigTable = new Table(UnitValue.createPercentArray(new float[]{34, 33, 33}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(10);

        sigTable.addCell(signatureCell("ACCORD DE LA DIRECTION"));
        sigTable.addCell(signatureCell("SIGNATURE CAISSE"));
        sigTable.addCell(signatureCell("SIGNATURE BÉNÉFICIAIRE"));

        document.add(sigTable);
    }

    // =========================================================
    //  HELPERS
    // =========================================================

    private Paragraph labelValueRow(String label, String value) {
        return new Paragraph()
                .add(new Text(label + "  ").setBold().setFontSize(8))
                .add(new Text(value != null ? value : "").setFontSize(8))
                .setBackgroundColor(new DeviceGray(0.85f))
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(3)
                .setMarginBottom(0);
    }

    private Cell enteteCell(String label) {
        return new Cell()
                .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f))
                .setBackgroundColor(new DeviceGray(0.75f))
                .setPadding(5)
                .setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(label).setBold().setFontSize(8));
    }

    private Cell ligneCell(String value, TextAlignment align, boolean bleu) {
        return new Cell()
                .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f))
                .setPadding(5)
                .setTextAlignment(align)
                .add(new Paragraph(value).setFontSize(9)
                        .setFontColor(bleu ? BIC_BLEU : ColorConstants.BLACK)
                        .setBold());
    }

    private Cell signatureCell(String label) {
        Table inner = new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(100));
        inner.addCell(new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph(label).setBold().setFontSize(8)
                        .setTextAlignment(TextAlignment.CENTER)));
        inner.addCell(new Cell().setHeight(32)
                .setBorder(new SolidBorder(ColorConstants.BLACK, 0.7f)));
        return new Cell().setBorder(Border.NO_BORDER).setPadding(3).add(inner);
    }

    private String format(double d) {
        return String.format("%,.0f", d);
    }

    // =========================================================
    //  CONVERSION MONTANT → LETTRES (FR)
    //  ⚠️ static obligatoire car elle contient des membres static
    // =========================================================
    public static class NumberToWordsFr {

        private static final String[] UNITES = {
                "", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf",
                "dix", "onze", "douze", "treize", "quatorze", "quinze", "seize",
                "dix-sept", "dix-huit", "dix-neuf"
        };

        private static final String[] DIZAINES = {
                "", "", "vingt", "trente", "quarante", "cinquante",
                "soixante", "soixante", "quatre-vingt", "quatre-vingt"
        };

        public static String convert(long nombre) {
            if (nombre == 0) return "zéro";
            if (nombre < 0) return "moins " + convert(-nombre);
            return convertGroup(nombre).trim();
        }

        private static String convertGroup(long n) {
            if (n < 20) {
                return UNITES[(int) n];
            }
            if (n < 100) {
                int d = (int) (n / 10);
                int u = (int) (n % 10);
                String base = DIZAINES[d];
                if (d == 7 || d == 9) {
                    return base + "-" + UNITES[10 + u];
                }
                if (u == 0) return base;
                if (u == 1 && d != 8) return base + "-et-un";
                return base + "-" + UNITES[u];
            }
            if (n < 1000) {
                long c = n / 100;
                long r = n % 100;
                String cent = (c == 1) ? "cent" : convertGroup(c) + " cent";
                if (r == 0) return c > 1 ? cent + "s" : cent;
                return cent + " " + convertGroup(r);
            }
            if (n < 1_000_000) {
                long m = n / 1000;
                long r = n % 1000;
                String mille = (m == 1) ? "mille" : convertGroup(m) + " mille";
                if (r == 0) return mille;
                return mille + " " + convertGroup(r);
            }
            if (n < 1_000_000_000) {
                long m = n / 1_000_000;
                long r = n % 1_000_000;
                String millions = convertGroup(m) + (m > 1 ? " millions" : " million");
                if (r == 0) return millions;
                return millions + " " + convertGroup(r);
            }
            long m = n / 1_000_000_000;
            long r = n % 1_000_000_000;
            String milliards = convertGroup(m) + (m > 1 ? " milliards" : " milliard");
            if (r == 0) return milliards;
            return milliards + " " + convertGroup(r);
        }
    }
}