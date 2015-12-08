package Tokenizer;

import myungha.utils.SimpleFileReader;
import simple.io.myungha.DirectoryReader;
import simple.io.myungha.SimpleFileWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by mhjang on 4/26/2015.
 * Various ways of document tiling
 */
public class DocumentTiler {
    HashMap<String, LinkedList<String>> documents;

    /**
     * @param dir String directory to the clueweb document files
     */
    public DocumentTiler(String dir) {
        try {
            DirectoryReader dr = new DirectoryReader(dir);
            // (documentName, LinkedList: document lines)
            documents = new HashMap<String, LinkedList<String>>();
            for (String filename : dr.getFileNameList()) {
                SimpleFileReader sr = new SimpleFileReader(dir + filename);
                LinkedList<String> lines = new LinkedList<String>();
                while (sr.hasMoreLines()) {
                    lines.add(sr.readLine());
                }
                filename = filename.replaceAll(".html", "");
                documents.put(filename, lines);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String dir = "C://Users/mhjang/Research/WikiLinking/clueweb_bprm/";
        String dirToTile = "C://Users/mhjang/Research/WikiLinking/tiled_bprm/";
        String baseDir = "C://Users/mhjang/Research/WikiLinking/";
        DocumentTiler dt = new DocumentTiler(dir);
        dt.clinchTile(dirToTile, baseDir + "clueweb_plaintext_clinch/");
        dt.tileAllFixedNum(baseDir + "clueweb_plaintext_tilesize5/", 5);
        dt.tileAllFixedSize(baseDir + "clueweb_plaintext_tilenum5/", 5);
    }

    /**
     * Read the document that's already tiled from Marti Hearst's text tiling approach
     * and chop up the document with the same number of tiles of that document.
     * This is mainly to observe to see if their text-tiling approach provides better result
     * when the number of tiles are the same per each file.
     *
     * @param dirToTiled directory to the tiled documents
     */
    public void clinchTile(String dirToTiled, String outputDir) {
        try {
            File directory = new File(outputDir);
            if(!directory.exists())
                directory.mkdirs();

            DirectoryReader dr = new DirectoryReader(dirToTiled);
            System.out.println("clinching tiles");
            SimpleFileWriter sw = new SimpleFileWriter("qrel/clinch_tile_comp.txt");
            for (String filename : dr.getFileNameList()) {
                SimpleFileReader sr = new SimpleFileReader(dirToTiled + filename);
                boolean tileOpened = false;
                int numOfTiles = 0;
                ArrayList<Integer> eachTileSize = new ArrayList<Integer>();
                int tileSize = 0; // measured by num of lines each tile
                while (sr.hasMoreLines()) {
                    String line = sr.readLine();
                    if (!tileOpened && line.contains("<TILE>")) {
                        tileOpened = true;
                        numOfTiles++;
                    } else if (line.contains("</TILE>")) {
                        tileOpened = false;
                        eachTileSize.add(tileSize);
                    } else if (tileOpened) {
                        tileSize++;
                    }
                }
                filename = filename.replaceAll(".html", "");

                int fixedTileSize = tileFixedNum(outputDir, filename, numOfTiles);
                int misMatch = 0;
                for (Integer i : eachTileSize) {
                    misMatch += Math.abs(fixedTileSize - i);
                }
                sw.writeLine(filename + "\t" + (double) (misMatch) / (double) (numOfTiles));

            }
            sw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Tile one document using the number of fixed tiles
     * @param outputDir
     * @param filename
     * @param numOfTiles
     * @return
     */
    private int tileFixedNum(String outputDir, String filename, int numOfTiles) {
        int tileSize = 0;
        try {
            LinkedList<String> lines = documents.get(filename);
            int documentSize = lines.size();
            if(documentSize == 0) return 0;
            SimpleFileWriter sw = new SimpleFileWriter(outputDir + filename);
            tileSize = (int) ((double) documentSize / (double) numOfTiles);
            boolean tileOpened = false;
            if(tileSize==0) {
                System.out.println("Tile size zero error!:" + filename + "\t" + documentSize + ", " + numOfTiles);
                sw.writeLine("<TILE>");
                for (String l : lines) {
                    sw.writeLine(l);
                }
                sw.writeLine("</TILE>");
                return 1;
            }
            for (int i = 0; i < documentSize; i++) {
                if (i % tileSize == 0) {
                    sw.writeLine("<TILE>");
                    tileOpened = true;
                }
                sw.writeLine(lines.get(i));

                if (i % tileSize == tileSize - 1) {
                    sw.writeLine("</TILE>");
                    tileOpened = false;
                }
            }
            if (tileOpened) {
                sw.writeLine("</TILE>");
            }
            sw.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return tileSize;

    }


    /**
     * outputs files with the fixed number of tiles
     * @param outputDir
     * @param k
     */
    public void tileAllFixedNum(String outputDir, int k) {
        File directory = new File(outputDir);
        if(!directory.exists())
            directory.mkdirs();
        for(String filename : documents.keySet()) {
            tileFixedNum(outputDir, filename, k);
        }
    }

    private void tileFixedSize(String outputDir, String filename, int tileSize) {
        try {
            LinkedList<String> lines = documents.get(filename);
            SimpleFileWriter sw = new SimpleFileWriter(outputDir + filename);

            boolean tileOpened = false;
            for (int i = 0; i < lines.size(); i++) {
                if (i % tileSize == 0) {
                    sw.writeLine("<TILE>");
                    tileOpened = true;
                }
                sw.writeLine(lines.get(i));
                if (i % tileSize == tileSize - 1) {
                    sw.writeLine("</TILE>");
                    tileOpened = false;
                }
            }
            if (tileOpened) {
                sw.writeLine("</TILE>");
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * outputs files with the fixed size of tiles
     */
    public void tileAllFixedSize(String outputDir, int aTileSize) {
        File directory = new File(outputDir);
        if(!directory.exists())
            directory.mkdirs();
        for(String filename : documents.keySet()) {
            tileFixedSize(outputDir, filename, aTileSize);
        }
    }

}
