#include <pthread.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <utils/log.h>

void *thread_posix_funtion(void *arg){
	(void*) arg;
	int i;
	for(i=0; i<30; i++){
		// 在 终端控制行 输出
		printf("hello thread i = %d\n", i);
		// 在 Android Native 层 Log 输出
		ALOGD("hello thread i = %d\n", i);
		sleep(1);
	}

	return NULL;
}

int int main(void)
{
	// 声明一个线程 - 线程 ID 为 myThread
	pthread_t myThread;

	// 创建一个线程
	// 线程 ID :  myThread
	// 线程默认属性 : NULL
	// 线程创建后该执行的任务方法 : thread_posix_funtion
	// 线程默认属性 : NULL
	if( pthread_create(&myThread, NULL, thread_posix_funtion, NULL) ){
		ALOGA("error create thread !! \n");
		abort();
	}
	sleep(1);

	// 主线程继续运行 - pthread_join : 阻塞等待子线程完成任务
	if ( pthread_join(myThread, NULL) ){
		ALOGA("error join thread !! \n");
		abort();
	}
	ALOGD("hello thread has run end , and then exit \n");

	exit(0);
}