package com.marsh.exec.stream;

/**
 * @author marsh
 * @date 2022年04月13日 11:55
 */
public interface LineOutputListener {

    /**
     * 当接收到一行数据时触发该方法
     * @param line
     */
    void processLine(String line);
}
