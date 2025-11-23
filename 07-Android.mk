LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
# print msg
$(warning "MyApp of LOCAL_PATH is $(LOCAL_PATH) ")

# Module name should match apk name to be installed
LOCAL_MODULT := MyApp

# Both user or eng , it alwasys build this app
LOCAL_MODULT_TAGS := optional

LOCAL_SRC_FILES := $(LOCAL_MODULT).apk
LOCAL_MODULT_CLASS := APPS
LOCAL_MODULT_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)

# sign : presigned / platform
LOCAL_CERTIFICATE := RRESIGNED

include $(BUILD_PREBUILT)
