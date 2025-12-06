## 1、epoll 出现的背景

首先了解 2个 基本概念：
1. **阻塞IO（blocking IO）**:  
	**Socket 的阻塞模式意味着必须要做完 IO 操作（包括错误）才会返回**。
2. **非阻塞IO（blocking IO）**:
	**非阻塞模式下无论操作是否完成都会立刻返回，需要通过其他方式来判断具体操作是否成功。**
	
回想一下之前的 网络通信  demo：
针对 Server 端它做的事情如下：

```c++
 while(1) {
        cliun_len = sizeof(cliun);
        if ((connfd = accept(listenfd,
	        (struct sockaddr *)&cliun, &cliun_len)) < 0) {
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
// 上面代码其实流程：
// -> 客户端连接后，一直等待读取客户端发过来的数据
// 这里的 accept 和 read 默认一般都是一个阻塞的方法，即所谓的 blocking IO
// 在调用 read 时候，会一直等到 read 到了数据才会进行返回。
// 在没有数据这段时间那么程序就只能一直停留在 read 这个方法里面，不能继续执行下一步。
```

大家明显有没有发现一个问题，当前这个线程好像就只能服务一个客户端，但是真实的场景很可能有很多个客户端连接服务端，那这个时候服务端 是怎么做到和客户端同时通信的呢？你可以想到有哪些方法？

1. 我们可以考虑针对一个客户端一个线程的方式，那样每个线程分别和一个客户端进行数据读取及回复，这种其实在客户端本身数量不会超过 1-2 个情况下是完全可以的，但是一旦客户端的数量一多呢？假设有 20 个客户端与服务器进行通信，难道服务端还来 20 个线程，甚至更多 20000 个客户端呢？那不可能 20000 个线程吧

2. 大家可能会想到 read 方法可以不可以做成那种 nonblocking IO 模式，在调用 read 时候，如果发现系统没有数据就立即返回，有数据就读取。这里告诉大家的是确实有这样的不阻塞模式。具体设置方法可以参考如下：

```c++
// fcntl 函数可以将一个socket 句柄设置成非阻塞模式
flags = fcntl(sockfd, F_GETFL, 0); //获取文件的flags值。 
fcntl(sockfd, F_SETFL, flags | O_NONBLOCK); 
//设置成非阻塞模式；
flags = fcntl(sockfd,F_GETFL,0);
fcntl(sockfd,F_SETFL,flags&~O_NONBLOCK); 
//设置成阻塞模式；
```

当 read 方法是非阻塞后，那么我们是不是可以一个线程里面不断的循环调用 read 各个客户端的数据，那么代码可能就变成这样：

```c++
while(1) {
	read(fd1...);//客户1
	read(fd2...);//客户2
	read(fd3...);//客户3
}
```

但以上方式也有一些严重问题：
如：**当客户都没有数据时候，那么这个线程就一直在空跑，死循环的调用 read 数据，实际又还没有，白白浪费大量 cpu**。这里有同学会说那么 while 时候进行一个延时不就行了么？其实这种思路也是不行的，因为你没办法确认这个延时多少合适，如果小了一样耗费 cpu，如果设置大了，那么就会造成接收数据的延时。

由于上述方案的种种缺点，引出了 **I/O 多路复用技术（I/O multiplexing）**。**复用是指在同一个进程（线程）中，处理多路 I/O，多路指多个文件描述符**。它核心思想收集进程感兴趣的全部描述符，然后调用一个函数，当这些描述符中的一个或多个准备好 I/O 时，函数返回并告知进程是哪些描述符准备好了。此时进程只需要去这些准备好的描述符上操作即可。**这多路复用的在 linux 上具体实现方法目前使用最多的是 select，poll，epoll。目前 android 系统中大部分都是 epoll**。
所以这里就重点介绍 **epoll**。

## 2、epoll 主要方法

epoll 接口

