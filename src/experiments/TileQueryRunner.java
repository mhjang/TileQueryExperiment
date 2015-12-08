package experiments;

import Tokenizer.Stemmer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import utils.SimpleFileReader;
import utils.SimpleFileWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by mhjang on 12/8/2015.
 */
public class TileQueryRunner {

    public static DocumentQueryGenerator querygen = new DocumentQueryGenerator();
    public static WikiRetrieval wr = new WikiRetrieval();
    public static Stemmer stemmer = new Stemmer();

    public static void main(String[] args) throws Exception {
        String sourceDir;
        int querySize = 10;
        int global = 0;
        int retrieve = 20;
        Options options = new Options();
        options.addOption("dir", true, "input file source");
        options.addOption("querySize", true, "# of terms used for a query in each tile (default: 10)");
        options.addOption("global", true, "# of global terms included for the query (default: 0)");
        options.addOption("useFullText", false, "Whether use of the full-text-tile or not (default: Off)");
        options.addOption("retrieve", true, "# of documents retrieved for a query (default: 20)");

        SimpleFileWriter tileWriter = null;
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if(!cmd.hasOption("source")) {
            System.out.println("You need the input source: -source inputDir");
            return;
        }
        else {
            sourceDir = cmd.getOptionValue("source");
        }
        WikiRetrieval wr = new WikiRetrieval();
        try {
            Path p = Paths.get(sourceDir);
            String docName = p.getFileName().toString();

            LinkedList<experiments.Tile> tiles = readTiles(sourceDir);
            String fullText = joinTiles(tiles);

                if (cmd.hasOption("useFullText")) {
                    tiles.add(new experiments.Tile(fullText));
                }

                if (cmd.hasOption("querSize")) {
                    querySize = Integer.parseInt(cmd.getOptionValue("querySize"));
                }

                if(cmd.hasOption("global")) {
                    global = Integer.parseInt(cmd.getOptionValue("global"));
                    if(global > querySize) {
                        System.out.println("# of global query cannot exceed the # of query. Try again");
                        return;
                    }
                }

                if(cmd.hasOption("retrieve")) {
                    retrieve = Integer.parseInt(cmd.getOptionValue("retrieve"));
                }

                HashMap<String, Double> weightedRankedList = runTileExperiment(tiles, global, querySize);
                List<Map.Entry<String, Double>> entries = new LinkedList(weightedRankedList.entrySet());
                Collections.sort((List) entries, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        Map.Entry<String, Double> m1 = (Map.Entry<String, Double>) (o1);
                        Map.Entry<String, Double> m2 = (Map.Entry<String, Double>) (o2);

                        return Double.compare(m2.getValue(), m1.getValue());
                    }
                });

                int rank = 1;
                for (Map.Entry<String, Double> m : entries) {
                    tileWriter.writeLine(docName + "\t 0 \t" + docName + "\t" + rank + "\t" + m.getValue() + "\t" + "tile");
                    if (rank == retrieve) break;
                    rank++;
                }
                tileWriter.close();

            } catch(Exception e) {
            e.printStackTrace();
        }
        }



    static LinkedList<Tile> readTiles(String dir) throws IOException {
        System.out.println(dir);
        SimpleFileReader sr = new SimpleFileReader(dir);
        boolean tileOpened = false;
        StringBuilder tileBuilder = new StringBuilder();
        LinkedList<Tile> tiles = new LinkedList<Tile>();

        while (sr.hasMoreLines()) {
            String line = sr.readLine();
            if (!tileOpened && line.contains("<TILE>")) {
                tileOpened = true;
                tileBuilder.append(line.replace("<TILE>", ""));
            } else if (line.contains("</TILE>")) {
                tileOpened = false;
                tileBuilder.append(line.replace("</TILE>", ""));
                String stemmedText = stemmer.stemString(tileBuilder.toString(), false);
                //       System.out.println(stemmedText);
                Tile t = new Tile(stemmedText);
                tiles.add(t);
                tileBuilder = new StringBuilder();
                //
            } else if (tileOpened) {
                tileBuilder.append(line);
            }
        }
        return tiles;
    }



    private static HashMap<String, Double> runTileExperiment(LinkedList<Tile> tiles, int tfInclude, int querySize) {
        HashMap<String, Double> weightedDocs = new HashMap<String, Double>();
        String fullText = joinTiles(tiles);
        String globalQuery = querygen.generateQuerybyFrequency(fullText, tfInclude);
        for (Tile t : tiles) {
            List<ScoredDocument> docs = (List<ScoredDocument>) wr.runQuery(querygen.generateQuerybyFrequency(t.text, (querySize - tfInclude)) + " " + globalQuery);
            int rank = 1;
            int n = docs.size()+1;
            for (ScoredDocument sd : docs) {
                if(!weightedDocs.containsKey(sd.documentName))
                    weightedDocs.put(sd.documentName, 0.0);
                weightedDocs.put(sd.documentName, weightedDocs.get(sd.documentName) + (double)(n - rank) * t.importance);
                rank++;
            }

        }

        return weightedDocs;
    }

    private static String joinTiles(LinkedList<Tile> tiles) {
        StringBuilder fullText = new StringBuilder();
        for(Tile t : tiles) {
            fullText.append(t.text + "\n");
        }
        return fullText.toString();
    }

}

