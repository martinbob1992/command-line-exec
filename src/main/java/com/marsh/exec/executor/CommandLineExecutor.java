package com.marsh.exec.executor;

import cn.hutool.core.util.ReflectUtil;
import com.marsh.exec.callback.SimpleStateCallback;
import com.marsh.exec.callback.StateCallback;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.exec.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 自定义了一个命令行的实现类，主要扩展了回调事件及增加了创建线程已达到配合线程池使用
 * @author Marsh
 * @date 2022-04-12日 17:43
 */
public class CommandLineExecutor extends DefaultExecutor {

    @Getter
    @Setter
    private StateCallback callback  = new SimpleStateCallback();
    @Getter
    @Setter
    private ExecuteWatchdog watchdog;
    private static Method setProcessNotStartedMethod = ReflectUtil.getMethod(ExecuteWatchdog.class, "setProcessNotStarted");
    private IOException exceptionCaught;

    /** worker thread for asynchronous execution */
    @Getter
    private Thread executorThread;

    public void execute(final CommandLine command, final Map<String, String> environment,
                        final ExecuteResultHandler handler) throws ExecuteException, IOException {
        this.executorThread = createThread(command,environment,handler);
        getExecutorThread().start();
    }

    /**
     * 方法线程不安全，该方法请保证不要重复调用
     * @param command
     * @return
     */
    public Thread createThread(final CommandLine command){
        return createThread(command,null,callback);
    }

    /**
     * 方法线程不安全，该方法请保证不要重复调用
     * @param command
     * @param environment
     * @param handler
     * @return
     */
    @SneakyThrows
    public Thread createThread(final CommandLine command, final Map<String, String> environment, final ExecuteResultHandler handler){
        if (getWorkingDirectory() != null && !getWorkingDirectory().exists()) {
            throw new IOException(getWorkingDirectory() + " doesn't exist.");
        }

        if (watchdog != null) {
            ReflectUtil.invoke(watchdog,setProcessNotStartedMethod);
        }

        final Runnable runnable = new Runnable()
        {
            public void run()
            {
                int exitValue = Executor.INVALID_EXITVALUE;
                try {
                    boolean flag = true;
                    if (handler instanceof StateCallback){
                        flag = ((StateCallback)handler).onProcessBefore();
                    }
                    if (flag){
                        exitValue = executeInternal(command, environment, getWorkingDirectory(), getStreamHandler());
                        handler.onProcessComplete(exitValue);
                    }
                } catch (final ExecuteException e) {
                    if (handler instanceof StateCallback){
                        if (watchdog != null && watchdog.killedProcess()) {
                            // it was killed on purpose by the watchdog
                            ((StateCallback)handler).onProcessTimeout(e);
                        } else {
                            handler.onProcessFailed(e);
                        }
                    } else {
                        handler.onProcessFailed(e);
                    }
                } catch (final Exception e) {
                    handler.onProcessFailed(new ExecuteException("Execution failed", exitValue, e));
                }
            }
        };
        return createThread(runnable, "Exec Default Executor");
    }


    /**
     * Execute an internal process. If the executing thread is interrupted while waiting for the
     * child process to return the child process will be killed.
     *
     * @param command the command to execute
     * @param environment the execution environment
     * @param dir the working directory
     * @param streams process the streams (in, out, err) of the process
     * @return the exit code of the process
     * @throws IOException executing the process failed
     */
    private int executeInternal(final CommandLine command, final Map<String, String> environment,
                                final File dir, final ExecuteStreamHandler streams) throws IOException {

        setExceptionCaught(null);

        final Process process = this.launch(command, environment, dir);

        try {
            streams.setProcessInputStream(process.getOutputStream());
            streams.setProcessOutputStream(process.getInputStream());
            streams.setProcessErrorStream(process.getErrorStream());
        } catch (final IOException e) {
            process.destroy();
            throw e;
        }

        streams.start();

        try {

            // add the process to the list of those to destroy if the VM exits
            if (this.getProcessDestroyer() != null) {
                this.getProcessDestroyer().add(process);
            }

            // associate the watchdog with the newly created process
            if (watchdog != null) {
                watchdog.start(process);
            }

            int exitValue = Executor.INVALID_EXITVALUE;

            try {
                exitValue = process.waitFor();
            } catch (final InterruptedException e) {
                process.destroy();
            }
            finally {
                // see http://bugs.sun.com/view_bug.do?bug_id=6420270
                // see https://issues.apache.org/jira/browse/EXEC-46
                // Process.waitFor should clear interrupt status when throwing InterruptedException
                // but we have to do that manually
                Thread.interrupted();
            }

            if (watchdog != null) {
                watchdog.stop();
            }

            try {
                streams.stop();
            }
            catch (final IOException e) {
                setExceptionCaught(e);
            }

            closeProcessStreams(process);

            if (getExceptionCaught() != null) {
                throw getExceptionCaught();
            }

            if (watchdog != null) {
                try {
                    watchdog.checkException();
                } catch (final IOException e) {
                    throw e;
                } catch (final Exception e) {
                    throw new IOException(e.getMessage());
                }
            }

            if (this.isFailure(exitValue)) {
                throw new ExecuteException("Process exited with an error: " + exitValue, exitValue);
            }

            return exitValue;
        } finally {
            // remove the process to the list of those to destroy if the VM exits
            if (this.getProcessDestroyer() != null) {
                this.getProcessDestroyer().remove(process);
            }
        }
    }


    /**
     * Close the streams belonging to the given Process.
     *
     * @param process the <CODE>Process</CODE>.
     */
    private void closeProcessStreams(final Process process) {

        try {
            process.getInputStream().close();
        }
        catch (final IOException e) {
            setExceptionCaught(e);
        }

        try {
            process.getOutputStream().close();
        }
        catch (final IOException e) {
            setExceptionCaught(e);
        }

        try {
            process.getErrorStream().close();
        }
        catch (final IOException e) {
            setExceptionCaught(e);
        }
    }

    private IOException getExceptionCaught() {
        return this.exceptionCaught;
    }

    private void setExceptionCaught(final IOException e) {
        if (this.exceptionCaught == null) {
            this.exceptionCaught = e;
        }
    }

}
