package com.marsh.exec.build;

import com.marsh.exec.callback.StateCallback;
import com.marsh.exec.executor.CommandLineExecutor;
import com.marsh.exec.interfaces.CommandArguments;
import com.marsh.exec.parse.CommandArgumentsParse;
import com.marsh.exec.stream.LineOutputListener;
import com.marsh.exec.stream.LineOutputStream;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * 命令行线程构建工具类可配合线程池进行任务限流操作
 *
 * Thread thread1 = CommandLineThreadBuild.commandLine(CommandArguments接口对象).timeout(100L).build();
 * Thread thread2 = CommandLineThreadBuild.commandLine(CommandArguments接口对象).timeout(100L).build();
 *
 * 方式1：
 * thread1.start();
 * thread2.start();
 *
 * 方式2：
 * LinkedBlockingQueue queue = new LinkedBlockingQueue();
 * ExecutorService executorService = new ThreadPoolExecutor(1, 3, 0, TimeUnit.SECONDS, queue);
 * executorService.execute(thread1);
 * executorService.execute(thread2);
 * @see com.marsh.exec.annotations.Arguments 关于命令行构建可参考这个注解
 * @author Marsh
 * @date 2022-04-12日 10:45
 */
public class CommandLineThreadBuild {

    /**
     * 命令行
     */
    private CommandLine commandLine;
    /**
     * 任务超时时间
     */
    private Long timeout;
    /**
     * 任务执行过程回调
     */
    private StateCallback stateCallback;
    /**
     * 任务执行过程中输入输出流处理器
     */
    private ExecuteStreamHandler streamHandler;


    private CommandLineThreadBuild(CommandLine commandLine) {
        this.commandLine = commandLine;
    }


    /**
     * 通过静态方法构建命令行
     *
     * @param commandLine
     * @return
     */
    public static CommandLineThreadBuild commandLine(CommandLine commandLine) {
        return new CommandLineThreadBuild(commandLine);
    }

    /**
     * 通过静态方法构建命令行
     *
     * @param commandArguments
     * @return
     */
    public static CommandLineThreadBuild commandLine(CommandArguments commandArguments) {
        return new CommandLineThreadBuild(CommandArgumentsParse.parse(commandArguments));
    }

    /**
     * 设置超时时间
     *
     * @param timeout
     * @return
     */
    public CommandLineThreadBuild timeout(Long timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * 设置输入输出流
     *
     * @param streamHandler
     * @return
     */
    public CommandLineThreadBuild streamHandler(ExecuteStreamHandler streamHandler) {
        this.streamHandler = streamHandler;
        return this;
    }


    /**
     * 设置自定义事件回调
     *
     * @param stateCallback
     * @return com.marsh.exec.build.CommandLineThreadBuild
     * @author Marsh
     * @date 2022-04-13
     */
    public CommandLineThreadBuild stateCallback(StateCallback stateCallback) {
        this.stateCallback = stateCallback;
        return this;
    }

    /**
     * 构建一个命令行线程任务,后续通过start()运行或者线程池来进行运行
     * @return
     */
    public Thread build() {
        CommandLineExecutor executor = new CommandLineExecutor();
        if (stateCallback != null) {
            executor.setCallback(stateCallback);
        }
        if (timeout != null && timeout > 0) {
            executor.setWatchdog(new ExecuteWatchdog(timeout));
        }
        if (streamHandler != null) {
            executor.setStreamHandler(streamHandler);
        } else {
            // 替换默认的PumpStreamHandler对象出现字符串乱码问题
            executor.setStreamHandler(new PumpStreamHandler(new LineOutputStream(new LineOutputListener() {
                @Override
                public void processLine(String line) {
                    System.out.println(line);
                }
            }), new LineOutputStream(new LineOutputListener() {
                @Override
                public void processLine(String line) {
                    System.err.println(line);
                }
            })));
        }
        return executor.createThread(commandLine);
    }

    /**
     * 构建一个命令行线程任务并且立即执行
     */
    public void exec(){
        build().start();
    }
}
