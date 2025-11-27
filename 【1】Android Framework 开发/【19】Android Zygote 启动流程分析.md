
## 1、Zygote 简单介绍

在 **Android** 系统中，**普通应用程序进程** 以及运行系统的服务 **system_server 进程** 都是由 **Zygote 进程** fork 出来的，它也叫做 `孵化器` 。它通过 **Linux** 中的 fork 形式创建 **应用程序进程 和 system_server**。由于 **Zygote 进程** 在启动的时候会创建 java 虚拟机环境，因此通过  fork 而创建的 **应用程序进程 或 system_server 进程** 可以在内部获取 java 虚拟机环境，不需要单独为每一个进程创建 java 虚拟机环境。

## 2、Zygote 启动脚本

`init.rc` 是以 **import** 方式来引入各个模块的 rc 文件的，包括前面讲过的 **bootanimation、surfaceflinger** 等也是，**Zygote** 同样也是。
路径： `system/core/rootdir/init.zygote32.rc`
这里有多个 zygote.rc ，是因为 Android 系统支持 64位 和 32位 的原因。
此处分析 32位 这个文件

```shell
# 关键在第一句：
# 根据前面分析过的 bootanimation，大概得知 Zygote 进程名称为 zygote
# 执行程序为 app_process，class name 为 main
# 因此接下来去分析 app_process 的源码即可

service zygote /system/bin/app_process -Xzygote /system/bin --zygote --start-system-server
    class main
    priority -20
    user root
    group root readproc reserved_disk
    socket zygote stream 660 root system
    socket usap_pool_primary stream 660 root system
    onrestart exec_background - system system -- /system/bin/vdc volume abort_fuse
    onrestart write /sys/power/state on
    # NOTE: If the wakelock name here is changed, then also
    # update it in SystemSuspend.cpp
    onrestart write /sys/power/wake_lock zygote_kwl
    onrestart restart audioserver
    onrestart restart cameraserver
    onrestart restart media
    onrestart restart --only-if-running media.tuner
    onrestart restart netd
    onrestart restart wificond
    task_profiles ProcessCapacityHigh MaxPerformance
    critical window=${zygote.critical_window.minute:-off} target=zygote-fatal
```

## 3、 app_process 中的 app_main.cpp 源码分析

一般可以通过 **framework** 目录下  `grep app_process` 即可得知 **app_process** 的源码位置
```shell
grep app_process ./ -rn

# 结果 :  ./app_process/Android.mk:45:LOCAL_MODULE:= app_process
# 代码路径 : frameworks/base/cmds/app_process/ 
```

[app_main.cpp - Android Code Search](https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/cmds/app_process/app_main.cpp)

```c++
....
	if (zygote) {
		// 此处会调用一系列方法（包括 JNI ）
		// 最终调用到 Java 层的 ZygoteInit 类中的 main 函数
		runtime.start("com.android.internal.os.ZygoteInit", args, zygote);
	} else if (!className.empty()) {
		runtime.start("com.android.internal.os.RuntimeInit", args, zygote);
	} else {
		fprintf(stderr, "Error: no class name or --zygote supplied.\n");
		app_usage();
		LOG_ALWAYS_FATAL("app_process: no class name or --zygote supplied.");
	}
....
```

## 4、ZygoteInit 源码分析

路径： `framworks/base/core/java/com/android/internal/os/ZygoteInit.java`

[ZygoteInit.java - Android Code Search](https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/java/com/android/internal/os/ZygoteInit.java)

```java
public static void main(String argv[]) {
	....
		// Zygote goes into its own process group.
        try {
	        // 此处就是将 Zygote 进程 pid 号设置为 0 、 pgid 也设置为 0
            Os.setpgid(0, 0);
        } catch (ErrnoException ex) {
            throw new RuntimeException("Failed to setpgid(0,0)", ex);
        }
        // else 见源码
        // preload() : 加载系统的类、资源、openGL、共享库等等
        // -> preload(bootTimingsTraceLog); -> preloadClasses(); 
        // preloadClasses 方法中会用到一个文件 /system/etc/preloaded-classes
        // 这个文件应该是在编译的时候 在当前模块文件系统 拷贝到 Android 系统文件中
        // -> 【A】
        
        // -> zygoteServer = new ZygoteServer(isPrimaryZygote);
        
        // -> forkSystemServer(abiList, zygoteSocketName, zygoteServer);
	....
}
```

【A】具体加载了一些什么系统的类，我们可以找一下

```shell
cd frameworks
find -name "preloaded-classes"
# 结果 : frameworks/base/config/preloaded-classes
```

至此我们可以看到 Zygote 起来之后做的事情有
1. 预加载系统的类、资源、openGL、共享库等等
2. 注册 Zygote 为 Socket 的服务端
3. fork system_server 进程


















