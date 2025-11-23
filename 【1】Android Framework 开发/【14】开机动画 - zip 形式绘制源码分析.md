## 1、 zip 包的制作

**zip** 中需包含 开机动画所需播放的文件，参考以下格式来存放，另外还需要存放一个名为 `desc.txt` 的文件。（ **里面需要根据您存放的开机动画图片来写一些描述** ）

![[14-zip-create-process-1.png|200x200]]

`desc.txt` 文件中的内容参考 `frameworks/base/cmds/bootanimation/FORMAT.md` 文件描述
（见附件文件中的 **L13~L71** 内容）

![[14-FORMAT-md.java]]

接着我们解析`desc.txt` 文件中的内容
```sh
// desc.txt 示例
1080 360 60
c 1 0 part0 #ffee00 c c
c 0 0 part1 #ffee00 c c
c 1 0 part2 #ffee00 c c
c 1 1 part3 #ffee00 c c
c 1 0 part4 #ffee00 c c
```

```sh
<width> <height> <fps>
<type> <count> <pause> <path> [<color>] [CLOCK1] [CLOCK2]

L23~L33 :   文件的第一行需规定 WIDTH HEIGHT FPS ，也就是开机动画图片的 宽、高、帧率

一般情况下包含前面四种信息即可，后面两种是可选的
TYPE COUNT PAUSE PATH [#RGBHEX CLOCK]

L41~L71 :   第一个字符 [TYPE] 可选" p "、" c "、" f "
	        p : 这部分将会一直播放，直到开机启动完成后才会打断
			c : 这部分将会一直播放，不管如何都会执行到播放结束为止
			f : 与“p”指令相同，但额外增加了在持续播放时对指定帧数进行淡出处理的功能。
			    仅对首个被中断的“f”片段实施淡出操作，后续其他“f”片段将被直接跳过。
			    
			第二个数字 [COUNT] 可选 0 、 1
			0 : 无限循环
			1 : 只循环一次
			
			第三个数字 [PAUSE] 自定义暂停时间，单位为帧
			0 : 表示不暂停
			1 : 暂停1帧。（以60fps为例，1帧 ≈ 16ms ）
			
			第四个字符串 [PATH] 文件夹名称
			指的是这个部分的动画存储在 bootanimation.zip 中的哪个文件夹里。
			系统会按照文件名顺序（如 0000.png, 0001.png, .... ）播放所有图片
			
			第五个字符串 [RGBHEX] 背景颜色（可选）
			如果省略，默认为黑色 #000000
			如果不省略，则以输入的颜色填充背景，如 #ffee00 黄色
			
			第六个字符和第七个字符 [CLOCK1] [CLOCK2] 为坐标参数
			CLOCK1 和 CLOCK2 是用于手表设备显示时间的可选参数。
```

## 2、 源码分析

若以 zip 包形式来播放开机动画，则在 `BootAnimation.cpp` 里的 threadLoop() 方法中
```cpp
bool BootAnimation::threadLoop() {
	....
    if (mZipFileName.isEmpty()) {
        ALOGD("No animation file");
        result = android();  // 这里是 Android 默认的开机动画，以帧动画绘制实现
    } else {
	    // 以 zip 包形式播放开机动画，关键代码就在于 movie() 函数里
        result = movie();
    }
    ....
}
```

接着我们就来分析 `BootAnimation::movie()` 这个方法
（见附件 **L330** 开始）

![[14-Bootanimation.cpp]]

















