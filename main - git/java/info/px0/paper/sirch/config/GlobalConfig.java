package info.px0.paper.sirch.config;

import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.px0.paper.sirch.BuildConfig;
import info.px0.paper.sirch.R;
import info.px0.paper.sirch.UI;
import info.px0.paper.sirch.irc.DCCHandler;
import info.px0.paper.sirch.irc.IRCHandler;

public class GlobalConfig {
    private final static ArrayList<ServerConfig> scServers = new ArrayList<>();
    private final static ArrayList<IRCHandler> ircHandles = new ArrayList<>();
    private final static ArrayList<DCCHandler> dccHandlers = new ArrayList<>();
    public final static String[] strValidExtensions = {".mp3",".aac",".ogg",".wma"}; //($ config? ex. add video files etc...)
    private static String strNickname = "";
    private static String strAltNickname = "";
    private static String strDlFolder = "";
    private static boolean bHdlStarted = false;
    //TODO:option to remove toasts ex: search too fast etc.

    public static void startHandlers() {
        //for each server in config start a new handler.
        for (ServerConfig scLoop : scServers) {
            ircHandles.add(new IRCHandler(scLoop, UI.getHdlMain()));
            UI.setbInitConf(false);
            UI.addLine("Connecting to: " + scLoop.getStrHostname() + ":" + scLoop.getiHostPort());
        }
        bHdlStarted = true;
    }

    public static boolean isNickValid(String strNick) {
        if (strNick.compareTo("") == 0) return false;
        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher = pattern.matcher(strNick);
        if (matcher.find()) return false;
        String patternAN = "^[a-zA-Z0-9]*$";
        return strNick.matches(patternAN);
    }

    public static ArrayList<ServerConfig> getScServers() {
        return scServers;
    }

    public static ArrayList<IRCHandler> getIrcHandles() {
        return ircHandles;
    }

    public static ArrayList<DCCHandler> getDccHandlers() {
        return dccHandlers;
    }

    public static void updateHdl (Handler hdlMain)
    {
        IRCHandler.setHdlMain(hdlMain);
        DCCHandler.setHdlDl(hdlMain);
    }

    public static void addServer(ServerConfig scServer) {
        scServers.add(scServer);
    }

    public static void newDccHandler (DisplayResultConfig drcResult) {
        dccHandlers.add(new DCCHandler(UI.getHdlMain(),drcResult));
    }

    public static void addRequest(ResultConfig rcRequest) {
        for (IRCHandler ihFind : ircHandles)
            if(ihFind.getScServer().findResult(rcRequest.getStrUUID()))
                ihFind.addRequest(rcRequest);
    }

    public static boolean isbHdlStarted() {
        return bHdlStarted;
    }

    public static void setbHdlStarted(boolean bHdlStarted) {
        GlobalConfig.bHdlStarted = bHdlStarted;
    }

    public static String getStrNickname() {
        return strNickname;
    }

    public static void setStrNickname(String strNickname) {
        GlobalConfig.strNickname = strNickname;
    }

    public static String getStrAltNickname() {
        return strAltNickname;
    }

    public static void setStrAltNickname(String strAltNickname) {
        GlobalConfig.strAltNickname = strAltNickname;
    }

    public static String getStrDlFolder() {
        return strDlFolder;
    }

    public static void setStrDlFolder(String strDlFolder) {
        GlobalConfig.strDlFolder = strDlFolder;
    }

    public static void clear() {
        for (ServerConfig scLoop : scServers)
            scLoop.clear();
        scServers.clear();
        for (IRCHandler ihLoop : ircHandles)
            ihLoop.close();
        ircHandles.clear();
    }
}
