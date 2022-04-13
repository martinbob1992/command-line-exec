package com.marsh.exec.callback;

import org.apache.commons.exec.ExecuteException;

/**
 * 简单的命令行执行状态回调实现类
 * @author Marsh
 * @date 2022-04-13日 11:24
 */
public class SimpleStateCallback implements StateCallback{

    @Override
    public boolean onProcessBefore() {
        return true;
    }

    @Override
    public void onProcessComplete(int exitValue) {

    }

    @Override
    public void onProcessFailed(ExecuteException e) {
        throw new RuntimeException(e);
    }

    @Override
    public void onProcessTimeout(ExecuteException e) {
        System.err.println("任务执行超时!控制台返回值:"+e.getExitValue());
    }
}
