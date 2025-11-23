
从  [[【11】开机动画的结束流程]]   笔记可得知，**Android 默认的开机动画**是在 `BootAnimation.cpp` 里的 `android()` 方法中，使用 **openGL 绘制** 来实现的。

我们来看 **Android 默认的开机动画** 是怎么实现的 -> 分析 `BootAnimation::android()` 方法
**（见附件文件的  L792  ）**

![[11-BootAnimation-cpp.cpp]]

在 `frameworks/base/core/res/assets/images` 目录下有两张图片，分别是：
**android-logo-mask.png**   // android logo 字样图，中间是镂空的

![[11-android-logo-mask.png]]

**android-logo-shine.png**   // 扫光图，实现文件渐变效果的

![[11-android-logo-shine.png]]

开机动画的大概原理就是将 **android-logo-shine.png**  放在屏幕底下，
然后将 **android-logo-mask.png** 盖在扫光图之上，之后将扫光图从左往右慢慢移动就实现了开机动画的 **Android 文字渐变** 的效果。

openGL 绘制的大致流程为：
生成纹理  ->  绑定纹理  ->  对纹理设定2D数据  ->  纹理参数设定  ->  绘制  ->  释放纹理

![[11-openGL-processon.png]]













