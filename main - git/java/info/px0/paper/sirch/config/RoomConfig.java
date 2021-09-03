package info.px0.paper.sirch.config;

import java.util.ArrayList;
import java.util.List;

public class RoomConfig {
    private boolean bJoined = false;
    private String strRoomName;
    private String strFindCmd;
    private String strDelayedSearch = "";
    private ArrayList<UserConfig> alUsers;
    private String[] strValidExtensions = {".mp3",".aac",".ogg",".wma"}; //($ config? ex. add video files etc...)

    public RoomConfig(String strRoomName, String strFindCmd) {
        super();
        this.alUsers = new ArrayList<>();
        this.strRoomName = strRoomName;
        this.strFindCmd = strFindCmd;
    }

    public String getStrRoomName() {
        return strRoomName;
    }
    public void setStrRoomName(String strRoomName) {
        this.strRoomName = strRoomName;
    }
    public String getStrFindCmd() {
        return strFindCmd;
    }
    public void setStrFindCmd(String strFindCmd) {
        this.strFindCmd = strFindCmd;
    }

    public ArrayList<UserConfig> getAlUsers() {
        return alUsers;
    }
    public void setAlUsers(ArrayList<UserConfig> strUsers) {
        this.alUsers = strUsers;
    }

    public String getStrDelayedSearch() {
        return strDelayedSearch;
    }

    public void setStrDelayedSearch(String strDelayedSearch) {
        this.strDelayedSearch = strDelayedSearch;
    }

    public String[] getStrValidExtensions() {
        return strValidExtensions;
    }

    public void setStrValidExtensions(String[] strValidExtensions) {
        this.strValidExtensions = strValidExtensions;
    }

    public boolean getbJoined() {
        return bJoined;
    }
    public void setbJoined(boolean bJoined) {
        this.bJoined = bJoined;
    }

    public void addUser(String strUser)
    {
        alUsers.add(new UserConfig(strUser));
    }

    public void addUser(String strUser, char cMode)
    {
        alUsers.add(new UserConfig(strUser,cMode));
    }

    public void addUser(String strUser, char[] cMode)
    {
        alUsers.add(new UserConfig(strUser,cMode));
    }

    public boolean delUser(String strUser)
    {
        for (int i = 0; i < alUsers.size(); i++)
            if (alUsers.get(i).getStrNickname().compareToIgnoreCase(strUser) == 0)
            {
                alUsers.remove(i);
                return true;
            }
        return false;
    }

    public void addMode(String strUser, char cMode)
    {
        for (UserConfig ucSearch : alUsers)
            if (strUser.compareToIgnoreCase(ucSearch.getStrNickname()) == 0)
                ucSearch.addMode(cMode);
    }

    public void remMode(String strUser, char cMode)
    {
        for (UserConfig ucSearch : alUsers)
            if (strUser.compareToIgnoreCase(ucSearch.getStrNickname()) == 0)
                ucSearch.remMode(cMode);
    }

    public void clearUsers()
    {
        alUsers.clear();
    }

    public boolean findUser(String strUser)
    {
        for (UserConfig strSearch : alUsers)
            if (strUser.compareToIgnoreCase(strSearch.getStrNickname()) == 0)
                return true;
        return false;
    }

    public String listUsers() //debug mostly
    {
        String strList = "";
        for (UserConfig strSearch : alUsers)
            strList = strList + strSearch.getStrNickname() + " ";
        return strList;
    }

    public String listUsersMode() //debug mostly
    {
        String strList = "";
        for (UserConfig strSearch : alUsers)
            if (strSearch.getStrMode().compareToIgnoreCase("") == 0)
                strList = strList + strSearch.getStrNickname() + " ";
            else
                strList = strList + "+" + strSearch.getStrMode() + " " + strSearch.getStrNickname() + " ";
        return strList;
    }

    public void replaceUser(String strUser, String strNewNick)
    {
        for (int i = 0; i < alUsers.size(); i++)
            if (alUsers.get(i).getStrNickname().compareToIgnoreCase(strUser) == 0)
                alUsers.get(i).setStrNickname(strNewNick);
    }

    public boolean isVoice(String strUser) //looks for voice or higher
    {
        String strMode = "";
        for (int i = 0; i < alUsers.size(); i++)
            if (alUsers.get(i).getStrNickname().compareToIgnoreCase(strUser) == 0)
                strMode = alUsers.get(i).getStrMode();
        if (strMode.compareToIgnoreCase("") != 0)
            return true;
        else
            return false;
    }

    public void clear() {
        strRoomName = null;
        strFindCmd = null;
        alUsers = null;
    }
}
