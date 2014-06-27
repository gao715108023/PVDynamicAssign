package com.hdt.common;

import com.hdt.bean.OrderBean;

abstract class Order {
	
	public abstract void addOrder();

    public abstract void delOrder();

    public abstract OrderBean getOrder();
}
