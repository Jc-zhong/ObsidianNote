## 1、什么是进程

进程是具有独立功能的程序，在一个数据集合上运行的过程，它是系统进行资源分配和调度的一个独立单位

## 2、如何查看 Android 进程

ps -A
查看自己进程的 pid :  ps -A | grep "packageName"

![[04-ps-A-01.png]]

## 3、Application、Activity 和进程有什么关系

Application、Activity 知识进程虚拟机的一个类对象，只是属于系统的一个组件，和进程并没有直接联系。
Android 支持为每个组件可以独立进程的方式运行

![[04-app-act-relation-01.png]]

## 4、APP 进程 system_server 进程的代码

**APP进程** ： 一般 **Android SDK** 里面的代码都是运行在 **APP** 进程的，一般 SDK 里面是 **android.jar** ，其实也就是我们编译出来的 **framework.jar** . 因为我们 **APP** 需要以来 **SDK** 才可以编译通过，说明各个 **APP** 肯定会使用 SDK 中的代码，这个代码块属于所有的 **APP** 共用，古修改其中的一个类就会影响所有的应用进程

![[04-android-jar-pwd.png]]

  

**system_servier** ： 一般 **com.android.server.*** 相关的类都是运行在 **system_server** ，这一部分平时是接触不太到的，因为普通应用根本无法引入相关的 server 代码，这些代码属于 **system_server** 特殊应用自己的代码，普通应用只能通过跨进程通信的方式与其通信获取相关数据以及接收控制，一般 **java** 代码对应的是 **services.jar**

## 5、主要 jar 的介绍

在 android 源码目录下
cd out/target/product/generic_x86_64/system/framework/

![[04-android-main-jar-01.png]]

|   |   |   |
|---|---|---|
|framework-res.apk|android 系统资源库|图片、布局、开机动画、SDK引用的控件等|
|framework.jar|android 的 SDK 核心代码||
|services.jar|框架层服务端的编译后生成的 jar 包||

![[04-appThread-ssThread.png]]

## 6、实际项目需求的修改代码原则

需要具体分析需求，按以下几步的优先级进行

1、明确业务需求，分析业务是否可以应用层面（即APP的业务代码）通过标准接口实现，如果是真实项目又同时要考虑到是否这个 APP 可能被其他的第三方应用替代，或者被卸载等

2、如果业务代码无法通过标准接口实现，则再考虑应用程序的框架修改是否可以实现，也就是前面说的 android sdk 部分，即 framework.jar ，要考虑该部分代码是运行于所有的 app ，要考虑功耗影响、稳定性，尽量把修改面变小，不可以修改 SDK 的标准接口，只能考虑新加，但是接口的具体实现可以根据情况修改，修改错误也可能黑屏

3、framework.jar 部分的框架也无法实现的时候，就需要考虑在 system_server 中去修改相关的 service 等是否可以满足，这一部分需要谨慎修改，一不小心很可能导致系统无法启动或黑屏         

![[04-app-framework-systemServer.png]]

                                                **最高优先级 -> 次优先级 -> 最低优先级**