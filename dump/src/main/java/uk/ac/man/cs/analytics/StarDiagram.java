package uk.ac.man.cs.analytics;

import uk.ac.man.cs.util.IOHelper;
import uk.ac.man.cs.data.*;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.*;
import java.util.*;

/**
 * Created by chris on 02/11/18.
 */

public class StarDiagram {

    private String resultPath;
    private String analyticsPath;
    private IOHelper io;

    public StarDiagram(String rPath, String aPath){
        this.resultPath = rPath;
        this.analyticsPath = aPath; 
        this.io = new IOHelper();
    } 

    public void run() throws IOException {

        File resultDir = new File(this.resultPath);

        for(File ontFile : resultDir.listFiles()){

            String path = this.analyticsPath + "/" + ontFile.getName(); 
            this.io.createFolder(path);

            LinkedList<String[]> statsList = parseStatistics(ontFile);
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

                Point[] points = percentages2points(percentages); 
                writePoints(points, path + "/" + stats[0]);
            }
        }
    }

    private LinkedList<String[]> parseStatistics(File file) throws IOException{

        LinkedList<String[]> res = new LinkedList<String[]>(); 

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            //String line = br.readLine();//get rid of first line (which only specifies the format and does not contain data) 
            String line;
            while ((line = br.readLine()) != null) {
                String [] parts = line.split(",");
                res.add(parts);
            }
        } 
        return res; 
    }

    private Point[] percentages2points(double[] percentages){
        double x = 1; 
        double y = 0; 

        Point[] res = new Point[percentages.length];

        double degree = 0;

        for(int i = 0; i<percentages.length; i++){
            double xCoord = percentages[i] * x * Math.cos(degree) - percentages[i] * y * Math.sin(degree) ;
            double yCoord = percentages[i] * y * Math.cos(degree) + percentages[i] * x * Math.sin(degree) ;

            Point p = new Point(xCoord, yCoord);
            res[i] = p;

            degree += 2*Math.PI/percentages.length; 
        }

        return res;
    }

    private void writePoints(Point[] points, String path){ 
        for(int i = 0; i<points.length; i++){
            io.writeAppend("0 0", path);
            io.writeAppend(Double.toString(points[i].getX()) + " " +
                    Double.toString(points[i].getY()), path);
            io.writeAppend("",path); //empty line
        }
    } 
}
