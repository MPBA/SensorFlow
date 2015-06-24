package eu.fbk.mpba.sensorsflows.plugins.data;

/**
 * Created by toldo on 17/02/2015.
 */
public class Exl {
    private String mName;
    private String mPlace;
    private String mMac;

    public Exl(String name, String mac, String place) {
        this.mName = name;
        this.mMac = mac;
        this.mPlace = place;
    }

    public String getmMac() {
        return mMac;
    }

    public void setmMac(String mMac) {
        this.mMac = mMac;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmPlace() {  return mPlace;}

    public void setmPlace(String mPlace) { this.mPlace = mPlace; }
}