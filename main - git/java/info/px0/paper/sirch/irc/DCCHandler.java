package info.px0.paper.sirch.irc;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import info.px0.paper.sirch.config.DisplayResultConfig;
import info.px0.paper.sirch.config.ResultConfig;

/**
 * Created by Paper on 24/08/2016.
 */
public class DCCHandler extends Thread {

    //private Socket sSocket;
    //private SocketSend ssSender;
    //private SocketRecv srReceiver;
    private LinkedBlockingQueue<String> lbqSend = new LinkedBlockingQueue<>();
    private static Handler hdlDl;
    private int intReceived = 0;
    private boolean bClose = false;

    private final DisplayResultConfig drcResult;

    public DCCHandler (Handler hdlDl, DisplayResultConfig drcResult) {
        super();

        DCCHandler.hdlDl = hdlDl;
        this.drcResult = drcResult;
        start();
    }

    @Override
    public void run() {
        try {
            long lAddress = drcResult.getlAddress();
            String strIP = (lAddress >> 24 & 0xff) + "." + (lAddress >> 16 & 0xff) + "." + (lAddress >> 8 & 0xff) + "." + (lAddress & 0xff);

            hdlMsg("&Connecting to: " + strIP + " " + drcResult.getiPort());
            Socket sSocket = null;
            SocketRecv srReceiver = null;

            if ((drcResult.getiPort() > 65535) || (drcResult.getiPort() == 0) || (strIP.compareToIgnoreCase("0.0.0.0") == 0) || (strIP.compareToIgnoreCase("255.255.255.255") == 0)) bClose = true;
            else {
                try {
                    sSocket = new Socket(strIP, drcResult.getiPort());

                    //ssSender = new SocketSend(sSocket, lbqSend);
                    srReceiver = new SocketRecv(sSocket, drcResult);
                } catch (IOException ex) {
                    Log.d("Sirch", "Error: " + ex);
                    drcResult.setiStatus(99);
                    hdlDownload(drcResult);
                }
            }



            while ((!bClose) && (sSocket != null) && (srReceiver != null)) {

                try {
                    boolean bSleep = true;
                    if (!srReceiver.getLbqToHandle().isEmpty()) {
                        String strToParse = srReceiver.getLbqToHandle().take();

                        hdlMsg(this.getId() + " DCC DOWNLOAD: " + strToParse);

                        bSleep = false;
                    }
                    if ((srReceiver.isbFileReceived()) && (srReceiver.getLbqToHandle().isEmpty())) {
                        hdlMsg(this.getId() + " DCC DOWNLOAD FINISHED: " + drcResult.getStrFilename());
                        drcResult.setiStatus(4);
                        drcResult.setiProgress(100);
                        hdlDownload(drcResult);
                        srReceiver.close();
                        //ssSender.close();
                        sSocket.close();
                        bClose = true;
                    }
                    //hdlMsg(" DCC DOWNLOAD: ");
                    if (bSleep) {
                        if (intReceived != srReceiver.getIntReceived()){
                            if (intReceived == 0)
                                drcResult.setiStatus(3);
                            intReceived = srReceiver.getIntReceived();
                            int intPercent = intReceived*100/(int)drcResult.getlRealsize();
                            if (intPercent != drcResult.getiProgress()) {
                                drcResult.setiProgress(intPercent);
                                hdlDownload(drcResult);
                            }
                        }
                        Thread.sleep(50);
                    }
                } catch (InterruptedException e) {
                    bClose = true;
                    drcResult.setiStatus(99);
                    hdlDownload(drcResult);
                    e.printStackTrace();
                }
            }


        } catch (IOException e) {
            // TODO Auto-generated catch block
            drcResult.setiStatus(99);
            hdlDownload(drcResult);
            e.printStackTrace();
        }
    }

    private static void hdlMsg(String strMsg) {
        Message msgMain = hdlDl.obtainMessage(1);
        msgMain.obj = strMsg;
        hdlDl.sendMessage(msgMain);
    }

    private static void hdlDownload(DisplayResultConfig drcResult) {
        Message msgResult = hdlDl.obtainMessage(3);
        msgResult.obj = drcResult;
        hdlDl.sendMessage(msgResult);
    }

    public static Handler getHdlDl() {
        return hdlDl;
    }

    public static void setHdlDl(Handler hdlDl) {
        DCCHandler.hdlDl = hdlDl;
    }
}

/*
    RAW::: :serv!~sada@cable-173.246.6-37.ebox.ca NOTICE paperbot :DCC Send readme.txt (173.246.6.37)
    :serv!~sada@cable-173.246.6-37.ebox.ca NOTICE paperbot :DCC Send readme.txt (173.246.6.37)
    RAW::: :serv!~sada@cable-173.246.6-37.ebox.ca PRIVMSG paperbot :DCC SEND readme.txt 2918581797 4001 1519
    <paperbot><serv!~sada@cable-173.246.6-37.ebox.ca>:DCC SEND readme.txt 2918581797 4001 1519

    :serv!sada@Clk-C05BD492 NOTICE Paper :DCC Send Kid Cudi - Pursuit of Happiness.mp3 (192.168.183.182)
    :serv!sada@Clk-C05BD492 PRIVMSG Paper :DCC SEND Kid_Cudi_-_Pursuit_of_Happiness.mp3 3232282550 8881 4079744


    Size reported by irc client whisper 3.9mb = 4089446 bytes
    Size reported by irc client dcc send = 4079744 bytes
    Need to find acceptable % of size discrepancy

    4079744 - 4089446 = -9702
    4079744 - |-9702| = 4070042
    4070042*100 / 4079744 = 99.762190961001474602327989207166

    3.89mb = 4078960.64 bytes -> 4078961 bytes
    4079744 - 4078961 = 783
    4079744 - |783| = 4078961
    4078961*100 / 4079744 = 99.980807619301603237850218052897

    4.37GB *(1024*3)= 4692251771 bytes (real 4694691169)
    4694691169 - 4692251771 = 2439398
    4694691169 - |2439398| = 4692251771
    4692251771*100 / 4694691169 = 99.948039223195173288298572638229

    63.6kb * 1024 = 65126.4
    65149 - 64126 = 1023
    64126 * 100 / 65149 = 98.429753334663617246619288093447


*/