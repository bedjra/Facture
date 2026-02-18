package com.pro.Facture.service.Pdf;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceGray;
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
import com.pro.Facture.Entity.Place;
import com.pro.Facture.Entity.Recu;
import com.pro.Facture.repository.PlaceRepository;
import com.pro.Facture.repository.RecuRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        Recu recu = recuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reçu non trouvé"));

        Place place = placeRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("Cabinet non configuré"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            // 1. Gestion du dossier de stockage
            String folderName = "RecuPdf";
            Path folderPath = Paths.get(folderName);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            // 2. Nom du fichier basé sur le numéro de pièce ou l'ID
            String fileName = "Recu_" + (recu.getNumeroPiece() != null ? recu.getNumeroPiece().replace("/", "-") : id) + ".pdf";
            File destinationFile = new File(folderName, fileName);

            // 3. Initialisation du PDF
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(25, 35, 25, 35);

            // Construction du contenu
            buildRecuBlock(document, place, recu);

            document.close();

            // 4. Conversion en tableau d'octets
            byte[] pdfBytes = out.toByteArray();

            // 5. Sauvegarde physique dans le dossier RecuPdf
            try (FileOutputStream fos = new FileOutputStream(destinationFile)) {
                fos.write(pdfBytes);
            }

            return pdfBytes;

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération ou stockage PDF : " + e.getMessage(), e);
        }
    }

    // =========================================================
    //  BLOC DU REÇU
    // =========================================================
    private void buildRecuBlock(Document document, Place place, Recu recu) throws Exception {

        // Titre principal
        Text titrePrincipal = new Text("CFACI GROUP CONSULTING\n")
                .setBold()
                .setFontSize(26)
                .setFontColor(ColorConstants.DARK_GRAY);

        Text description = new Text("Cabinet d'expertise comptable et d'audit")
                .setFontSize(12)
                .setFontColor(ColorConstants.BLACK);

        Paragraph titre = new Paragraph()
                .add(titrePrincipal)
                .add(description)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5)
                .setBorderBottom(new SolidBorder(ColorConstants.BLACK, 1f))
                .setPaddingBottom(3);

        document.add(titre);

        // ── HEADER : [logo] | [DATE / PIÈCE / MONTANT] ──
        Table header = new Table(UnitValue.createPercentArray(new float[]{55, 45}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(8);

        Cell leftCell = new Cell().setBorder(Border.NO_BORDER).setPadding(0);
        if (place.getLogo() != null && place.getLogo().length > 0) {
            try {
                Image logo = new Image(ImageDataFactory.create(place.getLogo()))
                        .setWidth(100)
                        .setHeight(100);
                leftCell.add(logo);
            } catch (Exception ignored) {}
        }
        header.addCell(leftCell);

        Cell rightCell = new Cell().setBorder(Border.NO_BORDER).setPadding(0)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        rightCell.add(labelValueRow("DATE :", recu.getDate() != null ? recu.getDate().toString() : ""));
        rightCell.add(labelValueRow("PIÈCE DE CAISSE N° :", recu.getNumeroPiece() != null ? recu.getNumeroPiece() : ""));
        rightCell.add(labelValueRow("MONTANT :", recu.getMontantEncaisse() != null ? recu.getMontantEncaisse() + " FCFA" : ""));
        header.addCell(rightCell);

        document.add(header);

        // ── INFOS CABINET ──
        float s = 8.5f;
        document.add(new Paragraph()
                .add(new Text("ACTIVITE : ").setBold().setUnderline().setFontSize(s))
                .add(new Text("Audit, Assistance comptable, fiscale et sociale").setFontSize(s)));
        document.add(new Paragraph()
                .add(new Text("Travaux d'inventaire, Gestion des salaires-Conseils et Formation").setFontSize(s))
                .setMarginTop(-3));

        document.add(new Paragraph()
                .add(new Text("SIEGE : ").setBold().setUnderline().setFontSize(s))
                .add(new Text(place.getAdresse() != null ? place.getAdresse() : "").setFontSize(s)));

        String tels = (place.getTelephone() != null ? place.getTelephone() : "") +
                (place.getCel() != null && !place.getCel().isEmpty() ? " / " + place.getCel() : "");
        document.add(new Paragraph()
                .add(new Text("Tél : ").setBold().setUnderline().setFontSize(s))
                .add(new Text(tels).setFontSize(s)));

        document.add(new Paragraph()
                .add(new Text("E-mail :").setBold().setUnderline().setFontSize(s))
                .add(new Text(place.getEmail() != null ? place.getEmail() : "  ").setFontSize(s)));

        document.add(new Paragraph()
                .add(new Text("NIF :").setBold().setUnderline().setFontSize(s))
                .add(new Text("  1 001 727 149").setFontSize(s))
                .setMarginBottom(10));

        // ── TABLEAU BÉNÉFICIAIRE / SOMME / MOTIF ──
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(new SolidBorder(ColorConstants.BLACK, 1))
                .setMarginBottom(12);

        infoTable.addCell(new Cell().setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(ColorConstants.BLACK, 0.5f)).setPadding(7)
                .add(new Paragraph().add(new Text("Bénéficiaire : ").setBold().setFontSize(9))
                        .add(new Text(recu.getBeneficiaire() != null ? recu.getBeneficiaire() : "").setFontSize(9))));

        String montantLettre = convertirEnLettres(recu.getMontantEncaisse());
        infoTable.addCell(new Cell().setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(ColorConstants.BLACK, 0.5f)).setPadding(7)
                .add(new Paragraph().add(new Text("La somme de ( en lettre ) : ").setBold().setFontSize(9))
                        .add(new Text(montantLettre).setFontSize(9))));

        infoTable.addCell(new Cell().setBorder(Border.NO_BORDER).setPadding(7).setMinHeight(45)
                .add(new Paragraph().add(new Text("Motif : ").setBold().setFontSize(9))
                        .add(new Text(recu.getMotif() != null ? recu.getMotif() : "").setFontSize(9))));

        document.add(infoTable);

        // ── ZONES DE SIGNATURE ──
        Table sigTable = new Table(UnitValue.createPercentArray(new float[]{28, 28, 44}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(4);

        sigTable.addCell(signatureCell("ACCORD DE LA DIRECTION"));
        sigTable.addCell(signatureCell("SIGNATURE CAISSE"));
        sigTable.addCell(signatureCell("SIGNATURE BENEFICIAIRE"));

        document.add(sigTable);
    }

    private Paragraph labelValueRow(String label, String value) {
        return new Paragraph()
                .add(new Text(label + "  ").setBold().setFontSize(8))
                .add(new Text(value != null ? value : "").setFontSize(8))
                .setBackgroundColor(new DeviceGray(0.82f))
                .setPadding(3)
                .setMarginBottom(3);
    }

    private Cell signatureCell(String label) {
        Table inner = new Table(UnitValue.createPercentArray(new float[]{1})).setWidth(UnitValue.createPercentValue(100));
        inner.addCell(new Cell().setBorder(Border.NO_BORDER).add(new Paragraph(label).setBold().setFontSize(7).setTextAlignment(TextAlignment.CENTER)));
        inner.addCell(new Cell().setHeight(32).setBackgroundColor(new DeviceGray(0.82f)).setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f)));
        return new Cell().setBorder(Border.NO_BORDER).setPadding(3).add(inner);
    }

    // ── LOGIQUE CONVERSION LETTRES ──
    private static final String[] UNITES = {"", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf", "dix", "onze", "douze", "treize", "quatorze", "quinze", "seize", "dix-sept", "dix-huit", "dix-neuf"};
    private static final String[] DIZAINES = {"", "", "vingt", "trente", "quarante", "cinquante", "soixante", "soixante", "quatre-vingt", "quatre-vingt"};

    private String convertirEnLettres(java.math.BigDecimal montant) {
        if (montant == null) return "";
        long valeur = montant.longValue();
        if (valeur == 0) return "zéro franc CFA";
        return centainesEnLettres(valeur) + " franc" + (valeur > 1 ? "s" : "") + " CFA";
    }

    private String centainesEnLettres(long n) {
        if (n < 0) return "moins " + centainesEnLettres(-n);
        if (n == 0) return "";
        if (n < 20) return UNITES[(int) n];
        if (n < 100) {
            int d = (int) (n / 10), u = (int) (n % 10);
            if (d == 7 || d == 9) return DIZAINES[d] + "-" + UNITES[(int) (10 + n % 10)];
            String lien = (d == 8 && u == 0) ? "s" : (u == 1 && d != 8) ? "-et-un" : (u > 0) ? "-" + UNITES[u] : "";
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
            return centainesEnLettres(m) + "-million" + (m > 1 ? "s" : "") + (r > 0 ? "-" + centainesEnLettres(r) : "");
        }
        long m = n / 1_000_000_000, r = n % 1_000_000_000;
        return centainesEnLettres(m) + "-milliard" + (m > 1 ? "s" : "") + (r > 0 ? "-" + centainesEnLettres(r) : "");
    }
}