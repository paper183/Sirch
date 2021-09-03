package info.px0.paper.sirch.irc;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.*;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.px0.paper.sirch.R;
import info.px0.paper.sirch.UI;
import info.px0.paper.sirch.config.DisplayResultConfig;
import info.px0.paper.sirch.config.GlobalConfig;
import info.px0.paper.sirch.config.ResultConfig;
import info.px0.paper.sirch.config.RoomConfig;
import info.px0.paper.sirch.config.ServerConfig;
import info.px0.paper.sirch.irc.RawEnum.RawVal;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class IRCHandler extends Thread {

    private Socket sSocket;
    private SocketSend ssSender;
    private SocketRecv srReceiver;
    private static Handler hdlMain;
    private boolean bClose = false;
    private boolean bAltNick = true;
    private final ServerConfig scServer;
    private LinkedBlockingQueue<String> lbqSend = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<ResultConfig> lbqRequested = new LinkedBlockingQueue<>();

    public IRCHandler(ServerConfig scServer, Handler hdlMain) {
        super();

        IRCHandler.hdlMain = hdlMain;
        this.scServer = scServer;

        start();
    }

    public void addRequest(ResultConfig rcRequest) {
        lbqRequested.add(rcRequest);
        hdlMsg("REQUESTED " + rcRequest.toString());
    }

    public LinkedBlockingQueue<ResultConfig> getLbqRequested() {
        return lbqRequested;
    }

    public void setLbqRequested(LinkedBlockingQueue<ResultConfig> lbqRequested) {
        this.lbqRequested = lbqRequested;
    }

    public static void setHdlMain(Handler hdlMain) {
        IRCHandler.hdlMain = hdlMain;
    }

    public static Handler getHdlMain() {
        return hdlMain;
    }

    public ServerConfig getScServer() {
        return scServer;
    }

    public LinkedBlockingQueue<String> getLbqSend() {
        return lbqSend;
    }

    public void setLbqSend(LinkedBlockingQueue<String> lbqSend) {
        this.lbqSend = lbqSend;
    }

    @Override
    public void run() {
        try {

            sSocket = new Socket(scServer.getStrHostname(), scServer.getiHostPort());

            ssSender = new SocketSend(sSocket, lbqSend);
            srReceiver = new SocketRecv(sSocket);

            // TODO Verify full USER command syntax
            lbqSend.put("USER " + scServer.getStrNickname() + " 0 * :" + scServer.getStrNickname());
            lbqSend.put("NICK " + scServer.getStrNickname());

            while (!bClose) {

                try {
                    boolean bSleep = true;
                    if (!srReceiver.getLbqToHandle().isEmpty()) {
                        String strToParse = srReceiver.getLbqToHandle().take();
                        if ((strToParse != null) && (!strToParse.equals(""))) {
                            //hdlMsg(this.getId() + ": " + strToParse);
                            parse(strToParse);
                            bSleep = false;
                        }
                    }
                    if ((!lbqRequested.isEmpty()) && (scServer.getIsConnected())) {
                        ResultConfig rcRequest = lbqRequested.take();
                        String strRoom = scServer.findUserRoom(rcRequest.getStrNickname()); //TODO: pick first room where nick is found and self is allowed to talk (room=+m|M or banned user>=+v)
                        lbqSend.add("PRIVMSG " + strRoom + " :!" + rcRequest.getStrNickname() + " " + rcRequest.getStrFilename());
                        Log.d("Sirch","PRIVMSG " + strRoom + " :!" + rcRequest.getStrNickname() + " " + rcRequest.getStrFilename());
                        bSleep = false;
                    }
                    if (bSleep) Thread.sleep(50);
                } catch (InterruptedException e) {
                    bClose = true;
                    scServer.setIsConnected(false);
                    e.printStackTrace();
                }
            }


        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            scServer.setIsConnected(false);
            e1.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            scServer.setIsConnected(false);
            e.printStackTrace();
        }
    }

    private String remFormat (String strFormatted) {
        return strFormatted.replaceAll("[\u0001\u0002\u001f\u0016\u000f]", "");
    }

    private String remColors (String strColored)
    {
        return strColored.replaceAll("\u0003[0-9]{1,2}(,[0-9]{1,2})?", "");
    }

    private String remFormatColors (String strColored)
    {
        return remColors(remFormat(strColored));
    }

    private void parse(String strToParse) {
        try {

            strToParse = remFormatColors(strToParse);

            /* String[] strLines = strToParse.split("\\r?\\n");
            //for (String strLine : strLines) {*/ // Seems unnecessary, tested, same results without

            //Split the string into tokens or words from the space character
            String[] strWords = strToParse.split("\\s+");
            if (strWords.length > 1) {
                Integer iRaw;

                try {
                    iRaw = Integer.parseInt(strWords[1]);
                } catch (NumberFormatException e) {
                    //No raw code to treat
                    iRaw = 0;
                }

                //hdlMsg("|||TEST||| " + iRaw + " " + strToParse); //Test if each line is correctly parsed, see comment about splitting \r\n

                if (strWords[0].compareToIgnoreCase("PING") == 0)
                    lbqSend.put("PONG " + strWords[1]);
                else if (RawVal.forValue(iRaw) != null) { // EDIT (iRaw > 0 && iRaw < 999 && RawVal.forValue(iRaw) != null)
                    //hdlMsg("||||| " + iRaw + " " + strToParse);

                    switch (RawVal.forValue(iRaw)) {

                        case RplMotdEnd:
                        case ErrNoMotd:
                            scServer.setbMotd(true);
                            scServer.setIsConnected(true);
                            scServer.setStrServername(strWords[0]);
                            scServer.setStrMyNick(strWords[2]);
                            for (RoomConfig itrRooms : scServer.getRcRooms())
                                lbqSend.put("JOIN " + itrRooms.getStrRoomName());

                            hdlMsg("Connected to: " + scServer.getStrHostname() + "(" + scServer.getStrServername() + ")", true);

                            break;

                        case RplNamReply:
                            String[] strAllUsers = strToParse.split(":");
                            String[] strUserparse = strAllUsers[2].split("\\s+");

                            for (String strToAdd : strUserparse) {

                                if ((strToAdd.charAt(0) == '@') || (strToAdd.charAt(0) == '+') || (strToAdd.charAt(0) == '%') || (strToAdd.charAt(0) == '&') || (strToAdd.charAt(0) == '~')) {
                                    int i = 0;
                                    String strModes = "";
                                    while ((strToAdd.charAt(i) == '@') || (strToAdd.charAt(i) == '+') || (strToAdd.charAt(i) == '%') || (strToAdd.charAt(i) == '&') || (strToAdd.charAt(i) == '~')) {
                                        strModes = strModes + strToAdd.charAt(i);
                                        i++;
                                    }
                                    char[] cModes = strModes.toCharArray();
                                    scServer.addUser(strWords[4], strToAdd.substring(i), cModes);
                                }
                                else
                                    scServer.addUser(strWords[4], strToAdd);
                            }
                            //hdlMsg("DEBUG: " + strWords[4] + " " + scServer.listUsers(strWords[4]));
                            //hdlMsg("DEBUG: " + strWords[4] + " " + scServer.listUsersMode(strWords[4]));

                            break;

                        case RplEndOfNames:
                            //scServer.joinedRoom(strWords[3]); changed to JOIN when nick = strMyNick
                            break;

                        case ErrCannotSendToChan:
                            scServer.leftRoom(strWords[3]);
                            break;

                        case ErrNoNicknameGiven:
                        case ErrErroneusNickname:
                        case ErrNickNameInUse:
                            if (bAltNick) { //avoids flooding alt nickname
                                bAltNick = false;
                                lbqSend.put("NICK " + scServer.getStrAltNick());
                            } else {
                                hdlMsg(UI.getmContext().get().getString(R.string.err_nickname), true);
                                bAltNick = true;
                                close();
                            }
                            break;

                        default:
                            break;
                    }

                }
                else {

                    String strCmdUser = strWords[0];
                    if ((strCmdUser.charAt(0) == ':')
                            && (strWords[0].indexOf('!') >= 0)
                            && (strWords[0].indexOf('@') >= 0)) {
                        strCmdUser = strCmdUser.split(":")[1].split("!")[0];
                    }

                    if (strWords[1].compareToIgnoreCase("JOIN") == 0)
                    {
                        if (strCmdUser.compareToIgnoreCase(scServer.getStrMyNick()) != 0)
                            scServer.addUser(strWords[2].replaceAll(":",""),strCmdUser);
                        else {
                            String sRoomname = strWords[2].replaceAll(":","");
                            scServer.joinedRoom(sRoomname);
                            hdlMsg("Joined: (" + scServer.getStrServername().replaceAll(":","") + ") " + sRoomname, true);
                            RoomConfig rcDelayedSearch = scServer.getRoomConf(sRoomname);
                            if (rcDelayedSearch != null) {
                                String sDelayedSearch = rcDelayedSearch.getStrDelayedSearch();
                                if ((sDelayedSearch != null) && (sDelayedSearch.compareToIgnoreCase("") != 0))
                                    lbqSend.put("PRIVMSG " + rcDelayedSearch.getStrRoomName() + " :" + rcDelayedSearch.getStrFindCmd() + " " + rcDelayedSearch.getStrDelayedSearch());
                            }
                        }
                    }
                    else if (strWords[1].compareToIgnoreCase("PART") == 0)
                    {
                        if (strCmdUser.compareToIgnoreCase(scServer.getStrMyNick()) != 0)
                            scServer.delUser(strWords[2],strCmdUser);
                        else scServer.leftRoom(strWords[2]);
                    }
                    else if (strWords[1].compareToIgnoreCase("NICK") == 0)
                    {
                        if (strCmdUser.compareToIgnoreCase(scServer.getStrMyNick()) != 0)
                            scServer.replaceUser(strCmdUser,strWords[2].replaceAll(":",""));
                        else scServer.setStrMyNick(strWords[2].replaceAll(":",""));
                    }
                    else if (strWords[1].compareToIgnoreCase("QUIT") == 0)
                    {
                        if (strCmdUser.compareToIgnoreCase(scServer.getStrMyNick()) != 0)
                            scServer.quitUser(strCmdUser);
                        else scServer.selfQuit();
                    }
                    else if (strWords[1].compareToIgnoreCase("KICK") == 0)
                    {
                        String strKicked = strWords[3];
                        if (strKicked.compareToIgnoreCase(scServer.getStrMyNick()) != 0)
                            scServer.delUser(strWords[2],strKicked);
                        else scServer.leftRoom(strWords[2]);
                        //hdlMsg("DEBUG: " + scServer.listUsers(strWords[2]));
                    }
                    else if (strWords[1].compareToIgnoreCase("MODE") == 0)
                    {
                        if (Array.getLength(strWords) > 4){ //:serv!sada@Clk-C05BD492 MODE #mp3 -vvv bot PapCell Paper
                            char[] cModes = strWords[3].substring(1).toCharArray();
                            int i = 0;
                            for (char cMode : cModes) {
                                if ((strWords[3].charAt(0) == '+') && (4 + i < Array.getLength(strWords)))
                                    scServer.addMode(strWords[2],strWords[4 + i],cMode);
                                else if ((strWords[3].charAt(0) == '-') && (4 + i < Array.getLength(strWords)))
                                    scServer.remMode(strWords[2],strWords[4 + i],cMode);
                                i++;
                            }
                            //hdlMsg("DEBUG MODE: " + scServer.listUsersMode(strWords[2]));
                        }
                    }

                    else if (strWords[1].compareToIgnoreCase("PRIVMSG") == 0)
                    {
                        //DONE : parse replies (Only from Voiced, default config)
                        /*
                        :serv!sada@Clk-C05BD492 PRIVMSG bot :!serv 02 Wordplay.mp3 ::INFO:: 5.0MB  OmenServe v2.71 
                        :serv!sada@Clk-C05BD492 PRIVMSG bot :!serv 03 Geek in the Pink.mp3 ::INFO:: 6.3MB  OmenServe v2.71 
                         */

                        /*hdlMsg("DEBUG PRIVMSG " + strWords[2].compareToIgnoreCase(scServer.getStrMyNick()) + " " + scServer.isVoice(strCmdUser) + " "
                                + strWords[3].substring(1).compareToIgnoreCase("!" + strCmdUser) + " " + strWords[3].substring(1));*/

                        if ((strWords[2].compareToIgnoreCase(scServer.getStrMyNick()) == 0)
                                && (scServer.isVoice(strCmdUser))) {
                            if (strWords[3].substring(1).compareToIgnoreCase("!" + strCmdUser) == 0) {
                                //hdlMsg("DEBUG FIRSTIF ");

                                String strFilename = strToParse.split(":")[2];
                                String strFilesize = "";
                                String strValidExt = checkValidExtension(strFilename);

                                //verify if valid file extension is found in message and stop at first found
                                if (strValidExt.compareToIgnoreCase("") != 0) {

                                    Pattern patSize = Pattern.compile("\\b[0-9]+([.,][0-9]+)?\\s?[gkm]b\\b", Pattern.CASE_INSENSITIVE);
                                    Matcher matchSize = patSize.matcher(strToParse);
                                    if (matchSize.find())
                                        strFilesize = strToParse.substring(matchSize.start(), matchSize.end());

                                    strFilename = strFilename.substring(strCmdUser.length() + 2, strFilename.indexOf(strValidExt) + strValidExt.length());

                                    if (!scServer.bFindResult(strCmdUser, strFilename)) {
                                        ResultConfig newResult = new ResultConfig(strCmdUser, strFilename, strFilesize);
                                        newResult.setStrRooms(scServer.getUserRooms(strCmdUser));
                                        scServer.addResult(newResult);
                                        hdlResult(newResult);
                                    }


                                    //hdlMsg("ADD RESULT: '" + strCmdUser + "' '" + strFilename + "' '" + strFilesize + "'"); //debug
                                }

                                //debug
                                /*for (ResultConfig rcResFound : scServer.findResult(strCmdUser, strFilename))
                                    hdlMsg("FOUND RESULT: " + rcResFound.toString());*/
                                /*for (String strResFound : scServer.findResult("", ""))
                                    hdlMsg("FOUND ALL: " + strResFound);*/
                            }
                            //  :Mr_Mp3!Mr_Mp3@207.253.88.90 PRIVMSG Sirch :DCC SEND Twenty_One_Pilots_-_06_-_Forest.mp3                          3489486938 1024 3932288
                            //:Toke_N_D!jdavidson@cpe-98.com PRIVMSG Sirch :DCC SEND "Twenty One Pilots - Blurryface - 05 - Tear In My Heart.mp3" 4294967295 0 7678955 127
                            else if ((strWords[3] + " " + strWords[4]).compareToIgnoreCase(":DCC SEND") == 0)
                            {
                                hdlMsg("DCC: " + strToParse);

                                int intWords = Array.getLength(strWords);
                                if (intWords >= 9) {
                                    String strFilename;
                                    long lAddr;
                                    int iPort;
                                    long lFilesize;

                                    if (strWords[5].charAt(0) == '"') {
                                        String strSplit[] = strToParse.split("\"");
                                        strFilename = strSplit[1];
                                        strSplit = strSplit[2].split("\\s+");
                                        lAddr = Long.parseLong(strSplit[1]);
                                        iPort = Integer.parseInt(strSplit[2]);
                                        lFilesize = Long.parseLong(strSplit[3]);
                                        hdlMsg("DCCQUOTES: " + strFilename + " " + lAddr + " " + iPort + " " + lFilesize);
                                    } else {
                                        strFilename = strWords[5];
                                        lAddr = Long.parseLong(strWords[6]);
                                        iPort = Integer.parseInt(strWords[7]);
                                        lFilesize = Long.parseLong(strWords[8]);
                                        hdlMsg("DCCNOQ: " + strFilename + " " + lAddr + " " + iPort + " " + lFilesize);
                                    }

                                    String strValidExt = checkValidExtension(strFilename);

                                    if (strValidExt.compareToIgnoreCase("") != 0) {
                                        hdlMsg("Finding: " + strCmdUser + " " + strFilename);
                                        for (ResultConfig rcLoop : scServer.findResult(strCmdUser, strFilename)) {
                                            int iSt = 2;
                                            if (iPort == 0) iSt = 88;
                                            DisplayResultConfig drcLoop = new DisplayResultConfig(rcLoop, lFilesize, 1, lAddr, iPort, iSt);

                                            drcLoop.setStrFilename(strFilename);
                                            //hdlMsg((drcLoop.isbReverseDCC() ? "REVERSE " : "") + "DCC: " + drcLoop);
                                            hdlDownload(drcLoop);
                                        }
                                    }
                                }
                            }
                            //:Paper!pap@50F1CC57.7ED1203C.BE652884.IP PRIVMSG bot : Request Denied  I Don't Have pink-11-here.mp3 Check Your Spelling Or Get My Newest List  OmenServe v2.71
                            else if (strToParse.toLowerCase().contains("request denied")) {

                                String strFilename = strToParse.split(":")[2];
                                String strExtra = "";
                                String strValidExt = checkValidExtension(strFilename);

                                if (strValidExt.compareToIgnoreCase("") != 0) {
                                    strExtra = strFilename.substring(strFilename.indexOf(strValidExt) + strValidExt.length(), strFilename.length());
                                    strFilename = strFilename.substring(0, strFilename.indexOf(strValidExt) + strValidExt.length());

                                    for (ResultConfig rcLoop : scServer.getResResults()) { //findResult(strCmdUser, strFilename)
                                        if ((rcLoop.getStrNickname().compareToIgnoreCase(strCmdUser) == 0) && (strFilename.toLowerCase().contains(rcLoop.getStrFilename().toLowerCase()))) {

                                            DisplayResultConfig drcDenied = new DisplayResultConfig(rcLoop, 6, strExtra);
                                            hdlMsg("DRCDENIED: " + strFilename + " EXTRA: " + strExtra);
                                            hdlDownload(drcDenied);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else if (strWords[1].compareToIgnoreCase("NOTICE") == 0)
                    {
                        //:Paper!pap@50F1CC57.7ED1203C.BE652884.IP NOTICE bot :DCC Send Paper-default(2017-06-10)-OS.zip (192.168.183.152)
                        //:Paper!pap@50F1CC57.7ED1203C.BE652884.IP NOTICE bot : Request Accepted  List Has Been Placed In The Priority Queue At Position 1  OmenServe v2.71 
                        //:Paper!pap@50F1CC57.7ED1203C.BE652884.IP NOTICE bot : Request Accepted  File: pink-11-here.mp3  Queue Position: 1  Allowed: 1 of 2  Min CPS: 50  OmenServe v2.71 

                        //:Paper!pap@50F1CC57.7ED1203C.BE652884.IP NOTICE PapCell : Request Accepted � File: 01. My Chemical Romance - Look Alive, Sunshine.mp3 � Queue Position: 1 � Allowed: 1 of 2 � Min CPS: 50 � OmenServe v2.71 �
                        if ((strWords[2].compareToIgnoreCase(scServer.getStrMyNick()) == 0)
                                && (scServer.isVoice(strCmdUser))) {
                            if (strToParse.toLowerCase().contains("request accepted")) {

                                String strFilesearch[] = strToParse.split("File: ");
                                String strFilename = "";
                                if (Array.getLength(strFilesearch) >= 2) strFilename = strFilesearch[1];
                                String strValidExt = checkValidExtension(strFilename);

                                if (strValidExt.compareToIgnoreCase("") != 0) {
                                    strFilename = strFilename.substring(0, strFilename.indexOf(strValidExt) + strValidExt.length());
                                    String strExtra = "";
                                    String strPos[] = strToParse.toLowerCase().split("position: ");
                                    if (Array.getLength(strPos) >= 2) {
                                        strExtra = strPos[1];
                                        int iPosition = Integer.parseInt(strExtra.substring(0, 2).trim());
                                        strExtra = "Position: " + iPosition;
                                    }

                                    for (ResultConfig rcLoop : scServer.findResult(strCmdUser, strFilename)) {
                                        DisplayResultConfig drcUpdate = new DisplayResultConfig(rcLoop, 5, strExtra);
                                        hdlMsg("DRCUPDATE: " + drcUpdate);
                                        hdlDownload(drcUpdate);
                                    }
                                }

                                hdlMsg("ACCEPTED \"" + strFilename + "\"");
                            }
                        }
                    }

                    /*scServer.addResult("a","b"); //Debug unique id generation
                    hdlMsg("DEBUG UID: " + scServer.listUID());*/
                }
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String checkValidExtension (String strFilename) {
        for (String strExt : GlobalConfig.strValidExtensions)
            if (strFilename.indexOf(strExt) > 0)
                return strExt;
        return "";
    }

    private static void hdlMsg(String strMsg) {
        Message msgMain = hdlMain.obtainMessage(1);
        msgMain.obj = strMsg;
        hdlMain.sendMessage(msgMain);
    }

    private static void hdlMsg(String strMsg, boolean isToast) {
        if (!isToast) hdlMsg(strMsg);
        else {
            Message msgMain = hdlMain.obtainMessage(4);
            msgMain.obj = strMsg;
            hdlMain.sendMessage(msgMain);
        }
    }

    private static void hdlResult(ResultConfig rcResult) {
        Message msgResult = hdlMain.obtainMessage(2);
        msgResult.obj = rcResult;
        hdlMain.sendMessage(msgResult);
    }

    private static void hdlDownload(DisplayResultConfig drcResult) {
        Message msgResult = hdlMain.obtainMessage(3);
        msgResult.obj = drcResult;
        hdlMain.sendMessage(msgResult);
    }
    public void close() {
        System.out.println("IRCHANDLER CLOSE");
        bClose = true;
        scServer.setIsConnected(false);
        ssSender.close();
        srReceiver.close();
        scServer.clear();
        lbqSend.clear();
        try {
            sSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
