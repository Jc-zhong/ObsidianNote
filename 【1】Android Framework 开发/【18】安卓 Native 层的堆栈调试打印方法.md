
## 前言

0. 为什么要打印函数堆栈？
-> 打印调用堆栈可以把问题发生时的函数调用关系打出来，非常有利于理解 **函数调用关系**。
-> 比如 函数 **A** 可能被 **B\C\D** 调用，如果只看代码， **B\C\D** 谁调用 **A** 都有可能，如果打印出调用堆栈，直接就把谁调用的给打印出来了。
-> 不仅如此，打印函数调用堆栈还有另一个好处。**在 Android 代码里，函数命名很多雷同的，虚函数调用，几个类里的函数名相同等** ，即使用 source insight 工具看也未必容易看清函数调用关系。如果使用堆栈打印，很容易看到函数调用逻辑。

## 堆栈调试打印方法

c++代码架构经常特别复杂，可以使用  **android::CallStack** 将所在线程的调用栈打印出来
基本用法：
1. 进入对应的 cpp 文件，放开 LOG 的权限
```c++
// 放开 #define LOG_NDEBUG 0 注释，且变成 #define LOG_NDEBUG 1
// 如在 `frameworks/base/cmds/bootanimation` 中
// 将 #define LOG_NDEBUG 0 修改为 #define LOG_NDEBUG 1
```

2. 声明头文件
```c++
// 如在 `frameworks/base/cmds/bootanimation/BootAnimation.cpp` 中
// 将以下头文件声明在对应的 cpp 文件中
#include <utils/CallStack.h>
#include <utils/Log.h>
```

3. 调用方法
```c++
// 如在 `frameworks/base/cmds/bootanimation/BootAnimation.cpp` 中
// 在该类下任意一个你想要追踪的方法中，添加以下方法
// 如在 Bootanimation::movie() 方法中添加
ALOGE("Bootanimation::movie start @@@@@@@@@ ");
android::CallStack callStack;
callStack.update();
callStack.log("Bootanimation::movie");   // 输出到 logcat
```

4. 配置编译规则
```c++
// mk 或者 bp 中需要链接以下 so 库
libutils
libcutils
```

5. 整编编译 & 验证
```c++
make
emulator

adb shell
logcat -b all | grep "Bootanimation"

```

调试日志：

![[18-log1.png]]


![[18-log2.png]]


