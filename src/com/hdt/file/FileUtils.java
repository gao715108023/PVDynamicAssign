package com.hdt.file;

import com.hdt.common.PageView;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {

    public Map<Integer, Map<Integer, Double>> getPerDayPV(String filePath) {
        Map<Integer, Map<Integer, Double>> pvMap = new HashMap<Integer, Map<Integer, Double>>();
        File csv = new File(filePath); // CSV文件
        BufferedReader br = null;
        try {

            br = new BufferedReader(new FileReader(csv));
            // 读取直到最后一行
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                if (lineNumber == 0) {
                    lineNumber++;
                    continue;
                }
                // 把一行数据分割成多个字段
                String[] split = line.split(",");

                int pvId = Integer.parseInt(getNum(split[0]));
                double count = Double.parseDouble(getNum(split[2]));
                if (pvMap.containsKey(pvId)) {
                    Map<Integer, Double> cityNoMap = pvMap.get(pvId);
                    while (true) {
                        int cityNo = getCityNoRandom(367);
                        if (cityNoMap.containsKey(cityNo)) {
                            continue;
                        } else {
                            cityNoMap.put(cityNo, count);
                            break;
                        }
                    }
                } else {
                    Map<Integer, Double> cityNoMap = new HashMap<Integer, Double>();
                    while (true) {
                        int cityNo = getCityNoRandom(367);
                        if (cityNoMap.containsKey(cityNo)) {
                            continue;
                        } else {
                            cityNoMap.put(cityNo, count);
                            break;
                        }
                    }
                    pvMap.put(pvId, cityNoMap);
                }
                lineNumber++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return pvMap;
    }

    private static String getNum(String src) {
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(src);
        return m.replaceAll("").trim();
    }

    private static int getCityNoRandom(int no) {
        Random random = new Random();
        return Math.abs(random.nextInt()) % no;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        //FileUtils.getPerDayPV("dailypvinfo.csv");
    }
}
