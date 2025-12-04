
## 1、拷贝编译模拟器刷机所需要的 img 文件

**编译生成物的路径：  out/target/product/generic_x86_64/**

![[07-log1.png]]

**给模拟器刷机需要的 img 文件**

|                       |                   |                     |
| --------------------- | ----------------- | ------------------- |
| **encryptionkey.img** | **kernel-ranchu** | **ramdisk.img**     |
| **system-qemu.img**   | **usderdata.img** | **vendor-qemu.img** |

**讲其拷贝到 Windows 上（ 通过 共享文件（映射网络驱动器） 的方式 ）**

![[07-log2.png]]

## 2、在 Android Studio 中创建虚拟设备

### 2.1、点击 AVD Manager 创建虚拟设备

![[07-avd1.png|300x200]]

![[07-avd2.png|300x200]]

选择 phone Pixel 1920x1080

![[07-avd3.png|300x200]]

在 x86 Images 下找到与我们编译出来最相近的机型，如 API 27 / ABI x86_64  / Android 8.1

![[07-avd4.png|300x200]]

然后给设备取名字  MyPhone   接着  Finish 等待创建

![[07-avd5.png|300x200]]

创建完后暂时不要点击启动

![[07-avd6.png|300x200]]



### 2.2、接着在 SDK 路径下找文件，确认虚拟设备已创建完成

xxx/sdk/.android/avd/MyPhone.avd/

![[07-avd7.png|200x200]]

![[07-avd8.png|200x200]]

接着再继续找  xxx/sdk/system-images/android-27/default/x86_64/

![[07-avd9.png|200x200]]

到此可看出这里面的 img 文件 就是我们需要用 共享文件（映射网络驱动）里的 img  替换掉的文件。也就是以下 6 个文件替换，直接覆盖即可**
（另外可以在拷贝之前备份一下所需替换的东西，以作对比）**

![[07-avd10.png|200x200]]

![[07-avd11.png|200x200]]


## 3、启动模拟器验证

此处我们在  xxx/sdk/emulator/  目录下启动命令行
输出指令：  ./emulator -avd MyPhone

![[07-avd12.png|300x200]]

**但此时报错提示 sdk/avd/  目录下没有找到所需启动的 AVD 设备**
**此时 我们将 sdk\.android\avd  的 MyPhone 设备拷贝一份到  sdk/avd/  目录下**

![[07-avd13.png|200x200]]

  ![[07-avd14.png|200x200]]

拷贝完成后我们再输入指令拉起模拟器：  ./emulator -avd MyPhone

 ![[07-avd15.png|300x200]]


---

如果需要重置 AVD 设备的状态，我们可以到对应目录下删掉所需文件

cd sdk/.android/avd/MyPhone.avd/

rm \*.qcow2

删除完后再次启动模拟器即可

![[07-avd16.png|300x200]]

![[07-avd17.png|300x200]]


