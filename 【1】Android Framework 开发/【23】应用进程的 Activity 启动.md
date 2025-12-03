
当应用进程被创建后，最开始执行的就是 **ActivityThread** 的 `main` 方法
以下我们接着以 **FallbackHome** 为例

[ActivityThread.java - Android Code Search](https://cs.android.com/android/platform/superproject/+/android-latest-release:frameworks/base/core/java/android/app/ActivityThread.java?q=activityThread)

路径： `frameworks/base/core/java/android/app/ActivityThread.java`

```java
public static void main(String[] args) {
	....
	Looper.prepareMainLooper();
	Process.setArgV0("<pre-initialized>");
	// Find the value for {@link #PROC_START_SEQ_IDENT} 
	// if provided on the command line.
	// It will be in the format "seq=114"
	long startSeq = 0;
	if (args != null) {
		for (int i = args.length - 1; i >= 0; --i) {
			if (args[i] != null && args[i].startsWith(PROC_START_SEQ_IDENT)) {
				startSeq = Long.parseLong(
						args[i].substring(PROC_START_SEQ_IDENT.length()));
			}
		}
	}
	ActivityThread thread = new ActivityThread();
	thread.attach(false, startSeq);
	if (sMainThreadHandler == null) {
		sMainThreadHandler = thread.getHandler();
	}
	if (false) {
		Looper.myLooper().setMessageLogging(new
				LogPrinter(Log.DEBUG, "ActivityThread"));
	}
	// End of event ActivityThreadMain.
	Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
	Looper.loop();
	throw new RuntimeException("Main thread loop unexpectedly exited");
}
// 这里主要是对 loop 进行初始化，然后再创建 ActivityThread 对象并调用它的 attach 方法
```

注意：到这只是 **FallbackHome** 的进程被创建&启动了，但是它的 **Activity** 还没有被创建）

接着来分析 **ActivityThread** 的 `attach` 方法

```java
// -> thread.attach(false, startSeq)
// -> mgr.attachApplication(mAppThread)
// 这里主要应用端会获取 system_server 中的 AMS 对象（IBinder 对象），然后调用 attachApplication 方法，这个方法的真正实现是在 ActivityManagerService
// -> attachApplicationLocked() 到 AMS 中
// -> app = mPidSelfLocked.get(pid)
// -> thread.bindApplication() 这里 AMS 通过跨进程通信调用 ActivityThread 的 bindApplication 方法，而这个方法又属于 ActivityThread 的内部类 ApplicationThread 中的方法
// -> ApplicationThread::bindApplication() 到 ActivityThread 中
// -> sendMessage(H.BIND_APPLICATION, data) 这里使用了 handle 发送消息
// 待 handle 接收到消息之后处理
// -> handleBindApplication()
// -> final LoadedApk pi = getPackageInfo()
// -> mInstrumentation = (Instrumentation)  构造 Instrumentation
// -> mInstrumentation.onCreate() 
// -> mInstrumentation.callApplicationOnCreate(app)
// -> app.onCreate() 
// 这里的 Instrumentation 是一个抽象对象
// 到这里我们就可以知道，为什么我们 app 应用在被创建时会回调 onCreate 方法

// -> 我们回到 AMS 中的 thread.bindApplication() 地方
// -> mStackSupervisor.attachApplicationLocked()
// -> realStartActivityLocked()
// -> app.thread.scheduleLaunchActivity() 这里 AMS 通过跨进程通信调用 ActivityThread 的 scheduleLaunchActivity 方法
// -> ActivityThread::scheduleLaunchActivity() 到 ActivityThread 中
// -> sendMessage(H.LAUNCH_ACTIVITY, r)
// -> handleLaunchActivity()
// -> Activity a = performLaunchActivity()  构造 Activity
// -> handleResumeActivity()
// 至此我们就分析完了应用进程是如何启动的
```

总结流程图：

![[23-image1.png]]







