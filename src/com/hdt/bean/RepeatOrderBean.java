package com.hdt.bean;

import java.util.List;
import java.util.Set;

/**
 * Created by gaochuanjun on 13-12-26.
 */
public class RepeatOrderBean {

    private List<OrderBean> repeatOrders;

    private Set<String> ketSet;

    public List<OrderBean> getRepeatOrders() {
        return repeatOrders;
    }

    public void setRepeatOrders(List<OrderBean> repeatOrders) {
        this.repeatOrders = repeatOrders;
    }

    public Set<String> getKetSet() {
        return ketSet;
    }

    public void setKetSet(Set<String> ketSet) {
        this.ketSet = ketSet;
    }
}
