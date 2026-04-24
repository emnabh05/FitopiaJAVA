package tn.esprit.Pidev3A49.utils;

import tn.esprit.Pidev3A49.Models.SupplementOrder;
import tn.esprit.Pidev3A49.Models.SupplementOrderItem;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class InvoiceExporter {

    private static final DateTimeFormatter ORDER_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter INVOICE_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private InvoiceExporter() {
    }

    public static Path exportInvoiceHtml(SupplementOrder order) throws IOException {
        if (order == null) {
            throw new IllegalArgumentException("Commande introuvable.");
        }

        LocalDateTime orderDate = order.getCreatedAt() == null ? LocalDateTime.now() : order.getCreatedAt();
        List<SupplementOrderItem> items = order.getItems() == null ? List.of() : order.getItems();
        String invoiceNumber = "FACT-" + INVOICE_NUMBER_FORMATTER.format(orderDate);

        String itemsRows = items.isEmpty()
                ? """
                <tr>
                    <td colspan="4" class="empty">Aucun article sur cette facture.</td>
                </tr>
                """
                : items.stream()
                .map(InvoiceExporter::toItemRowHtml)
                .reduce("", String::concat);

        String shippingValue = isZero(order.getShippingCost()) ? "GRATUIT" : formatPrice(order.getShippingCost());
        String discountValue = isZero(order.getDiscountAmount()) ? "0.00 DT" : formatPrice(order.getDiscountAmount());
        String notesValue = order.getNotes() == null || order.getNotes().isBlank()
                ? "-"
                : escapeHtml(order.getNotes());

        String html = """
                <!doctype html>
                <html lang="fr">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>Facture %s</title>
                    <style>
                        :root {
                            --ink: #153447;
                            --muted: #5b7280;
                            --line: #d5e3eb;
                            --accent: #0f6a58;
                            --panel: #f4f8fb;
                        }
                        * { box-sizing: border-box; }
                        body {
                            margin: 0;
                            padding: 32px;
                            background: linear-gradient(140deg, #eef5f8, #f7fbfd);
                            font-family: "Segoe UI", "Arial", sans-serif;
                            color: var(--ink);
                        }
                        .invoice {
                            max-width: 980px;
                            margin: 0 auto;
                            background: white;
                            border: 1px solid var(--line);
                            border-radius: 18px;
                            overflow: hidden;
                        }
                        .header {
                            display: flex;
                            justify-content: space-between;
                            align-items: start;
                            gap: 16px;
                            padding: 24px;
                            background: var(--panel);
                            border-bottom: 1px solid var(--line);
                        }
                        .title {
                            margin: 0 0 10px 0;
                            font-size: 46px;
                            font-weight: 900;
                            letter-spacing: 0.4px;
                            color: #0d3952;
                        }
                        .meta {
                            margin: 0;
                            color: var(--muted);
                            font-size: 15px;
                            line-height: 1.6;
                            font-weight: 600;
                        }
                        .print-btn {
                            border: none;
                            padding: 12px 18px;
                            border-radius: 12px;
                            background: linear-gradient(90deg, #0d5f4f, #138568);
                            color: white;
                            font-size: 15px;
                            font-weight: 800;
                            cursor: pointer;
                            white-space: nowrap;
                        }
                        .grid {
                            display: grid;
                            grid-template-columns: 1fr 1fr;
                            gap: 14px;
                            padding: 20px 24px;
                        }
                        .card {
                            border: 1px solid var(--line);
                            border-radius: 14px;
                            padding: 14px 16px;
                            background: #fcfeff;
                        }
                        .card h3 {
                            margin: 0 0 8px 0;
                            font-size: 18px;
                            letter-spacing: 0.6px;
                            color: #0e4e44;
                        }
                        .card p {
                            margin: 6px 0;
                            color: #315162;
                            font-size: 14px;
                            line-height: 1.4;
                        }
                        .items {
                            width: calc(100%% - 48px);
                            margin: 0 24px 18px 24px;
                            border-collapse: collapse;
                        }
                        .items thead th {
                            text-align: left;
                            font-size: 13px;
                            text-transform: uppercase;
                            letter-spacing: 1px;
                            color: #567183;
                            background: #f1f6fa;
                            padding: 12px;
                            border-top: 1px solid var(--line);
                            border-bottom: 1px solid var(--line);
                        }
                        .items td {
                            border-bottom: 1px solid #e5eef4;
                            padding: 14px 12px;
                            font-size: 14px;
                        }
                        .items td:nth-child(2),
                        .items td:nth-child(3),
                        .items td:nth-child(4) {
                            text-align: right;
                            font-weight: 700;
                            color: #29495b;
                            white-space: nowrap;
                        }
                        .empty {
                            text-align: center !important;
                            color: #6a8190 !important;
                            font-style: italic;
                        }
                        .totals {
                            width: 380px;
                            margin: 0 24px 18px auto;
                            border: 1px solid var(--line);
                            border-radius: 14px;
                            padding: 12px 14px;
                        }
                        .row {
                            display: flex;
                            justify-content: space-between;
                            align-items: center;
                            padding: 7px 0;
                            font-size: 15px;
                            color: #355468;
                            font-weight: 600;
                        }
                        .total {
                            margin-top: 8px;
                            padding-top: 10px;
                            border-top: 1px solid var(--line);
                            font-size: 32px;
                            color: #08344f;
                            font-weight: 900;
                        }
                        .notes {
                            margin: 0 24px 18px 24px;
                            border: 1px solid var(--line);
                            border-radius: 14px;
                            padding: 12px 14px;
                            font-size: 14px;
                            color: #3f5a6a;
                            background: #fbfdff;
                        }
                        .notes b {
                            color: #1d4457;
                        }
                        .stamp-zone {
                            display: flex;
                            justify-content: end;
                            align-items: center;
                            gap: 14px;
                            padding: 6px 24px 4px 24px;
                        }
                        .stamp {
                            width: 126px;
                            height: 126px;
                            border: 3px solid #0f6b48;
                            border-radius: 50%%;
                            padding: 8px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            text-align: center;
                            color: #0f6b48;
                            position: relative;
                        }
                        .stamp::before {
                            content: "";
                            position: absolute;
                            inset: 6px;
                            border: 2px solid #0f6b48;
                            border-radius: 50%%;
                        }
                        .stamp-content {
                            position: relative;
                            z-index: 1;
                            line-height: 1.2;
                        }
                        .stamp-top {
                            font-size: 7px;
                            font-weight: 800;
                            letter-spacing: 0.2px;
                        }
                        .stamp-brand {
                            margin-top: 4px;
                            font-size: 22px;
                            font-weight: 900;
                            letter-spacing: 0.3px;
                        }
                        .stamp-name {
                            margin-top: 4px;
                            font-size: 12px;
                            font-weight: 800;
                        }
                        .stamp-role {
                            margin-top: 2px;
                            font-size: 8px;
                            font-weight: 700;
                        }
                        .signature {
                            min-width: 150px;
                        }
                        .signature .label {
                            color: #1f3342;
                            font-size: 14px;
                            margin-bottom: 5px;
                        }
                        .signature .specimen {
                            font-family: "Brush Script MT", "Segoe Script", cursive;
                            font-size: 24px;
                            margin-bottom: 4px;
                            border-bottom: 1px solid #232f39;
                            width: fit-content;
                            padding-right: 10px;
                        }
                        .signature .name {
                            font-family: "Brush Script MT", "Segoe Script", cursive;
                            font-size: 28px;
                            color: #111;
                        }
                        .footer {
                            padding: 10px 24px 24px 24px;
                            color: #607889;
                            font-size: 14px;
                        }
                        @media print {
                            body { background: white; padding: 0; }
                            .invoice { border: none; border-radius: 0; }
                            .print-btn { display: none; }
                        }
                    </style>
                </head>
                <body>
                <article class="invoice">
                    <header class="header">
                        <div>
                            <h1 class="title">FACTURE CLIENT</h1>
                            <p class="meta">
                                Facture No: %s<br>
                                Date: %s
                            </p>
                        </div>
                        <button class="print-btn" onclick="window.print()">Imprimer facture</button>
                    </header>

                    <section class="grid">
                        <div class="card">
                            <h3>EMETTEUR</h3>
                            <p><b>Fitopia Supplements</b></p>
                            <p>El Ghazela, Ariana, Tunisie</p>
                            <p>customerservice@fitopia.com</p>
                            <p>+216 58 936 689</p>
                        </div>
                        <div class="card">
                            <h3>CLIENT</h3>
                            <p><b>%s</b></p>
                            <p>%s</p>
                            <p>%s</p>
                            <p>%s, %s %s</p>
                        </div>
                    </section>

                    <table class="items">
                        <thead>
                            <tr>
                                <th>Produit</th>
                                <th>Qte</th>
                                <th>Prix unitaire</th>
                                <th>Total</th>
                            </tr>
                        </thead>
                        <tbody>
                            %s
                        </tbody>
                    </table>

                    <section class="totals">
                        <div class="row"><span>Sous-total</span><span>%s</span></div>
                        <div class="row"><span>Livraison</span><span>%s</span></div>
                        <div class="row"><span>Remise</span><span>%s</span></div>
                        <div class="row total"><span>Total TTC</span><span>%s</span></div>
                    </section>

                    <section class="notes">
                        <b>Mode de paiement:</b> %s<br>
                        <b>Notes:</b> %s
                    </section>

                    <section class="stamp-zone">
                        <div class="stamp">
                            <div class="stamp-content">
                                <div class="stamp-top">HEALTH &amp; FITNESS CENTER</div>
                                <div class="stamp-brand">FITOPIA</div>
                                <div class="stamp-name">Adam Tourel</div>
                                <div class="stamp-role">GENERAL MANAGER</div>
                            </div>
                        </div>
                        <div class="signature">
                            <div class="label">Signature:</div>
                            <div class="specimen">Specimen</div>
                            <div class="name">Ali Touaiti</div>
                        </div>
                    </section>

                    <footer class="footer">
                        Merci pour votre confiance. Cette facture peut etre conservee comme preuve d'achat.
                    </footer>
                </article>
                </body>
                </html>
                """.formatted(
                escapeHtml(invoiceNumber),
                escapeHtml(invoiceNumber),
                escapeHtml(ORDER_DATE_FORMATTER.format(orderDate)),
                escapeHtml(fullName(order)),
                escapeHtml(valueOrDash(order.getEmail())),
                escapeHtml(valueOrDash(order.getPhone())),
                escapeHtml(valueOrDash(order.getAddress())),
                escapeHtml(valueOrDash(order.getCity())),
                escapeHtml(valueOrDash(order.getPostalCode())),
                itemsRows,
                formatPrice(order.getSubtotal()),
                escapeHtml(shippingValue),
                escapeHtml(discountValue),
                formatPrice(order.getTotalAmount()),
                escapeHtml(valueOrDash(order.getPaymentMethod())),
                notesValue
        );

        Path target = Files.createTempFile(
                "fitopia-facture-" + INVOICE_NUMBER_FORMATTER.format(LocalDateTime.now()) + "-",
                ".html"
        );
        Files.writeString(target, html, StandardCharsets.UTF_8);
        return target;
    }

    private static String toItemRowHtml(SupplementOrderItem item) {
        String productName = escapeHtml(valueOrDash(item.getSupplementName()));
        int quantity = Math.max(item.getQuantity(), 0);
        String unitPrice = formatPrice(item.getUnitPrice());
        String lineTotal = formatPrice(item.getLineTotal());

        return """
                <tr>
                    <td>%s</td>
                    <td>%d</td>
                    <td>%s</td>
                    <td>%s</td>
                </tr>
                """.formatted(productName, quantity, unitPrice, lineTotal);
    }

    private static String fullName(SupplementOrder order) {
        String fullName = (valueOrDash(order.getFirstName()) + " " + valueOrDash(order.getLastName())).trim();
        return fullName.replace(" -", "").replace("- ", "");
    }

    private static String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }

    private static String formatPrice(BigDecimal amount) {
        BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount;
        return safeAmount.setScale(2, RoundingMode.HALF_UP).toPlainString() + " DT";
    }

    private static boolean isZero(BigDecimal amount) {
        return amount == null || amount.compareTo(BigDecimal.ZERO) == 0;
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
