package experiments;

import Clustering.Document;
import Clustering.DocumentCollection;
import TeachingDocParser.Tokenizer;
import TermScoring.TFIDF.TFIDFCalculator;
import Tokenizer.HTMLParser;
import Tokenizer.Stemmer;
import Tokenizer.StopWordRemover;
import com.google.common.collect.*;
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


    public String generateQueryByFrequency(String[] tokens, int k) {
        Multiset<String> docTermBag = HashMultiset.create();

        for(String t : tokens) {
            docTermBag.add(t);
        }
        StringBuilder query = new StringBuilder();
        int count = 0;
        for (String term : Multisets.copyHighestCountFirst(docTermBag).elementSet()) {
            if(term.length() > 1) {
                if (count++ == k) break;
                query.append(term + " ");

            }
        }
        //        System.out.println();
        return query.toString();
    }




}
