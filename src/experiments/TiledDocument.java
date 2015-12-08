package experiments;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by mhjang on 5/15/2015.
 */
public class TiledDocument {
    Multiset<String> tokenset;
    HashMap<String, Double> probabilityMap = new HashMap<String, Double>();

    public TiledDocument(LinkedList<Tile> tiles) {
        tokenset = HashMultiset.create();
        for(Tile t : tiles) {
            tokenset.addAll(new ArrayList<String>(Arrays.asList(t.tokens)));
        }
        constructTermProbablity();
    }

    private void constructTermProbablity() {
        for(String t : tokenset.elementSet()) {
            probabilityMap.put(t, (double) tokenset.count(t) / (double) tokenset.size());
        }

    }

    public double getTermProb(String t) {
        return probabilityMap.get(t);
    }

    private String joinTiles(LinkedList<Tile> tiles) {
        StringBuilder fullText = new StringBuilder();
        for(Tile t : tiles) {
            fullText.append(t.text + "\n");
        }
        return fullText.toString();
    }
}
