## 一、ADB 简介

ADB 是 Android Debug Bridge 的缩写，是一个多功能命令行工具，用于与 Android 设备（真机或模拟器）进行通信。它是 Android SDK 的一部分。

### 安装和配置

通过 Android Studio 安装：
- 安装 Android Studio → SDK Manager → Android SDK Platform-Tools
- ADB 路径：[SDK目录]/platform-tools/adb

独立安装：
- 下载 Platform-Tools：[https://developer.android.com/studio/releases/platform-tools](https://developer.android.com/studio/releases/platform-tools)
- 解压后添加到系统环境变量 PATH 中

验证安装：
adb version

---
## 二、设备连接管理

### 1. 查看连接设备

查看已连接的设备列表
adb devices
adb devices -l

输出示例：
List of devices attached
emulator-5554 device
84B5T15A09012345 device

### 2. 连接模拟器

模拟器通常自动连接，端口规则：
- 第一个模拟器：emulator-5554
- 第二个模拟器：emulator-5556
- 以此类推...

### 3. 连接真机（USB调试）

启用USB调试后，通过USB连接
adb devices
// 如果设备未识别，可能需要安装设备驱动

### 4. 无线连接（Wi-Fi调试）

首先通过USB连接设备
adb devices
adb tcpip 5555 # 设置设备监听5555端口
  
断开USB，通过Wi-Fi连接（需要设备IP地址）
adb connect 192.168.1.100:5555

断开无线连接
adb disconnect 192.168.1.100:5555

### 5. 设备选择

当连接多个设备时，需要指定目标设备，使用-s参数指定设备
adb -s emulator-5554 shell

设置默认设备
adb -d shell # 默认USB设备
adb -e shell # 默认模拟器

---
## 三、应用管理

### 1. 安装应用

基本安装
adb install app.apk

覆盖安装（保留数据）
adb install -r app.apk

允许测试包安装
adb install -t app.apk

授予所有权限
adb install -g app.apk

多设备安装到指定设备
adb -s device_id install app.apk

### 2. 卸载应用

卸载应用（需要包名）
adb uninstall com.example.app

卸载但保留数据和缓存
adb uninstall -k com.example.app

### 3. 应用包管理

查看已安装的所有包
adb shell pm list packages

查看第三方应用包
adb shell pm list packages -3

查看系统应用包
adb shell pm list packages -s

按关键字搜索包
adb shell pm list packages | grep keyword

查看应用的安装路径
adb shell pm path com.example.app

清除应用数据
adb shell pm clear com.example.app

### 4. 应用信息

查看应用详细信息
adb shell dumpsys package com.example.app

查看当前前台应用
adb shell dumpsys window windows | grep -E 'mCurrentFocus'

查看应用版本信息
adb shell dumpsys package com.example.app | grep version

---
## 四、文件操作

### 1. 文件传输

推送文件到设备
adb push local_file.txt /sdcard/

从设备拉取文件
adb pull /sdcard/file.txt ./

推送整个文件夹
adb push local_folder/ /sdcard/

拉取整个文件夹
adb pull /sdcard/folder/ ./

### 2. 文件浏览和管理

进入设备shell进行文件操作
adb shell

### 3. 在shell中的常用文件命令
ls -la # 列出文件
cd /sdcard # 切换目录
pwd # 显示当前路径
rm file.txt # 删除文件
cp src dest # 复制文件
mv old new # 移动/重命名文件

---
## 五、系统操作和调试

### 1. 日志查看

查看实时日志
adb logcat

查看日志并过滤标签
adb logcat -s TAG_NAME

查看包含某个关键字的日志
adb logcat | grep "keyword"

查看特定进程的日志
adb logcat --pid=$(adb shell pidof com.example.app)

清除日志缓冲区
adb logcat -c

将日志保存到文件
adb logcat > log.txt

### 2. 系统信息

查看设备信息
adb shell getprop

查看特定属性
adb shell getprop ro.product.model # 设备型号
adb shell getprop ro.build.version.sdk # API级别
adb shell getprop ro.build.version.release # Android版本

查看CPU信息
adb shell cat /proc/cpuinfo

查看内存信息
adb shell cat /proc/meminfo

查看存储空间
adb shell df -h
### 3. 进程管理

查看运行中的进程
adb shell ps

查看某个应用的进程
adb shell ps | grep com.example.app

强制停止应用
adb shell am force-stop com.example.app

杀死进程
adb shell kill pid_number

### 4. 屏幕操作

截屏
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png ./

录屏（需要Android 4.4+）
adb shell screenrecord /sdcard/demo.mp4
按Ctrl+C停止录制

录屏带参数
adb shell screenrecord --size 1280x720 --time-limit 30 /sdcard/demo.mp4

---

## 六、Activity 和 Intent 操作

### 1. 启动Activity

启动应用的主Activity
adb shell am start -n com.example.app/.MainActivity

启动Activity并传递数据
adb shell am start -a android.intent.action.VIEW -d "[http://example.com](http://example.com/)"

启动Activity并传递extra数据
adb shell am start -n com.example.app/.MainActivity -e "key" "value"

### 2. 发送广播

发送自定义广播
adb shell am broadcast -a com.example.MY_ACTION

发送广播并传递数据
adb shell am broadcast -a com.example.MY_ACTION -e "message" "hello"

### 3. 调试相关

查看当前Activity
adb shell dumpsys activity activities | grep -E "mResumedActivity|mCurrentFocus"

查看Activity栈
adb shell dumpsys activity activities

---

## 七、输入和事件模拟

### 1. 输入文本

在焦点处输入文本
adb shell input text "Hello World"
注意：不能输入中文和特殊字符，需要其他方式

### 2. 按键事件

常用键值：
// 3=HOME, 4=BACK, 5=CALL, 6=ENDCALL, 24=VOLUME_UP, 25=VOLUME_DOWN
// 26=POWER, 27=CAMERA, 82=MENU, 220=BRIGHTNESS_DOWN

adb shell input keyevent 4 # 模拟返回键
adb shell input keyevent 3 # 模拟Home键
adb shell input keyevent 26 # 电源键

### 3. 触摸和手势

点击屏幕坐标
adb shell input tap 500 500

滑动操作
adb shell input swipe 100 500 300 500 100 # 从(100,500)滑动到(300,500)，用时100ms

长按（通过延长滑动时间实现）
adb shell input swipe 500 500 500 500 1000

---

## 八、高级功能

### 1. 端口转发

将设备端口转发到本地
adb forward tcp:6100 tcp:7100           # 设备7100端口→本地6100端口

查看所有转发的端口
adb forward --list

移除端口转发
adb forward --remove tcp:6100

### 2. 备份和恢复

备份应用数据（不包含APK）
adb backup -f backup.ab -apk com.example.app

恢复备份
adb restore backup.ab

### 3. 性能监控

查看CPU使用情况
adb shell top

查看内存使用情况
adb shell dumpsys meminfo

查看电池信息
adb shell dumpsys battery

设置电池状态（测试用）
adb shell dumpsys battery set level 50 # 设置电量50%
adb shell dumpsys battery set status 2 # 设置充电状态

---

## 九、实用脚本和技巧

### 1. 批量安装APK

安装目录下所有APKfor apk in *.apk; do adb install "$apk"; done

### 2. 批量截图并拉取

adb shell screencap -p /sdcard/screen.png
adb pull /sdcard/screen.png
adb shell rm /sdcard/screen.png

### 3. 监控应用启动时间

adb shell am start -W com.example.app/.MainActivity

### 4. 查看应用性能数据

查看应用CPU占用
adb shell dumpsys cpuinfo | grep com.example.app
  
查看应用内存占用
adb shell dumpsys meminfo com.example.app

---

## 十、故障排除

### 1. 常见问题解决

重启ADB服务
adb kill-server
adb start-server

查看ADB状态
adb status

检查设备连接状态
adb get-state

等待设备连接
adb wait-for-device

### 2. 权限问题

如果遇到权限被拒绝，尝试root权限
adb root
adb remount # 重新挂载系统分区为可写

---
## 总结表格：最常用ADB命令

|   |   |   |
|---|---|---|
|类别|命令|功能描述|
|设备管理|adb devices|查看连接设备|
|应用安装|adb install app.apk|安装应用|
|文件操作|adb push/pull|文件传输|
|日志查看|adb logcat|查看系统日志|
|调试信息|adb shell dumpsys|查看系统服务信息|
|Activity|adb shell am start|启动Activity|
|输入模拟|adb shell input|模拟用户输入|
