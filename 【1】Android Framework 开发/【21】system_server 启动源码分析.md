
在 **笔记 【20】** 中分析得知，**system_server** 是由 **Zygote** 调用 `Native` 层的方法，然后最终调用到 **Linux** 层的 `fork()` 方法才被创建出来的。
在 `Zygote.forkSystemServer` 返回后看代码如下：
```java
// frameworks/base/core/java/com/android/internal/os/ZygoteInit.java
if(pid == 0) {
	// 这里就是 system_server fork 成功之后执行的操作
	if(hasSecondZygote(abiList)) {
		waitForSecondaryZygote(socketName);
	}
	
	zygoteServer.closeServerSocket();
	return handleSystemServerProcess(parsedArgs);
}
```

由于 **system_serve**r 是复制 **Zygote** 的进程，因此也包含 **Zygote** 的 `zygoteServer`，对于 **system_server** 没有其他作用，需要将其先关闭，通过调用 `handleSystemServerProcess()`

```shell
# 查看系统环境变量的指令
env
# 结果 :
# SYSTEMSERVERCLASSPATH=/system/framework/services.jar:/system/framework/ethernet-service.jar:/system/framework/wifi-service.jar:/system/framework/com.android.location.provider.jar
```

[ZygoteInit.java - Android Code Search](https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/java/com/android/internal/os/ZygoteInit.java)

```java
// frameworks/base/core/java/com/android/internal/os/ZygoteInit.java
// -> handleSystemServerProcess()
// -> cl = createPathClassLoader()
// -> ZygoteInit.zygoteInit()
// -> ZygoteInit.nativeZygoteInit()   启动 binder 线程池
// 这里先调用 ZygoteInit.nativeZygoteInit()，这是一个 JNI 方法，实现在 AndroidRuntime 里面，最后调到 app_main.cpp 的 onZygoteInit() ，来启动 binder 线程池
// -> 【A】
// -> onZygoteInit()
// -> RuntimeInit.applicationInit()  【B】
// -> findStaticMain()  -> return Runnable
// -> ZygoteInit::main()
```

```shell
# 【A】
# 这里我们需要找一下 ZygoteInit.nativeZygoteInit() 的实现在哪里
cd frameworks
grep "_nativeZygoteInit" ./ -rn
# 结果 :
# frameworks/base/core/jni/AndroidRuntime.cpp:222 static void com_android_internal_os_zygoteInit_nativeZygoteInit(JNIEnv* env, jobject clazz)
# 结束 -> 返回
```

分析完上述代码得知，**ZygoteInit 最终会利用反射的机制去调用某个 class 类中的方法**
因此我们需要找到这个 `class` 是什么

```java
// 【B】 在 RuntimeInit.applicationInit() 方法中调用了 findStaticMain()
// 而这个 findStaticMain(args.startClass, args.startArgs, classLoader) 的参数
// args.startClass 来自于 applicationInit 的传参
// applicationInit() 是被 ZygoteInit.zygoteInit() 所调用的
// ZygoteInit.zygoteInit() 是被 handleSystemServerProcess() 调用的
// 得知 这个 args.startClass 来自于 handleSystemServerProcess 中传递过来的
// -> handleSystemServerProcess() 中的 parsedArgs.remainingArgs
// 接着追踪得知 parsedArgs.remainingArgs 是由 forkSystemServer() 方法调过来的
// -> 在 forkSystemServer() 中，结合代码分析得知:
// args.startClass 实际就是 system_server 的包名 "com.android.server.SystemServer"
```

至此我们就可以知道：**ZygoteInit 最终会利用反射机制去调用 com.android.server.SystemServer 这个类的 main 方法**

[SystemServer.java - Android Code Search](https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/services/java/com/android/server/SystemServer.java;drc=61197364367c9e404c7da6900658f1b16c42d0da;bpv=0;bpt=0;l=1110)

