package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.html.CssAppliers;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;
import helpers.*;
import models.AwbRequest;
import models.Order;
import models.PrinterOrientation;
import org.apache.commons.lang3.StringEscapeUtils;
import play.Logger;
import play.i18n.Lang;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.*;

import static play.mvc.Controller.request;
import static play.mvc.Http.Context.Implicit.response;

public class AwbController {

    private final String SOURCE_CODE_URL = "https://github.com/ninja-van/awb-generator";

    private Messages messages;

    @Inject
    public AwbController(MessagesApi messagesApi) {
        Collection<Lang> candidates = Collections.singletonList(new Lang(Locale.US));
        this.messages = messagesApi.preferred(candidates);
    }

    public Result getAwb() {
        String json = request().body().asJson().toString();
        ObjectMapper objectMapper = new ObjectMapper();

        AwbRequest request;
        try {
            request = objectMapper.readerFor(AwbRequest.class).readValue(json);
        } catch (IOException e) {
            return Results.status(500);
        }

        ResultWrapper result = renderAwbWithOrientation(request.getOrders(), request.getPrintingSize(), request.getPrinterOrientation());

        response().setHeader("Content-Disposition", "inline; filename=awb.pdf");

        Result playResult = Results.status(Http.Status.OK, (byte[]) result.getResult());
        return playResult.as("application/pdf");
    }

    private ResultWrapper renderAwbWithOrientation(List<Order> orders, String printingSize, PrinterOrientation printerOrientation) {
        try {
            if (printerOrientation.equals(PrinterOrientation.LANDSCAPE) && printingSize.equals("A5")) {
                return new ResultWrapper<>(new RuntimeException("Combination is not supported"));
            }

            if (printerOrientation.equals(PrinterOrientation.PORTRAIT) && printingSize.equals("A6")) {
                return new ResultWrapper<>(new RuntimeException("Combination is not supported"));
            }

            if (orders.isEmpty()) {
                return new ResultWrapper<>(new RuntimeException("No orders found!"));
            }

            Map<String, Object> otherData = new HashMap<>();
            otherData.put("printingSize", printingSize);
            otherData.put("hideShipperDetails", false);


            Document document;
            if (otherData.get("printingSize").equals("A4") || otherData.get("printingSize").equals("A5")) {
                if (printerOrientation.equals(PrinterOrientation.LANDSCAPE)) {
                    document = new Document(PageSize.A4.rotate(), 16, 16, 16, 10);
                } else {
                    document = new Document(PageSize.A4, 35, 35, 35, 25);
                }
            } else {
                document = new Document(PageSize.A4.rotate(), 35, 35, 20, 20);
            }


            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PdfWriter pdfWriter = PdfWriter.getInstance(document, byteArrayOutputStream);
            document.open();

            StringBuilder html = new StringBuilder("<html><body>");

            html.append("<style>");
            html.append(getDefaultCssStyle());
            html.append(".text_right { text-align: right; }");
            html.append(".text_center { text-align: center; }");
            html.append(".border { border: 1px solid #000; }");
            html.append(this._generateAwbHtmlStyle(otherData.get("printingSize").toString(), printerOrientation));
            html.append("</style>");

            int i = 0;
            for (Order order : orders) {
                otherData.put("addFooterMargin", false);
                if (otherData.get("printingSize").equals("A5") && i % 2 == 0 && i < orders.size() - 1 || otherData.get("printingSize").equals("A6") && i % 2 == 1 && (i + 1) % 4 != 0 && i < orders.size() - 1) {
                    otherData.put("addFooterMargin", true);
                }

                if (otherData.get("printingSize").equals("A6")) {
                    if (i % 2 == 0) {
                        html.append("<table cellspacing='0' cellpadding='0' style='width: 100%;'>");
                        html.append("<tr>");
                    }

                    html.append("<td style='width: 49%; vertical-align: top;'>");
                }

                html.append(this._generateAwbHtmlContent(order, otherData));

                if (otherData.get("printingSize").equals("A6")) {
                    html.append("</td>");

                    if (i % 2 == 0) {
                        html.append("<td style='width: 2%;'></td>");
                    }

                    if (i % 2 == 0 && i >= orders.size() - 1) {
                        html.append("<td style='width: 49%; vertical-align: top;'></td>");
                    }

                    if (i % 2 == 1 || i >= orders.size() - 1) {
                        html.append("</tr>");
                        html.append("</table>");
                    }
                }

                html.append("<div style='font-size:12px;'>Source codes of this service can be accessed at " + SOURCE_CODE_URL + " and may be licensed pursuant to open source MIT license.</div>");

                if ((otherData.get("printingSize").equals("A4") && i < orders.size() - 1) ||
                    (otherData.get("printingSize").equals("A5") && i % 2 != 0 && i < orders.size() - 1) ||
                    (otherData.get("printingSize").equals("A6") && (i + 1) % 4 == 0 && i < orders.size() - 1)) {
                    html.append("<div style='page-break-after:always'></div>");
                }

                i++;
            }

            html.append("</body></html>");

            CSSResolver cssResolver = XMLWorkerHelper.getInstance().getDefaultCssResolver(true);

            XMLWorkerFontProvider fontProvider = new XMLWorkerFontProvider(XMLWorkerFontProvider.DONTLOOKFORFONTS);
            String PATH_TO_FONT = "/public/fonts/NotoSans-Black.ttf";
            fontProvider.register(getFontUrl(PATH_TO_FONT).toString());
            CssAppliers cssAppliers = new CssAppliersImpl(fontProvider);
            HtmlPipelineContext htmlContext = new HtmlPipelineContext(cssAppliers);
            htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());
            htmlContext.setImageProvider(new Base64ImageProvider());

            PdfWriterPipeline pdf = new PdfWriterPipeline(document, pdfWriter);
            HtmlPipeline html2 = new HtmlPipeline(htmlContext, pdf);
            CssResolverPipeline css = new CssResolverPipeline(cssResolver, html2);

            XMLWorker worker = new XMLWorker(css, true);
            XMLParser p = new XMLParser(worker);
            p.parse(new ByteArrayInputStream(html.toString().getBytes()));

            document.addAuthor(SOURCE_CODE_URL + " licensed under the MIT License");
            document.addCreator("Please access " + SOURCE_CODE_URL + " for source codes of this service");

            document.close();

            return new ResultWrapper<>(byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            Logger.error("Exception stack:", e);
            return new ResultWrapper<>(e);
        }
    }


