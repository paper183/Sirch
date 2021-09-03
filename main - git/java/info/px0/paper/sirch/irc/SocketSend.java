package info.px0.paper.sirch.irc;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketSend extends Thread {

    private final BufferedWriter bwSend;
    private boolean bRun = true;
    private final LinkedBlockingQueue<String> lbqSend;

    public SocketSend(Socket sckIRC, LinkedBlockingQueue<String> lbqMain) throws IOException {
        bwSend = new BufferedWriter(new OutputStreamWriter(sckIRC.getOutputStream()));
        lbqSend = lbqMain;
        start();
    }

    @Override
    public void run() {
        System.out.println("Starting sender");

        while(bRun) {
            //System.out.println("Running sender");
            try {
                if (!lbqSend.isEmpty()) {
                    String strWrite = lbqSend.take();
                    if ((strWrite != null) && (!strWrite.equals(""))) {
                        bwSend.write(strWrite + "\n");
                        bwSend.flush();
                    }
                }
                else Thread.sleep(50);
            }
            catch(IOException e) {
                bRun = false;
                System.out.println("IOException: " + e);
            }
            catch (InterruptedException ex) {
                bRun = false;
                System.out.println("InterruptedException: " + ex);
            }
        }
    }

    public void close() {
        System.out.println("SEND CLOSE");
        bRun = false;
        lbqSend.clear();
        try {
            bwSend.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

