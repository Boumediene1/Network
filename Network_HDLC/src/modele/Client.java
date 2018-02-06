/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modele;

import java.io.*;
import java.net.*;

import generator.FrameGen;
import vue.GUI;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Ilia-
 */
public class Client extends Thread {
    GUI gui;
    String filename, hostname;
    int portNumber;
    Socket s;
    private ArrayList<Timer> timers;
    private ArrayList<String> slidingWindow;

    int totalACK;
    int firstElement;
    int totalSent;

    public Client(GUI gui, String filename) {
        this.gui = gui;
        this.filename = filename;
    }

    public void run() {
        startClient();
        return;
    }

    public void startClient() {
        portNumber = gui.getClientPortNumber();
        hostname = gui.getClientHostname();
        if (hostname.equals("")) {
            afficher("Please specify a hostname");
            return;
        }
        
        if (filename.equals("")) {
            afficher("Please specify a file to send");
            return;
        }

        File fileText = new File(filename);

        String fromServer;
        timers = new ArrayList<>();
        slidingWindow = new ArrayList<>();
        totalSent = 0;

        Boolean connected = false;
        FileInputStream inputStream = null;
        Scanner sc = null;
        BufferedReader in = null;
        PrintWriter out = null;

        totalACK = 0;
        firstElement = 0;
        String foundFrame;
        int foundFrameIndex;

        try {
            Parser.fileReader(fileText);
            s = new Socket(hostname, portNumber);
            gui.changeClientStatus(true);
            out = new PrintWriter(s.getOutputStream(), true); // To write to the stream  
            in = new BufferedReader(new InputStreamReader(s.getInputStream())); // to read from the stream   
            inputStream = new FileInputStream("GENERATEDFRAMES.txt");

            sc = new Scanner(inputStream, "UTF-8");

            // 1. Add connection frame to window and send it
            if (sc.hasNextLine()) {
                slidingWindow.add(sc.nextLine());
                FrameGen.windowPrinter(gui, true, slidingWindow);
                totalSent++;

                log("\n>>-------------------->> SENDING >>-------------------->>");
                out.println(slidingWindow.get(0));
                FrameGen.frameLogger(gui, true, slidingWindow.get(0));
                timerAdder(5, out);

            }

            // 2. Client is listening for servers answers

            while ((fromServer = in.readLine()) != null) {

                // 3. Extraction of type and number from received frame

                String unstuffedFrame = FrameGen.frameUnstuffer(fromServer);
                String extractedType = FrameGen.typeExtractor(unstuffedFrame);
                int extractedNum = FrameGen.numExtractor(unstuffedFrame);

                log("\n<<--------------------<< RECEIVED : " + extractedNum + " <<--------------------<<");

                // 4. Seperate case for connection frame
                // 4.1 Remove the acknowledged connection frame from window and advance the firstElement index
                // 4.2 Add 4 elements to fill up the window and send each of them

                if (extractedType.equals("D")) {
                    log("<<----------<< CONNECTION FRAME <<----------<< ");
                    connected = true;
                    totalACK++;
                    timers.get(0).cancel();
                    timers.get(0).purge();
                    timers.remove(0);
                    slidingWindow.remove(0);
                    firstElement++;

                    for (int i = 0; i < 4; i++) {
                        if (sc.hasNextLine()) {
                            slidingWindow.add(sc.nextLine());
                            FrameGen.windowPrinter(gui, true, slidingWindow);
                            totalSent++;

                            log("\n>>-------------------->> SENDING >>-------------------->>");
                            out.println(slidingWindow.get(i));
                            FrameGen.frameLogger(gui, true, slidingWindow.get(i));
                            timerAdder(5, out);

                        }
                    }
                }
                // 5. Once connection is established, enter the general case

                else if (connected) {

                    // 6. Seperate management depending on the frame type received
                    // 6.1 For case A, check if we have received the right ACK by checking the number of the frame
                    // 6.2 If that is the case, slide the window by removing first element and adding one (update first element index). Send the added element.

                    switch (extractedType) {

                    case "A":
                        log("<<----------<< RR FRAME : " + extractedNum + " <<----------<<");

                        if (extractedNum == (firstElement % 8)) {
                            windowUpdaterOutputter(sc, out);
                        } else {
                            log("<<----------<< CUMULATIVE : " + extractedNum + " <<----------<<");
                            foundFrame = FrameGen.frameFinder(slidingWindow, extractedNum);
                            if (!foundFrame.equals("not found")) {
                                foundFrameIndex = slidingWindow.indexOf(foundFrame);

                                for (int i = 0; i <= foundFrameIndex; i++) {
                                    windowUpdaterOutputter(sc, out);
                                }
                            } else {
                                log("\n>>-------------------->>-------------------->> CRITICAL ERROR : FRAME NOT FOUND >>-------------------->>-------------------->>");
                            }
                        }
                        break;

                    case "S":
                        log("<<----------<< SREJ FRAME : " + extractedNum + " <<----------<< ");
                        foundFrame = FrameGen.frameFinder(slidingWindow, extractedNum);

                        if (!foundFrame.equals("not found")) {
                            foundFrameIndex = slidingWindow.indexOf(foundFrame);

                            log("\n>>-------------------->> SENDING >>-------------------->>");
                            out.println(foundFrame);
                            FrameGen.frameLogger(gui, true, foundFrame);
                            timerReplacer(5, out, foundFrameIndex);

                        }
                    }

                    if (!sc.hasNext() && slidingWindow.isEmpty()) {
                        out.println("bye");
                        log(">>-------------------->>-------------------->> TOTAL SENT : " + totalSent
                                + " >>-------------------->>-------------------->>");
                        log("<<--------------------<<--------------------<< TOTAL ACK : " + totalACK
                                + " <<--------------------<<--------------------<<");
                    }
                }
            }

        }

        catch (SocketException e) {
            gui.showMessage("Socket closed on client side");
        } catch (UnknownHostException e) {
            afficher("Unknown Host " + e.getMessage());
            return;
        } catch (IOException e) {
            e.printStackTrace();
            e.getCause();
            afficher(e.getMessage());
            //            gui.showMessage(("Couldn't get I/O for the connection to " + hostname));
            return;
        } finally {
            try {
                sc.close();
                inputStream.close();
                in.close();
                s.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                gui.showMessage("Error while closing stuff");
            }
            out.close();
            gui.changeClientStatus(false);
        }
    }