    private String _generateAwbHtmlStyle(String printingSize, PrinterOrientation printerOrientation) {
        return AwbUtil.generateAwbPrintingStyleByPaperSize(printingSize, printerOrientation);
    }

    private URL getCompanyLogo() {
        return this.getClass().getResource("logo.png");
    }

    @SuppressWarnings("deprecation")
    private String _generateAwbHtmlContent(Order order, Map<String, Object> otherData) {
        String html = "";

        String deliveryTiming = "-";
        String instructions = order.getDeliveryInstructions();

        int tableCellPadding = 4;
        if (otherData.get("printingSize") != null && otherData.get("printingSize").equals("A6")) {
            tableCellPadding = 2;
        }

        html += "<table cellspacing='0' cellpadding='6' style='width: 100%;' class='border'><tr><td>";
        html += "<table cellspacing='0' cellpadding='" + tableCellPadding + "' style='width: 100%;' class='awb_table'>";
        html += "<tr>";
        html += "<td class='text_center'>";
        html += generateAwbHtmlBarcodeHeader(
            messages,
            getCompanyLogo(),
            order.getCompanyUrl(),
            order.getTrackingId(),
            order.getParcelSize(),
            order.getSortCode()
        );

        html += "</td>";
        html += "</tr>";
        html += "<tr>";
        html += "<td>";
        html += "<table cellspacing='0' cellpadding='0' border='0' width='100%'>";
        html += "<tr>";
        html += "<td style='width: 39%;'>";
        html += "<table cellspacing='0' cellpadding='" + tableCellPadding + "' class='box'>";
        html += "<tr>";
        html += "<td class='box_title'>" + Messages.get("renderAwb.FromSender") + "</td>";
        html += "</tr>";
        html += "<tr>";
        html += "<td class='box_content' valign='top'>";
        html += "<table cellspacing='0' cellpadding='0' class='box_content_table'>";
        html += "<tr>";
        html += "<td valign='middle' style='width: 12%;'><img src='" + this.getClass().getResource("/public/images/ic_person_black_18dp_2x.png") + "' class='info_icon' /></td>";
        html += "<td valign='middle' style='width: 3%;'></td>";
        html += "<td valign='middle' style='width: 85%;' class='address'>";

        if (otherData.get("hideShipperDetails") != null && otherData.get("hideShipperDetails").equals(true)) {
            html += StringEscapeUtils.escapeHtml3(order.getSentFrom() != null ? order.getSentFrom() : "");
        } else if (order.getFromName() != null) {
            html += StringEscapeUtils.escapeHtml3(StringUtils.ellipsis(order.getFromName(), 35));
        }

        if (order.getFromAddress2() != null && order.getFromCity() != null
            && order.getFromAddress2().toLowerCase().contains(order.getFromCity().toLowerCase())) {
            order.setFromAddress2(order.getFromAddress2().replace(order.getFromCity(), ""));
        }

        if (order.getToAddress2() != null && order.getToCity() != null
            && order.getToAddress2().toLowerCase().contains(order.getToCity().toLowerCase())) {
            order.setToAddress2(order.getToAddress2().replace(order.getToCity(), ""));
        }

        html += "</td>";
        html += "</tr>";
        html += "<tr><td colspan='3' class='margin'></td></tr>";

        if (Boolean.FALSE.compareTo((Boolean) otherData.get("hideShipperDetails")) == 0) {
            html += "<tr>";
            html += "<td valign='middle'><img src='" + this.getClass().getResource("/public/images/ic_call_black_18dp_2x.png") + "' class='info_icon' /></td>";
            html += "<td valign='middle'></td>";
            html += "<td valign='middle' class='address'>" + (order.getFromContact() != null ? StringEscapeUtils.escapeHtml3(StringUtils.ellipsis(order.getFromContact(), 35)) : "") + "</td>";
            html += "</tr>";
            html += "<tr><td colspan='3' class='margin'></td></tr>";
            html += "<tr>";
            html += "<td valign='top'><img src='" + this.getClass().getResource("/public/images/ic_place_black_18dp_2x.png") + "' class='info_icon' /></td>";
            html += "<td valign='top'></td>";
            html += "<td valign='top' class='address'>" + StringEscapeUtils.escapeHtml3(StringUtils.ellipsis(order.getFromAddress1() + " " + (order.getFromAddress2() == null ? "" : order.getFromAddress2()) + " " + (order.getFromCity() == null ? "" : order.getFromCity()) + " " + (order.getFromCountry() == null ? "" : order.getFromCountry()) + " " + ((order.getFromPostcode() == null ? "" : order.getFromPostcode())), 300)) + "</td>";

            html += "</tr>";
            html += "<tr><td colspan='3' class='margin'></td></tr>";
        }

        html += "</table>";
        html += "</td>";
        html += "</tr>";
        html += "</table>";

        html += "<div class='margin'></div>";

        html += "<table cellspacing='0' cellpadding='" + tableCellPadding + "' class='box' style='border: 3px solid #000000;'>";
        html += "<tr>";
        html += "<td class='row_content' valign='middle'>";
        html += "<table cellspacing='0' cellpadding='0' class='box_content_table'>";
        html += "<tr>";
        html += "<td valign='middle' style='width: 14%;'>" + Messages.get("renderAwb.COD") + ":</td>";
        html += "<td valign='middle' style='width: 1%;'></td>";
        html += "</tr>";
        html += "</table>";
        html += "</td>";
        html += "</tr>";
        html += "</table>";

        html += "</td>";
        html += "<td style='width: 2%;'></td>";
        html += "<td style='width: 59%;'>";
        html += "<table cellspacing='0' cellpadding='" + tableCellPadding + "' class='box'>";
        html += "<tr>";
        html += "<td class='box_title'>" + Messages.get("renderAwb.ToAddressee") + "</td>";
        html += "</tr>";
        html += "<tr>";
        html += "<td class='box_content' valign='top'>";
        html += "<table cellspacing='0' cellpadding='0' class='box_content_table'>";
        html += "<tr>";
        html += "<td valign='middle' style='width: 8%;'><img src='" + this.getClass().getResource("/public/images/ic_person_black_18dp_2x.png") + "' class='info_icon' /></td>";
        html += "<td valign='middle' style='width: 2%;'></td>";
        html += "<td valign='middle' style='width: 90%'>" + StringEscapeUtils.escapeHtml3(StringUtils.ellipsis(order.getToName(), 50)) + "</td>";
        html += "</tr>";
        html += "<tr><td colspan='3' class='margin'></td></tr>";


        String toContact = (order.getToContact() != null ? StringEscapeUtils.escapeHtml3(StringUtils.ellipsis(order.getToContact(), 50)) : "");

        if (!StringUtils.isEmpty(toContact)) {
            String lastFourDigits = toContact.substring(toContact.length() - 4);
            toContact = toContact.replace(lastFourDigits, "****");
        }

        String disclaimer = "Note: The contact number & address information on the Air Waybill have been " +
            "temporarily hidden as we are in the midst of a system enhancement. You can still process and ship " +
            "out your orders as per usual.";

        html += "<tr>";
        html += "<td valign='middle'><img src='" + this.getClass().getResource("/public/images/ic_call_black_18dp_2x.png") + "' class='info_icon' /></td>";
        html += "<td valign='middle'></td>";
        html += "<td valign='middle'  class='address'>" + toContact + "</td>";
        html += "</tr>";
        html += "<tr><td valign='top' colspan='3' class='margin'></td></tr>";
        html += "<tr>";
        html += "<td valign='top'></td>";
        html += "<td valign='top'></td>";
        html += "<td valign='bottom' style='font-size:8px;'>" + disclaimer + "</td>";
        html += "</tr>";

        html += "</table>";
        html += "</td>";
        html += "</tr>";
        html += "</table>";

        html += "<div class='margin'></div>";

        html += "<table cellspacing='0' cellpadding='" + tableCellPadding + "' class='box'>";
        html += "<tr>";
        html += "<td class='row_content' valign='middle'>";
        html += "<table cellspacing='0' cellpadding='0' class='box_content_table'>";
        html += "<tr>";
        html += "<td valign='middle' style='width: 8%;'><img src='" + this.getClass().getResource("/public/images/ic_access_time_black_18dp_2x.png") + "' class='info_icon' /></td>";
        html += "<td valign='middle' style='width: 20%;'>" + Messages.get("renderAwb.deliverBy") + ":</td>";
        html += "<td valign='middle' style='width: 2%;'></td>";
        html += "<td valign='middle' style='width: 70%;'>" + deliveryTiming + "</td>";
        html += "</tr>";
        html += "</table>";
        html += "</td>";
        html += "</tr>";
        html += "</table>";

        html += "</td>";
        html += "</tr>";
        html += "</table>";
        html += "</td>";
        html += "</tr>";
        html += "<tr>";
        html += "<td>";
        html += "<table cellspacing='0' cellpadding='" + tableCellPadding + "' class='box box_content_table'>";
        html += "<tr>";
        html += "<td class='box_title'>" + Messages.get("Comments") + "</td>";
        html += "</tr>";
        html += "<tr>";
        html += "<td class='comment' valign='top'>" + (instructions != null ? StringEscapeUtils.escapeHtml3(StringUtils.ellipsis(instructions.replaceAll("//*", ""), 350)) : "") + "</td>";
        html += "</tr>";
        html += "</table>";
        html += "</td>";
        html += "</tr>";
        html += "</table>";
        html += "</td></tr></table>";

        if (otherData.get("addFooterMargin") != null && otherData.get("addFooterMargin").equals(true)) {
            html += "<table cellspacing='0' cellpadding='0' style='width: 100%;'><tr><td class='footer_margin'></td></tr></table>";
        }

        return html;
    }

