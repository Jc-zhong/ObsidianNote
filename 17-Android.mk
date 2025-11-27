LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := MyThread.cpp \
		Main.cpp \

LOCAL_SHARE_LIBRARIES := libandroid_runtime \
		libcutils \
		libutils \
		liblog

LOCAL_MODULE := android_thread
LOCAL_PRELINK_MODULE := false

include $(BUILD_EXECUTABLE)


include $(CLEAR_VARS)

LOCAL_SRC_FILES := thread_posix.c
LOCAL_MODULE := linux_thread
LOCAL_SHARE_LIBRARIES := liblog
LOCAL_PRELINK_MODULE := false

include $(BUILD_EXECUTABLE)