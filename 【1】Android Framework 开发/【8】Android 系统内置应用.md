## 背景

定制系统的时候，经常会遇到有客户需求，或者是自己想要集成某一个 APP，但是又不想提供源码，只提供APK，而且要做成不卸载的 系统应用 system app

## 前言-内置应用

内置应用也分为 不带 so 和 带so 的应用，两者集成方式会有所不同
我们可以通过打开 apk 的方式，查看其是否包含 lib 目录来区分是否带 so 库

![[08-image1.png|300x200]]

  ![[08-image2.png|300x200]]
![[08-image3.png|300x200]]
  

## 1、创建模块文件夹

在 **package/apps/** 目录下创建相应的模块文件夹，如： **MyApp**

**（ 所有系统内应应用大部分都在 package/apps/ 中 ）**

![[08-image4.png|200x200]]

在我们自己的 Module 中写好 Android.mk 文件，比如：

![[08-image5.png|300x200]]

![[08-image6.png|200x200]]

![[08-image7.png|200x200]]


**注意：如果是带有 so  的 apk ，则要在 mk 文件里面声明提取 so 的位置**
**LOCAL_PREBUILT_JNI_LIBS := lib/x86_64/libnative-lib.so**

![[07-Android.mk]]

![[07-Android_SO.mk]]


以及把 编译好的 apk 放进该目录

  

## 2、添加 Module 到工程配置文件中

在 **build/make/target/product/core.mk**  加入对应的 **Module** 名字

**思考点：如何知道我们的模块是配置在哪个文件中的？**

根据网上搜索资料得知，配置编译模块的 mk 文件一般都在 build 下 或者 vendor 下

此时我们可以在 build 目录下执行搜索指令，如：

grep "Launcher2" ./ -rn

![[08-image8.png]]

这样我们就可以知道是在这个位置的 core.mk 文件进行配置的  **build/make/target/product/core.mk** 

我们还可以再 vi 查看一下该文件写了什么： **vi ./make/target/product/core.mk**

![[08-image9.png]]

![[08-image10.png|200x200]]

![[08-image11.png|200x200]]

![[08-image12.png|200x200]]

## 3、整编 make ，刷机验证

编译后，可以看到写在 mk 文件中的 打印正常输出了，并且在 out/target/product/generic_x86_64/system/app  目录下也能找到我们继承进去的 MyApp

![[08-image13.png|200x200]]

![[08-image14.png|200x200]]


执行 emulator 拉起模拟器

![[08-image15.png|200x200]]