    private URL getFontUrl(String url) {
        return this.getClass().getResource(url);
    }

    private String getDefaultCssStyle() {
        String css = "";
        css += "body, td, p { font-family: arial unicode ms; }";
        css += "pre, tt, code, kbd, samp { font-family: arial unicode ms; font-size: 9pt; line-height: 12pt; }";
        css += "dt { margin: 0; }";
        css += "body { margin: 10%; font-size: 12pt; }";
        css += "p, dl, multicol { margin: 1em 0; }";
        css += "dd { margin-left: 40px; margin-bottom: 0; margin-right: 0; margin-top: 0; }";
        css += "blockquote, figure { margin: 1em 40px; }";
        css += "center { display: block; text-align: center; }";
        css += "blockquote[type='cite'] { border: 3px solid; padding-left: 1em; border-color: blue; border-width: thin; margin: 1em 0; }";
        css += "h1 { font-size: 2em; font-weight: bold; margin: 0.67em 0; }";
        css += "h2 { font-size: 1.5em; font-weight: bold; margin: 0.83em 0; }";
        css += "h3 { font-size: 1.17em; font-weight: bold; margin: 1em 0; }";
        css += "h4 { font-size: 1em; font-weight: bold; margin: 1.33em 0; }";
        css += "h5 { font-size: 0.83em; font-weight: bold; margin: 1.67em 0; }";
        css += "h6 { font-size: 0.67em; font-weight: bold; margin: 2.33em 0; }";
        css += "listing { font-size: medium; margin: 1em 0; white-space: pre; }";
        css += "xmp, pre, plaintext { margin: 1em 0; white-space: pre; }";
        css += "table { margin-bottom: 0; margin-top: 0; margin-left: 0; margin-right: 0; text-indent: 0; }";
        css += "caption { text-align: center; }";
        css += "tr { vertical-align: inherit; }";
        css += "tbody { vertical-align: middle; }";
        css += "thead { vertical-align: middle; }";
        css += "tfoot { vertical-align: middle; }";
        css += "table > tr { vertical-align: middle; }";
        css += "td { padding: 1px; text-align: inherit; vertical-align: inherit; }";
        css += "th { display: table-cell; font-weight: bold; padding: 1px; vertical-align: inherit; }";
        css += "sub { font-size: smaller; vertical-align: sub; }";
        css += "sup { font-size: smaller; vertical-align: super; }";
        css += "nobr { white-space: nowrap; }";
        css += "mark { background: none repeat scroll 0 0 yellow; color: black; }";
        css += "abbr[title], acronym[title] { border-bottom: 1px dotted; }";
        css += "ul, menu, dir { list-style-type: disc; }";
        css += "ul li ul { list-style-type: circle; }";
        css += "ol { list-style-type: decimal; }";
        css += "hr { color: gray; height: 2px; }";
        return css;
    }

