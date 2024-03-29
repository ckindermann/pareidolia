package uk.ac.man.cs.util;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import com.opencsv.CSVWriter;
import com.opencsv.CSVReader;
import java.util.*;
import java.nio.file.*;
import java.io.FileNotFoundException;


public final class IOHelper{

    public static void createFolder(String path) throws Exception { 

        //file already exists
        Path p = Paths.get(path);
        if(Files.exists(p)) {
            File file = new File(path);
            if(!file.isDirectory()){ 
                throw new FileAlreadyExistsException(file.getAbsolutePath() + " is not a directory!");
            } 
            if(file.length() > 0) { 
                throw new DirectoryNotEmptyException(file.getAbsolutePath() + " already exists and is not empty!"); 
            } 
        } else {
            //file does not exist
            File dir = new File(path);
            dir.mkdirs(); 
        } 
    }

    public static Set<String> readFile(String file) throws IOException {
        return readFile(new File(file));
    }

    public static Set<String> readFile(File file) throws IOException {
       Set<String> elements = new HashSet<>();

       try (BufferedReader br = new BufferedReader(new FileReader(file))) {
           String line;
           while ((line = br.readLine()) != null) {
               elements.add(line);
           }
       }

       return elements;
    }

    public static LinkedList<String> readTextFile(File file) throws IOException {

        LinkedList<String> textfile = new LinkedList<String>();

       try (BufferedReader br = new BufferedReader(new FileReader(file))) {
           String line;
           while ((line = br.readLine()) != null) {
               textfile.add(line);
           }
       } 
       return textfile;
    }

    public static void writeString(String destFile, String s){
        if(s != null){
            try{
                BufferedWriter bw = new BufferedWriter(new FileWriter(destFile)); 
                bw.write(s);
                bw.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public static void writeApenndList(List<String> l, String destFile){
        Iterator<String> lIter = l.iterator();
        while(lIter.hasNext()){
            String line = lIter.next();
            writeAppend(line, destFile); 
        } 
    }

    public static void writeAppend(String s, String destFile){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(destFile, true));
            bw.write(s);
            bw.newLine();
            bw.close();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        } 
    }

    public static void writeStringSet(String destFile, Set<String> patterns){
        if(!patterns.isEmpty()){
            try{
                BufferedWriter bw = new BufferedWriter(new FileWriter(destFile));
                Iterator it = patterns.iterator();

                while(it.hasNext()){
                    bw.write((String) it.next());
                    bw.newLine();
                }
                bw.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
