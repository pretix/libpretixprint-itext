package eu.pretix.libpretixprint;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;

public class Test {
    public String testData = "[{\"bottom\": \"274.60\", \"italic\": false, \"fontfamily\": \"Open Sans\", \"text\": \"Sample event name\", \"color\": [0, 0, 0, 1], \"align\": \"left\", \"type\": \"textarea\", \"left\": \"17.50\", \"content\": \"event_name\", \"fontsize\": \"16.0\", \"bold\": false, \"width\": \"175.00\"}, {\"bottom\": \"262.90\", \"italic\": false, \"fontfamily\": \"Open Sans\", \"text\": \"Sample product description\", \"color\": [0, 0, 0, 1], \"align\": \"left\", \"type\": \"textarea\", \"left\": \"17.50\", \"content\": \"itemvar\", \"fontsize\": \"13.0\", \"bold\": false, \"width\": \"110.00\"}, {\"bottom\": \"252.50\", \"italic\": false, \"fontfamily\": \"Open Sans\", \"text\": \"John Doe\", \"color\": [0, 0, 0, 1], \"align\": \"left\", \"type\": \"textarea\", \"left\": \"17.50\", \"content\": \"attendee_name\", \"fontsize\": \"13.0\", \"bold\": false, \"width\": \"110.00\"}, {\"bottom\": \"180.18\", \"italic\": false, \"fontfamily\": \"Open Sans\", \"text\": \"Fontsize 10, Font height: 3,52mm\", \"color\": [0, 0, 0, 1], \"align\": \"left\", \"type\": \"textarea\", \"left\": \"149.53\", \"content\": \"other\", \"fontsize\": \"10.0\", \"bold\": false, \"width\": \"113.45\"}, {\"bottom\": \"174.69\", \"italic\": false, \"fontfamily\": \"Open Sans\", \"text\": \"Fontsize 11, Font height: 3,88mm\", \"color\": [0, 0, 0, 1], \"align\": \"left\", \"type\": \"textarea\", \"left\": \"149.62\", \"content\": \"other\", \"fontsize\": \"11.0\", \"bold\": false, \"width\": \"113.45\"}, {\"bottom\": \"169.20\", \"italic\": false, \"fontfamily\": \"Open Sans\", \"text\": \"Fontsize 12, Font height: 4,23mm\", \"color\": [0, 0, 0, 1], \"align\": \"left\", \"type\": \"textarea\", \"left\": \"149.58\", \"content\": \"other\", \"fontsize\": \"12.0\", \"bold\": false, \"width\": \"113.45\"}, {\"bottom\": \"162.95\", \"italic\": false, \"fontfamily\": \"Open Sans\", \"text\": \"Fontsize 13, Font height: 4,59mm\", \"color\": [0, 0, 0, 1], \"align\": \"left\", \"type\": \"textarea\", \"left\": \"149.51\", \"content\": \"other\", \"fontsize\": \"13.0\", \"bold\": false, \"width\": \"113.45\"}, {\"bottom\": \"156.94\", \"italic\": false, \"fontfamily\": \"Open Sans\", \"text\": \"Fontsize 14, Font height: 4,94mm\", \"color\": [0, 0, 0, 1], \"align\": \"left\", \"type\": \"textarea\", \"left\": \"149.54\", \"content\": \"other\", \"fontsize\": \"14.0\", \"bold\": false, \"width\": \"113.45\"}, {\"bottom\": \"149.90\", \"italic\": false, \"fontfamily\": \"Open Sans\", \"text\": \"Fontsize 15, Font height: 5,29mm\", \"color\": [0, 0, 0, 1], \"align\": \"left\", \"type\": \"textarea\", \"left\": \"149.55\", \"content\": \"other\", \"fontsize\": \"15.0\", \"bold\": false, \"width\": \"113.45\"}, {\"type\": \"barcodearea\", \"left\": \"190.00\", \"size\": \"60.00\", \"bottom\": \"60.00\"}]";

    public static void main(String[] args)
            throws DocumentException, IOException {
        new Test().createPdf();
    }

    public void createPdf()
            throws DocumentException, IOException {
        PdfReader reader = new PdfReader("/home/raphael/proj/pretix/res/testbackground.pdf");
        Rectangle psize = reader.getPageSize(1);

        Document document = new Document(new Rectangle(psize.getWidth(), psize.getHeight()));
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("/tmp/java-out.pdf"));
        document.open();

        PdfContentByte cb = writer.getDirectContent();
        cb.addTemplate(writer.getImportedPage(reader, 1), 0, 0);
        document.add(new Paragraph("Hello World!"));
        document.close();
    }

}
