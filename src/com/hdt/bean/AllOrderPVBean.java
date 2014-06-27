package com.hdt.bean;

/**
 * Created by gaochuanjun on 13-12-24.
 */
public class AllOrderPVBean {

    private int orderId;

    private int count;

    private double percent;

    private double totalPercent;

    private int pos;

    private int cityNo;

    public double getTotalPercent() {
        return totalPercent;
    }

    public void setTotalPercent(double totalPercent) {
        this.totalPercent = totalPercent;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }


    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getCityNo() {
        return cityNo;
    }

    public void setCityNo(int cityNo) {
        this.cityNo = cityNo;
    }
}
