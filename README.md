# java样例代码
```java
      //一个简单的ping命令封装
      @Getter
      @Setter
      public class PingCommandLine implements CommandArguments {
 
          @Arguments(defaultValue = "4",args = {"-n"})
          private Long number;
          @Arguments(required = false,args = {"-w"})
          private Long timeout;
          @Arguments(defaultValue = "false",args = {"-4"})
          private boolean ipv4;
          @Arguments(defaultValue = "false",args = {"-6"})
          private boolean ipv6;
          @Arguments
          private String ip;
 
          @Override
          public String command() {
              return "ping";
          }
      }
      
    
```

```java
    // 获取CommandLine命令行对象
    public static void main(String[] args) {
      // 使用这个命令行
      PingCommandLine ping = new PingCommandLine();
      ping.setIp("192.168.1.91");
      ping.setIpv4(true);
      // 解析为CommandLine命令行对象
      CommandLine commandLine = CommandArgumentsParse.parse(ping);
      // [ping, -n, 4, -4, 192.168.1.91]
      System.out.println(commandLine.toString()); 
    }
```

```java
    // 运行命令行
    public static void main(String[]args){
        PingCommandLine ping = new PingCommandLine();
        ping.setIp("192.168.1.91");
        ping.setIpv4(true);
        CommandLineThreadBuild.commandLine(ping).timeout(6000L).exec();
        Thread.sleep(30000L);
    }
```

```java
    // 通过线程池运行命令行程序
    public static void main(String[]args){
        PingCommandLine ping1 = new PingCommandLine();
        ping1.setIp("192.168.1.91");
        ping1.setIpv4(true);
        PingCommandLine ping2 = new PingCommandLine();
        ping2.setIp("192.168.1.90");
        //设置每次ping的时候超时时间
        ping2.setTimeout(200L);
        //设置ping 6次
        ping2.setNumber(6);

        Thread thread1 = CommandLineThreadBuild.commandLine(ping1).timeout(6000L).build();
        Thread thread2 = CommandLineThreadBuild.commandLine(ping2).timeout(10000L).build();
        LinkedBlockingQueue queue = new LinkedBlockingQueue();
        ExecutorService executorService = new ThreadPoolExecutor(1, 3, 0, TimeUnit.SECONDS, queue);
        executorService.execute(thread1);
        executorService.execute(thread2);
    }
```
