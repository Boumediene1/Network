/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modele;
import generator.FrameGen;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Ilia-
 */
public class Parser {
    /*
    public static void main(String[] args) throws IOException {
        fileReader(new File("testT.txt"));
    }
   */
     public static void fileReader(File fileText) throws IOException{
         
        File fileFrame = new File("GENERATEDFRAMES.txt");

        FileInputStream inputStream = null;
        Scanner sc = null;
        
        int lineLength;
        int dataSize = 8;
        int remaining;
        int bufferSize = dataSize;
        int startIndex;
        String buffer = "";
        
        try {
            inputStream = new FileInputStream(fileText);
            sc = new Scanner(inputStream, "UTF-8");
            
            FileWriter fw = new FileWriter(fileFrame);
            BufferedWriter bw = new BufferedWriter(fw);
           
            bw.write(FrameGen.genFrame());
            bw.newLine();

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                lineLength = line.length();
                
                remaining = bufferSize - buffer.length();
                if(lineLength < remaining){
                    buffer += line;
                    buffer += "#";
                    bufferSize++;
                }
                else{
                    buffer += line.substring(0, remaining);
                    bw.write(FrameGen.genFrame("I", buffer));
                    bw.newLine();
                    startIndex = remaining;
                    remaining = 0;
                    buffer = "";
                    bufferSize = dataSize;
                    
                    while(startIndex + dataSize <= lineLength){
                        bw.write((FrameGen.genFrame("I", line.substring(startIndex, startIndex + dataSize))));
                        bw.newLine();
                        startIndex += dataSize;
                    }
                    
                    if(startIndex + dataSize > lineLength){
                        buffer = line.substring(startIndex, lineLength);
                        buffer += "#";
                        bufferSize++;
                    }
                    
                }
                
                
        }
        
        bw.close();

        
        // note that Scanner suppresses exceptions
        if (sc.ioException() != null) {
            throw sc.ioException();
        }
        } finally {
            if (inputStream != null) {
            inputStream.close();
            }
            if (sc != null) {
            sc.close();
            }
        }
        
        
    }
         
}
