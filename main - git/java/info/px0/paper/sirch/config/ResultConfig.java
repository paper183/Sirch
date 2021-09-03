package info.px0.paper.sirch.config;

import android.os.Handler;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultConfig {
    private String strNickname;
    private String strFilename;
    private String strRooms;
    private long lFilesize; //filesize in bytes
    private boolean bRequested = false;
    private final String strUUID;

    public ResultConfig() {
        this.strNickname = "";
        this.strFilename = "";
        this.strRooms = "";
        this.lFilesize = 0;
        this.strUUID = UUID.randomUUID().toString();
    }

    public ResultConfig(ResultConfig rcPrevious) {
        this.strNickname = rcPrevious.getStrNickname();
        this.strFilename = rcPrevious.getStrFilename();
        this.strRooms = rcPrevious.getStrRooms();
        this.lFilesize = rcPrevious.getlFilesize();
        this.strUUID = rcPrevious.getStrUUID();
    }

    public ResultConfig(String strNickname, String strFilename , String strFilesize) {
        this.strNickname = strNickname;
        this.strFilename = strFilename;
        this.strRooms = "";
        this.setlFilesize(strFilesize);
        this.strUUID = UUID.randomUUID().toString();
    }

    public String getStrRooms() {
        return strRooms;
    }

    public void setStrRooms(String strRooms) {
        this.strRooms = strRooms;
    }

    public String getStrNickname() {
        return strNickname;
    }

    public void setStrNickname(String strNickname) {
        this.strNickname = strNickname;
    }

    public String getStrFilename() {
        return strFilename;
    }

    public void setStrFilename(String strFilename) {
        this.strFilename = strFilename;
    }

    public boolean isbRequested() {
        return bRequested;
    }

    public void setbRequested(boolean bRequested) {
        this.bRequested = bRequested;
    }

    public long getlFilesize() {
        return lFilesize;
    }

    public void setlFilesize(long lFilesize) {
        this.lFilesize = lFilesize;
    }

    public void setlFilesize(String strFilesize) {
        long lFilesize = 0;

        Pattern patSize = Pattern.compile("\\b[0-9]+([.,][0-9]+)?\\s?[gkm]b\\b", Pattern.CASE_INSENSITIVE);
        Matcher matchSize = patSize.matcher(strFilesize);
        if (matchSize.find()) {
            char cOrder = strFilesize.charAt(matchSize.end() - 2);
            strFilesize = strFilesize.replaceAll("(?i)\\s?[gkm]b\\b", "");
            strFilesize = strFilesize.replaceAll(",", ".");
            double dFilesize = Double.parseDouble(strFilesize);
            switch (Character.toLowerCase(cOrder))
            {
                case 'k':
                    lFilesize = Math.round(dFilesize*1024);
                    break;
                case 'm':
                    lFilesize = Math.round(dFilesize*1024*1024);
                    break;
                case 'g':
                    lFilesize = Math.round(dFilesize*1024*1024*1024);
                    break;
            }
        }

        this.lFilesize = lFilesize;
    }

    public String getReadableSize() {
        String strReadable = "";
        int i = 0;
        double dSize = getlFilesize();
        while ((dSize > 1024) && (i < 3))
        {
            dSize /= 1024;
            i++;
        }
        strReadable = "" + Math.round(dSize*100.0)/100.0;
        switch (i){
            case 1:
                strReadable += " KiB";
                break;
            case 2:
                strReadable += " MiB";
                break;
            case 3:
            default:
                strReadable += " GiB";
                break;

            case 0:
                strReadable += " B";
                break;
        }
        return strReadable;
    }

    public String getStrUUID() {
        return strUUID;
    }

    public boolean equals(ResultConfig rcCompare) {
        if (strUUID.compareTo(rcCompare.getStrUUID()) == 0)
            return true;
        return false;
    }

    public String toString () {
        return getStrNickname() + " " + getStrFilename() + " " + getlFilesize() + " " + getStrUUID();
    }
}