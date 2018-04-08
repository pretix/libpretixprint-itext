package eu.pretix.libpretixprint.templating;

public interface ContentProvider {
    String getTextContent(String content, String text);
    String getBarcodeContent(String content);
}
