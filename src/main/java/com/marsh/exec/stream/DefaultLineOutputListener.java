package com.marsh.exec.stream;

/**
 * @author marsh
 * @date 2022年04月13日 11:55
 */
public class DefaultLineOutputListener implements LineOutputListener {

    /**
     * 默认实现仅将接收到的信息打印到控制台，如果需要解析控制台返回的数据可以重新实现一个子类自行处理
     * @author Marsh
     * @date 2022-04-13
     * @param line
     */
    @Override
    public void processLine(String line) {
        System.out.println(line);
    }
}
