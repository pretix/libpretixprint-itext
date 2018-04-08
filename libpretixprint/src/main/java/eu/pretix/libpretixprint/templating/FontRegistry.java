package eu.pretix.libpretixprint.templating;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FontRegistry {
    private static FontRegistry ourInstance = new FontRegistry();

    public static FontRegistry getInstance() {
        return ourInstance;
    }

    private Map<FontSpecification, BaseFont> fontPaths;

    private FontRegistry() {
        fontPaths = new HashMap<>();
    }

    public void add(String fontName, FontSpecification.Style style, String path) throws IOException, DocumentException {
        BaseFont baseFont = BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        fontPaths.put(new FontSpecification(fontName, style), baseFont);
    }

    public BaseFont get(String fontName, FontSpecification.Style style) {
        return fontPaths.get(new FontSpecification(fontName, style));
    }
}
