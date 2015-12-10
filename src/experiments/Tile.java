package experiments;

import Tokenizer.Stemmer;
import Tokenizer.StopWordRemover;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.TagTokenizer;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.utility.Parameters;
import utils.QuoteReplace;

import java.util.HashMap;
import java.util.List;

/**
 * Created by mhjang on 5/11/2015.
 */
public class Tile {
    String text;
    static StopWordRemover sr;
    double stopwordContainment = 0.0;
    int numOfTokens = 0;
    double importance = 1.0;
    HashMap<String, Double> probabilityMap = new HashMap<String, Double>();
    String[] tokens;
    static Tile fullTextTile;
    public Tile(String t) {
        text = t;


        text = QuoteReplace.replaceQuote(text);
        TagTokenizer tt = new TagTokenizer();
        Stemmer stemmer = new Stemmer();
        Document d = tt.tokenize(text);
        List<ScoredDocument> results = null;

      //  String tokenizedQuery = StrUtil.join(d.terms, " ");

        String[] stemmed = new String[d.terms.size()];
        int i=0;
        for(String term : d.terms) {
            stemmed[i++] = stemmer.stemString(term, false);
        }

        tokens = sr.removeStopWords(stemmed);

        numOfTokens = tokens.length;
        if (tokens.length > 0)
            stopwordContainment = (double) (stemmed.length - tokens.length) / (double) stemmed.length;
        else
            stopwordContainment = 0.0;

        constructTermProbablity();
    }
    private void constructTermProbablity() {
        Multiset<String> terms = HashMultiset.create();
        for(String t : tokens) {
            terms.add(t);
        }

        for(String t : terms.elementSet()) {
            probabilityMap.put(t, (double) terms.count(t) / (double) terms.size());
        }

    }

    public double getTermProb(String t) {
        return probabilityMap.get(t);
    }
}