```c++
// epoll 操作过程需要三个接口，分别如下：
#include <sys/epoll.h>
int epoll_create(int size);
int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event);
int epoll_wait(int epfd, struct epoll_event * events, int maxevents, int timeout);

// [1] epoll_create
int epoll_create(int size);
// 创建一个 epoll 的句柄，size 用来告诉内核这个监听的数目一共有多大。
// 这个参数不同于select()中的第一个参数，给出最大监听的fd+1的值。需要注意的是，当创建好epoll句柄后，它就是会占用一个fd值，在linux下如果查看/proc/进程id/fd/，是能够看到这个fd的，所以在使用完epoll后，必须调用close()关闭，否则可能导致fd被耗尽。

// [2] epoll_ctl
int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event);
// epoll的事件注册函数，它不同与select()是在监听事件时告诉内核要监听什么类型的事件epoll的事件注册函数，它不同与select()是在监听事件时告诉内核要监听什么类型的事件，而是在这里先注册要监听的事件类型。
// 第一个参数是 epoll_create() 的返回值
// 第二个参数表示动作，用三个宏来表示：
// -- EPOLL_CTL_ADD：注册新的 fd 到 epfd 中；
// -- EPOLL_CTL_MOD：修改已经注册的 fd 的监听事件；
// -- EPOLL_CTL_DEL：从 epfd 中删除一个fd；
// 第三个参数是需要监听的 fd；
// 第四个参数是告诉内核需要监听什么事，struct epoll_event结构如下：
struct epoll_event {
  __uint32_t events;  /* Epoll events */
  epoll_data_t data;  /* User data variable */
};
// events 可以是以下几个宏的集合：
// EPOLLIN ：表示对应的文件描述符可以读（包括对端 SOCKET 正常关闭）；
// EPOLLOUT：表示对应的文件描述符可以写；
// EPOLLPRI：表示对应的文件描述符有紧急的数据可读（这里应该表示有带外数据到来）；
// EPOLLERR：表示对应的文件描述符发生错误；
// EPOLLHUP：表示对应的文件描述符被挂断；
// EPOLLET ：将EPOLL设为边缘触发(Edge Triggered)模式，这是相对于水平触发(Level Triggered)来说的；
// EPOLLONESHOT：只监听一次事件，当监听完这次事件之后，如果还需要继续监听这个 socket 的话，需要再次把这个 socket 加入到 EPOLL 队列里。

// [3] epoll_wait
int epoll_wait(int epfd, struct epoll_event * events, int maxevents, int timeout);
// 等待事件的产生，类似于select()调用。
// 参数 events 用来从内核得到事件的集合，maxevents 告之内核这个 events 有多大，这个 maxevents 的值不能大于创建 epoll_create() 时的 size
// 参数 timeout 是超时时间（毫秒，0 会立即返回，-1将不确定，也有说法说是永久阻塞）。
// 该函数返回需要处理的事件数目，如返回 0 表示已超时。
```

## 3、epoll 工作模式

epoll 对文件描述符的操作有两种模式：**LT（level trigger）和ET（edge trigger）。**
**LT 模式 是默认模式**，LT 模式与 ET 模式的区别如下：

**LT 模式 ：当 epoll_wait 检测到描述符事件发生并将此事件通知应用程序，应用程序可以不立即处理该事件。下次调用 epoll_wait 时，会再次响应应用程序并通知此事件**。

**ET 模式 ：当 epoll_wait 检测到描述符事件发生并将此事件通知应用程序，应用程序必须立即处理该事件。如果不处理，下次调用 epoll_wait 时，不会再次响应应用程序并通知此事件**。

ET 模式 在很大程度上减少了 epoll 事件被重复触发的次数，因此效率要比 LT 模式高。epoll 工作在 ET 模式的时候，必须使用非阻塞套接口，以避免由于一个文件句柄的阻塞读/阻塞写操作把处理多个文件描述符的任务饿死。

## 4、epoll 的实战 demo

服务端 epoll ：

