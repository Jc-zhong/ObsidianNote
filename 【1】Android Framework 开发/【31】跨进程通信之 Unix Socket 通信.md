## 1、专门用于跨进程通信的 Unix Socket

做过 **java** 相关 **app** 开发肯定学过 **socket** 通信等一些小程序来实现局域网通信，学会了通过 **ip 地址** 来进行一种网络通信，但这种通信一般用于 **跨设备访问场景**。

而我们如果 **在同一个设备上，只是进程与进程之间的一种通信**，那就完全没有必要使用这种网络通信方式，因为明显感觉不合适，进程间本身处于一个设备上，为啥还要通过 **ip 网络**，网卡走一圈再回来。所以针对这个问题就出现了本节课要讲的 **Unix Domain Socket** 通信。

**UNIX Domain SOCKET** 是在 **Socket** 架构上发展起来的用于同一台主机的 **进程间通讯（IPC）**。它不需要经过网络协议栈，不需要打包拆包、计算校验和、维护序列号应答等。只是将应用层数据从一个进程拷贝到另一个进程。**UNIX Domain SOCKET** 有 **SOKCET_DGRAM** **和SOCKET_STREAM** 两种模式，类似于 **UDP和TCP**，但是面向消息的 **UNIX socket** 也是可靠的，消息既不会丢失也不会顺序错乱。

## 2、与正常网络 socket 的使用差异

最大差异看一下 socket 方法：

```c++
int socket(int protofamily, int type, int protocol);
// protofamily：即协议域，又称为协议族（family）
// type：指定socket类型。常用的socket类型有，SOCK_STREAM（TCP）、SOCK_DGRAM（UDP）

// 这里我们 Unix Socket 通信情况下选择 AF_UNIX
// 协议族决定了 socket 的地址类型，在通信中必须采用对应的地址，如:
// AF_INET 决定了要用 ipv4地址（32位的）与端口号（16位的）的组合**
// AF_UNIX 决定了要用一个绝对路径名作为地址

int bind(int sockfd, const struct sockaddr addr, socklen_t addrlen);
// 这里的bind方法里面sockaddr 在上一节讲解网络通信时候时候用的是 sockaddr_in 结构体
// 传入最主要是端口和ip，这里 unix socket 对应结构体则是 sockaddr_un：

struct sockaddr_un {
  uint8_t sun_len;
  sa_family_t sun_family; /* AF_LOCAL */
  char sun_path[104]; /* null-terminated pathname */
};
// sun_path 主要传入一个绝对路径就可以，因为不需要 **ip** 进行网络通信。  
// 其他部分基本和网络 socket 一样  
```

通信流程图：

![[31-image1.png]]

### 3、实战Unix Socket通信

客户端代码：

```c
#include <stdlib.h>
#include <stdio.h>
#include <stddef.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <errno.h>
#include <string.h>
#include <unistd.h>

#define MAXLINE 80

char *client_path = "client-socket";
char *server_path = "server-socket";

int main() {
	struct  sockaddr_un cliun, serun;
	int len;
	char buf[100];
	int sockfd, n;

	if ((sockfd = socket(AF_UNIX, SOCK_STREAM, 0)) < 0){
			perror("client socket error");
			exit(1);
	}

    memset(&serun, 0, sizeof(serun));
    serun.sun_family = AF_UNIX;
    strncpy(serun.sun_path,server_path ,
                   sizeof(serun.sun_path) - 1);
    if (connect(sockfd, (struct sockaddr *)&serun, 
	    sizeof(struct sockaddr_un)) < 0) {
        perror("connect error");
        exit(1);
    }
    printf("please input send char:");
    while(fgets(buf, MAXLINE, stdin) != NULL) {
         write(sockfd, buf, strlen(buf));
         n = read(sockfd, buf, MAXLINE);
         if ( n < 0 ) {
            printf("the other side has been closed.\n");
         }else {
            printf("received from server: %s \n",buf);
         }
         printf("please input send char:");
    }
    close(sockfd);
    return 0;
}
```

服务端代码：

```c
#include <stdlib.h>
#include <stdio.h>
#include <stddef.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <errno.h>
#include <string.h>
#include <unistd.h>
#include <ctype.h>

#define MAXLINE 80

char *socket_path = "server-socket";

int main() {
    struct sockaddr_un serun, cliun;
    socklen_t cliun_len;
    int listenfd, connfd, size;
    char buf[MAXLINE];
    int i, n;
    if ((listenfd = socket(AF_UNIX, SOCK_STREAM, 0)) < 0) {
        perror("socket error");
        exit(1);
    }
    memset(&serun, 0, sizeof(serun));
    serun.sun_family = AF_UNIX;
    strncpy(serun.sun_path,socket_path ,
                   sizeof(serun.sun_path) - 1);
    //这个相当于把之前的地址要移除，不然上一个server没有结束，移除会报错already in use
    unlink(socket_path);
	if (bind(listenfd, (struct sockaddr *)&serun, 
		sizeof(struct sockaddr_un)) < 0) {
        perror("bind error");
        exit(1);
    }
    printf("UNIX domain socket bound\n");
    if (listen(listenfd, 20) < 0) {
        perror("listen error");
        exit(1);
    }
    printf("Accepting connections ...\n");
    while(1) {
        cliun_len = sizeof(cliun);
        if ((connfd = accept(listenfd,
	         (struct sockaddr *)&cliun, &cliun_len)) < 0){
            perror("accept error");
            continue;
        }
        printf("new client connect to server,client sockaddr === %s \n",
	        ((struct sockaddr *)&cliun)->sa_data);
        while(1) {
            memset(buf,0,sizeof(buf));
            n = read(connfd, buf, sizeof(buf));
            if (n < 0) {
                perror("read error");
                break;
            } else if(n == 0) {
                printf("EOF\n");
                break;
            }
            printf("received: %s\n", buf);
            for(i = 0; i < n; i++) {
                buf[i] = toupper(buf[i]);
            }
            write(connfd, buf, n);
        }
        close(connfd);
    }
    close(listenfd);
    return 0;
}
```

结果：  
服务端 先启动，然后 客户端 后启动
然后输入任何一个字符 **“hello”** 会立即受到 服务端 回复的大小字母 **“HELLO”**

![[31-image2.png]]










