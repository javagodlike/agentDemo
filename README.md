# Java Instrumentation指南

<a name="e05dce83"></a>
#### 简介
它提供了向现有编译的Java类添加字节码的功能。使用 Instrumentation，开发者可以构建一个独立于应用程序的代理程序（Agent），用来监测和协助运行在 JVM 上的程序，甚至能够**替换和修改某些类的定义**。开发者就可以实现更为灵活的运行时虚拟机监控和 Java 类操作了，这样的特性实际上提供了一种虚拟机级别支持的 AOP 实现方式，使得开发者无需对 JDK 做任何升级和改动，就可以实现某些** AOP **的功能了。<br />　　“java.lang.instrument”包的具体实现，依赖于 **JVMTI**。JVMTI（Java Virtual Machine Tool Interface）是一套由 Java 虚拟机提供的，为 JVM 相关的工具提供的本地编程接口集合。JVMTI 是从 Java SE 5 开始引入，整合和取代了以前使用的 Java Virtual Machine Profiler Interface (JVMPI) 和 the Java Virtual Machine Debug Interface (JVMDI)，而在 Java SE 6 中，JVMPI 和 JVMDI 已经消失了。JVMTI 提供了一套”代理”程序机制，可以支持第三方工具程序以代理的方式连接和访问 JVM，并利用 JVMTI 提供的丰富的编程接口，完成很多跟 JVM 相关的功能<br />

<a name="ed9b56b2"></a>
#### Java Agent
简介中提到Instrumentation可以构建Java Agent，有如下两种方式
* static – makes use of the _premain_ to load the agent using -javaagent option
* dynamic – makes use of the _agentmain _to load the agent into the JVM using the [Java Attach API](https://docs.oracle.com/javase/7/docs/jdk/api/attach/spec/com/sun/tools/attach/package-summary.html)

<a name="ffe90605"></a>
#### Static Load 静态加载
首先假设一个业务场景，我们要从ATM机取款，但是之前的代码没有添加耗时统计功能，我们首先使用静态加载的方式来完成这项工作。

先定义一个业务类
```java
public static void main(String[] args) throws InterruptedException {
  while (true) {
    Atm.withdraw(10);
    TimeUnit.SECONDS.sleep(10);
  }
}
```

具体的取钱实现
```java
public class Atm {
    public static void withdraw(int money) {
        System.out.println("取钱 " + money);
    }
}
```

静态加载实现
```java
public static void premain(String agentArgs, Instrumentation instrumentation) {
  System.out.println("Executing premain ...");
  instrumentation.addTransformer(new StaticTransformer());
}
```
静态加载使用_premain_方法，该方法将在任何应用程序代码运行之前运行

StaticTransformer具体字节码修改实现
```java
public class StaticTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer)
        throws IllegalClassFormatException {

        byte[] byteCode = classfileBuffer;

        if ("Atm".equals(className)) {
            System.out.println("Instrumenting......");
            try {
                ClassPool classPool = ClassPool.getDefault();
                CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(
                    classfileBuffer));
                CtMethod[] methods = ctClass.getDeclaredMethods();
                for (CtMethod method : methods) {
                    method.addLocalVariable("startTime", CtClass.longType);
                    method.insertBefore("startTime = System.nanoTime();");
                    method.insertAfter("System.out.println(\"Time consumed "
                        + "(nano sec): \"+ (System.nanoTime() - startTime) );");
                }
                byteCode = ctClass.toBytecode();
                ctClass.detach();
                System.out.println("Instrumentation complete.");
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        return byteCode;
    }
}
```

静态加载的输出内容：
```
Executing premain ...
Instrumenting......
Instrumentation complete.
取钱 10
Time consumed (nano sec): 105750
取钱 10
Time consumed (nano sec): 95082
```

<a name="6d795bf7"></a>
#### Dynamic Load 动态加载
仍然使用ATM机取款的例子，这次使用动态加载
```java
public static void agentmain(String agentArgs, Instrumentation instrumentation) throws UnmodifiableClassException {
        System.out.println("DynamicAgent invoked.");
        instrumentation.addTransformer(new StaticTransformer(), true);
        instrumentation.retransformClasses(Atm.class);
    }
```

动态加载的输出内容：
```
取钱 10
DynamicAgent invoked.
Instrumenting......
Instrumentation complete.
取钱 10
Time consumed (nano sec): 205647
取钱 10
Time consumed (nano sec): 168807
```


在本文中，我们讨论了Java Instrumentation API。我们研究了如何静态和动态地将Java代理加载到JVM中。<br />

<a name="aafb2b14"></a>
#### 具体运行步骤如下：
1. mvn clean install
1. 静态加载方式
  1. AtmClient的VM options 增加-javaagent:StaticAgent/target/agent.jar=XXX
  1. 运行AtmClient，可以看到静态加载方式的运行结果
3. 动态加载方式
  1. 移除AtmClient的VM options
  1. 运行AtmClient
  1. 运行AttachTest
