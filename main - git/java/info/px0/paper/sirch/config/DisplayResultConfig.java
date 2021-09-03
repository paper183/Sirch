package info.px0.paper.sirch.config;

/**
 * Created by Paper on 19/09/2016.
 */

public class DisplayResultConfig extends ResultConfig {

    private long lRealsize = 0;
    private int iProgress = 0;
    private long lAddress = 0;
    private int iPort = 0;
    private int iStatus = 0;
    private String strExtra = "";

    public DisplayResultConfig(ResultConfig rcResult)
    {
        super(rcResult);
    }

    public DisplayResultConfig(ResultConfig rcResult, int iStatus, String strExtra)
    {
        super(rcResult);
        this.iStatus = iStatus;
        this.strExtra = strExtra;
    }

    public DisplayResultConfig(DisplayResultConfig drcResult, int iStatus, String strExtra)
    {
        super(drcResult);
        this.lRealsize = drcResult.getlRealsize();
        this.iProgress = drcResult.getiProgress();
        this.lAddress = drcResult.getlAddress();
        this.iPort = drcResult.getiPort();
        this.iStatus = iStatus;
        this.strExtra = strExtra;
    }

    public DisplayResultConfig(ResultConfig rcResult, long lRealsize, int iProgress, long lAddress, int iPort, int iStatus)
    {
        super(rcResult);
        this.lRealsize = lRealsize;
        this.iProgress = iProgress;
        this.lAddress = lAddress;
        this.iPort = iPort;
        this.iStatus = iStatus;
    }

    public int getiStatus() {
        return iStatus;
    }

    public void setiStatus(int iStatus) {
        this.iStatus = iStatus;
    }

    public long getlAddress() {
        return lAddress;
    }

    public void setlAddress(long lAddress) {
        this.lAddress = lAddress;
    }

    public int getiPort() {
        return iPort;
    }

    public void setiPort(int iPort) {
        this.iPort = iPort;
    }

    public long getlRealsize() {
        return lRealsize;
    }

    public void setlRealsize(long lRealsize) {
        this.lRealsize = lRealsize;
    }

    public int getiProgress() {
        return iProgress;
    }

    public void setiProgress(int iProgress) {
        this.iProgress = iProgress;
    }

    public String getStrExtra() {
        return strExtra;
    }

    public void setStrExtra(String strExtra) {
        this.strExtra = strExtra;
    }

    @Override
    public String toString () {
        return ((getiStatus() != 0) ? (getiStatus() + " ") : "") + ((getiProgress() != 0) ? (getiProgress() + " ") : "") + getStrFilename() + " " + getReadableSize();
    }
}
