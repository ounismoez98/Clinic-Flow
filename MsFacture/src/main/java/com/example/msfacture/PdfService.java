package com.example.msfacture;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    public byte[] generateFacturePdf(Facture facture, DTOPatient dtoPatient) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 🔹 Title
            document.add(new Paragraph("FACTURE")
                    .setBold()
                    .setFontSize(18));

            // 🔹 Info
            document.add(new Paragraph("Facture N°: " + facture.getId()));
            document.add(new Paragraph("Date: " + facture.getDateFacture()));
            if (dtoPatient != null) {
                document.add(new Paragraph("Patient: " + dtoPatient.getNom() + " " + dtoPatient.getPrenom()));
            } else {
                document.add(new Paragraph("Patient ID: " + facture.getPatientId()));
            }
            document.add(new Paragraph("Statut: " + facture.getStatut()));

            document.add(new Paragraph("\n"));

            // 🔹 Table (lignes)
            Table table = new Table(3);
            table.useAllAvailableWidth();

            table.addHeaderCell("Description");
            table.addHeaderCell("Quantité");
            table.addHeaderCell("Prix");

            System.out.println("Generating PDF for Facture #" + facture.getId() + " with " + facture.getLignes().size() + " lines.");

            for (LigneFacture ligne : facture.getLignes()) {
                System.out.println("Adding line to PDF: " + ligne.getDescription());
                table.addCell(ligne.getDescription() != null ? ligne.getDescription() : "");
                table.addCell(String.valueOf(ligne.getQuantite()));
                table.addCell(String.valueOf(ligne.getPrix()));
            }

            document.add(table);

            document.add(new Paragraph("\n"));

            // 🔹 Totals
            document.add(new Paragraph("Montant HT: " + facture.getMontantHT()));
            document.add(new Paragraph("TVA: " + facture.getTva()));
            document.add(new Paragraph("Montant TTC: " + facture.getMontantTTC()));

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }

        return out.toByteArray();
    }
}
