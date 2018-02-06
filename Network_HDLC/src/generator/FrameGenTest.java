/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package generator;

import java.util.*;
import java.io.*;

/**
 *
 * @author Ilia-
 */
public class FrameGenTest {
    
    private static int frameNum = 1;
    
    public static void main(String[] args)throws IOException {
        
        File frameFile = new File("GeneratedFrames");
        frameFile.createNewFile();
        
        //genFrame(frameFile);
        genFrame(frameFile, "I", "Hello");
        
        
        
    }
    
    public static void genFrame(File file)throws IOException{
        
//        FileWriter writer = new FileWriter(file);
        
        String flag = "01111110";
        // data is empty
        // crc applied to data is empty
        String frameType = "D";
        
        
        int numInt = 0;
        String numString = Integer.toBinaryString(numInt);
        // fill numString with zeros on the left to make sur it is written on 8 bits
        while(numString.length() < 8){
            numString = "0"+ numString;
        }
        
        String toBitStuff = stringToBinary(frameType) + numString;
        String bitStuffed = bitStuffer(toBitStuff);
        String frameToSend = flag + bitStuffed + flag;
        
        System.out.println("This is a connection frame");
        System.out.println("Flag is : " + flag);
        System.out.println("Type is : " + frameType + " Binary : " + stringToBinary(frameType));
        System.out.println("Num is : " + numInt + " Binary : " + numString);
        System.out.println("We need to bitstuff frameType & num : " + toBitStuff + " Bitstuffed : " + bitStuffed);
        System.out.println("flag + bitStuffed + flag : " + frameToSend);
   
        //writer.write();
        
    }
    
    public static void genFrame(File file, String frameType, String dataToSend)throws IOException{
        
//        FileWriter writer = new FileWriter(file);
        
        String flag = "01111110";
        String numString = Integer.toString(frameNum);
        // fill numString with zeros on the left to make sur it is written on 8 bits
        while(numString.length() < 8){
            numString = "0"+ numString;
        }

        String data = stringToBinary(dataToSend);
        System.out.println("Given data : " + dataToSend + " Binary : " + data);
        int[] generator = {1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        String stringGenerator ="1100000000000001";
        int appendedDataSize = generator.length + data.length();
        System.out.println("Generator length : " + generator.length);
        System.out.println("Data length : " + data.length());
        int[] appendedM = new int[appendedDataSize];
        int[] remainder = new int[appendedDataSize];
        String crcData;
        
        int temp;    
        for(int i = 0; i < data.length(); i++){
            temp = Character.getNumericValue(data.charAt(i));
            appendedM[i] = temp;
            remainder[i] = temp;
        }
        
        
        System.out.println("Message with added zeros : " + Arrays.toString(appendedM));
        System.out.println("Remainder before CRC" + Arrays.toString(remainder));

        remainder = crcCalculator(generator, remainder).clone();
        crcData = crcDataBuilder(remainder, appendedM);
        
        String toBitStuff = stringToBinary(frameType) + numString + crcData + stringGenerator;
        String bitStuffed = bitStuffer(toBitStuff);
        String frameToSend = flag + bitStuffed + flag;
        
        System.out.println("This is a Data frame");
        System.out.println("Flag is : " + flag);
        System.out.println("Type is : " + frameType + " Binary : " + stringToBinary(frameType));
        System.out.println("Num is : " + frameNum + " Binary : " + numString);
        System.out.println("Data is : " + dataToSend + " Binary : " + data);
        System.out.println("CRC procedure below : ");
        System.out.println("Remainder after CRC : " + Arrays.toString(remainder));
        System.out.println("Message to be sent : " + crcData);
        System.out.println("What needs to get bitstuffed : " + toBitStuff + " Bitstuffed : " + bitStuffed);
        System.out.println("Whole frame  = flag + bitStuffed + flag : " + frameToSend);
        System.out.println("Unstuffing verification : "+ bitUnStuffer(bitStuffed).equals(toBitStuff));
        System.out.println("CRC verification : "+ Arrays.toString(crcCalculator(generator,appendedM)));
        
        frameNum++;
    }
    
    public static int[] crcCalculator(int generator[], int remainder[]){
        int step = 0;
                      
        while(true){
            //System.out.println(step);
            for(int i = 0; i < generator.length; i++){
                remainder[step + i] = remainder[step + i]^generator[i];
            }
            //System.out.println(remainder[step]);
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
        
        String binaryContent = "";
        byte[] bytes = frameContent.getBytes();
               
        for (byte b : bytes){
            int val = b;
            for (int i = 0; i < 8; i++){
                binaryContent += ((val & 128) == 0 ? "0" : "1");
                val <<= 1;
            }
        }
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
    
    
    
    
    
    
}
