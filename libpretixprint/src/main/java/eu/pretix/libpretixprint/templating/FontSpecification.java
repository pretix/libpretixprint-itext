package eu.pretix.libpretixprint.templating;

import java.util.Objects;

public class FontSpecification {
    public enum Style {
        REGULAR, ITALIC, BOLD, BOLDITALIC
    }

    private Style style;
    private String name;

    public FontSpecification(String name, Style style) {
        this.style = style;
        this.name = name;
    }

    public Style getStyle() {
        return style;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FontSpecification that = (FontSpecification) o;
        return style == that.style &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(style, name);
    }
}
