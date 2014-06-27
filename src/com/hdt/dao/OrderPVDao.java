package com.hdt.dao;

import com.hdt.bean.OrderBean;
import com.hdt.bean.PageViewBean;

import java.util.List;

public interface OrderPVDao {

    public List<OrderBean> select_order_table(String updateTime);

    public List<PageViewBean> select_pv_table(String updateTime);

}
