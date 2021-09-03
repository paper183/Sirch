package info.px0.paper.sirch.irc;

import android.os.Environment;
import android.os.Looper;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.LinkedBlockingQueue;

import info.px0.paper.sirch.R;
import info.px0.paper.sirch.UI;
import info.px0.paper.sirch.config.DisplayResultConfig;
import info.px0.paper.sirch.config.GlobalConfig;

class SocketRecv extends Thread {

    //private BufferedReader brRecv;
    private boolean bRun = true;
    private DisplayResultConfig drcFile;
    private boolean bFile = false;
    private boolean bFileReceived = false;
    private int intReceived = 0;
    private Socket socketReceive;
    private final LinkedBlockingQueue<String> lbqToHandle = new LinkedBlockingQueue<>();
    //private final LinkedBlockingQueue<Character> lbqChar = new LinkedBlockingQueue<>();
    SocketRecv(Socket sckIRC) throws IOException {
        socketReceive = sckIRC;

        start();
    }
    SocketRecv(Socket sckIRC, DisplayResultConfig drcFile) throws IOException {

        socketReceive = sckIRC;
        this.drcFile = drcFile;
        this.bFile = true;
        //brRecv = new BufferedReader(new InputStreamReader(sckIRC.getInputStream()));
        start();
    }

    boolean isbFileReceived() {
        return bFileReceived;
    }

    int getIntReceived() {
        return intReceived;
    }

    LinkedBlockingQueue<String> getLbqToHandle() {
        return lbqToHandle;
    }

    /*public LinkedBlockingQueue<Character> getLbqChar() {
        return lbqChar;
    }*/

    @Override
    public void run() {
        System.out.println("Starting receiver");

        try {
            BufferedReader brRecv = new BufferedReader(new InputStreamReader(socketReceive.getInputStream(), Charset.forName("ISO-8859-1")));

            while (bRun) {
                //System.out.println("Running receiver");
                try {
                    if (brRecv.ready()) {
                        if (bFile) {
                            InputStream is=socketReceive.getInputStream();
                            //lbqToHandle.put("STARTING DCC RECEIVE " + drcFile.getStrFilename());
                            receiveFile(is);
                        } else {
                            String strRead = brRecv.readLine(); // ISO-8859-1 UTF-8  , Charset.forName("Windows-1252")  Charset.forName("UTF-8")
                            if (!strRead.equals("")) {
                                lbqToHandle.put(strRead);
                                //System.out.println("Read not null: " + strRead);
                            }
                        }

                    } else Thread.sleep(50);
                } catch (IOException ex) {
                    bRun = false;
                    System.out.println("IOException: " + ex);
                    ex.printStackTrace();
                } catch (InterruptedException ex) {
                    bRun = false;
                    System.out.println("InterruptedException: " + ex);
                    ex.printStackTrace();
                }
            }
        }
        catch (IOException ex) {
            bRun = false;
            System.out.println("IOException: " + ex);
            ex.printStackTrace();
        }
    }

    void close() {
        System.out.println("RECV CLOSE");
        bRun = false;
        lbqToHandle.clear();
        try {
            //brRecv.close();
            socketReceive.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void receiveFile(InputStream is)
            throws IOException {
        //Log.i("IMSERVICE", "FILERECCC-1");


        if (is!= null) {
            FileOutputStream fos;
            BufferedOutputStream bos;
            try {
                fos = new FileOutputStream(GlobalConfig.getStrDlFolder() + drcFile.getStrFilename(), true);
                bos = new BufferedOutputStream(fos);
                byte[] aByte = new byte[1024];
                int bytesRead;

                while (((bytesRead = is.read(aByte)) != -1) && (!bFileReceived)) {
                    bos.write(aByte, 0, bytesRead);
                    intReceived += bytesRead;
                    if (intReceived >= drcFile.getlRealsize()){
                        lbqToHandle.add("BYTES " + intReceived + " / " + drcFile.getlRealsize());
                        bFileReceived = true;
                        bRun = false;
                    }
                }
                bos.flush();
                bos.close();
                //Log.i("IMSERVICE", "FILERECCC-2");

            } catch (IOException ex) {
                Looper.prepare();
                UI.dialogSettingsInvalid(UI.getmContext().get().getString(R.string.file_title_opfailed),
                        ex.toString());
                System.out.println("IOException: " + ex);
                bRun = false;
            }
        }
    }
}

