
**电脑配置：双核，内存至少16G，硬盘空间200G以上，独立安装 Ubuntu 16.04 系统**

## **1、安装 ubuntu 16.04**

（因为这个版本可以直接安装 openjdk-8，其他版本自行找方法）

## **2、openjdk-8 的安装**

sudo apt-get install openjdk-8-jdk

## **3、设置默认 java 和  javac （可选）**

由于本人当前 ubuntu 上没有安装其他版本的 jdk ，所以跳过此步骤
sudo update-altematives --config java
sudo update-altematives --config javac

## **4、检查版本**

java -version

// 打印如下：
openjdk version "1.8.0_252"
OpenJDK Runtime Environment(build 1.8.0_252-8u252-b09-1~16.04-b09)
OpenJDK 64-Bit Server VM(build 25.252-b09,mixed mode)

## 5、安装所需要的软件安装包

sudo apt-get install git-core gnupg flex bison gperf build-essential zip curl zliblg-dev gcc-multilib g++-multilib libc6-dev-i386 lib32ncurses5-dev x11proto-core-dev libx11-dev lib32z-dev libgl1-mesa-dev libxml2-utils xsltproc unzip

or

sudo apt-get install git-core gnupg flex bison gperf build-essential zip curl zlib1g-dev gcc-multilib g++-multilib libc6-dev-i386 lib32ncurses5-dev x11proto-core-dev libx11-dev lib32z-dev libgl1-mesa-dev libxml2-utils xsltproc unzip


## 6、源码下载

### 6.1 网络下载方法：

国内不可以访问 google 故无法下载 AOSP 源码，国内清华大学源有相关镜像
参考链接：    [AOSP | 镜像站使用帮助 | 清华大学开源软件镜像站 | Tsinghua Open Source Mirror](https://mirror.tuna.tsinghua.edu.cn/help/AOSP/)

### 6.2 本地解压方式：

下载链接地址：https://pan.baidu.com/s/1Jwsrb-zwrQO-HEHo5eo9Jg
提取码：uu1j

百度云下载相关的源码包，进行本低解压，下载里面提供的 android-8.1.0_r1.7z 文件
下载完成后在 ubuntu 系统对应的下载目录启动终端命令行，输入如下指令进行解压

sudo apt-get install p7zip
7zr x android-8.1.0_r1.7z
会有一个 overwirte 提示，直接输入 y 同意即可

## 7、编译 AOSP 代码

1、  . buildnvsetup.sh       
2、  lunch
// 这里我们选择：6   --> aosp_x86_64
3、  make
// 等待编译完成，如果编译完成后，会出现如下打印：
//  build completed successfully (05:44:08(hh:mm:ss)) 

4、emulator  
// 执行拉起模拟器的命令

![[01-Emulator-01.png|200x50]]

---

## # 编译可能会碰到的问题

![[01-Build-error-01.png]]

提示  gnutls_handshake() failed: The TLS connection wa non-properly terminated 网络链接和鉴权等错误

**解决方案有两个：**

**1、直接关闭 jack 方式**

把原来的整编指令 make 改成 make ANDROID_COMPILE_WITH_JACK=fallse
这种方式就是不管什么原因 jack 导致无法编译，直接关闭 jack ，规避问题

**2、如果还想使用 jack ，就得解决问题的根本**

原因： jdk 自行更新新版本，新版本的说明为
释放说明： [https://java.com/en/dowmload/help/release_changes.html](https://java.com/en/dowmload/help/release_changes.html)
释放日期：[https://java.com/en/dowmload/help/release_dates.html](https://java.com/en/dowmload/help/release_dates.html)

![[01-java-release-msg-01.png]]



release Note:
Other notes: Disable TLS 1.0 and 1.1
TLS 1.0 and 1.1 are versions of the TLS protocol that are no longer considered secure and have been superseded by more secure and modern versions(TLS 1.2 and 1.3).
These versions have now been disabled by default. If you encounter issues, you can , at your own risk , re-enable the versions by removing "TLSv1" and/or "TLSv1.1" from the jdk.tls.disabledAlgorithms security property in the java security configuration file.

See JDK-8002343

**大概意思是，在 2021-04-20  java 8 发布了一个新版本，对 TLS进行了优化，不再支持这种不安全的 TLS 1.0 和 1.1 ，如果要开启就要去 jdk.tls.disabledAlgorithms 把它给去除**

**具体做法：**
**去   /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/java.security 文件中找到 jdk.tls.disabledAlgorithms 的 config**

![[01-java-security-file-cp.png]]

**所以我们就要删除这两个 TLSv1,TLSv1.1**
**编辑命令：   sudo vi /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/security/java.security**
**删除后进行 wq 保存，最后把系统重启一下。**

**然后再进行 make 整编，就可以编译通过了。**
**编译生成物的路径：  out/target/product/generic_x86_64/**

![[01-build-target-cp-01.png]]

**给模拟器刷机需要的 img 文件**

**encryptionkey.img**
**kernel-ranchu**
**ramdisk.img**
**system-qemu.img**
**usderdata.img**
**vendor-qemu.img**

![[01-img-file-cp-01.png]]