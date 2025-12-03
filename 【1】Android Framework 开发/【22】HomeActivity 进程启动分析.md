
system_server 启动 FallbackHome 篇

```shell
# 补充知识：
# Android 日志分为 main 、 system 、 event 、 radio 这几种类型
# 平常我们使用的 logcat 日志是属于 main 日志
# 如果我们需要看 event 类型的日志，则需要添加 logcat 参数
logcat -b event
# 结果: am_pss / am_kill / am_proc_died 等等
```

前面 **笔记 【21】** 了解到，在 `startOtherServices()` 中不仅仅会启动系统服务，还会调用各个服务的 `systemReady()` 方法，这里需要分析第一个 **Activity** 的启动，也就是 **Launcher** 
此处我们从 **AMS** 开始分析

[ActivityManagerService.java - Android Code Search](https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java;drc=61197364367c9e404c7da6900658f1b16c42d0da;bpv=0;bpt=0;l=8961)

路径 : 
`frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java`
```java
public void systemReady(final Runnable goingCallback, TimingsTraceLog tracelog) {
	....
	// mAtmInternal.startHomeOnAllDisplays(currentUserId, "systemReady");
	startHomeActivityLocked(currentUserId, "systemReady");
	mStackSupervisor.resumeFocusedStackTopActivityLocked();
	....
}
```

这里省略了大部分非需分析的代码，我们可以看到 `startHomeActivityLocked()` 方法被调用，从名字可以看得出它要启动 **Home 类型的 Activity**
**Launcher 就是一种 Home 类型的 Activity** ，但这里其实并不是 Launcher ，而是设置中的 **FallbackHome ，它也是一个 Home 类型的 Activity** ，这里 **FallbackHome** 是 google 新加入的，主要是因为涉及整个 Android 系统的加密等原因，系统在还没有完全解锁前，不可以启动 **Launcher** ，因为 **Launcher** 中明显和 **各个第三方应用** 耦合较多（比如桌面可能显示着一堆各个应用的 Widget），如果直接将 **Launcher** 作为 **FallbackHome** 启动，相对就会要求 **Launcher** 以来的应用也是支持解密类型，这肯定是不显示的。
因此系统就启动了 **FallbackHome** （一个什么也不显示的界面来作为启动真正 **Launcher** 的一个过度）

接着我们就来分析一下是怎么启动 **FallbackHome** ，又是凭什么确定就是它的？

```java
// -> startHomeActivityLocked()
// -> getHomeIntent()  设置 intent category 为 android.intent.category.HOME
// 为后续查到 HOME 类型的 Activity 做准备
// -> resolveActivityInfo() 根据 intent 去查到 HOME 类型的 Activity 
// 注意这里它是找不到 Launcher 应用的，而是匹配到一个 FallbackHome 的 空 Activity
// 而后等待系统解锁\解密状态，FallbackHome 去启动 Launcher ，然后结束自身程序
// -> mActivityStarter.startHomeActivityLocked()
// -> mLastHomeActivityStartResult = startActivityLocked()
// -> mLastStartActivityResult = startActivity() 
// -> ActivityRecord r = new ActivityRecord()
// -> return startActivity()
// -> result = startActivityUnchecked()
// -> mTargetStack.startActivityLocked()
// -> mSupervisior.resumeFocusedStackTopActivityLocked()
// -> mStackSupervisor.startSpecificActivityLocked()
// -> mService.startProcessLocked()
// -> return startProcessLocked()
// -> startProcessLocked()
// -> entryPoint = "android.app.ActivityThread"  重点
// -> startResult = Process.start()
// -> zygoteProcess.start()
// -> return startViaZygote()
// -> return zygoteSendArgsAndGetResult()
// -> 至此， zygoteSendArgsAndGetResult 这里就调用了 BufferWriter 的 writer 对象进行了 socket 的写入数据
// -> zygoteSocket.connect(socketAddress)  这里的 socketAddress 实际就是 "zygote"
// -> zygoteInputStream = new DataInputStream()
// -> zygoteWriter = new BufferedWriter()
// -> 
```

```java
// 知识补充：
// 我们在研究代码时（如 AMS ），需要一些日志辅助查看
// 此时我们可以在 ActivityManagerDebugConfig.java 中
// （路径：frameworks/base/services/core/java/com/android/server/am/ ）
// 把 DEBUG_ALL 属性置为 true :  static final boolean DEBUG_ALL = true;
// 这样我们就可以打开 AMS 的日志辅助信息来研究代码
```

![[22-image1.png]]

**AMS** 把要创建一个进程的主要参数等准备好，然后发送给 **zygote** 端，**zygote** 端接收到发来的数据后创建对应的进程。进创建对应的进程的同时，还会执行传过来的 `entryPoint` 即 `"android.app.ActivityThread"` 类的 `main` 方法

接下来看看分析之前 **Zygote** 部分的接收和创建过程，由前面的笔记分析得知，**zygote** 本身是一直循环执行 `runSelectLoop` 里面的 `while` 循环的 ：

```java
Runnable runSelectLoop(String abiList) {
	....
	while(true) {
		if(i == 0) {
			// 如果是本身 serverSocket 的 fd 有输入消息，则保存住新建连接的客户端 peer
			// 和 fd ，比如：第一次 system_server / AMS 就需要绑定
			ZygoteConnection newPeer = acceptCommandPeer(abiList);
			peers.add(newPeer);
			fds.add(newPeer.getFileDesciptor);
		} else {
			// 如果哪个 peer 或者 fd 有消息了，则使用 processOneCommand 读取里面消息
			ZygoteConnection connection = peer.get(i);
			final Runnable command = connection.processOneCommand(this);
			if(mIsForkChild) {
				
			}
		}
	}
	....
}
```

```java
// -> connection.processOneCommand()
// -> args = readArgumentList()  读取接收到的数据
// -> parsedArgs = new Arguments(args)  解析数据
// -> pid = Zygote.forkAndSpecialize() 根据解析到的数据创建好新的进程
try {
	if(pid == 0) {
		// in child
		zygoteServer.setForkChild();
		ztgotrServer.closeServerSocket();
		IoUtils.closeQuietly(serverPipeFd);
		serverPipeFd = null;
		return handleChildProc(parsedArgs, descriptors, childPipeFd);
	} else {
		// in the parent, A pid < 0 indicates a failure and will be handled
		// in handleParentProc
		IoUtils.closeQuietly(childPipeFd);
		childPipeFd = null;
		handleParentProc(pid, descriptors, serverPipeFd);
		return null;
	}
}
// -> return handleChildProc()
// -> return ZygoteInit.zygoteInit()
// -> return RuntimeInit.applicationInit()
// -> return findStaticMain()  在这里就会去找 entryPoint 这个类的 main 方法去执行
// ( entryPoint = "android.app.ActivityThread" )
// 这也就是我们每个 app 创建时为什么都会走 ActivityThread 的 main 方法
// caller = zygoteServer.runSelectLoop(abiList)
// caller.run()
// -> ActivityThread::main()  如果是 system_server 的话 使用的是 systemMain
// -> thread.attach(false)    如果是 system_server 的话 使用的是 attach(true)
// -> final IActivityManager mgr = ActivityManager.getService()
// -> mgr.attachApplication(mAppThread)  
// -> 到这里我们就可以知道 app 的创建为什么是从 application 开始的
// -> 这里我们代入 FallbackHome 来继续研究
// -> 笔记 【23】
```









