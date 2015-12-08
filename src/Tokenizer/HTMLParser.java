package Tokenizer;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.*;
import org.xml.sax.ContentHandler;

import java.io.FileInputStream;
import java.io.InputStream;


/**
 * Created by mhjang on 2/2/14.
 */
public class HTMLParser {
    public HTMLParser() {

    }
    public String parse(String filename) {
        String plainText = null;
        try {
            InputStream input = new FileInputStream(filename);
            ContentHandler handler = new BodyContentHandler(1000000);
            Metadata metadata = new Metadata();
            new HtmlParser().parse(input, handler, metadata, new ParseContext());
            plainText = handler.toString();
  //          System.out.println(plainText);
        }catch(Exception e) {
            e.printStackTrace();
        }
        return plainText;

    }

}
