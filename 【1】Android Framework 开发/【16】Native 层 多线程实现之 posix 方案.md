
## 前言：为什么要有线程

一个进程可以有多个线程，这个进程本身也叫做主线程。通常主线程分配任务给子线程做。程序设计的时候就可以某一时刻不止做一件事情，每一个线程处理各自独立的任务。

多个线程自动可以访问相同的存储地址空间和文件描述符。

每个线程都包含表示执行环境所必须的信息，其中包括线程 ID 、一组寄存器、栈、调度优先级和策略、信号屏蔽字、errno 变量及线程私有数据。一个进程所有信息对该进程所有线程共享，包括可执行代码、程序的全局内存和堆内存、栈以及文件描述符。

## 1、线程 ID 

每一个线程都有一个线程 ID
```c++
// 用于关于表示线程 ID 数据结构
pthread_t 

// 用于比较线程
pthread_equal (pthread_t __thread1, pthread_t __thread2);  

// 用于返程线程 ID
pthread_t pthread_self(void);
```

## 2、线程创建

```c++
pthread_create()

/*
* __newthread     : 函数成功返回将 ID 存储在此变量中
* __attr          : 定制线程属性
* __start_routine : 函数指针
* __arg           : 传递给函数的函数
**/
int pthread_create(pthread_t *__restrict__newthread,
				const pthread_attr_t *__restrict_attr,
				void *(*__start_routine)(void *),
				void *__restrict__arg);
```

## 3、线程终止

如果进程中的任意线程调用了 **exit**  、 **\_Exit**  ，那么整个进程就会终止。
并且如果信号的默认动作就是终止进程，那么收到该信号的进程也会终止。

单个线程可以通过 **3 种** 方式退出，因此可以在不终止整个进程的情况下，停止它的控制流。
1. 线程可以简单地从启动例程中返回，返回值是线程的退出码。
2. 线程可以被同一进程的其他线程取消。
3. 线程调用 **pthread_exit**

## 4、实战演练

1. 模块代码 和 mk 文件
（在源码下任意路径创建 `android_thread` 目录，然后把下面文件拷贝到该目录下）

![[16-thread_posix.c]]

![[16-Android 1.mk]]


2. 编译模块
若报错了可通过 **man** 指令查询缺少的头文件
```sh
make linux_thread
make android_thread
man abort
man exit
```

3. 编译完成后拉起 **模拟器**，然后将编译生成的文件 **push** 到 **模拟器** 中，然后再执行该文件
```
emulator
adb push out/tartget/product/xxx/system/bin/linux_thread data/local/

adb shell
cd data/local
ll
./linux_thread
```

日志输出：

![[16-log1.png|200x200]]


















