package info.px0.paper.sirch.config;

import java.util.ArrayList;
import java.util.Random;
//import java.util.Vector;

public class ServerConfig {
    private boolean bMotd = false;
    private boolean isConnected = false;
    private String strHostname;
    private String strServername;
    private int iHostPort;
    private String strNickname;
    private String strAltNick;
    private String strMyNick;
    private final ArrayList<RoomConfig> rcRooms = new ArrayList<>();
    private final ArrayList<ResultConfig> resResults = new ArrayList<>();

    public ServerConfig(String strHostname, int iHostPort) {
        super();
        this.strMyNick = "";
        this.strServername = "";
        this.strHostname = strHostname;
        this.iHostPort = iHostPort;
        this.strNickname = GlobalConfig.getStrNickname();
        this.strAltNick = GlobalConfig.getStrAltNickname();
    }

    //Variable Arguments Constructor
    public ServerConfig(String strHostname, int iHostPort, RoomConfig ... rcRoomsArgs) {
        super();
        this.strMyNick = "";
        this.strServername = "";
        this.strHostname = strHostname;
        this.iHostPort = iHostPort;
        this.strNickname = GlobalConfig.getStrNickname();
        this.strAltNick = GlobalConfig.getStrAltNickname();
        for (RoomConfig rcArg : rcRoomsArgs) {
            rcRooms.add(rcArg);
        }

    }
    public boolean getbMotd() {
        return bMotd;
    }
    public void setbMotd(boolean bMotd) {
        this.bMotd = bMotd;
    }
    public boolean getIsConnected() {
        return isConnected;
    }
    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }
    public String getStrNickname() {
        return strNickname;
    }
    public void setStrNickname(String strNickname) {
        this.strNickname = strNickname;
    }
    public String getStrAltNick() {
        return strAltNick;
    }
    public void setStrAltNick(String strAltNick) {
        this.strAltNick = strAltNick;
    }
    public String getStrHostname() {
        return strHostname;
    }
    public void setStrMyNick(String strMyNick) {
        this.strMyNick = strMyNick;
    }
    public String getStrMyNick() {
        return strMyNick;
    }
    public void setStrServername(String strServername) {
        this.strServername = strServername;
    }
    public String getStrServername() {
        return strServername;
    }
    public void setStrHostname(String strHostname) {
        this.strHostname = strHostname;
    }
    public int getiHostPort() {
        return iHostPort;
    }
    public void setiHostPort(int iHostPort) {
        this.iHostPort = iHostPort;
    }
    public ArrayList<RoomConfig> getRcRooms() {
        return rcRooms;
    }
    public ArrayList<ResultConfig> getResResults() {
        return resResults;
    }

    public void addRoom(RoomConfig rcRoom) {
        rcRooms.add(rcRoom);
    }

    public void joinedRoom(String strRoom)
    {
        for (RoomConfig itrRoom : rcRooms)
            if (itrRoom.getStrRoomName().compareToIgnoreCase(strRoom) == 0)
                itrRoom.setbJoined(true);
    }

    public void leftRoom(String strRoom)
    {
        for (RoomConfig itrRoom : rcRooms)
            if (itrRoom.getStrRoomName().compareToIgnoreCase(strRoom) == 0) {
                itrRoom.setbJoined(false);
                itrRoom.clearUsers();
            }
    }

    public void selfQuit()
    {
        for (RoomConfig itrRoom : rcRooms) {
            itrRoom.setbJoined(false);
            itrRoom.clearUsers();
        }
        setIsConnected(false);
    }

    public void addUser(String strRoom, String strUser)
    {
        for (RoomConfig itrRoom : rcRooms)
            if (itrRoom.getStrRoomName().compareToIgnoreCase(strRoom) == 0)
                itrRoom.addUser(strUser);
    }

    public void addUser(String strRoom, String strUser, char cMode)
    {
        for (RoomConfig itrRoom : rcRooms)
            if (itrRoom.getStrRoomName().compareToIgnoreCase(strRoom) == 0)
                itrRoom.addUser(strUser,cMode);
    }

    public void addUser(String strRoom, String strUser, char[] cMode)
    {
        for (RoomConfig itrRoom : rcRooms)
            if (itrRoom.getStrRoomName().compareToIgnoreCase(strRoom) == 0)
                itrRoom.addUser(strUser,cMode);
    }

    public void delUser(String strRoom, String strUser)
    {
        for (RoomConfig itrRoom : rcRooms)
            if (itrRoom.getStrRoomName().compareToIgnoreCase(strRoom) == 0)
                itrRoom.delUser(strUser);
    }

    public void quitUser(String strUser)
    {
        for (RoomConfig itrRoom : rcRooms)
            itrRoom.delUser(strUser);
    }

    public void addMode(String strRoom, String strUser, char cMode)
    {
        for (RoomConfig rcRoom : rcRooms)
            if (rcRoom.getStrRoomName().compareToIgnoreCase(strRoom) == 0)
                rcRoom.addMode(strUser,cMode);
    }

    public void remMode(String strRoom, String strUser, char cMode)
    {
        for (RoomConfig rcRoom : rcRooms)
            if (rcRoom.getStrRoomName().compareToIgnoreCase(strRoom) == 0)
                rcRoom.remMode(strUser,cMode);
    }

    public void clearUsers(String strRoom)
    {
        for (RoomConfig itrRoom : rcRooms)
            if (itrRoom.getStrRoomName().compareToIgnoreCase(strRoom) == 0)
                itrRoom.clearUsers();
    }

    public boolean findUser(String strRoom, String strUser)
    {
        for (RoomConfig itrRoom : rcRooms)
            if (itrRoom.getStrRoomName().compareToIgnoreCase(strRoom) == 0)
                if (itrRoom.findUser(strUser))
                    return true;
        return false;
    }

    public boolean findUser(String strUser) //find user in any joined room
    {
        for (RoomConfig itrRoom : rcRooms)
            if ((itrRoom.getbJoined()) && (itrRoom.findUser(strUser)))
                return true;
        return false;
    }

    public String findUserRoom(String strUser) //find user in any joined room, return room
    {
        for (RoomConfig itrRoom : rcRooms)
            if ((itrRoom.getbJoined()) && (itrRoom.findUser(strUser)))
                return itrRoom.getStrRoomName();
        return "";
    }

    public String getUserRooms(String strUser) //find user in any joined room, return room
    {
        String rooms = "";
        for (RoomConfig itrRoom : rcRooms)
            if ((itrRoom.getbJoined()) && (itrRoom.findUser(strUser)))
                rooms = rooms + " " + itrRoom.getStrRoomName();
        return rooms;
    }

    public RoomConfig getRoomConf(String strRoom)
    {
        for (RoomConfig itrRoom : rcRooms)
            if (itrRoom.getStrRoomName().compareToIgnoreCase(strRoom) == 0)
                    return itrRoom;
        return null;
    }

    public String checkValidExtension (String strFilename, String strRoom) {
        for (String strExt : getRoomConf(strRoom).getStrValidExtensions())
            if (strFilename.indexOf(strExt) > 0)
                return strExt;
        return "";
    }

    public String listUsers(String strRoom) //debug mostly
    {
        String strList = "";
        for (RoomConfig itrRoom : rcRooms)
            if (itrRoom.getStrRoomName().compareToIgnoreCase(strRoom) == 0)
                strList = itrRoom.listUsers();
        return strList;
    }

    public String listUsersMode(String strRoom) //debug mostly
    {
        String strList = "";
        for (RoomConfig itrRoom : rcRooms)
            if (itrRoom.getStrRoomName().compareToIgnoreCase(strRoom) == 0)
                strList = itrRoom.listUsersMode();
        return strList;
    }

    public void replaceUser(String strUser, String strNewNick)
    {
        for (RoomConfig itrRoom : rcRooms)
            itrRoom.replaceUser(strUser,strNewNick);
    }

    public boolean isVoice(String strRoom, String strUser) //looks for voice or higher
    {
        for (RoomConfig itrRoom : rcRooms)
            if ((itrRoom.getStrRoomName().compareToIgnoreCase(strRoom) == 0) && itrRoom.isVoice(strUser))
                return true;
        return false;
    }

    public boolean isVoice(String strUser) //looks for voice or higher in any joined room
    {
        for (RoomConfig itrRoom : rcRooms)
            if ((itrRoom.getbJoined()) && itrRoom.isVoice(strUser))
                return true;
        return false;
    }

    public void clear() {
        strHostname = null;
        strServername = null;
        strNickname = null;
        strAltNick = null;
        strMyNick = null;
        for (RoomConfig rcLoop : rcRooms)
            rcLoop.clear();
        rcRooms.clear();
    }

    public void addResult (String strNickname, String strFilename, String strFilesize) {
        ResultConfig newResult = new ResultConfig(strNickname,strFilename, strFilesize);
        resResults.add(newResult);
    }

    public void addResult (ResultConfig rcConfig) {
        resResults.add(rcConfig);
    }

    public void delResult (String strUUID) {
        for (int i = 0; i < resResults.size(); i++)
            if (resResults.get(i).getStrUUID().compareToIgnoreCase(strUUID) == 0)
                resResults.remove(i);
    }

    public ArrayList<ResultConfig> findResult (String strNickname, String strFilename) { // optional paramaters by passing empty string ""
        ArrayList<ResultConfig> alFindResult = new ArrayList<>();
        for (ResultConfig itrResults : resResults)
            if (((strNickname.compareToIgnoreCase("") == 0) || (itrResults.getStrNickname().compareToIgnoreCase(strNickname) == 0))
                && ((strFilename.compareToIgnoreCase("") == 0) || ((itrResults.getStrFilename().compareToIgnoreCase(strFilename) == 0) || (itrResults.getStrFilename().replaceAll("\\s","_").compareToIgnoreCase(strFilename) == 0))))
                alFindResult.add(itrResults);
        return alFindResult;
    }

    public boolean bFindResult (String strNickname, String strFilename) { // optional paramaters by passing empty string ""
        for (ResultConfig itrResults : resResults)
            if (((strNickname.compareToIgnoreCase("") == 0) || (itrResults.getStrNickname().compareToIgnoreCase(strNickname) == 0))
                    && ((strFilename.compareToIgnoreCase("") == 0) || ((itrResults.getStrFilename().compareToIgnoreCase(strFilename) == 0) || (itrResults.getStrFilename().replaceAll("\\s","_").compareToIgnoreCase(strFilename) == 0))))
                return true;
        return false;
    }

    public boolean findResult (String strUUID) {
        for (ResultConfig itrResults : resResults)
            if (itrResults.getStrUUID().compareToIgnoreCase(strUUID) == 0)
                return true;
        return false;
    }
}
