
**修改自己的第一行代码，在框架运行**

## 1、安装查看源码的工具

安装 vim :  sudo apt-get install vim
下载 android studio :  http:/www.android-studio.org/
注意选择的型号， ubuntu 系统需要选择 Linux 版的

## 2、初始化仓库

因为压缩包只有代码，没有 git 仓库，而 git 仓库空间太大，只需要对会修改的地方建立仓库即可

cd frameworks
git init
git add *
git commit -m "init first version"

如果还没有设置 git 邮箱等
git config --global user.email ["test@example.com"](mailto:"test@example.com")
git config --global user.name "test"

## 3、添加自己的一句 log 打印在 framework 上

cd framework/base
find -name Activity.java
vi ./core/java/android/app/Activity.java

在 onCreate 中添加一句
android.util.Log.i("test1","Activity is onCtrate");

( 注意：修改 frameworks 代码时，需要给指定的类加上对应的包名，不要让代码工具自动导包，尽量把修改范围缩小一点 )

回到根目录
make framework
make systemimage

编译完成后执行：

. build/envsetup.sh
lunch sdk_phone_x86_64
emulator
