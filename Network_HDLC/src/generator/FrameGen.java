/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package generator;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import vue.GUI;
/**
 *
 * @author Ilia-
 */
public class FrameGen {
    
    public static final String FLAG = "01111110";
    private static final String GENS = "1100000000000001";
    private static final int[] GENI = {1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
    private static int numInt = 0;
    
    public static String genFrame()throws IOException{
            
        String frameType = "D";
        String numString = Integer.toBinaryString(numInt%8);
        
        while(numString.length() < 8)
            numString = "0"+ numString;
        
                
        String toBitStuff = stringToBinary(frameType) + numString;
        String bitStuffed = bitStuffer(toBitStuff);
        String frameToSend = FLAG + bitStuffed + FLAG;
        
        numInt++;
        
        return frameToSend;
        
    }
    
    public static String genFrame(String frameType, String dataToSend)throws IOException{
         
        
        String numString = Integer.toBinaryString(numInt%8);
        while(numString.length() < 8){
            numString = "0"+ numString;
        }

        String data = stringToBinary(dataToSend);
        int appendedDataSize = GENI.length + data.length();
        int[] appendedM = new int[appendedDataSize];
        int[] remainder = new int[appendedDataSize];
        String crcData;
        
        int temp;    
        for(int i = 0; i < data.length(); i++){
            temp = Character.getNumericValue(data.charAt(i));
            appendedM[i] = temp;
            remainder[i] = temp;
        }
      
        remainder = crcCalculator(GENI, remainder).clone();
        crcData = crcDataBuilder(remainder, appendedM);
        
        String toBitStuff = stringToBinary(frameType) + numString + crcData + GENS;
        String bitStuffed = bitStuffer(toBitStuff);
        String frameToSend = FLAG + bitStuffed + FLAG;
        
        numInt++;
        
        return frameToSend;
     
    }
    
    public static int[] crcCalculator(int generator[], int remainder[]){
        int step = 0;
                      
        while(true){
            for(int i = 0; i < generator.length; i++){
                remainder[step + i] = remainder[step + i]^generator[i];
            }
            while(remainder[step] == 0 && step != remainder.length-1){
                step ++;
            }
            if((remainder.length - step) < generator.length)
                break;
        }

        return remainder;
        
    }
    
    public static String crcDataBuilder(int remainder[], int appendedM[]){

        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < appendedM.length; i++){
            appendedM[i] = appendedM[i]^remainder[i];
            builder.append(appendedM[i]);
        }
        
        return builder.toString();
    }
     
    public static String stringToBinary(String frameContent){
//        System.out.println("Frame content : " + frameContent);
        String binaryContent = "";
        byte[] bytes = frameContent.getBytes();
               
        for (byte b : bytes){
            int val = b;
            for (int i = 0; i < 8; i++){
                binaryContent += ((val & 128) == 0 ? "0" : "1");
                val <<= 1;
            }
        }
//        System.out.println("binaryContent : " + binaryContent);

        return binaryContent;
    }
    
    public static String bitStuffer(String frameDataPart){
        
        int oneCounter = 0;
        StringBuilder sb = new StringBuilder();
        
        
        for(int i = 0; i < frameDataPart.length(); i++){
            if(frameDataPart.charAt(i) == '1'){
                sb.append(frameDataPart.charAt(i));
                oneCounter++;
            } else{
                sb.append(frameDataPart.charAt(i));
                oneCounter = 0;
            }
            if(oneCounter == 5){
                sb.append("0");
                oneCounter = 0;
            }
        }
        return sb.toString();
    }
    
    public static String bitUnStuffer(String stuffedFramePart){
                
        int oneCounter = 0;
        StringBuilder sb = new StringBuilder();
        
        for(int i = 0; i < stuffedFramePart.length(); i++){
            if(stuffedFramePart.charAt(i) == '1'){
                sb.append(stuffedFramePart.charAt(i));
                oneCounter++;
            }else if(oneCounter < 5){
                sb.append(stuffedFramePart.charAt(i));
                oneCounter = 0;
            }else{
                oneCounter = 0;
            }
        }
        return sb.toString();      
    }
    
    public static String frameUnstuffer(String stuffedFrame){
        return FLAG + bitUnStuffer(stuffedFrame.substring(8, stuffedFrame.length() - 8)) + FLAG;
    }
    

    public static int numExtractor(String unstuffedFrame){
        return Integer.parseInt(unstuffedFrame.substring(16, 24), 2);
    }
    
    public static String typeExtractor(String unstuffedFrame){
        int charCode = Integer.parseInt(unstuffedFrame.substring(8, 16), 2);
        String str = new Character((char)charCode).toString();
        
        return str;
    }
    
    public static String dataExtractor(String unstuffedFrame){
        return unstuffedFrame.substring(24, unstuffedFrame.length() - 24);
    }
    
    public static Boolean dataVerifier(String extractedData){
        int[] remainder = new int[extractedData.length()];
        
        for(int i = 0; i < extractedData.length(); i++){
            remainder[i] = Integer.parseInt(Character.toString(extractedData.charAt(i)));
        }
        remainder = FrameGen.crcCalculator(GENI, remainder).clone();
        for(int j = 0; j < remainder.length; j++){
            if(remainder[j] != 0)
                return false;
        }
        return true;
    }
    
    public static void frameLogger(GUI gui, boolean client, String stuffedFrame) {
        String unstuffedFrame = FrameGen.frameUnstuffer(stuffedFrame);
        String extractedType = FrameGen.typeExtractor(unstuffedFrame);
        int extractedNum = FrameGen.numExtractor(unstuffedFrame);
        
        gui.log(client, ">>---------->> FRAMELOG >> NUM : " + extractedNum +"   TYPE : " + extractedType + " <<----------<<");
	}
    
    public static String frameFinder(ArrayList<String> slidingWindow, int extractedNum) {
		Iterator<String> windowIterator = slidingWindow.iterator();
	    
        while(windowIterator.hasNext()){
            String temp = windowIterator.next();
            if(numExtractor(temp) == extractedNum){
                return temp;
            }
        }
    
        return "not found";
	}
    
    public static void windowPrinter(GUI gui, boolean client, ArrayList<String> slidingWindow) {
		Iterator<String> windowIterator = slidingWindow.iterator();
        gui.log(client,"---------->> SLIDING WINDOW CONTENT : [ ");
        while(windowIterator.hasNext()){
            gui.log(client,numExtractor(windowIterator.next()) + " ");  
        }
        gui.log(client,"] <<----------\n");
		
	}
    
}

