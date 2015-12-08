package experiments;

import Clustering.Document;
import Clustering.DocumentCollection;
import TeachingDocParser.Tokenizer;
import TermScoring.TFIDF.TFIDFCalculator;
import Tokenizer.HTMLParser;
import Tokenizer.Stemmer;
import Tokenizer.StopWordRemover;
import com.google.common.collect.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import utils.DirectoryManager;
import utils.SimpleFileReader;
import org.lemurproject.galago.core.parse.TagTokenizer;
import org.lemurproject.galago.core.retrieval.ScoredDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/*
 * Created by mhjang on 1/27/15.
 * Generates a query from a given document */

public class DocumentQueryGenerator {
    Stemmer stemmer = new Stemmer();
    TagTokenizer tagTokenizer = new TagTokenizer();
    StopWordRemover sr = new StopWordRemover();

    public DocumentQueryGenerator() {
        Tile.sr = this.sr;
    }

    static class Tile {
        String text;
        static StopWordRemover sr;
        double stopwordContainment = 0.0;
        int numOfTokens = 0;
        double importance = 0.0;

        public Tile(String t) {
            text = t;
            String[] tokens = text.split(" ");
            String[] newTokens = sr.removeStopWords(tokens);
            numOfTokens = tokens.length;
            if (tokens.length > 0)
                stopwordContainment = (double) (tokens.length - newTokens.length) / (double) tokens.length;
            else
                stopwordContainment = 0.0;
        }
    }


