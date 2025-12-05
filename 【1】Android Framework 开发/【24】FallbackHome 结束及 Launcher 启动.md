## 1、FallbackHome的结束

上一讲已经分析到了 **FallbackHome Activity** 的启动，但其实 **FallbackHome** 只是系统由未解密到解密过程的一个过度界面，只要用户把系统解锁过一次后，那么就一直会把系统的 **Home** 变成**Launcher** ，这个时候 **FallbackHome** 的使命就已经完成，不会再出现了。所以经常用户会发现现在手机普遍存在一个问题就是第一次解锁手机会发现有 `1-2s` 的一个空白界面，接下来才是显示**Launcher** 的一个图标界面。

![[24-image1.png]]

那接下就详细分析 **FallbackHome** 源码看看是怎么结束的  
这时候就需要去 **FallbackHome** 的 **Activity** 代码中看，
源码路径：`packages/apps/Settings/src/com/android/settings/FallbackHome.java`

![[24-FallbackHome.java]]

注册监听系统的解锁 **ACTION_USER_UNLOCKED** 广播，根据这个条件来触发检测，本质上这里其实 **FallbackHome** 就是一直在不断的检测系统中的获取的 **Home** 类型的 **Activtiy** 还是不是自己，如果不是了，比如是 **Launcher** ，那说明系统已经可以启动真正 **Launcher** 了。
但细心同学会发现，这个只看到了 **FallbackHome** finish自己，并没有看到有启动 **Launcher** 的操作？那 **Launcher** 到底是怎么启动的呢？

## 2、 Launcher 的真正启动

这里如果正面分析可能会较为困难，即使我把对应的调用流程又重新给大家写一遍对大家作用也不是很大，这里给大家用我们的老方法打日志堆栈的方式来辅助分析定位真正的 **Launcher** 是怎么启动的：
上节课已经分析了 **FallbackHome** 启动是 **AMS** 的 `startHomeActivityLocked`，那么有理由相信 **Launcher** 的启动一样会调用这个方法，所以在这个方法加入堆栈日志打印，看到了系统启动要进入 **Launcher** 时，有如下打印：

```cpp
 boolean startHomeActivityLocked(int userId, String reason) {
        android.util.Log.i("test2","startHomeActivityLocked reason = " +
		         reason,new Exception());
		//省略。。。。
}

```

![[24-image2.png]]

明显看到了这里有调用 `startHomeActivityLocked`，而且这次的 `reason` 是
`noMoreActivities resumeHomeStackTask `，那么到底是哪里调用到这个地方呢？

这时候看堆栈就可以详细准确定位出如下流程：
**FallbackHome** 请求 `finish` 自己，当然会请求执行 `onPasue` 方法：
`ActivityManagerService.activityPaused` -> 
触发调用到 `ActivityStack.activityPausedLocked` -> 
调用到 `ActivityStack.completePauseLocked` ->
调用到 `ActivityStack.finishCurrentActivityLocked` ->
调用到 `ActivityStackSupervisor.resumeFocusedStackTopActivityLocked` ->
....  ->
最后调用到了`ActivityManagerService.startHomeActivityLocked`

如上堆栈一打印里面把代码行数和方法都打印了，非常详细，相比自己去一步步分析效率高出太多，也准确许多，不用去挨个每个条件进行分析。
到了 `startHomeActivityLocked` 之后相信有前几节视频带领就不需要这里再进行分析了。























































