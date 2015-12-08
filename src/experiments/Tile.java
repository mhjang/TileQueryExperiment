package experiments;

import Tokenizer.StopWordRemover;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import java.util.HashMap;

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
    public Tile(String t) {
        text = t;
        String[] rawTokens = text.split(" ");
        tokens = sr.removeStopWords(rawTokens);

        numOfTokens = tokens.length;
        if (tokens.length > 0)
            stopwordContainment = (double) (rawTokens.length - tokens.length) / (double) tokens.length;
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