    public static void generateDropDownMenu() {
        String dir = "/Users/mhjang/Desktop/Research/WikiLinking/data/clueweb_plaintext/tiled/";
        DirectoryManager dr = new DirectoryManager(dir);
        DocumentQueryGenerator queryGen = new DocumentQueryGenerator();

        for (String filename : dr.getFileNameList()) {
            try {
                SimpleFileReader sr = new SimpleFileReader(dir + filename);
                StringBuilder sb = new StringBuilder();
                while (sr.hasMoreLines()) {
                    sb.append(sr.readLine());
                }
                String keyword = queryGen.generateQuerybyFrequency(sb.toString(), 1);
                System.out.println("<li role=\"presentation\"><a role=\"menuitem\" tabindex=\"-1\" href" +
                        "=\"" + filename + ".html>" + keyword + "</a></li>");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * for the new dataset
     */
    public static void generateRanking() {
        DocumentQueryGenerator gen = new DocumentQueryGenerator();
        String cleanedDocPath = "C:\\Users\\mhjang\\Desktop\\pages_to_annotate_cleaned\\";
        DirectoryManager dm = new DirectoryManager(cleanedDocPath);
        WikiRetrieval wr = new WikiRetrieval();




        try {
            for (String fileName : dm.getFileNameList()) {
                SimpleFileReader sr = new SimpleFileReader(cleanedDocPath + fileName);
                StringBuilder sb = new StringBuilder();
                while(sr.hasMoreLines()) {
                    sb.append(sr.readLine());
                }
                String text = sb.toString();
                List<ScoredDocument> results = wr.runQuery(gen.generateQuerybyFrequency(text, 10));
                for(ScoredDocument sd : results) {
                    System.out.println(fileName + "\t" + 0 + "\t" + sd.documentName + "\t" + sd.rank + "\t" + sd.score + "\t" + "original");
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }


    public void showQuery(String documentName) throws IOException {
        LinkedList<Tile> tiles = new LinkedList<Tile>();
        String baseDir = "C:\\Users\\mhjang\\Research\\WikiLinking\\tiled_bprm\\";

    //    SimpleFileReader sr = new SimpleFileReader(baseDir + documentName);
        SimpleFileReader sr = new SimpleFileReader(documentName);
        WikiRetrieval wr = new WikiRetrieval();


        boolean tileOpened = false;
        StringBuilder tileBuilder = new StringBuilder();
        StringBuilder fullTextBulider = new StringBuilder();

        System.out.println("Tiled Query");
        while (sr.hasMoreLines()) {
            String line = sr.readLine();
            if (!tileOpened && line.contains("<TILE>")) {
                tileOpened = true;
                tileBuilder.append(line.replace("<TILE>", "") + "\n");
            } else if (line.contains("</TILE>")) {
                tileOpened = false;
                tileBuilder.append(line.replace("</TILE>", "") + "\n");
                 System.out.println("TILE: " + tileBuilder.toString());
                 List<ScoredDocument> docs = (List<ScoredDocument>) wr.runQuery(generateQuerybyFrequency(tileBuilder.toString(), 10));
                 for (ScoredDocument sd : docs) {
                      System.out.println(sd.documentName);
                       }
                fullTextBulider.append(tileBuilder.toString());
                tileBuilder = new StringBuilder();
                //
            } else if (tileOpened) {
                tileBuilder.append(line + "\n");
            }
        }
        System.out.println("Full Query");
        List<ScoredDocument> docs = (List<ScoredDocument>) wr.runQuery(generateQuerybyFrequency(fullTextBulider.toString(), 10));
        for (ScoredDocument sd : docs) {
            System.out.println(sd.documentName);
        }

    }

    private static void addPool(HashMap<String, HashSet<String>> querySet, String filePath) {
        try {
            SimpleFileReader sr = new SimpleFileReader(filePath);
            while (sr.hasMoreLines()) {
                String line = sr.readLine();
                String[] tokens = line.split("\t");
                if (!querySet.containsKey(tokens[0]))
                    querySet.put(tokens[0], new HashSet<String>());
                querySet.get(tokens[0]).add(tokens[2]);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }






    // using top K frequency
    public String generateQuerybyTFIDF(String dir, String filename, int k) {
        try {
            HTMLParser parser = new HTMLParser();
            String parsedString = parser.parse(dir + filename);
            parsedString = parsedString.replace("\t", " ");
            parsedString = parsedString.replace("\n", " ");
            Stemmer stemmer = new Stemmer();
            StopWordRemover sr = new StopWordRemover();


            TFIDFCalculator tfidf = new TFIDFCalculator(false);
            tfidf.calulateTFIDF(TFIDFCalculator.LOGTFIDF, dir, Tokenizer.UNIGRAM, false);
            DocumentCollection dc = tfidf.getDocumentCollection();

            HashMap<String, Document> documentSet = dc.getDocumentSet();
            for (String docName : documentSet.keySet()) {
                Document doc = documentSet.get(docName);
                LinkedList<Map.Entry<String, Double>> topRankedTerms = doc.getTopTermsTFIDF(k);
                System.out.print(doc.getName() + ": ");
                for(Map.Entry<String, Double> e: topRankedTerms) {
                    System.out.print(e.getKey() + " ");
                }
                System.out.println();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    // using top K frequency
    public String generateQuerybyFrequency(String text, int k) {
        try {
            text = text.replace("\t", " ");
            text = text.replace("\n", " ");

            org.lemurproject.galago.core.parse.Document queryDoc = tagTokenizer.tokenize(text);
            Multiset<String> docTermBag = HashMultiset.create();

            for (String t : queryDoc.terms) {
                if (!sr.stopwords.contains(t)) {
                    docTermBag.add(t);
                }
            }
            int count = 0;
            StringBuilder query = new StringBuilder();

            for (String term : Multisets.copyHighestCountFirst(docTermBag).elementSet()) {
                if(term.length() > 1) {
                    query.append(term + " ");
                    if (count++ == k) break;
                }
            }
            //        System.out.println();
            return query.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    // using top K frequency
    public String generateQuerybyFrequency(String dir, String filename, int k) {
        try {
            HTMLParser parser = new HTMLParser();
            String parsedString = parser.parse(dir + filename);

            parsedString = parsedString.replace("\t", " ");
            parsedString = parsedString.replace("\n", " ");
            Stemmer stemmer = new Stemmer();
            StopWordRemover sr = new StopWordRemover();

            String stemmedString = stemmer.stemString(parsedString, true);
            Multiset<String> docTermBag = HashMultiset.create();
            String[] terms = stemmedString.split("\\s");
            for (String t : terms) {
                if (!sr.stopwords.contains(t)) {
                    docTermBag.add(t.trim().replace(".", " "));
                }
            }
            int count = 0;
            StringBuilder query = new StringBuilder();

            for (String term : Multisets.copyHighestCountFirst(docTermBag).elementSet()) {
                if (term.length() > 1) {
                    //              System.out.print(term + "( " + docTermBag.count(term) + " )");
                    query.append(term + " ");
                    if (count++ == k) break;
                }
            }
            //        System.out.println();
            System.out.println(filename + ": " + query.toString());
            return query.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
