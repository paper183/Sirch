package info.px0.paper.sirch.config;

/**
 * Created by Paper on 24/08/2016.
 */
public class UserConfig {

    private String strNickname;
    private String strMode;

    public UserConfig (String strNickname, char cMode) {
        this.strNickname = strNickname;
        this.strMode = "";
        addMode(cMode);
    }

    public UserConfig (String strNickname, char[] cMode) {
        this.strNickname = strNickname;
        this.strMode = "";
        for (char cModeLoop : cMode)
            addMode(cModeLoop);
    }

    public UserConfig (String strNickname) {
        this.strNickname = strNickname;
        this.strMode = "";
    }

    public void addMode (char cMode) {
        switch (cMode) {
            case '+':
                cMode = 'v';
                break;
            case '@':
                cMode = 'o';
                break;
            case '%':
                cMode = 'h';
                break;
            case '&':
                cMode = 'a';
                break;
            case '~':
                cMode = 'q';
                break;
            default:
                break;
        }
        cMode = Character.toLowerCase(cMode);
        if (getStrMode().indexOf(cMode) == -1)
            setStrMode(getStrMode() + String.valueOf(cMode));
    }

    public void remMode (char cMode) {
        if (getStrMode().indexOf(cMode) >= 0)
            setStrMode(getStrMode().replaceAll(String.valueOf(cMode).toLowerCase(), ""));
    }

    public String getStrNickname() {
        return strNickname;
    }

    public void setStrNickname(String strNickname) {
        this.strNickname = strNickname;
    }

    public String getStrMode() {
        return strMode;
    }

    public void setStrMode(String strMode) {
        this.strMode = strMode;
    }

}
