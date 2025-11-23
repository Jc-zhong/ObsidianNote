## 目标：实现以 zip 包替换修改 Android 开机动画

![[15-bootani-zip-processon.png]]

## 1、 准备 part 动画资源

准备好各 part 部分的动画资源，注意图片分辨率尽量保持一致
## 2、 编写 desc.txt

```c++
<width> <height> <fps>
<type> <count> <pause> <path> [<color>] [CLOCK1] [CLOCK2]
```

## 3、 存储方式打包 bootanimation.zip

注意：打包一定要用 **存储方法** 打包
我们可以通过以下指令进行打包（`进入到准备好的资源文件路径下执行`）
```sh
zip -r -X -Z stroe bootanimation part*/* desc.txt
```
## 4、 预置到 /system/media

**这一步需要在 Android.mk 进行 cp 操作**

比如：我们可以在 `frameworks/base/cmds/bootanimation/` 目录下的 **Android.mk** 文件中添加

```mk
$shell cp $(LOCAL_PATH)/bootanimation.zip $(ANDROID_PRODUCT_OUT)/system/media/bootanimation.zip)
```

写好 mk 文件后就可以进行编译了，编译完成后，可以在 
`out/tartget/product/xxx/system/media` 目录下看下文件有没有被打包过来

## 5、 其他注意点

如果是使用 **PRODUCT_COPY_FILES** 方式来进行拷贝的话，需要注意
### 5.1 **设置权限**

```
PRODUCT_COPY_FILES += \
    vendor/path/to/file:system/etc/file:0644 \
    vendor/path/to/executable:system/bin/executable:0755 \
    vendor/path/to/config:system/etc/config:0600
    
## 权限格式说明
0644：所有者可读写，其他用户只读 
0755：所有者可读可写可执行，其他用户可读可执行
0600：仅所有者可读写
```

### 5.2 **SELinux 权限**

 在 device.mk 或 sepolicy 中
 ```
 # 文件上下文标签
file_contexts:
/system/etc/file u:object_r:system_file:s0
/system/bin/executable u:object_r:system_file:s0
 ```

自定义 SELinux 策略
```
# 在 .te 文件中添加
allow your_domain system_file:file { read write execute };
```


