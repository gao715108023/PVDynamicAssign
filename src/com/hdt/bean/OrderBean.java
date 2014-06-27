package com.hdt.bean;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by gaochuanjun on 13-12-21.
 */
public class OrderBean {

    private int orderId;

    private Set<String> posList;

    private Set<String> areaList;

    private Set<String> posAreaList;

    private int priority;

    private boolean state;

    private double pvNum;

    private Map<String, Double> kpiMap;

    public OrderBean() {
        kpiMap = new HashMap<String, Double>();
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Set<String> getPosList() {
        return posList;
    }

    public void setPosList(Set<String> posList) {
        this.posList = posList;
    }

    public Set<String> getAreaList() {
        return areaList;
    }

    public void setAreaList(Set<String> areaList) {
        this.areaList = areaList;
    }

    public Set<String> getPosAreaList() {
        return posAreaList;
    }

    public void setPosAreaList(Set<String> posAreaList) {
        this.posAreaList = posAreaList;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public double getPvNum() {
        return pvNum;
    }

    public void setPvNum(double pvNum) {
        this.pvNum = pvNum;
    }

    public Map<String, Double> getKpiMap() {
        return kpiMap;
    }

    public void setKpiMap(Map<String, Double> kpiMap) {
        this.kpiMap = kpiMap;
    }
}