    private void timerReplacer(int seconds, PrintWriter out, int foundFrameIndex) {
    	timers.get(foundFrameIndex).cancel();
        timers.get(foundFrameIndex).purge();
        timers.set(foundFrameIndex, new Timer());
        int frameNumber = FrameGen.numExtractor(slidingWindow.get(foundFrameIndex));
        timers.get(foundFrameIndex).schedule(new ResendFrame(frameNumber, out, slidingWindow), seconds * 1000);
        log("----->> TIMER : 5s");
        log("----->> TIMERS EXISTING : " + timers.size() + "\n");

    }

    private void windowUpdaterOutputter(Scanner sc, PrintWriter out) {
        timers.get(0).cancel();
        timers.get(0).purge();
        timers.remove(0);
        slidingWindow.remove(0);
        totalACK++;
        firstElement++;

        if (sc.hasNextLine()) {
            slidingWindow.add(sc.nextLine());
            FrameGen.windowPrinter(gui, true, slidingWindow);

            log("\n>>-------------------->> SENDING >>-------------------->>");
            out.println(slidingWindow.get(3));
            FrameGen.frameLogger(gui, true, slidingWindow.get(3));
            timerAdder(5, out);
            totalSent++;

        }

    }

    private void timerAdder(int seconds, PrintWriter out) {
        timers.add(new Timer());
        int frameNumber = FrameGen.numExtractor(slidingWindow.get(slidingWindow.size() - 1));
        timers.get(timers.size() - 1).schedule(new ResendFrame(frameNumber, out, slidingWindow), seconds * 1000);
        log("----->> TIMER : 5s");
        log("----->> TIMERS EXISTING : " + timers.size() + "\n");
    }

    public void stopClient() {
        try {
            interrupt();
            s.close();
            gui.changeClientStatus(false);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            gui.showMessage("Client has disconnected");
        }
    }

    public void afficher(String message) {
        gui.showMessage(message);
    }

    public void log(String message) {
        gui.logn(true, message);
    }

    public void test() {
        gui.simulateTransfer();
    }

    public void setFile(String fileChooser) {
        filename = fileChooser;
    }

    public class ResendFrame extends TimerTask {
        private int frameNumber;
        private PrintWriter out;
        private ArrayList<String> slidingWindow;
        private String foundFrame;

        ResendFrame(int frameNumber, PrintWriter out, ArrayList<String> slidingWindow) {
            this.frameNumber = frameNumber;
            this.out = out;
            this.slidingWindow = new ArrayList<>(slidingWindow);

        }

        @Override
        public void run() {
            this.foundFrame = FrameGen.frameFinder(this.slidingWindow, this.frameNumber);
            if (!this.foundFrame.equals("not found")) {
                log("\n>>-------------------->> TIMER EXPIRED : RESENDING >>-------------------->>");
                FrameGen.frameLogger(gui, true, this.foundFrame);
                out.println(this.foundFrame);
            } else {
                log("\n>>-------------------->>-------------------->> CRITICAL TIMER ERROR : FRAME NOT FOUND >>-------------------->>-------------------->>");
            }

        }

    }
}
