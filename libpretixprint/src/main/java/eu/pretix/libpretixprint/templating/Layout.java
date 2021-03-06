package eu.pretix.libpretixprint.templating;

import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.RectangleReadOnly;
import com.itextpdf.text.io.StreamUtil;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import eu.pretix.libpretixprint.helpers.BarcodeQR;
import eu.pretix.libpretixprint.helpers.EmbeddedLogos;

import static com.itextpdf.text.Utilities.millimetersToPoints;

public class Layout {
    private JSONArray elements;
    private InputStream backgroundInputStream;
    private Iterator<ContentProvider> contentProviders;

    public Layout(JSONArray elements, String background, Iterator<ContentProvider> contentProvider) throws FileNotFoundException {
        this(elements, new FileInputStream(background), contentProvider);
    }

    public Layout(JSONArray elements, InputStream background, Iterator<ContentProvider> contentProviders) {
        this.elements = elements;
        this.backgroundInputStream = background;
        this.contentProviders = contentProviders;
    }

    private void drawImage(JSONObject data, InputStream istream, PdfContentByte cb) throws IOException, DocumentException, JSONException {
        if (istream == null) return;
        Image img = Image.getInstance(StreamUtil.inputStreamToArray(istream));
        float width = millimetersToPoints((float) data.getDouble("width"));
        float height = millimetersToPoints((float) data.getDouble("height"));
        img.scaleToFit(width, height);
        float x = millimetersToPoints((float) data.getDouble("left"));
        float y = millimetersToPoints((float) data.getDouble("bottom"));
        if (img.getScaledWidth() < width) {
            x += (width - img.getScaledWidth()) / 2.0;
        }
        if (img.getScaledHeight() < height) {
            y += (height - img.getScaledHeight()) / 2.0;
        }
        cb.addImage(img, img.getScaledWidth(), 0, 0, img.getScaledHeight(), x, y);
    }

    private void drawPoweredBy(JSONObject data, String style, PdfContentByte cb) throws IOException, DocumentException, JSONException {
        String b64data = "";
        if (style.equals("white")) {
            b64data = EmbeddedLogos.POWERED_BY_PRETIX_WHITE;
        } else {
            b64data = EmbeddedLogos.POWERED_BY_PRETIX_DARK;
        }
        Image img = Image.getInstance(Base64.decode(b64data));
        float size = millimetersToPoints((float) data.getDouble("size"));
        img.scalePercent(size * 100f / img.getPlainHeight());
        cb.addImage(
                img, img.getScaledWidth(), 0, 0, img.getScaledHeight(),
                millimetersToPoints((float) data.getDouble("left")),
                millimetersToPoints((float) data.getDouble("bottom"))
        );
    }

    private void drawQrCode(JSONObject data, String text, PdfContentByte cb) throws IOException, DocumentException, JSONException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        BarcodeQR bqr = new BarcodeQR(
                text,
                (int) millimetersToPoints((float) data.getDouble("size")),
                hints
        );

