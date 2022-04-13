package com.marsh.exec.parse;

import com.marsh.exec.annotations.Arguments;
import com.marsh.exec.annotations.ValueConstants;
import com.marsh.exec.interfaces.CommandArguments;
import org.apache.commons.exec.CommandLine;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 命令行解析工具
 * @author Marsh
 * @date 2022-04-13日 11:12
 */
public class CommandArgumentsParse {

    /**
     * 缓存对应的class类中所有标记了@Arguments注解的字段
     */
    private static final ConcurrentMap<Class, LinkedHashMap<Field, Arguments>> classFieldCache = new ConcurrentHashMap<>();

    public static CommandLine parse(CommandArguments commandArguments){
        if (commandArguments == null || commandArguments.command() == null) {
            throw new IllegalStateException("命令参数或命令名称不能为null");
        }
        LinkedHashMap<Field, Arguments> fieldCache = classFieldCache.get(commandArguments.getClass());
        if (fieldCache == null){
            fieldCache = classFieldCache.computeIfAbsent(commandArguments.getClass(),(c) ->{
                LinkedHashMap<Field,Arguments> linkedHashMap = new LinkedHashMap<>();
                Field[] declaredFields = c.getDeclaredFields();
                Arrays.stream(declaredFields).forEach(field -> {
                    Arguments arguments = field.getAnnotation(Arguments.class);
                    if (arguments != null){
                        field.setAccessible(true);
                        linkedHashMap.put(field,arguments);
                    }
                });
                return linkedHashMap;
            });
        }
        return buildCommandLine(fieldCache,commandArguments);
    }

    private static CommandLine buildCommandLine(LinkedHashMap<Field, Arguments> cache,CommandArguments commandArguments){
        String cmdName = commandArguments.command();
        final CommandLine commandLine = new CommandLine(cmdName);
        cache.forEach((field, arguments) -> {
            Object fieldValue = null;
            try {
                fieldValue = field.get(commandArguments);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (fieldValue == null && !ValueConstants.DEFAULT_NONE.equals(arguments.defaultValue())){
                fieldValue = arguments.defaultValue();
            }
            if (fieldValue == null){
                if (arguments.required()){
                    throw new RuntimeException(commandArguments.getClass() + "." + field.getName()
                            + "不能为null! 可以将required = false来避免异常");
                }
                return;
            }
            if (field.getType().isAssignableFrom(Boolean.class) || field.getType().isAssignableFrom(boolean.class)){
                // 对布尔类型的数据进行特殊处理
                if (Boolean.TRUE.equals(new Boolean(fieldValue.toString()))){
                    String[] args = arguments.args();
                    if (args == null && args.length == 0){
                        throw new RuntimeException(commandArguments.getClass() + "." + field.getName()
                                + "字段为布尔类型binder属性不能为空!");
                    }
                    commandLine.addArguments(args);
                }
            } else {
                String[] args = arguments.args();
                if (args != null && args.length > 0){
                    commandLine.addArguments(args);
                }
                commandLine.addArgument(fieldValue.toString());
            }
        });
        return commandLine;
    }
}