    private String generateAwbHtmlBarcodeHeader(
        Messages messages,
        URL logo,
        String companyUrl,
        String orderTrackingNumber,
        String parcelSize,
        String sortCode
    ) {
        return "<table cellspacing='0' cellpadding='0' border='0' style='width: 100%;'>" +
            "<tr>" +
            "<td style='width: 25%; text-align: left; padding: 0'>" + "<img class='qr_code' src='data:image/png;base64," + generateBarcodeQRCode(orderTrackingNumber) + "' style='margin: 0;' />" + "</td>" +
            "<td style='width: 1%;'></td>" +
            "<td style='width: 30%; text-align: left;'>" +
            "<img src='" + logo + "' style='height: 160px;' /><br />" +
            "<span class='subtitle'>" + messages.at("renderAwb.AIRWAYBILL") + " " + companyUrl + "</span><br />" +
            "</td>" +
            "<td style='width: 2%;'></td>" +
            "<td style='width: 41%;'>" +
            "<table cellspacing='0' cellpadding='8' class='sort_code_box'>" +
            "<tr><td>" +
            "<div class='text_center sort_code'>" + sortCode + "</div>" +
            "<div style='height: 3px;'></div>" +
            "<div class='text_center sort_code_guide'>FOR INTERNAL USE</div>" +
            "</td></tr>" +
            "</table>" +
            "<div class='sort_code_margin'></div>" +
            "<div class='tracking_num'>" + StringEscapeUtils.escapeHtml3(orderTrackingNumber) + "</div>" +
            "<img class='barcode_code' src='data:image/png;base64," + generateBarcode128String(orderTrackingNumber) + "' /><div style='height: 3px;'></div>" + "<div class='tracking_num'>" +
            messages.at("Size") + "/" + messages.at("Weight") + ": " +
            parcelSize +
            "</div>" +
            "</td>" +
            "</tr>" +
            "</table>";
    }

    private String generateBarcodeQRCode(String orderTrackingNumber) {
        BarcodeQRCode qrCode = new BarcodeQRCode(orderTrackingNumber, 500, 500, null);
        java.awt.Image qrCodeImg = qrCode.createAwtImage(Color.BLACK, Color.WHITE);
        return Images.encodeToString(qrCodeImg, "PNG");
    }

    private String generateBarcode128String(String orderTrackingNumber) {
        Barcode128 code128 = new Barcode128();
        code128.setCode(orderTrackingNumber);
        java.awt.Image barcode1DImg = code128.createAwtImage(Color.BLACK, Color.WHITE);
        return Images.encodeToString(barcode1DImg, "PNG");
    }
}
