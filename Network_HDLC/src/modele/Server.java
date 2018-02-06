package modele;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.*;
import java.util.ArrayList;

import generator.FrameGen;

// import javax.swing.SwingWorker;

// import java.awt.Color;
import java.io.*;
// import generator.FrameGen;
import vue.GUI;

/**
 *
 * @author Ilia-
 */

public class Server extends Thread {
    GUI gui;
    ServerSocket ss;
    String RR, S;

    public Server(GUI gui) {
        this.gui = gui;
    }

    public void run() {
        try {
            startServer();
        } catch (IOException e) {
            gui.showMessage(e.getMessage());
        }
        return;
    }

    public void stopServer() {
        try {
            interrupt();
            ss.close();
            gui.changeServerStatus(false);
        } catch (IOException e) {
            log("Server has disconnected");
        }
    }

    private void startServer() throws IOException {
        String fromClient;
        int expectingCounter = 0;
        ArrayList<String> slidingWindowBuffer = new ArrayList<String>();

        PrintWriter out = null;
        BufferedReader in = null;
        BufferedWriter bw = null;

        try {
            ss = new ServerSocket(gui.getServerPort());
            gui.changeServerStatus(true);
            Socket cs = ss.accept();

            out = new PrintWriter(cs.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(cs.getInputStream()));

            File fileFrame = new File("GENERATEDSERVER.txt");
            FileWriter fw = new FileWriter(fileFrame);
            bw = new BufferedWriter(fw);

            ArrayList<String> slidingWindow = new ArrayList<String>();
//            boolean connected = false;
            boolean retransmissionMode = false;
            
            while ((fromClient = in.readLine()) != null) {
                if (fromClient.equals("bye")) {
                    log("\n  ->RECEIVED BYE");
                    break;
                }
                /*String[] tokens = fromClient.split(" ");
                if(tokens[0].equals("GET")){
                	BufferedReader br = null;
                	if(tokens[1].equals("/")){
                		br = new BufferedReader(new FileReader("index.html"));
                	}
            		else{
            			br = new BufferedReader(new FileReader(tokens[1].split("/")[1]));
            		}
                  	 String line = null;
                	 while ((line = br.readLine()) != null) {
                		 out.println(line);
                	 };
                	 br.close();
                	
                }*/

                String unstuffedFrame = FrameGen.frameUnstuffer(fromClient);
                String extractedType = FrameGen.typeExtractor(unstuffedFrame);
                int extractedNum = FrameGen.numExtractor(unstuffedFrame);

                log("\n<<--------------------<< RECEIVED : " + extractedNum + " <<--------------------<<");
                FrameGen.frameLogger(gui, false, fromClient);
                log("<<----------<< EXPECTED : " + expectingCounter % 8 + " <<----------<<");

                if (slidingWindowBuffer.contains(fromClient)) {
                    log("<<----------<< ALREADY IN BUFFER <<----------<<");
                    RR = RRFrameMaker(extractedNum, "A");

                    normalResponseMaker(RR, out);

                } else {
                    slidingWindow.add(fromClient);

                    if (slidingWindowBuffer.size() == 4) {
                        slidingWindowBuffer.remove(0);
                        slidingWindowBuffer.add(fromClient);
                    } else {
                        slidingWindowBuffer.add(fromClient);
                    }
                    FrameGen.windowPrinter(gui, false, slidingWindow);
                    FrameGen.windowPrinter(gui, false, slidingWindowBuffer);

                    if (retransmissionMode) {
                        log("\n<<--------------------<< RETRANSMISSION MODE <<--------------------<<");

                        if (extractedNum == (expectingCounter % 8)) {
                            log("<<----------<< RIGHT NUMBER <<----------<<");

                            if (FrameGen.dataVerifier(FrameGen.dataExtractor(unstuffedFrame))) {
                                log("<<----------<< CRC VERIFIED <<----------<<");
                                retransmissionMode = false;
                                addToDoc(slidingWindow.get(slidingWindow.size() - 1), bw);
                                slidingWindow.remove(slidingWindow.size() - 1);
                                RR = RRFrameMaker(extractedNum);

                                normalResponseMaker(RR, out);
                                FrameGen.windowPrinter(gui, false, slidingWindow);
                                FrameGen.windowPrinter(gui, false, slidingWindowBuffer);

                                expectingCounter++;

                                while (!retransmissionMode && !slidingWindow.isEmpty()) {

                                    if (FrameGen.numExtractor(
                                            FrameGen.frameUnstuffer(slidingWindow.get(0))) == (expectingCounter % 8)) {
                                        log("<<----------<< CHECK : "
                                                + FrameGen.numExtractor(FrameGen.frameUnstuffer(slidingWindow.get(0)))
                                                + " <<----------<<");

                                        if (FrameGen.dataVerifier(FrameGen
                                                .dataExtractor(FrameGen.frameUnstuffer(slidingWindow.get(0))))) {
                                            log("<<----------<< CRC VERIFIED <<----------<<");
                                            RR = RRFrameMaker(expectingCounter % 8);
                                            addToDoc(slidingWindow.get(0), bw);

                                            slidingWindow.remove(0);

                                            normalResponseMaker(RR, out);
                                            FrameGen.windowPrinter(gui, false, slidingWindow);
                                            FrameGen.windowPrinter(gui, false, slidingWindowBuffer);

                                            expectingCounter++;
                                        } else {
                                            log("<<----------<< CRC NOT VERIFIED <<----------<<");
                                            S = RRFrameMaker(expectingCounter % 8, "S");
                                            slidingWindow.remove(0);
                                            slidingWindowBuffer.remove(slidingWindow.get(0));

                                            normalResponseMaker(S, out);
                                            FrameGen.windowPrinter(gui, false, slidingWindow);
                                            FrameGen.windowPrinter(gui, false, slidingWindowBuffer);

                                            retransmissionMode = true;
                                        }
                                    }
                                }
                            } else {
                                log("<<----------<< CRC NOT VERIFIED <<----------<<");
                                S = RRFrameMaker(extractedNum, "S");
                                slidingWindowBuffer.remove(slidingWindow.get(0));
                                slidingWindow.remove(0);

                                normalResponseMaker(S, out);
                                FrameGen.windowPrinter(gui, false, slidingWindow);
                                FrameGen.windowPrinter(gui, false, slidingWindowBuffer);
                            }
                        }
                    } else {

                        switch (extractedType) {

                        case "D":
                            log("<<----------<< CONNECTION FRAME <<----------<< ");
                            if (extractedNum == 0) {
//                                connected = true;

                                RR = RRFrameMaker(extractedNum, "D");
                                slidingWindow.remove(0);

                                normalResponseMaker(RR, out);
                                FrameGen.windowPrinter(gui, false, slidingWindow);
                                FrameGen.windowPrinter(gui, false, slidingWindowBuffer);

                                expectingCounter++;

                            }
                            break;

                        case "I":
                            log("<<----------<< DATA FRAME : " + extractedNum + " <<----------<< ");
                            Boolean verified = FrameGen.dataVerifier(FrameGen.dataExtractor(unstuffedFrame));

                            if ((extractedNum == (expectingCounter % 8)) && verified) {
                                log("<<----------<< RIGHT NUMBER <<----------<<");
                                log("<<----------<< CRC VERIFIED <<----------<<");
                                RR = RRFrameMaker(extractedNum);
                                addToDoc(slidingWindow.get(0), bw);
                                slidingWindow.remove(0);

                                normalResponseMaker(RR, out);
                                FrameGen.windowPrinter(gui, false, slidingWindow);
                                FrameGen.windowPrinter(gui, false, slidingWindowBuffer);

                                expectingCounter++;
                            } else {
                                log("<<----------<< CRC NOT VERIFIED <<----------<<");
                                retransmissionMode = true;
                                slidingWindowBuffer.remove(slidingWindow.get(0));
                                slidingWindow.remove(0);
                                S = RRFrameMaker(extractedNum, "S");

                                normalResponseMaker(S, out);
                                ;
                                FrameGen.windowPrinter(gui, false, slidingWindow);
                                FrameGen.windowPrinter(gui, false, slidingWindowBuffer);

                            }
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            afficher(e.getMessage());
        } finally {
            in.close();
            out.close();
            ss.close();
            bw.close();
            gui.changeServerStatus(false);
        }
    }

    private void addToDoc(String frame, BufferedWriter bw) throws IOException {
        String unstuffedFrame = FrameGen.frameUnstuffer(frame);
        String data = FrameGen.dataExtractor(unstuffedFrame);
        data = data.substring(0, data.length() - 8);
        char c;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {

            if (i % 8 == 0 && i != 0) {
                int charCode = Integer.parseInt(sb.toString(), 2);
                c = (char) charCode;
                if (c == '#'){
                	gui.rebuildText("\n");
                    bw.newLine();
                }
                else{
                	gui.rebuildText(c+"");
                    bw.write(c);
                }

                sb.setLength(0);
                sb = new StringBuilder();
                sb.append(Character.toString(data.charAt(i)));
            } else {
                sb.append(Character.toString(data.charAt(i)));
            }

        }

    }

    public String RRFrameMaker(int RRFrameNum, String frameType) {
        String numString = Integer.toBinaryString(RRFrameNum);
        while (numString.length() < 8)
            numString = "0" + numString;

        String toBitStuff = FrameGen.stringToBinary(frameType) + numString;
        String bitStuffed = FrameGen.bitStuffer(toBitStuff);
        String frameToSend = FrameGen.FLAG + bitStuffed + FrameGen.FLAG;

        return frameToSend;

    }

    public void RRsender(String frameToSend, PrintWriter out) {
        out.println(frameToSend);
    }

    public String RRFrameMaker(int RRFrameNum) {
        return RRFrameMaker(RRFrameNum, "A");
    }

    public void afficher(String message) {
        gui.showMessage(message);
    }

    public void log(String message) {
        gui.logn(false, message);
    }

    public void normalResponseMaker(String response, PrintWriter out) {
        log("\n>>-------------------->> SENDING >>-------------------->>");
        FrameGen.frameLogger(gui, false, response);
        out.println(response);
    }
}
