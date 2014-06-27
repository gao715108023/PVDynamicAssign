package com.hdt.main;

import com.hdt.bean.OrderBean;
import com.hdt.bean.PageViewBean;
import com.hdt.bean.RepeatOrderBean;
import com.hdt.dao.OrderPVDao;
import com.hdt.utils.MybatisUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class Job {

    private static final Log LOG = LogFactory.getLog(Job.class);

    private Map<String, Double> pvValueMap;

    private List<OrderBean> allOrderBeans;

    private List<OrderBean> stateTrueOrderS;

    private List<OrderBean> stateFalseOrderS;

    private volatile boolean running = true;

    private OrderPVDao orderPVDao;

    public Job() {
        orderPVDao = MybatisUtils.session.getMapper(OrderPVDao.class);
    }

    /**
     * 加载当天的库存PV，并且按定向单元进行分类
     */
    public void loadInitPV() {
        List<PageViewBean> pageViewBeans = orderPVDao.select_pv_table("2013-12-31 00:00:00");
        for (PageViewBean pageViewBean : pageViewBeans) {
            String key = pageViewBean.getPos() + ":" + pageViewBean.getArea();
            pvValueMap.put(key, pageViewBean.getPvValue());
        }

//        pvValueMap.put("www.sina.com.cn:上海", 20.0);
//        pvValueMap.put("www.sina.com.cn:北京", 47.0);
//        pvValueMap.put("www.sina.com.cn:深圳", 32.0);
//        pvValueMap.put("www.sohu.com:上海", 82.0);
//        pvValueMap.put("www.sohu.com:北京", 104.0);
//        pvValueMap.put("www.sohu.com:广州", 11.0);
//        pvValueMap.put("www.sohu.com:南京", 7.0);
        if (pvValueMap.size() == 0 || pvValueMap == null) {
            LOG.error("PV数据库获取异常");
            return;
        } else {
            LOG.debug(pvValueMap);
        }
    }

    /**
     * 加载当天待完成的订单，将订单封装成OrderBean，组成一个数组
     */
    public void loadInitOrder() {
        allOrderBeans = orderPVDao.select_order_table("2013-12-31 00:00:00");
        if (allOrderBeans.size() == 0 || allOrderBeans == null) {
            LOG.error("订单数据获取异常！");
            return;
        } else {
            LOG.debug(allOrderBeans);
        }
    }

    /**
     * 初始化存放具有重复定向单元的订单的list
     */
    public void initRepeatList() {
        //repeatOrderBeans = new ArrayList<RepeatOrderBean>();

        stateTrueOrderS = new ArrayList<OrderBean>();

        stateFalseOrderS = new ArrayList<OrderBean>();
    }


    /**
     * 对订单进行分类，将有定向单元一样的订单放在一个List中
     */
    private List<RepeatOrderBean> classfiOrder() {
        List<RepeatOrderBean> repeatOrderBeans = new ArrayList<RepeatOrderBean>();
        //遍历所有订单
        for (OrderBean orderBean : allOrderBeans) {

            Set<String> posAreaList = orderBean.getPosAreaList();//该订单的定向单元

//            if (repeatOrderBeans == null || repeatOrderBeans.size() == 0) {
//                repeatOrderBeans = new ArrayList<RepeatOrderBean>();
//                addNewRepeatOrder(orderBean);
//                continue;
//            }
            boolean flag = true;//标记位，判断是否一个全新的订单，没有重复的定向单元

            //查看该订单在重复的定向单元列表中是否已存在
            b:
            for (RepeatOrderBean repeatOrderBean : repeatOrderBeans) {
                Set<String> ketSet = repeatOrderBean.getKetSet();//所有重复的定向单元
                if (ketSet.containsAll(posAreaList)) {//如果重复订单列表中存在该定向单元
                    addRepeatOrder(orderBean, repeatOrderBean, repeatOrderBeans);//将该订单加入该列表
                    flag = false;
                    break b;
                }
//                c:
//                for (String key : posAreaList) {
//                    if (ketSet.contains(key)) {
//                        addRepeatOrder(orderBean, repeatOrderBean);
//                        flag = false;
//                        break b;
//                    }
//                }
            }

            if (flag) {//如果重复订单列表中不存在该定向单元
                addNewRepeatOrder(orderBean, repeatOrderBeans); //生产一个新的列表
            }
        }
        return repeatOrderBeans;
    }

    /**
     * r
     * 将PV按最小流量算法分配给所有订单
     */
    public boolean assignPV(List<RepeatOrderBean> repeatOrderBeans) {

        boolean result = true;

        if ((allOrderBeans.size() == stateTrueOrderS.size() + stateFalseOrderS.size()) || (repeatOrderBeans.size() == 0 || repeatOrderBeans == null)) {
            LOG.info("所有订单均已执行完成，程序即将退出计算，请等待......");
            running = false;
            result = false;
            return result;
        }


        //遍历重复定向单元列表

        int size = repeatOrderBeans.size();

        List<Integer> indexList = new ArrayList<Integer>();

        for (int i = 0; i < size; i++) {

            RepeatOrderBean repeatOrderBean = repeatOrderBeans.get(i);

            if (repeatOrderBean.getRepeatOrders().size() == 0 || repeatOrderBean.getRepeatOrders() == null) {
                LOG.info("该重复列表中已经没有订单，删除该重复列表: " + i);
                indexList.add(i);
                continue;
            }

            //获取需求PV最小的定向单元
            OrderBean orderBean = getMinPvOrder(repeatOrderBean);

            //获取重复列表中剩余量最少的PV
            String minKey = getMinPV(orderBean.getPosAreaList());

            if (minKey == null) {
                LOG.warn("库存中已经没有足够的库存满足订单：" + orderBean.getOrderId() + ". 订单状态：不可完成！");
                orderBean.setState(false);
                stateFalseOrderS.add(orderBean);
                repeatOrderBean.getRepeatOrders().remove(orderBean);//将该订单从重复列表中删除
                continue;
            }

            //如果剩余的PV值满足订单需求最少的PV
            if (pvValueMap.get(minKey) > orderBean.getPvNum()) {
                //将剩余的PV值减去被该订单占用的PV
                double value = pvValueMap.get(minKey) - orderBean.getPvNum();
                pvValueMap.put(minKey, value);
                orderBean.setState(true);//将订单的状态设置为执行完成
                orderBean.getKpiMap().put(minKey, orderBean.getPvNum());
                stateTrueOrderS.add(orderBean);//将该订单加入可执行订单列表中
                repeatOrderBean.getRepeatOrders().remove(orderBean);//将该订单从重复列表中删除
            } else if (pvValueMap.get(minKey) < orderBean.getPvNum()) {//如果剩余的PV不够订单需求最少的PV
                double value = orderBean.getPvNum() - pvValueMap.get(minKey);
                orderBean.setPvNum(value);
                orderBean.getKpiMap().put(minKey, pvValueMap.get(minKey));
                pvValueMap.remove(minKey);//从库存中删除该定向单元
            } else {//如果剩余的PV正好满足订单需求最少的PV
                pvValueMap.remove(minKey);//从库存中删除该定向单元
                orderBean.setState(true);//将订单的状态设置为执行完成
                orderBean.getKpiMap().put(minKey, orderBean.getPvNum());
                stateTrueOrderS.add(orderBean);//将该订单加入可执行订单列表中
                repeatOrderBean.getRepeatOrders().remove(orderBean);//将该订单从重复列表中删除
            }

        }

        if (indexList.size() > 0 && indexList != null) {
            for (int i : indexList) {
                repeatOrderBeans.remove(i);
            }
        }
        return result;
    }

    public void offerService() {
        loadInitPV();
        loadInitOrder();
        List<RepeatOrderBean> repeatOrderBeans = classfiOrder();
        while (running) {
            boolean result = assignPV(repeatOrderBeans);
            if (result) {
                repeatOrderBeans = reloadList(repeatOrderBeans);
            }
        }

        printInfo();
    }

    private void printInfo() {
        for (OrderBean orderBean : allOrderBeans) {
            Set<String> keyS = orderBean.getKpiMap().keySet();
            LOG.info("---------------------------------------------------------");
            LOG.info("订单：" + orderBean.getOrderId());
            for (String key : keyS) {
                LOG.info(key + "--------->" + orderBean.getKpiMap().get(key));
            }
        }
    }

    /**
     * 将订单加入重复列表中
     *
     * @param orderBean
     * @param repeatOrderBean
     */
    private void addRepeatOrder(OrderBean orderBean, RepeatOrderBean repeatOrderBean, List<RepeatOrderBean> repeatOrderBeans) {
        //将订单加入重复列表中
        repeatOrderBean.getRepeatOrders().add(orderBean);
        //将该订单的定向单元合并在一起
        for (String key : orderBean.getPosAreaList()) {
            repeatOrderBean.getKetSet().add(key);
        }
    }

    /**
     * 获取需求最少PV值的订单
     *
     * @param repeatOrderBean
     * @return
     */
    private OrderBean getMinPvOrder(RepeatOrderBean repeatOrderBean) {
        double min = Double.MAX_VALUE;
        OrderBean minOrderBean = null;
        List<OrderBean> orderBeans = repeatOrderBean.getRepeatOrders();
        for (OrderBean orderBean : orderBeans) {
            if (orderBean.getPvNum() < min) {
                min = orderBean.getPvNum();
                minOrderBean = orderBean;
            }
        }
        return minOrderBean;
    }

    /**
     * 获取重复列表中剩余量最少的PV
     *
     * @param posAreaList
     * @return
     */
    private String getMinPV(Set<String> posAreaList) {
        double min = Double.MAX_VALUE;
        String minKey = null;
        for (String key : posAreaList) {
            if (pvValueMap.get(key) < min) {
                min = pvValueMap.get(key);
                minKey = key;
            }
        }
        return minKey;
    }

    /**
     * 重新分配重复列表
     *
     * @param oldRepeatOrderBeanList
     */
    private List<RepeatOrderBean> reloadList(List<RepeatOrderBean> oldRepeatOrderBeanList) {

        List<RepeatOrderBean> newRepeatOrderBeanList = new ArrayList<RepeatOrderBean>();

        for (RepeatOrderBean oldRepeatOrderBean : oldRepeatOrderBeanList) {
            //遍历之前的重复列表
            for (OrderBean orderBean : oldRepeatOrderBean.getRepeatOrders()) {

//            if (repeatOrderBeanList.size() == 0 || repeatOrderBeanList == null) {
//                addNewRepeatOrder(orderBean, repeatOrderBeanList);
//                continue;
//            }

                boolean flag = true;
                Set<String> posAreaSet = orderBean.getPosAreaList();//获取某订单的定向单元
                b:
                for (RepeatOrderBean newRepeatOrderBean : newRepeatOrderBeanList) {
                    Set<String> ketSet = newRepeatOrderBean.getKetSet();
                    if (ketSet.containsAll(posAreaSet)) {
                        addRepeatOrder(orderBean, oldRepeatOrderBean, newRepeatOrderBeanList);
                        flag = false;
                        break b;
                    }
//                c:
//                for (String key : posAreaSet) {
//                    if (ketSet.contains(key)) {
//                        addRepeatOrder(orderBean, repeatOrderBean1);
//                        flag = false;
//                        break b;
//                    }
//                }
                }

                if (flag) {
                    addNewRepeatOrder(orderBean, newRepeatOrderBeanList);
                }
            }
        }
        return newRepeatOrderBeanList;
    }

    /**
     * 生成一个新的重复列表
     *
     * @param orderBean
     * @param repeatOrderBeanList
     */
    private void addNewRepeatOrder(OrderBean orderBean, List<RepeatOrderBean> repeatOrderBeanList) {
        RepeatOrderBean repeatOrderBean = new RepeatOrderBean();
        List<OrderBean> repeatOrders = new ArrayList<OrderBean>();
        repeatOrders.add(orderBean);
        Set<String> ketSet = new HashSet<String>();
        for (String key : orderBean.getPosAreaList()) {
            ketSet.add(key);
        }
        repeatOrderBean.setRepeatOrders(repeatOrders);
        repeatOrderBean.setKetSet(ketSet);
        repeatOrderBeanList.add(repeatOrderBean);
    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Job job = new Job();
        job.offerService();
    }
}