```c++
#include <stdlib.h>
#include <stdio.h>
#include <stddef.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <errno.h>
#include <string.h>
#include <unistd.h>
#include <ctype.h>
#include <sys/epoll.h>
#define MAXLINE 80

char *socket_path = "server-socket";

int main() {
    struct sockaddr_un serun, cliun;
    socklen_t cliun_len;
    int listenfd, connfd, size;
    char buf[MAXLINE];
    int i, n;
    // 1. socket()
    if ((listenfd = socket(AF_UNIX, SOCK_STREAM, 0)) < 0) {
        perror("socket error");
        exit(1);
    }
    memset(&serun, 0, sizeof(serun));
    serun.sun_family = AF_UNIX;
    strncpy(serun.sun_path,socket_path ,
                   sizeof(serun.sun_path) - 1);
    unlink(socket_path);
    // 2. bind()
    if (bind(listenfd, (struct sockaddr *)&serun, 
	    sizeof(struct sockaddr_un)) < 0) {
        perror("bind error");
        exit(1);
    }
    printf("UNIX domain socket bound\n");
	// 3. listen()
    if (listen(listenfd, 20) < 0) {
        perror("listen error");
        exit(1);
    }
    printf("Accepting connections ...\n");
     // 4. 创建epoll树
    int epfd = epoll_create(1000);  // 此处 1000 无意义了，参数大于 0 即可
    if(epfd == -1) {
        perror("epoll_create");
        exit(-1);
    }
    // 5. 将用于监听的lfd挂的epoll树上（红黑树）
    struct epoll_event ev;   // 这个结构体记录了检测什么文件描述符的什么事件
    ev.events = EPOLLIN;
    ev.data.fd = listenfd;
    // ev 里面记录了检测 lfd 的什么事件
    epoll_ctl(epfd, EPOLL_CTL_ADD, listenfd, &ev);
    // 循环检测 委托内核去处理
    // 当内核检测到事件到来时会将事件写到这个结构体数组里
    struct epoll_event events[1024];
    while(1) {
	    // 最后一个参数表示阻塞
        int num = epoll_wait(epfd, events, sizeof(events)/sizeof(events[0]), -1);
        for (int i = 0;i < num; i++) {
            if(events[i].data.fd == listenfd) {
	            // 有连接请求到来
                int len = sizeof(cliun);
                int connfd = accept(listenfd, (struct sockaddr *)&cliun, &len);
                if(connfd == -1) {
                    perror("accept");
                    exit(-1);
                }
                printf("a new client connected! \n");
                // 将用于通信的文件描述符挂到 epoll 树上
                ev.data.fd = connfd;
                ev.events = EPOLLIN;
                epoll_ctl(epfd, EPOLL_CTL_ADD, connfd, &ev);
            } else {
                 // 通信也有可能是写事件
                if(events[i].events & EPOLLOUT) {
                    // 这里先忽略写事件
                    continue;
                }
                char buf[1024] = {0};
                int count = read(events[i].data.fd, buf, sizeof(buf));
                if(count == 0) {
	                // 客户端关闭了连接
                    printf("客户端关闭了连接。。。。\n");
                    // 将对应的文件描述符从 epoll 树上取下
                    close(events[i].data.fd);
                    epoll_ctl(epfd, EPOLL_CTL_DEL, events[i].data.fd, NULL);
                } else {
                    if(count == -1) {
                        perror("read");
                        exit(-1);
                    } else {
                        // 正常通信
                        printf("client say: %s\n" ,buf);
                        write(events[i].data.fd, buf, strlen(buf)+1);
                    }
                }
            }
        }
    }
    close(listenfd);
    return 0;
}

```

客户端 epoll ：

```c++
#include <stdlib.h>
#include <stdio.h>
#include <stddef.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <errno.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#define MAXLINE 80

char *client_path = "client-socket";
char *server_path = "server-socket";

int main() {
	struct  sockaddr_un cliun, serun;
	int len;
	char buf[100];
	int sockfd, n;
	// 1. socket()
	if ((sockfd = socket(AF_UNIX, SOCK_STREAM, 0)) < 0) {
			perror("client socket error");
			exit(1);
	}
//    signal(SIGPIPE, SIG_IGN);
    memset(&serun, 0, sizeof(serun));
    serun.sun_family = AF_UNIX;
    strncpy(serun.sun_path,server_path ,
                   sizeof(serun.sun_path) - 1);
    // 2. connect()
    if (connect(sockfd, (struct sockaddr *)&serun,
	     sizeof(struct sockaddr_un)) < 0) {
        perror("connect error");
        exit(1);
    }
    printf("please input send char:");
    // 等待输入事件
    while(fgets(buf, MAXLINE, stdin) != NULL) {
	     // 4. write()
         write(sockfd, buf, strlen(buf));
         // 5. read()
         n = read(sockfd, buf, MAXLINE);
         if ( n <= 0 ) {
            printf("the other side has been closed.\n");
            break;
         } else {
            printf("received from server: %s \n",buf);
         }
         printf("please input send char:");
    }
    printf("end server  date");
    // 6. close()
    close(sockfd);
    return 0;
}
```
