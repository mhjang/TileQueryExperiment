package utils;

/**
 * Created by mhjang on 12/9/15.
 */
public class QuoteReplace {
    public static String replaceQuote(String text) {
        text = text.replaceAll("\u2018", "'");
        text = text.replaceAll("\u201a", "'");
        text = text.replaceAll("\u2019", "'");
        text = text.replaceAll("\u201c", "\"");
        text = text.replaceAll("\u201d", "\"");
        text = text.replaceAll("\u201e", "\"");

        // convert <<,>> to "
        text = text.replaceAll("\u00ab", "\"");
        text = text.replaceAll("\u00bb", "\"");
        // convert <,> to "
        text = text.replaceAll("\u2039", "\"");
        text = text.replaceAll("\u203a", "\"");

        // corner marks to single-quotes
        text = text.replaceAll("\u300c", "'");
        text = text.replaceAll("\u300d", "'");
        return text;
    }

}
