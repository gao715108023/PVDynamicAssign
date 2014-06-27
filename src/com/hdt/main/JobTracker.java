package com.hdt.main;

import com.hdt.bean.AllOrderPVBean;
import com.hdt.bean.OrderBean;
import com.hdt.common.PageView;
import com.hdt.file.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class JobTracker {

    private static final Log LOG = LogFactory.getLog(JobTracker.class);

    Map<Integer, Map<Integer, Map<Integer, Double>>> orderMap;

    Map<Integer, Map<Integer, Double>> pvMap;

    Map<Integer, Map<Integer, Map<Integer, Double>>> posPvMap;

    public void loadInitPV(FileUtils fileUtils, String filePath) {

        //读取每日PV的流量，来源可以为数据库和文件，可以重写该方法，返回的格式必须为Map<Integer, Map<Integer, Double>>
        //第一个key为广告位，第二个key为地域位置，目前支持的分配策略优先级为：地域位置 > 广告位
        pvMap = fileUtils.getPerDayPV(filePath);

        if (pvMap.size() > 0 && pvMap != null) {
            LOG.info("成功读取PV数据源中的数据！");
            LOG.info(pvMap);
        } else {
            LOG.error("已经没有流量了，查看数据源中是否存在数据！");
        }
    }

    public void loadOrder() {

        //加载的order分为好几种不同的情况，通常情况下我们这样排列订单的，按照要求严格到不严格的顺序
        //要求了订单投放的广告位和地域位置以及投放的总PV，其它没有要求
        //第一个key为订单ID，第二个key为要求的所有广告位，格式为pos1:pos2:pos3......:posn，第三个key为要求的所有地域位置，格式位area1:area2:area3:......:arean，value为该订单要求的PV
        Map<Integer, Map<String, Map<String, Double>>> order_pos_area_map;

    }


    /**
     * 算法主循环
     */
    public void offerService() {


        while (true) {

            //第一步，总的剩余流量在没有其它订单的影响下是否已经无法完成,计算出所有订单所需要的PV占当天的各自PV百分比

            //遍历所有订单


            //第二步，计算所有订单中的总流量是否超过了100%，如果有超过的，则该流量中的部分订单需要放弃掉

        }
    }

    private Map<Integer, Map<Integer, Map<Integer, Double>>> calFirstStep(Map<Integer, Map<Integer, Map<Integer, Double>>> orderMap, Map<Integer, Map<Integer, Double>> pvMap) {

        Map<Integer, Map<Integer, Map<Integer, Double>>> posPvMap = new HashMap<Integer, Map<Integer, Map<Integer, Double>>>();

        Iterator<Map.Entry<Integer, Map<Integer, Map<Integer, Double>>>> iterator = orderMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, Map<Integer, Map<Integer, Double>>> entry = iterator.next();

            int orderId = entry.getKey();
            Map<Integer, Map<Integer, Double>> orderPosMap = entry.getValue();

            Iterator<Map.Entry<Integer, Map<Integer, Double>>> iterator1 = orderPosMap.entrySet().iterator();

            while (iterator1.hasNext()) {

                Map.Entry<Integer, Map<Integer, Double>> entry1 = iterator1.next();

                int orderPos = entry1.getKey();
                Map<Integer, Double> orderCityNoMap = entry1.getValue();


                Iterator<Map.Entry<Integer, Double>> iterator2 = orderCityNoMap.entrySet().iterator();

                while (iterator2.hasNext()) {
                    Map.Entry<Integer, Double> entry2 = iterator2.next();

                    int orderCityNo = entry2.getKey();
                    double needPVCount = entry2.getValue();
                    double total = pvMap.get(orderPos).get(orderCityNo);
                    double percent = (needPVCount / total) * 100;
                    addResult(posPvMap, orderPos, orderCityNo, orderId, percent);
                }
            }
        }
        return posPvMap;
    }

    private List<Set<Integer>> calOverLimit(Map<Integer, Map<Integer, Map<Integer, Double>>> posPvMap) {
        List<Set<Integer>> overLimitOrderList = new ArrayList<Set<Integer>>();
        List<AllOrderPVBean> allOrderPVBeans = new ArrayList<AllOrderPVBean>();
        Iterator<Map.Entry<Integer, Map<Integer, Map<Integer, Double>>>> iterator = posPvMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Map<Integer, Map<Integer, Double>>> entry = iterator.next();

            int pos = entry.getKey();
            Map<Integer, Map<Integer, Double>> cityNoMap = entry.getValue();

            Iterator<Map.Entry<Integer, Map<Integer, Double>>> iterator1 = cityNoMap.entrySet().iterator();

            while (iterator1.hasNext()) {
                Map.Entry<Integer, Map<Integer, Double>> entry1 = iterator1.next();

                int cityNo = entry1.getKey();
                Map<Integer, Double> orderIdMap = entry1.getValue();

                Collection<Double> values = orderIdMap.values();
                int total = 0;
                for (Double percent : values) {
                    total += percent;
                }
                if (total > 100) {
                    Set<Integer> keys = orderIdMap.keySet();
                    overLimitOrderList.add(keys);
                    for (Integer key : keys) {
                        AllOrderPVBean allOrderPVBean = new AllOrderPVBean();
                        allOrderPVBean.setPercent(orderIdMap.get(key));
                        allOrderPVBean.setOrderId(key);
                        allOrderPVBean.setTotalPercent(total);
                        allOrderPVBean.setPos(pos);
                        allOrderPVBean.setCityNo(cityNo);
                        allOrderPVBeans.add(allOrderPVBean);
                    }
                }
            }
        }
        return overLimitOrderList;
    }

    private Map<Integer, Integer> delOrder(List<Set<Integer>> overLimitOrderList, List<AllOrderPVBean> allOrderPVBeans, Map<Integer, Map<Integer, Map<Integer, Double>>> posPvMap) {
        Map<Integer, Integer> orderCount = new HashMap<Integer, Integer>();
        for (int i = 0; i < overLimitOrderList.size(); i++) {
            Set<Integer> orderSet = overLimitOrderList.get(i);
            for (Integer orderId : orderSet) {
                addMap(orderCount, orderId);
            }
        }
        List<Map.Entry<Integer, Integer>> infoIds = sortByValue(orderCount);
        for (int i = infoIds.size() - 1; i >= 0; i--) {
            if (allOrderPVBeans == null || allOrderPVBeans.size() == 0) {
                break;
            }
            Map.Entry<Integer, Integer> entry = infoIds.get(i);
            int orderId = entry.getKey();
            int value = entry.getValue();
            Iterator<AllOrderPVBean> iterator = allOrderPVBeans.iterator();
            while (iterator.hasNext()) {
                AllOrderPVBean allOrderPVBean = iterator.next();
                if (allOrderPVBean.getOrderId() == orderId) {
                    posPvMap.get(allOrderPVBean.getPos()).get(allOrderPVBean.getCityNo()).remove(allOrderPVBean.getOrderId());
                    allOrderPVBean.setTotalPercent(allOrderPVBean.getTotalPercent() - allOrderPVBean.getOrderId());
                    if (allOrderPVBean.getTotalPercent() <= 100) {
                        iterator.remove();
                    }
                }
            }
//            for (int j = 0; j < allOrderPVBeans.size(); j++) {
//                AllOrderPVBean allOrderPVBean = allOrderPVBeans.get(i);
//                if (allOrderPVBean.getOrderId() == orderId) {
//                    allOrderPVBean.setTotalPercent(allOrderPVBean.getTotalPercent() - allOrderPVBean.getOrderId());
//                }
//            }
        }
        return orderCount;
    }


    private List<Map.Entry<Integer, Integer>> sortByValue(Map<Integer, Integer> orderCount) {
        List<Map.Entry<Integer, Integer>> infoIds = new ArrayList<Map.Entry<Integer, Integer>>(orderCount.entrySet());
        Collections.sort(infoIds, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return (o1.getValue() - o2.getValue());
                //return (o1.getKey()).compareTo(o2.getKey());
            }
        });
        return infoIds;
    }


    private void addMap(Map<Integer, Integer> orderCount, int orderId) {
        if (orderCount.containsKey(orderId)) {
            int count = orderCount.get(orderId);
            count++;
            orderCount.put(orderId, count);
        } else {
            orderCount.put(orderId, 1);
        }
    }

    private void addResult(Map<Integer, Map<Integer, Map<Integer, Double>>> posPvMap, int pos, int cityNo, int orderId, double percent) {
        if (posPvMap.containsKey(pos)) {
            if (posPvMap.get(pos).containsKey(cityNo)) {
                posPvMap.get(pos).get(cityNo).put(orderId, percent);
            } else {
                Map<Integer, Double> orderIdMap = new HashMap<Integer, Double>();
                orderIdMap.put(orderId, percent);
                posPvMap.get(pos).put(cityNo, orderIdMap);
            }
        } else {
            Map<Integer, Double> orderIdMap = new HashMap<Integer, Double>();
            orderIdMap.put(orderId, percent);
            Map<Integer, Map<Integer, Double>> cityNoMap = new HashMap<Integer, Map<Integer, Double>>();
            posPvMap.put(pos, cityNoMap);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        List<Integer> list = new ArrayList<Integer>();
        list.add(5);
        list.add(3);
        list.add(7);
//        for (Integer l : list) {
//            System.out.println(l);
//        }
//
        HashMap<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
        map.put(1, 2);
        map.put(2, 4);
        map.put(3, 3);
//        System.out.println(list);
//        JobTracker jobTracker = new JobTracker();
//        jobTracker.sortByValue(map);
//
//        Set<Integer> keys = map.keySet();
//        for (Integer key : keys) {
//            System.out.println(map.get(key));
//        }

        List<Map.Entry<Integer, Integer>> infoIds = new ArrayList<Map.Entry<Integer, Integer>>(map.entrySet());
        Collections.sort(infoIds, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return (o1.getValue() - o2.getValue());
                //return (o1.getKey()).compareTo(o2.getKey());
            }
        });

        for (int i = 0; i < list.size(); i++) {
            Map.Entry<Integer, Integer> entry = infoIds.get(i);
            System.out.println(entry.getKey() + "(\"" + entry.getKey() + "\", " + entry.getValue() + ", \"" + map.get(entry.getKey()) + "\"),");
        }
        System.out.println(map);
    }
}
