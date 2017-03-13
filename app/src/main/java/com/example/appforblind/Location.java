package com.example.appforblind;

import java.util.Date;

/**
 * Created by Honey on 13-Mar-17.
 */

public class Location {

    double latitute;
    double longitute;
    Date timeStamp;

    public Location() {
    }

    public Location(double latitute, double longitute, Date timeStamp) {
        this.latitute = latitute;
        this.longitute = longitute;
        this.timeStamp = timeStamp;
    }

    public double getLatitute() {
        return latitute;
    }

    public void setLatitute(double latitute) {
        this.latitute = latitute;
    }

    public double getLongitute() {
        return longitute;
    }

    public void setLongitute(double longitute) {
        this.longitute = longitute;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}
