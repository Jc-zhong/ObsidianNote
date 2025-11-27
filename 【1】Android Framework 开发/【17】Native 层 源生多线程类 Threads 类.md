
## 前言

**Android native** 的 **Threads** 类是 **Android** 提供的一个基础类，源码路径：
`system/core/libutils/include/utils/Threads.h`
`system/core/libutils/Threads.cpp`

该类提供的基础功能涵盖了线程的生命周期：创建、运行、销毁。
主要成员函数如下：
```c++
// 0、该类本身继承于 RefBase（相当于一个智能指针），所以具有相应的一些特性
// Invoked after creation of initial strong pointer / reference
// onFirstRef() 将会在创建完一个 强指针\强引用 后被调用
// 可以理解为：当有 RefBase 这样的类被创建的时候，就会调用 onFirstRef() 方法
virtual void onFirstRef();

// 1、执行线程创建并通过 run 方法启动（和 java 有点差异）：
status_t run(const char* name, int32_t priority, size_t stack);

// 2、循环执行方法
// 创建完成后，开始执行 _threadLoop() 函数，该函数主要通过调用 threadLoop() 函数，因此
// 基类必须实现 threadLoop() 函数，作为线程执行函数，它是有返回值的方法
// 而且 _threadLoop 会根据返回值确定是否继续循环执行的方法。

// 3、线程请求退出方法
// 线程销毁，子类最好通过实现 requestExit() 函数，首先调用 Thread 类的 requestExit() 
// 函数，将线程状态 mExitPending 置为 true，然后中断 threadLoop
```

## 源码分析
`system/core/libutils/include/utils/Threads.h`
`system/core/libutils/Threads.cpp`

源码传送门：  [Threads.cpp - Android Code Search](https://cs.android.com/android/platform/superproject/main/+/main:system/core/libutils/Threads.cpp)
```c++
// 从下面的方法开始分析
status_t Thread::run(const char* name, int32_t priority, size_t stack)

// -> androidCreateRawThreadEtc
int androidCreateRawThreadEtc(android_thread_func_t entryFunction,
                               void *userData,
                               const char* threadName __android_unused,
                               int32_t threadPriority,
                               size_t threadStackSize,
                               android_thread_id_t *threadId)
// 其中 entryFunction 参数就是要执行的方法体

// -> _threadLoop
int Thread::_threadLoop(void* user){
	....
	do{
		....
		bool result;
        if (first) {
            first = false;
            self->mStatus = self->readyToRun();
            result = (self->mStatus == OK);
            
            if (result && !self->exitPending()) {
                // Binder threads (and maybe others) rely on threadLoop
                // running at least once after a successful ::readyToRun()
                // (unless, of course, the thread has already been asked to exit
                // at that point).
                // This is because threads are essentially used like this:
                //   (new ThreadSubclass())->run();
                // The caller therefore does not retain a strong reference to
                // the thread and the thread would simply disappear after the
                // successful ::readyToRun() call instead of entering the
                // threadLoop at least once.
                result = self->threadLoop();
            }
        } else {
            result = self->threadLoop();
        }
		....
	}while(strong != nullptr)
	....
}
```

## 实战应用

1. 源码文件
参考 **【16】** 笔记的工程中创建 `Main.cpp 、 MyThread.h 、 MyThread.cpp` 源码文件

![[17-MyThread.h]]

![[17-MyThread.cpp]]

![[17-Main.cpp]]

![[17-Android.mk]]

2. 编译模块
若报错了可通过 **man** 指令查询缺少的头文件
```sh
make linux_thread
make android_thread
```

3. 编译完成后拉起 **模拟器**，然后将编译生成的文件 **push** 到 **模拟器** 中，然后再执行该文件
```
emulator
adb push out/tartget/product/xxx/system/bin/android_thread data/local/

adb shell
cd data/local
ll
./android_thread
```

日志输出：
```shell
logcat | grep "MyThread"
logcat | grep 2342
```

![[17-log1.png]]


![[17-log2.png]]





















