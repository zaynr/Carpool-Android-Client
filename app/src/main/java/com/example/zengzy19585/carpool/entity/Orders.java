package com.example.zengzy19585.carpool.entity;

import com.baidu.mapapi.model.LatLng;

/**
 * Created by zaynr on 2017/7/13.
 */

public class Orders {
    private String oriAddress;
    private String destAddress;
    private String distance;
    private String aptTime;
    private LatLng start, end;

    public LatLng getStart() {
        return start;
    }

    public void setStart(LatLng start) {
        this.start = start;
    }

    public LatLng getEnd() {
        return end;
    }

    public void setEnd(LatLng end) {
        this.end = end;
    }

    public String getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(String serialNum) {
        this.serialNum = serialNum;
    }

    private String serialNum;

    public String getOriAddress() {
        return oriAddress;
    }

    public void setOriAddress(String oriAddress) {
        this.oriAddress = oriAddress;
    }

    public String getDestAddress() {
        return destAddress;
    }

    public void setDestAddress(String destAddress) {
        this.destAddress = destAddress;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getAptTime() {
        return aptTime;
    }

    public void setAptTime(String aptTime) {
        this.aptTime = aptTime;
    }
}
