### 1、Socket 是什么

![[30-image1.png]]

**Socket** 是应用层与 **TCP/IP协议族** 通信的 中间软件抽象层，它是一组接口。在设计模式中，**Socket** 其实就是一个门面模式，它把复杂的 **TCP/IP协议族** 隐藏在 **Socket** 接口后面，对用户来说，一组简单的接口就是全部，让 **Socket** 去组织数据，以符合指定的协议。

## 2、Socket 的方法介绍

既然是相互之间的通信，那就肯定有客户端和服务端，那么使用 **socket** 通信时候二者的具体流程是怎么样的？那我们肯定得了解 **socket** 都有具体哪些服务端和客户端的方法：
我们在看看具体的函数：

```c
int socket(int protofamily, int type, int protocol);
// socket 函数对应于普通文件的打开操作。
// 普通文件的打开操作返回一个文件描述字，而 socket() 用于创建一个 socket 描述符（socket descriptor），它唯一标识一个 socket。
// 这个 socket 描述字跟文件描述字一样，后续的操作都有用到它，把它作为参数，通过它来进行一些读写操作。

// [ protofamily ]：即协议域，又称为协议族（family）。
// 常用的协议族有，AF_INET、AF_INET6、AF_LOCAL**（或称AF_UNIX，Unix域socket,这个是我们的讲解重点）**、AF_ROUTE等等。协议族决定了socket的地址类型，在通信中必须采用对应的地址
// 如 AF_INET 决定了要用ipv4地址（32位的）与端口号（16位的）的组合、AF_UNIX决定了要用一个绝对路径名作为地址。

// [ type ]：指定socket类型。常用的socket类型有，SOCK_STREAM、SOCK_DGRAM、SOCK_RAW、SOCK_PACKET、SOCK_SEQPACKET等等（socket的类型有哪些？）

// [ protocol ]：故名思意，就是指定协议。常用的协议有，IPPROTO_TCP、IPPTOTO_UDP、IPPROTO_SCTP、IPPROTO_TIPC等，它们分别对应TCP传输协议、UDP 传输协议、STCP 传输协议、TIPC传输协议，只需要了解 TCP 和 UDP 既可以。
```

```c
int bind(int sockfd, const struct sockaddr addr, socklen_t addrlen);
// [ sockfd ]：即 socket 描述字，它是通过socket()函数创建了，唯一标识一个socket。bind()函数就是将给这个描述字绑定一个名字。
// [ addr ]：一个const struct sockaddr *指针，指向要绑定给sockfd的协议地址。这个地址结构根据地址创建socket时的地址协议族的不同而不同。
// [ addrlen ]：对应的是地址的长度。

int listen(int sockfd, int backlog);
// 第一个参数即为要监听的 socket 描述字
// 第二个参数为相应socket可以排队的最大连接个数。

int connect(int sockfd, const struct sockaddr *addr, socklen_t addrlen);
// 第一个参数即为客户端的 socket 描述字
// 第二参数为服务器的 socket 地址
// 第三个参数为 socket 地址的长度。

int accept(int sockfd, struct sockaddr addr, socklen_t addrlen);
// 第一个参数为服务器的 socket 描述字
// 第二个参数为指向 struct sockaddr * 的指针，用于返回客户端的协议地址
// 第三个参数为协议地址的长度。

// 其中 sockaddr 的具体定义如下：
// The actual structure passed for the addr argument will depend on the address family. The sockaddr structure is defined as something like:
struct sockaddr {
   sa_family_t sa_family;
   char        sa_data[14]; // 14 个字节表示端口和 ip 地址都在这里面
}
```

但它相当于一个我们 java 里面基类，具体真实类型还和协议族有关。例如是网络通信，则真实的是 **sockaddr_in**，它解决了 **sockaddr** 的缺陷，把 **port** 和 **addr** 分开储存在两个变量中

![[30-image2.png]]

```c
// 关闭函数
int close(int fd);

*ssize_t read(int fd, void buf, size_t count);
// read函数是负责从fd中读取内容.当读成功时，read返回实际所读的字节数
// 如果返回的值是0表示已经读到文件的结束了，小于0表示出现了错误。

*ssize_t write(int fd, const void buf, size_t count);
// write函数将buf中的字节内容写入文件描述符fd.成功时返回写的字节数。失败时返回-1，并设置errno变量。 
// 在网络程序中，当我们向套接字文件描述符写时有俩种可能。
// 1)write的返回值大于0，表示写了部分或者是全部的数据。
// 2)返回的值小于0，此时出现了错误。我们要根据错误类型来处理。
```

