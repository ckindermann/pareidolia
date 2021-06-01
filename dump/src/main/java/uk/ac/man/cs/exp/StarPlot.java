package uk.ac.man.cs.exp;

import uk.ac.man.cs.analytics.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.BufferedReader;

/**
 * Created by chris on 03/09/18.
 */

public class StarPlot {

    private static final Logger log = Logger.getLogger(String.valueOf(SimpleExtraction.class));

    public static void main(String[] args) throws IOException 
    { 
        String resultDir = args[0];
        String outputPath = args[1];//where results are written to

        StarPlot exp = new StarPlot();
        exp.run(resultDir, outputPath); 
    } 

    private void run(String resultDir, String outputPath) throws IOException{
        StarDiagram sd = new StarDiagram(resultDir, outputPath);
        sd.run(); 
    }
}

