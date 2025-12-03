
## 1、AOSP 常用网站

[Android Code Search](https://cs.android.com/)

[XRefAndroid - Support Android 16.0 & OpenHarmony 6.0 (AndroidXRef/AospXRef)](https://xrefandroid.com/)

[AOSPXRef - Android Source Code Cross Reference](http://aospxref.com/)

## 2、源码下载方法

[Window上如何下载阅读安卓AOSP某个模块源码_aosp代码查看-CSDN博客](https://blog.csdn.net/aqiloveyou/article/details/146897322)

---

参考文献传送门：
[Android从启动到程序运行整个过程的整理 - 融融一生 - 博客园](https://www.cnblogs.com/zyanrong/p/5661114.html)  
[Android系统架构开篇 - 知乎](https://zhuanlan.zhihu.com/p/26100298)

jar包搜索下载地址：[Maven Repository- Search-Browse-Explore](https://mvnrepository.com/)

---

**Android** 是基于 **Linux** 的一个操作系统，它可以分为五层，下面是它的层次架构图，可以记一下，因为后面应该会总结到 **SystemServer** 这些 **Application Framework** 层的东西

![[1-image1.png]]

**Android** 的五层架构从上到下依次是：`应用层，应用框架层，库层，运行时层，Linux内核层`。

![[1-image2.png]]

而在 **Linux** 中，它的启动可以归为一下几个流程：

![[1-image3.png]]

Boot Loader——>初始化内核——>。。。。。。 

当初始化内核之后，就会启动一个相当重要的祖先进程，也就是init进程，在Linux中所有的进程都是由init进程直接或间接fork出来的。

而对于Android来说，前面的流程都是一样的，而当init进程创建之后，会fork出一个Zygote进程，这个进程是所有Java进程的父进程。我们知道，Linux是基于C的，而Android是基于Java的（当然底层也是C）。所以这里就会fork出一个Zygote Java进程用来fork出其他的进程。【断点1】

总结到了这里就提一下之后会谈到的几个非常重要的对象以及一个很重要的概念。

- ActivityManagerServices（AMS）：它是一个服务端对象，负责所有的Activity的生命周期，ActivityThread会通过Binder与之交互，而AMS与Zygote之间进行交互则是通过Socket通信（IPC通信在之后会总结到）
    
- ActivityThread：它也就是我们俗称的UI线程/主线程，它里面存在一个main()方法，这也是APP的真正入口，当APP启动时，就会启动ActivityThread中的main方法，它会初始化一些对象，然后开启消息循环队列（之后总结），之后就会Looper.loop死循环，如果有消息就执行，没有就等着，也就是事件驱动模型（edt）的原理。
    
- ApplicationThread：它实现了IBinder接口，是Activity整个框架中客户端和服务端AMS之间通信的接口，同时也是ActivityThread的内部类。这样就有效的把ActivityThread和AMS绑定在一起了。
    
- Instrumentation：这个东西我把它理解为ActivityThread的一个工具类，也算是一个劳动者吧，对于生命周期的所有操作例如onCreate最终都是直接由它来执行的。
    

Android系统中的客户端和服务器的概念 

在Android系统中其实也存在着服务器和客户端的概念，服务器端指的就是所有App共用的系统服务，比如上面的AMS，PackageManagerService等等，这些系统服务是被所有的App共用的，当某个App想要实现某个操作的时候，就会通知这些系统服务。

继续断点1

当Zygote被初始化的时候，会fork出System Server进程，这个进程在整个的Android进程中是非常重要的一个，地位和Zygote等同，它是属于Application Framework层的，Android中的所有服务，例如AMS, WindowsManager, PackageManagerService等等都是由这个SystemServer fork出来的。所以它的地位可见一斑。

而当System Server进程开启的时候，就会初始化AMS，同时，会加载本地系统的服务库，创建系统上下文，创建ActivityThread及开启各种服务等等。而在这之后，就会开启系统的Launcher程序，完成系统界面的加载与显示。

【断点2】

![[1-image4.png]]

---

开机显示桌面、从桌面点击 App 图标到 Activity显示在屏幕上的过程又是怎样的呢？
下面介绍Android系统中的“画家” - SurfaceFlinger.
SurfaceFlinger 启动过程：

![[1-image5.png]]


