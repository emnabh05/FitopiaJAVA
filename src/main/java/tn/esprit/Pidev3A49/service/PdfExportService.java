package tn.esprit.Pidev3A49.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import tn.esprit.Pidev3A49.models.Event;
import tn.esprit.Pidev3A49.models.Participation;

import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class PdfExportService {

    private static final float MARGIN = 40f;
    private static final float ROW_HEIGHT = 18f;
    private static final float FONT_SIZE = 9f;
    private static final float TITLE_SIZE = 16f;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void exportEvents(List<Event> events, Path outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(842, 595)); // paysage simple
            document.addPage(page);

            float[] colWidths = {35, 95, 70, 70, 90, 45, 50, 55, 50, 55, 55, 70};
            String[] headers = {
                    "ID", "Title", "Type", "Date", "Location", "Actives",
                    "Capacity", "Restantes", "Premium", "Etat", "Price", "Created"
            };

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float y = page.getMediaBox().getHeight() - MARGIN;

                y = drawTitle(content, "Events Dashboard Export", y);
                y = drawTableHeader(content, headers, colWidths, y);

                for (Event event : events) {
                    if (y < MARGIN + ROW_HEIGHT) {
                        content.close();
                        page = new PDPage(new PDRectangle(842, 595));
                        document.addPage(page);

                        try (PDPageContentStream newContent = new PDPageContentStream(document, page)) {
                            y = page.getMediaBox().getHeight() - MARGIN;
                            y = drawTableHeader(newContent, headers, colWidths, y);

                            String[] row = buildEventRow(event);
                            y = drawRow(newContent, row, colWidths, y);
                        }
                    } else {
                        String[] row = buildEventRow(event);
                        y = drawRow(content, row, colWidths, y);
                    }
                }
            }

            document.save(outputPath.toFile());
        }
    }

    public void exportParticipations(List<Participation> participations, Path outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float[] colWidths = {70, 150, 180, 120, 70};
            String[] headers = {"ID", "Nom", "Email", "Date inscription", "Event ID"};

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float y = page.getMediaBox().getHeight() - MARGIN;

                y = drawTitle(content, "Participants Export", y);
                y = drawTableHeader(content, headers, colWidths, y);

                for (Participation participation : participations) {
                    if (y < MARGIN + ROW_HEIGHT) {
                        content.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);

                        try (PDPageContentStream newContent = new PDPageContentStream(document, page)) {
                            y = page.getMediaBox().getHeight() - MARGIN;
                            y = drawTableHeader(newContent, headers, colWidths, y);

                            String[] row = buildParticipationRow(participation);
                            y = drawRow(newContent, row, colWidths, y);
                        }
                    } else {
                        String[] row = buildParticipationRow(participation);
                        y = drawRow(content, row, colWidths, y);
                    }
                }
            }

            document.save(outputPath.toFile());
        }
    }

    private String[] buildEventRow(Event event) {
        return new String[]{
                String.valueOf(event.getIdEvent()),
                safe(limit(event.getTitre(), 22)),
                safe(limit(event.getTypeEvent(), 14)),
                event.getDateEvent() == null ? "-" : event.getDateEvent().format(DATE_FORMAT),
                safe(limit(event.getLieu(), 18)),
                "-",
                String.valueOf(event.getCapacite()),
                "-",
                event.isPremium() ? "Oui" : "Non",
                "-",
                String.format(Locale.US, "%.2f", event.getPrixEvent()),
                event.getCreatedAt() == null ? "-" : event.getCreatedAt().format(DATE_TIME_FORMAT)
        };
    }

    private String[] buildParticipationRow(Participation participation) {
        return new String[]{
                String.valueOf(participation.getIdParticipation()),
                safe(limit(participation.getNomParticipant(), 24)),
                safe(limit(participation.getEmailParticipant(), 32)),
                participation.getDateInscription() == null
                        ? "-"
                        : participation.getDateInscription().format(DATE_TIME_FORMAT),
                String.valueOf(participation.getIdEvent())
        };
    }

    private float drawTitle(PDPageContentStream content, String title, float y) throws IOException {
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, TITLE_SIZE);
        content.newLineAtOffset(MARGIN, y);
        content.showText(title);
        content.endText();
        return y - 28f;
    }

    private float drawTableHeader(PDPageContentStream content, String[] headers, float[] colWidths, float y) throws IOException {
        float x = MARGIN;

        for (int i = 0; i < headers.length; i++) {
            drawCellBorder(content, x, y, colWidths[i], ROW_HEIGHT);
            drawText(content, headers[i], x + 2, y - 12, true);
            x += colWidths[i];
        }

        return y - ROW_HEIGHT;
    }

    private float drawRow(PDPageContentStream content, String[] values, float[] colWidths, float y) throws IOException {
        float x = MARGIN;

        for (int i = 0; i < values.length; i++) {
            drawCellBorder(content, x, y, colWidths[i], ROW_HEIGHT);
            drawText(content, safe(values[i]), x + 2, y - 12, false);
            x += colWidths[i];
        }

        return y - ROW_HEIGHT;
    }

    private void drawCellBorder(PDPageContentStream content, float x, float y, float width, float height) throws IOException {
        content.addRect(x, y - height, width, height);
        content.stroke();
    }

    private void drawText(PDPageContentStream content, String text, float x, float y, boolean bold) throws IOException {
        content.beginText();
        content.setFont(bold ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, FONT_SIZE);
        content.newLineAtOffset(x, y);
        content.showText(text == null ? "" : text);
        content.endText();
    }

    private String safe(String value) {
        if (value == null) return "";
        return value
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ");
    }

    private String limit(String value, int max) {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }
}