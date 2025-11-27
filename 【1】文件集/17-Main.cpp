#define LOG_TAG "Main"

#include <utils/Log.h>
#include <utils/threads.h>
#include "MyThread.h"

using namesapce android;

int main(){

	sp<MyThread> thread = new MyThread;
	thread->run("MyThread", PRIORITY_URGENT_DISPLAY);
	while(1){
		if(!thread->isRunning()){
			ALOGD("main thread is not running , break while");
			break;
		}
	}

	ALOGD("main end");
	return 0;
}