### 3、Socket 的通信过程方法调用

服务端： `socket -> bind -> listen -> accept -> read/write -> close  `
客户端： `socket -> connect -> read/write -> close`

![[30-image3.png]]

### 4、socket 通信的实战代码

服务端代码：

```c
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <ctype.h>
#define MAXLINE 80
#define SERV_PORT 8000
int main(void) {
    struct sockaddr_in servaddr, cliaddr;
    socklen_t cliaddr_len;
    int listenfd, connfd;
    char buf[MAXLINE];
    char str[INET_ADDRSTRLEN];
    int i, n;
    // socket() 打开一个网络通讯端口，如果成功的话，
    // 就像 open() 一样返回一个文件描述符，
    // 应用程序可以像读写文件一样用 read/write 在网络上收发数据。
    listenfd = socket(AF_INET, SOCK_STREAM, 0);
    bzero(&servaddr, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    servaddr.sin_port = htons(SERV_PORT);
    // bind() 的作用是将参数 listenfd 和 servaddr 绑定在一起，
    // 使 listenfd 这个用于网络通讯的文件描述符监听 servaddr 所描述的地址和端口号。
    bind(listenfd, (struct sockaddr *)&servaddr, sizeof(servaddr));
    // listen() 声明 listenfd 处于监听状态，
    // 并且最多允许有 20 个客户端处于连接待状态，如果接收到更多的连接请求就忽略。
    listen(listenfd, 20);
    printf("Accepting connections ...\n");
    while (1) {
        cliaddr_len = sizeof(cliaddr);
        // 典型的服务器程序可以同时服务于多个客户端，
        // 当有客户端发起连接时，服务器调用的 accept() 返回并接受这个连接，
        // 如果有大量的客户端发起连接而服务器来不及处理
        // 尚未 accept 的客户端就处于连接等待状态。
        connfd = accept(listenfd, (struct sockaddr *)&cliaddr, &cliaddr_len);
        n = read(connfd, buf, MAXLINE);
        printf("received from %s at PORT %d\n",
               inet_ntop(AF_INET, &cliaddr.sin_addr, str, sizeof(str)),
               ntohs(cliaddr.sin_port));
        for (i = 0; i < n; i++) {
            buf[i] = toupper(buf[i]);
        }
        write(connfd, buf, n);
        close(connfd);
    }
}
```

客户端代码：

```c
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#define MAXLINE 80
#define SERV_PORT 8000

int main(int argc, char *argv[]) {
    struct sockaddr_in servaddr;
    char buf[MAXLINE];
    int sockfd, n;
    char *str;
    if (argc != 2) {
        fputs("usage: ./client message\n", stderr);
        exit(1);
    }
    str = argv[1];
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    bzero(&servaddr, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    inet_pton(AF_INET, "127.0.0.1", &servaddr.sin_addr);
    // htons()作用是将端口号由主机字节序转换为网络字节序的整数值。(host to net)
    servaddr.sin_port = htons(SERV_PORT);

// 由于客户端不需要固定的端口号，因此不必调用 bind()，客户端的端口号由内核自动分配。
// 注意，客户端不是不允许调用 bind()，只是没有必要调用 bind() 固定一个端口号，
// 服务器也不是必须调用 bind()，但如果服务器不调用 bind()，内核会自动给服务器分配监听端口，
// 每次启动服务器时端口号都不一样，客户端要连接服务器就会遇到麻烦。
	connect(sockfd, (struct sockaddr *)&servaddr, sizeof(servaddr));
    write(sockfd, str, strlen(str));
    n = read(sockfd, buf, MAXLINE);
    printf("Response from server:\n");
    write(STDOUT_FILENO, buf, n);
    printf("\n");
    close(sockfd);
    return 0;
}
```

运行结果：  
先运行服务端，在运行客户端
携带参数为 **“hello”**，服务端把它改写大写的 **“HELLO"**

![[30-image4.png]]