```java
// 路径 : frameworks/base/services/java/com/android/server/SystemServer.java
// -> SystemServer().run();
private void run() {
....
	// 创建 Loop 对象
	Looper.prepareMainLoop();
	
	// 加载系统 android_servers 的库 【C】
	System.loadLibrary("android_servers");
	// 创建系统的 Context
	createSystemContext();
	// 创建 system service manager.
	mSystemServiceManager = new SystemServiceManager(mSystemContext);
	mSystemServiceManager.setRuntimeRestart(mRuntimeRestart);
	LocalServices.addService(SystemServiceManager.calss, mSystemServiceManager);
	....
	// *** 以下是重点中的重点 ***
	// 启动引导服务
	startBootstrapServices();
	// 启动核心服务
	startCoreServices();
	// 启动其他服务
	startOtherServices();
	// ************************
	....
	Looper.loop();
....	
}
// 从上面可看出， system_server 的主线程和普通 app 主线程一样，是一个 Loop 管理的消息循环
// createSystemContext() 通过 Context 这个纽带来获取一些进程的信息环境
// 将 mSystemServiceManager 放入到了 LocalServices.addService() 中，这里的 SystemServiceManager 和在 binder 中常见的 ServiceManager 是不一样的
// SystemServiceManager 只是 system_server 中的一个类，它负责保存各个 SystemService 的全局变量，本身不涉及跨进程通信，而 ServiceManager 可以与 binder 跨进程等是强关联
```

```shell
# 【C】 找一下这个 android_servers 的库在哪里
cd frameworks
grep "android_servers" ./ -rn
# 结果 :
# frameworks/base/services/Android.mk:68:LOCAL_MODULE:= libandroid_servers
cd frameworks/base/services/
```

接着我们分析重点代码 **startBootstrapServices()**

```java
	....
	// Activity manager runs the show.
	t.traceBegin("StartActivityManager");
	// TODO: Might need to move after migration to WM.
	// 此处 ActivityTaskManagerService.Lifecycle 是一个的静态内部类，
	// 并且继承于 SystemService 类
	// 它在构造方法中会 new 一个 ActivityManagerService() 对象
	ActivityTaskManagerService atm = mSystemServiceManager.startService(
			ActivityTaskManagerService.Lifecycle.class).getService();
	mActivityManagerService = ActivityManagerService.Lifecycle.startService(
			mSystemServiceManager, atm);
	// 将 mSystemServiceManager 和 installer 传递给 AMS
	mActivityManagerService.setSystemServiceManager(mSystemServiceManager);
	mActivityManagerService.setInstaller(installer);
	mWindowManagerGlobalLock = atm.getGlobalLock();
	t.traceEnd();
	....
	
	// -> 【D】
	// Set up the Application instance for the system process and get started.
	t.traceBegin("SetSystemProcess");
	mActivityManagerService.setSystemProcess();  
	t.traceEnd();
```

[ActivityManagerService.java - Android Code Search](https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java;drc=61197364367c9e404c7da6900658f1b16c42d0da;bpv=0;bpt=0;l=1959)

```java
// 【D】 setSystemProcess()
	....
	// 在此处将 AMS 注册到 ServiceManager 中
	ServiceManager.addService(Context.ACTIVITY_SERVICE, 
	this, /* allowIsolated= */ true,DUMP_FLAG_PRIORITY_CRITICAL | 
		DUMP_FLAG_PRIORITY_NORMAL | DUMP_FLAG_PROTO);
	ServiceManager.addService(ProcessStats.SERVICE_NAME, mProcessStats);
	....
```

至此，我们可以看出，
1. **SystemServiceManager** 会调用 `startService()` 启动服务
2. 此时就会创建出对于的 **IBinder** 对象（如 **AMS** ）
3. 启动后会 **SystemServiceManager** 将该服务通过 `ServiceManager.addService()` 方法添加到 **ServiceManager** 的 **DNS** 中
4. **第三方应用** 或其他系统 **APP** 就可以通过访问 `ServiceManager.getService()` 方法与其通信

![[21-image1.png]]

```shell
# 我们可以通过 services list 指令查看系统有哪些服务
adb shell
services list
services list | grep "activity"
# 结果 : activity: [android.app.IActivityManager]
```

```java
// -> mActivityManagerService.systemReady()
....
// -> startCoreServices();  -> startOtherServices();
....
// 至此之上，系统必要的服务都已经启动完毕，以下是开始启动 app 进程了
// It is now time to start up the app processes...  (在 startOtherServices() 中)
// -> startSystemUi(context, windowManagerF);
....
```

## 总结：

1. **system_server** 把系统的所有服务分为了 `3 类` 进行启动
2. **startBootstrapServices() 启动引导服务**，从它的注释得知：系统服务之间存在着非常复杂的依赖关系，所以需要把一些重要的系统服务放在第一位启动，也就是引导服务。另外两个也是启动服务，只是构造 service 方法上有细微差别，此处不挨个具体分析了
3. **startCoreServices() 启动核心服务** 
4. **startOtherServices() 启动其他服务** ，这里不仅仅启动了一些服务，启动完 **Service** 后，还会调用各个 **Service** 的 `systemReady()` 方法
5. 最终进入到 **system_server** 的 `loop` 循环，等待接收消息处理。






















