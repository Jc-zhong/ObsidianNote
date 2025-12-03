
参考blog传送门 ： [Android跨进程通信：图文详解 Binder机制 原理](https://carsonho.blog.csdn.net/article/details/73560642)

---

![[25-image1.png]]

![[25-image2.png]]

[Activity.java - Android Code Search](https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/java/android/app/Activity.java?q=activity.java&ss=android%2Fplatform%2Fsuperproject%2Fmain:frameworks%2Fbase%2F)
[SystemServiceRegistry.java - Android Code Search](https://cs.android.com/android/platform/superproject/+/android-latest-release:frameworks/base/core/java/android/app/SystemServiceRegistry.java;l=308?q=SystemServiceRegistry)

以下来看看通过 `getSystemService` 方法是怎么拿到对应的 `binder` 对象的

```java
// Activity.java 中 getSystemService
// -> return super.getSystemService(name)  - Activity.java 
// -> return getBaseContext().getSystemService(name) 
// -> public abstract Object getSystemService(@ServiceName @NonNull String name)
// -> return SystemServiceRegistry.getSystemService(this, name) - ContextImpl.java
// -> public static Object getSystemService(@NonNull ContextImpl ctx, String name) 
// ContextImpl 调用了 SystemServiceRegistry 的 getSystemService 方法
// -> 而在这个 getSystemService 方法中，是在其一个集合中找到对应的 service 的
// -> 这就说明 系统的服务 都会预先被注册&存根才这个集合中
// -> 此时我们搜索对应的服务名，如 batterymanager 即可找到注册的地方 
// -> registerServices()
```

---















