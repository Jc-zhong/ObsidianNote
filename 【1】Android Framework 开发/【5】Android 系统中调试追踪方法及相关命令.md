
![[05-aosp-ansyc-01.png]]

## 1、源码分析方法

- 主动调用型：跟着源码一步步一行行的分析（ 如 startActivity() ）
- 被动调用型：即我们可能只知道结果方法，回调方法，需要反推从哪里调用过来的 （ 如 Activity 的 onCreate ）

## 2、日志打印技巧（被动调用型）

比如：onCreate 方法是如何回调过来的？

可以在自己的 MainActivity 的 onCreate 方法中加上 Log 打印，并在 Log 函数的第三个参数添加 **异常Exception** ，打印输出方法栈。  **Log.i("test1","onCreate", new () );**

![[05-log-method-impl.png]]

![[05-testMainAct-code-01.png]]

日志分析：（在框架层 Activity.java 中的 onCreate 方法添加  日志追踪打印 ）

![[05-log-ansyc1.png|200x50]]

通过这部分日志可看出

![[05-log-ansyc2.png]]

在 **ActivityThread.java **中的  **scheduleLaunchActivity** 方法中发送了 handle 消息

![[05-ActThread-code-01.png|300x100]]

![[05-ActThread-code-02.png|300x100]]

而这个 **scheduleLaunchActivity** 方法是属于 **ApplicationThread** 类，这是一个继承了 **IApplicationThread.Stub** 类的类，说明这个方法是通过跨进程调用过来的，并不是自己触发的。

![[05-ActThread-code-03.png|300x100]]

这个时候，即使我们通过 日志打印方法栈 的方法，也无法得知是哪个进程跨进程调用过来的。

![[05-ActThread-code-04.png|300x100]]

![[05-log-ansyc3.png]]

此时我们可以通过查代码的方式继续追踪，搜索关键方法 **scheduleLaunchActivity**

```shell
# 进入到源码目录
cd framework/base
grep "\.scheduleLaunchActivity" ./ -rn
```

**（这里需要注意的是  调用方法时前面肯定会包含 . ，因此我们搜索也把该 .  加上，并为其添加转义符号 \ ）**

![[05-log-ansyc4.png]]

这样我们就可以知道 **scheduleLaunchActivity** 是在
`framework/base/services/java/com/android/server/am/ActivityStackSupervisor.java`
中的 1457/1458 行调用的

![[05-ActStackSV-code.png]]

暂时分析到这，先不继续追踪是哪里调用到 ActivityStackSupervisor 的

## 3、主动调用型（ 如 startActivity() ）

通过 ctrl + 鼠标左键 点击方法 startActivity ，追踪到 Activity.java 中

![[05-Act-code1.png]]

![[05-Act-code2.png]]

再到 Instrumentation.java 中

![[05-Act-code3.png]]

关键部分，最终到 try catch 中，调用了 ActivityManager.getService().startActivity() 方法

![[05-Act-code4.png]]

ActivityManager.java 的 getService 方法

![[05-Act-code5.png]]

ActivityManagerService.java 的 startActivity 方法

![[05-Act-code6.png]]

结合例子总结，如图所示：

![[05-exampzj.png]]

日志分析：
1. 在 联系人APP进程 的 PeopleActivity 中点击启动了另一个 ContactEditorActivity
2. 联系人APP进程 (2166)发送了一个启动 Activity 的消息给到 system_server 服务端(1527)中的 AMS 组件
3. AMS 接收到消息后，经过一系列处理，再把消息发送回给 联系人APP进程，由它自身的 ActivityThread 类处理

![[05-log-ansyc5.png]]
