## 1、实战体验 Linux 的 fork 方法

直接上代码体验 fork 创建新的子进程

```c
// fork.c
#include <unistd.h>
#include <stdio.h>

int main(void) {
	int myPid;
	int count = 0;
	printf("main current process pid = %d ", getpid());
	
	myPid = fork();
	if(myPid == 0) {
		// 子进程执行的操作 myPid = 0 
		printf("child process pid = %d | ppid = %d \n", getpid(),getppid());
	} else {
		// 主进程执行的操作 myPid 为 fork 出来的 子进程 pid
		printf("this process current pid = %d | myPid = %d | ppid = %d \n",
			 getpid(), myPid, getppid());
	}
	
	int i = 0;
	while(1){
		sleep(10);
		i++;
		if(i >= 60) {
			printf("time out , break while");
			break;
		}
	}
	return 0;
}
```

因为是属于 Linux 的代码，只需要在 ubuntu 上运行即可

```shell
gcc fork.c -o fork
./fork
# 结果 : 
# 主进程输出 : main current process pid = 11160  
# 主进程输出 : this process current pid = 11160 | myPid = 11161 | ppid = 11135
# 子进程输出 : child process pid = 11161 | ppid = 11160
# 主进程号       11160  
# 子进程号       11161 
# 主进程的父进程  11135

# 这里需要注意的是，子进程被 fork 出来后，会同步 父进程 的代码执行（以调用 fork() 为起点）
# 这就能解释通为什么 子进程没有前面第一句 log 输出，只有后面的 log
```

## 2、Zygote fork 进程的源码分析

路径： `frameworks/base/core/java/com/android/internal/os/ZygoteInit.java` 

此处就接着 **笔记 【19】** 最后的 **forkSystemServer**

[ZygoteInit.java - Android Code Search](https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/java/com/android/internal/os/ZygoteInit.java)

```java
// -> forkSystemServer()
// -> pid = Zygote.forkSystemServer()
// -> pid = nativeForkSystemServer()  这是一个 JNI 函数，此时要到 Native 层去追踪
// 路径： frameworks/base/core/jni/com_android_internal_os_Zygote.cpp
// -> pid = zygote::ForkCommon()
// -> pid_t pid = fork();
  if (pid == 0) {
    // 子进程执行的操作
    if (is_priority_fork) {
      setpriority(PRIO_PROCESS, 0, PROCESS_PRIORITY_MAX);
    } else {
      setpriority(PRIO_PROCESS, 0, PROCESS_PRIORITY_MIN);
    }
    ....
  } else if (pid == -1) {
	// 创建子进程失败的情况
    ALOGE("Failed to fork child process: %s (%d)", strerror(errno), errno);
  } else {
    // 主进程执行的操作
	ALOGD("Forked child process %d", pid);
  }
// 回到 ZygoteInit.java 中
// -> caller = zygoteServer.runSelectLoop(abiList);
// -> ZygoteConnection newPeer = acceptCommandPeer(abiList);
// -> pid = Zygote.forkAndSpecialize()
// -> int pid = nativeForkAndSpecialize() 这里又是一个 JNI 函数
// 路径： frameworks/base/core/jni/com_android_internal_os_Zygote.cpp
// -> pid = zygote::ForkCommon()
// -> SpecializeCommon()
```








