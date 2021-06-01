package uk.ac.man.cs.analytics;

import uk.ac.man.cs.metrics.SignatureMetric;
import uk.ac.man.cs.metrics.StringPattern;
import uk.ac.man.cs.util.Pair;
import uk.ac.man.cs.util.StringMangler;
import uk.ac.man.cs.util.IOHelper;
import java.io.*;
import java.util.*;

public class HighPercentageFilter{

    private String resultPath;
    private String analyticsPath;
    private IOHelper io;

    public HighPercentageFilter(String rPath, String aPath){
        this.resultPath = rPath;
        this.analyticsPath = aPath;
        this.io = new IOHelper(); 
    }

    public void run() throws IOException {
        File resultDir = new File(this.resultPath); 

        for(File ontFile : resultDir.listFiles()){

            LinkedList<String[]> statsList = parseStatistics(ontFile);

            //step through a single file
            Iterator it = statsList.iterator(); 
            while(it.hasNext()){
                String[] stats = (String[]) it.next();
                String patternName = stats[0];

                double[] percentages = {Double.parseDouble(stats[1]),
                                        Double.parseDouble(stats[2]),
                                        Double.parseDouble(stats[3]),
                                        Double.parseDouble(stats[4]),
                                        Double.parseDouble(stats[5]),
                                        Double.parseDouble(stats[6]),
                                        Double.parseDouble(stats[7]),
                                        Double.parseDouble(stats[8]),
                                        Double.parseDouble(stats[9]),
                                        Double.parseDouble(stats[10]),
                                        Double.parseDouble(stats[11])}; 
                writeHighIndication(ontFile.getName(), patternName, 0.2, percentages);
            } 
        }
    }

    public void writeHighIndication(String ontName, String patName, double p, double[] percentages){ 
        for(int i=1; i<11; i++){
            if(percentages[i] > p) 
                System.out.println(ontName + " "+ patName + " " + lookUp(i) + " " + percentages[i]); 
        } 
    }

    public String lookUp(int i){
        if(i == 1)
            return "Covered entities";
        if(i == 2)
            return "Covered entitites in logical axioms";
        if(i == 3)
            return "Covered entities in declaration axioms";
        if(i == 4)
            return "Covered axioms";
        if(i == 5)
            return "Covered logical axioms";
        if(i == 6)
            return "Covered declaration axioms";
        if(i == 7)
            return "Instantiated declaration axioms";
        if(i == 8)
            return "Instantiated axioms";
        if(i == 9)
            return "Instantiated equivalent";
        if(i == 10)
            return "Instantiated weak";
        if(i == 11)
            return "Instantiated strong"; 
        return "";
    }


    private LinkedList<String[]> parseStatistics(File file) throws IOException{

        LinkedList<String[]> res = new LinkedList<String[]>(); 

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String [] parts = line.split(",");
                res.add(parts);
            }
        }
        return res; 
    }
}
