package com.marsh.exec.annotations;

import java.lang.annotation.*;

/**
 * 封装一个命令行对象注解
 *
 *     构建一个简单的ping命令
 *     @Getter
 *     @Setter
 *     public class PingCommandLine implements CommandArguments {
 *
 *         @Arguments(defaultValue = "4",args = {"-n"})
 *         private Long number;
 *         @Arguments(required = false,args = {"-w"})
 *         private Long timeout;
 *         @Arguments(defaultValue = "false",args = {"-4"})
 *         private boolean ipv4;
 *         @Arguments(defaultValue = "false",args = {"-6"})
 *         private boolean ipv6;
 *         @Arguments
 *         private String ip;
 *
 *         @Override
 *         public String command() {
 *             return "ping";
 *         }
 *     }
 *
 *     // 使用这个命令行
 *     PingCommandLine ping = new PingCommandLine();
 *     ping.setIp("192.168.1.91");
 *     ping.setIpv4(true);
 *     // 解析为CommandLine命令
 *     CommandLine commandLine = CommandArgumentsParse.parse(ping);
 *     System.out.println(commandLine.toString()); // [ping, -n, 4, -4, 192.168.1.91]
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Arguments {

    /**
     * 是否必填
     */
    boolean required() default true;

    /**
     * 默认值
     */
    String defaultValue() default ValueConstants.DEFAULT_NONE;

    /**
     * 绑定的参数
     */
    String[] args() default {};
}