        float size = millimetersToPoints((float) data.getDouble("size"));
        Image img = bqr.getImage();
        img.scaleToFit(size, size);
        cb.addImage(
                img, img.getScaledWidth(), 0, 0, img.getScaledHeight(),
                millimetersToPoints((float) data.getDouble("left")),
                millimetersToPoints((float) data.getDouble("bottom"))
        );
    }

    private void drawTextarea(JSONObject data, String text, PdfContentByte cb) throws IOException, DocumentException, JSONException {
        FontSpecification.Style style = FontSpecification.Style.REGULAR;
        if (data.getBoolean("bold") && data.getBoolean("italic")) {
            style = FontSpecification.Style.BOLDITALIC;
        } else if (data.getBoolean("bold")) {
            style = FontSpecification.Style.BOLD;
        } else if (data.getBoolean("italic")) {
            style = FontSpecification.Style.ITALIC;
        }

        float fontsize = (float) data.getDouble("fontsize");

        FontRegistry fontRegistry = FontRegistry.getInstance();
        BaseFont baseFont = fontRegistry.get(data.getString("fontfamily"), style);
        if (baseFont == null) {
            System.out.print("Unable to load font " + data.getString("fontfamily"));
            baseFont = fontRegistry.get("Open Sans", style);
        }
        Font font = new Font(baseFont, fontsize);

        font.setColor(
                data.getJSONArray("color").getInt(0),
                data.getJSONArray("color").getInt(1),
                data.getJSONArray("color").getInt(2)
        );

        ColumnText ct = new ColumnText(cb);

        text = text.replaceAll("<br[^>]*>", "\n");
        Paragraph para = new Paragraph(text, font);
        int alignment = 0;
        if (data.getString("align").equals("left")) {
            alignment = Element.ALIGN_LEFT;
        } else if (data.getString("align").equals("center")) {
            alignment = Element.ALIGN_CENTER;
        } else if (data.getString("align").equals("right")) {
            alignment = Element.ALIGN_RIGHT;
        }
        para.setAlignment(alignment);

        para.setLeading(fontsize);

        // Position with lower bound of "x" instead of lower bound of text, to be consistent with other implementations
        float ycorr = baseFont.getDescentPoint("x", fontsize) - baseFont.getDescentPoint(text, fontsize);

        // Simulate rendering to obtain real height
        ct.addElement(para);
        ct.setSimpleColumn(
                millimetersToPoints((float) data.getDouble("left")),
                millimetersToPoints((float) data.getDouble("bottom")) + ycorr,
                millimetersToPoints((float) (data.getDouble("left") + data.getDouble("width"))),
                millimetersToPoints((float) (data.getDouble("bottom") + 3000)) + ycorr,
                fontsize,
                alignment
        );
        ct.go(true);

        // Real rendering
        // We need to take into account the actual height of the text as well as put in some buffer for floating
        // point rounding, otherwise lines might go missing.
        cb.saveState();

        double alpha = -1D * data.optDouble("rotation", 0D) * 3.141592653589793D / 180.0D;
        float cos = (float) Math.cos(alpha);
        float sin = (float) Math.sin(alpha);

        float lineheight = (float) (Math.max(fontsize, baseFont.getAscentPoint(text, fontsize) - baseFont.getDescentPoint(text, fontsize)) + 0.0001);
        ct.addElement(para);
        float lly = millimetersToPoints((float) (data.getDouble("bottom")));
        float ury = millimetersToPoints((float) (data.getDouble("bottom"))) + ct.getLinesWritten() * lineheight;
        float llx = millimetersToPoints((float) data.getDouble("left"));
        float ycorrtop = baseFont.getAscentPoint("X", fontsize) - baseFont.getAscentPoint(text, fontsize);
        if (data.optBoolean("downward", false)) {
            lly = millimetersToPoints((float) (data.getDouble("bottom"))) - ct.getLinesWritten() * lineheight;
            ury = millimetersToPoints((float) data.getDouble("bottom"));
            ycorr = 0;
        } else {
            ycorrtop = 0;
        }
        cb.concatCTM(cos, sin, -sin, cos, llx, ury);

        ct.setSimpleColumn(
                0,
                lly - ury - ycorrtop + ycorr,
                millimetersToPoints((float) data.getDouble("width")),
                -ycorrtop + ycorr,
                fontsize,
                alignment
        );
        ct.go();
        cb.restoreState();
    }

    public void render(String filename)
            throws Exception {
        render(new FileOutputStream(filename));
    }

    public void render(OutputStream os)
            throws Exception {
        Document document;
        PdfReader reader = null;
        if (backgroundInputStream != null) {
            reader = new PdfReader(backgroundInputStream);
            if (reader.getNumberOfPages() < 1) {
                throw new Exception("Background PDF does not have a first page.");
            }

            document = new Document(reader.getPageSize(1));
        } else {
            document = new Document(new RectangleReadOnly(
                    (float) 8 * 72,
                    (float) (3.25 * 72)
            ));
        }
        PdfWriter writer = PdfWriter.getInstance(document, os);
        document.open();

        PdfContentByte cb = writer.getDirectContent();

        while(contentProviders.hasNext()) {
            ContentProvider cp = contentProviders.next();

            if (reader != null) {
                cb.addTemplate(writer.getImportedPage(reader, 1), 0, 0);
            }

            for (int i = 0; i < elements.length(); i++) {
                JSONObject obj = elements.getJSONObject(i);
                if (obj.getString("type").equals("barcodearea")) {
                    drawQrCode(obj, cp.getBarcodeContent(obj.optString("content")), cb);
                } else if (obj.getString("type").equals("textarea")) {
                    drawTextarea(obj, cp.getTextContent(obj.getString("content"), obj.getString("text")), cb);
                } else if (obj.getString("type").equals("imagearea")) {
                    drawImage(obj, cp.getImageContent(obj.getString("content")), cb);
                } else if (obj.getString("type").equals("poweredby")) {
                    drawPoweredBy(obj, obj.getString("content"), cb);
                }
            }
            if (contentProviders.hasNext()) {
                document.newPage();
            }
        }
        document.close();
    }
}

