package com.marsh.exec.interfaces;

import com.marsh.exec.parse.CommandArgumentsParse;

/**
 * 构建一个命令行对象
 * @see com.marsh.exec.annotations.Arguments
 * @see CommandArgumentsParse#parse(CommandArguments) 方法解析成命令行
 * @author marsh
 * @date 2022年04月12日 13:41
 */
public interface CommandArguments {

    /**
     * 命令的名字，例如：ping
     */
    String command();

}
