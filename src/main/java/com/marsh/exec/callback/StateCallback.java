package com.marsh.exec.callback;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;

/**
 * 命令执行状态回调
 * @author marsh
 * @date 2022年04月11日 9:49
 */
public interface StateCallback extends ExecuteResultHandler {

    /**
     * 在任务运行前执行,如果返回false则整个任务立即终止。由于是立即终止的任务，后续所有回调均不触发
     * @author Marsh
     * @date 2022-04-13
     * @return boolean
     */
    boolean onProcessBefore();

    /**
     * 任务执行超时
     * @param e
     */
    void onProcessTimeout(ExecuteException e);
}
