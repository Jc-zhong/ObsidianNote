## 1、加载源码到 Android Studio

执行以下四条命令：

```shell
# ( source 可以用 . 代替 ，即 . build/envsetup.sh )
. build/envsetup.sh 
# ( 选择要编译的项目 )
lunch 
# ( 这里的 -j4 表示用 4 线程来编译，可不加 )
make idegen -j4 
sudo development/tools/idegen/idegen.sh
```

执行完后会生成  `android.iml 和 android.ipr` 两个文件
`android.ipr` : 相当于是 AS 的工程文件，后面打开 AS 的时候，导入项目选择 android.ipr 即可 
`android.iml` : 可以修改一些规则，屏蔽掉不需要导入的文件

![[03-add-code-as-01.png|150x50]]

![[03-add-code-as-02.png|150]]


部署完之后，可以在 AS 中 `File -> Project Structure -> module -> android` 中，
开放所需要的代码，如 **kernel**

![[03-add-code-as-03.png|200x50]]

另外 **Dependencies** 中需要配置对应的 API 版本

![[03-add-code-as-04.png]]

## 2、查看当前 AOSP 源码的版本

如果没有指定版本，如何知道下载好的 AOSP 是什么版本？

找到 `build/make/core/version_defaults.mk` 文件打开，搜索 **PLATFORM_SDK_VERSION**

比如找到 **PLATFORM_SDK_VERSION := 28** ，从 SDK 版本可以知道 AOSP 版本是 9.0