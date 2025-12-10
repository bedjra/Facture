//package com.pro.Facture.service.Pdf;
//
//import com.itextpdf.text.*;
//import com.itextpdf.text.pdf.*;
//import com.itextpdf.text.pdf.draw.LineSeparator;
//import com.pro.Facture.Dto.CommandeRequestDto;
//import com.pro.Facture.Entity.Place;
//import org.springframework.stereotype.Service;
//
//import java.io.ByteArrayOutputStream;
//
//@Service
//public class CommandePdfService {
//
//    public byte[] genererPdf(CommandeRequestDto dto, Place place) {
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        Rectangle size = new Rectangle(250, 600);
//        Document doc = new Document(size, 10, 10, 10, 10);
//
//        try {
//            PdfWriter.getInstance(doc, out);
//            doc.open();
//
//            // FONTS
//            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
//            Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
//            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 8);
//            Font small = FontFactory.getFont(FontFactory.HELVETICA, 7);
//
//            // HEADER – LOGO + PLACE
//            PdfPTable head = new PdfPTable(2);
//            head.setWidthPercentage(100);
//            head.setWidths(new float[]{1f, 2.3f});
//            head.getDefaultCell().setBorder(Rectangle.NO_BORDER);
//
//            // LOGO
//            PdfPCell logo = new PdfPCell();
//            logo.setBorder(Rectangle.NO_BORDER);
//            logo.setHorizontalAlignment(Element.ALIGN_CENTER);
//            if (place.getLogo() != null) {
//                try {
//                    Image img = Image.getInstance(place.getLogo());
//                    img.scaleToFit(60, 60);
//                    logo.addElement(img);
//                } catch (Exception e) {
//                    logo.addElement(new Paragraph("LOGO", small));
//                }
//            }
//            head.addCell(logo);
//
//            // INFOS PLACE
//            PdfPCell info = new PdfPCell();
//            info.setBorder(Rectangle.NO_BORDER);
//            info.addElement(new Paragraph(place.getNom(), bold));
//            if (place.getAdresse() != null) info.addElement(new Paragraph(place.getAdresse(), small));
//            if (place.getEmail() != null) info.addElement(new Paragraph("✉ " + place.getEmail(), small));
//            String tel = "☎ " + place.getTelephone();
//            if (place.getCel() != null) tel += " | " + place.getCel();
//            info.addElement(new Paragraph(tel, small));
//            head.addCell(info);
//
//            doc.add(head);
//            addSeparator(doc);
//
//            // TITRE
//            Paragraph titre = new Paragraph("FACTURE / COMMANDE", title);
//            titre.setAlignment(Element.ALIGN_CENTER);
//            titre.setSpacingAfter(10);
//            doc.add(titre);
//
//            // INFORMATIONS COMMANDE
//            PdfPTable detail = new PdfPTable(2);
//            detail.setWidthPercentage(100);
//
//            add(detail, "Réf:", dto.getRef(), bold, normal);
//            add(detail, "Désignation:", dto.getDesign(), bold, normal);
//            add(detail, "HT:", format(dto.getHt()), bold, normal);
//            add(detail, "Retenue:", format(dto.getRetenue()), bold, normal);
//            add(detail, "TVA:", format(dto.getTva()) + " %", bold, normal);
//            add(detail, "Montant TTC:", format(dto.getMtTtc()), bold, bold);
//            add(detail, "Avance:", format(dto.getAvance()), bold, normal);
//            add(detail, "Net à payer:", format(dto.getNet()), bold, bold);
//
//            doc.add(detail);
//            addSeparator(doc);
//
//            // MESSAGE FINAL
//            Paragraph msg = new Paragraph("Merci pour votre confiance !", small);
//            msg.setAlignment(Element.ALIGN_CENTER);
//            msg.setSpacingBefore(8);
//            doc.add(msg);
//
//            doc.close();
//
//        } catch (Exception e) {
//            throw new RuntimeException("Erreur lors de la génération du PDF : " + e.getMessage());
//        }
//
//        return out.toByteArray();
//    }
//
//    // UTILITAIRES
//    private void addSeparator(Document doc) throws DocumentException {
//        LineSeparator line = new LineSeparator();
//        line.setLineColor(BaseColor.LIGHT_GRAY);
//        line.setLineWidth(0.6f);
//        doc.add(new Chunk(line));
//        doc.add(Chunk.NEWLINE);
//    }
//
//    private void add(PdfPTable tab, String l, String v, Font f1, Font f2) {
//        PdfPCell c1 = new PdfPCell(new Phrase(l, f1));
//        c1.setBorder(Rectangle.NO_BORDER);
//        PdfPCell c2 = new PdfPCell(new Phrase(v, f2));
//        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
//        c2.setBorder(Rectangle.NO_BORDER);
//        tab.addCell(c1);
//        tab.addCell(c2);
//    }
//
//    private String format(double d) {
//        return String.format("%.0f F", d);
//    }
//}
