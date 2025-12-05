
下面我们来以最基础的 **Activity** 是怎么起来的为例，进行 **binder** 通信分析

路径： `frameworks/base/core/java/android/app/Activity.java`

[Activity.java - Android Code Search](https://cs.android.com/android/platform/superproject/+/android-latest-release:frameworks/base/core/java/android/app/Activity.java)

```java
// -> startActivity() -> startActivityForResult()
// -> Instrumentation.ActivityResult ar = mInstrumentation.execStartActivity()
// -> int result = ActivityManager.getService().startActivity()  断点【A】
// -> IActivityManager.java 
// -> public int startActivity()
// 至此，我们可以看到 IActivityManager 是一个 binder 类的对象
// 它也有内部类 Stub 和 Proxy ，我们也可以找到它对应的 AIDL 文件 - IActivityManager.aidl
```

```java
// 断点【A】
// 我们来看一下 ActivityManager.getService() 是个什么东西
public static IActivityManager getService() {
	return IActivityManagerSingleton.get();
}

private static final Singleton<IActivityManager> IActivityManagerSingleton = 
	() -> {
		// 从这可看出 AMS 是从 ServiceManager 中获取到的服务
		// 并且它是一个 IBinder 对象的类，因此它可以进行跨进程通信
		final IBinder b = ServiceManager.getService(Context.ACTIVITY_SERVICE);
		final IActivityManager am = IActivityManager.Stub.asInterface(b);
		return am;
	};
```

这里我们也可以去看看 **system_server** 中是否真的有去添加 **AMS** 服务

```shell
adb shell 
service list | grep "activity"
# 结果： activity: [android.app.IActivityManager]
```

[SystemServer.java - Android Code Search](https://cs.android.com/android/platform/superproject/+/android-latest-release:frameworks/base/services/java/com/android/server/SystemServer.java;l=1?q=systemserver.java&sq=&ss=android%2Fplatform%2Fsuperproject)
[ActivityManagerService.java - Android Code Search](https://cs.android.com/android/platform/superproject/+/android-latest-release:frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java;l=1?q=ActivityManagerService.java&ss=android%2Fplatform%2Fsuperproject)

```java
// 在源码中的 setSystemProcess 方法也是可以找到 AMS 被添加到 ServiceManager 中的
// -> mActivityManagerService.setSystemProcess();
// -> public void setSystemProcess() 
public void setSystemProcess() {
        try {
	        // 这里将 AMS 添加到了 ServiceManager
            ServiceManager.addService(Context.ACTIVITY_SERVICE, this, 
            /* allowIsolated= */ true,
                    DUMP_FLAG_PRIORITY_CRITICAL | DUMP_FLAG_PRIORITY_NORMAL | 
                    DUMP_FLAG_PROTO);
            ServiceManager.addService(ProcessStats.SERVICE_NAME, mProcessStats);
            ....
        } catch {
	        ....
        }
// 断点【A】结束，跳出
```
















