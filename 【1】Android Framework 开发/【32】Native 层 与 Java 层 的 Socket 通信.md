
## Native 端作为服务端

```c++
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <arpa/inet.h>
#include <sys/select.h>
#include <sys/epoll.h>
#include <sys/un.h>
int main() {
    char *socket_path = "server-socket";
    struct sockaddr_un serun, cliun;
    socklen_t cliun_len;
    int listenfd, connfd, size;
    char buf[80];
    int i, n;
    if ((listenfd = socket(AF_UNIX, SOCK_STREAM, 0)) < 0) {
        perror("socket error");
        exit(1);
    }
    //unlink(socket_path);
    memset(&serun, 0, sizeof(serun));
    serun.sun_family = AF_UNIX;
    serun.sun_path[0] = 0;  // 代表使用抽象域名来使用 Socket
    // 注意： 设置了 sun_path 属性后，下面的字符串长度也要跟着修改
    // 如将 socket_path 拷贝到 sun_path 时，这个 + 1 就是从 sun_path[0] 之后再拷贝
    // 如 addrlen_ 大小为 sun_family + socket_path + 1 
    // sizeof() 会包含字符串结尾符， strlen() 则不会，但这里的 +1 还是 sun_path[0] 的长度
    strcpy(serun.sun_path + 1,socket_path);
    socklen_t addrlen_ = sizeof(serun.sun_family) + strlen(socket_path) + 1;
    int ret = bind(listenfd, (struct sockaddr* )&serun,addrlen_);
    if(ret == -1) {
        perror("bind");
        exit(0);
    }
    // 3. 监听
    ret = listen(listenfd, 20);
    if(ret == -1) {
        perror("listen");
        exit(0);
    }
    while(1) {
		printf("wait connect...\n");
		socklen_t l =   sizeof(struct sockaddr_un);
        int connfd = accept(listenfd, (struct sockaddr *)&cliun, &l);
        if(connfd == -1) {
            perror("accept");
            exit(-1);
        }
        printf("a new client connected! ");
        int count = read(connfd, buf, sizeof(buf));
        if(count == 0) {
	        //客户端关闭了连接
            printf("客户端关闭了连接。。。。\n");
        } else {
            if(count == -1) {
                perror("read");
                exit(-1);
            } else {
                //正常通信
                printf("client say: %s\n" ,buf);
				write(connfd,"received ok",sizeof("received ok"));
            }
        }
    }
    close(listenfd);
    return 0;
}
```

## Java 端作为客户端
（以下仅提供关键代码部分）

```java
  private void sendMessage() {
        try {
            lsocket = new LocalSocket();
            // LocalSocketAddress.Namespace.ABSTRACT 抽象域名模式
            // 除此之外还有 RESERVED 、 FILESYSTEM
            LocalSocketAddress address = new LocalSocketAddress(
	            "server-socket", LocalSocketAddress.Namespace.ABSTRACT);
            lsocket.connect(address);
            String result;
            Log.i("test","rootclient send cmd to rootServer cmd = " + 
	            sendCmdText.getText().toString());
            br = new BufferedWriter(new OutputStreamWriter(
	            lsocket.getOutputStream()));
            br.write(sendCmdText.getText().toString());
            br.newLine();
            br.flush();
//            br.close();
            Log.d("test", "========发送成功========");
            InputStream inputStream = lsocket.getInputStream();
            BufferedReader buffer = null;
            buffer = new BufferedReader(new InputStreamReader(inputStream));
            String tmpStr = null;
            while(inputStream.available() == 0) {
                try {
                //    Log.d("lsm", "========sleep======== inputStream.available() = " +inputStream.available());
                    Thread.sleep(1);
                } catch (Exception e) {
	                e.printStackTrace();
		            Log.i("test","rootclient rootServer 发送失败",e);
                }
            }
            byte[] buf = new byte[inputStream.available() -1];
            inputStream.read(buf,0,inputStream.available() -1) ;
            String str = new String(buf);
            Log.d("test", 
	            "========接受成功======== inputStream.available() = " +
		        inputStream.available() + "read str  = " + str);
            final String str1 = str;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    receiveText.setText(str1);
                }
            });
            buffer.close();
            lsocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("test","rootclient rootServer 发送失败",e);
        }
    }
```

源码位置：`system/core/libcutils/socket_local_client_unix.c`

[socket_local_client_unix.cpp - Android Code Search](https://cs.android.com/android/platform/superproject/main/+/main:system/core/libcutils/socket_local_client_unix.cpp)

## 编译&验证步骤

1. 源码根目录路径下创建 `android_thread` 目录
2. 编写 `Android.mk` 文件
3. 编译
4. 将生成物文件 `adb push` 到模拟器中 `/data/local/`
5. 验证

```mk
# Android.mk
include $(CLEAR_VARS)

LOCAL_SRC_FILES := unix_server.c
LOCAL_MODULE := unixServer
LOCAL_SHARED_LIBRARIES := liblog
LOCAL_PRELINK_MODULE := false

include $(BUILD_EXECUTABLE)
```

```shell
make unixServer
# 编译生成物路径： out/target/product/generic_x86_64/system/bin/unixServer

adb push out/target/product/generic_x86_64/system/bin/unixServer /data/local/
# push 完之后等待模拟器起来后，再到 /data/local/ 目录下把 unixServer 拉起来
```

## 如何查看是否是 **抽象域名**

```shell
netstat -an | grep "server-socket"
```

![[32-image2.png]]















