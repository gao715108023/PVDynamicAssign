package com.hdt.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CSVUtils {

    public static void writeCSV(String destFile, String... info) {
        File csv = new File(destFile); // CSV文件
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(csv, true));
            bw.newLine();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < info.length; i++) {
                sb.append(info[i]).append(",");
            }
            bw.write(sb.toString().substring(0, sb.toString().length() - 1));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        for (int i = 1; i <= 10; i++) {

        }
    }
}
