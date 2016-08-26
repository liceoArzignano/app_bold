LOCAL_PATH := $(call my-dir)

include $(CLEAN_VARS)

LOCAL_MODULE := safe-addon-jni
LOCAL_SRC_FILES := safe-addon-jni.c

include $(BUILD_SHARED_LIBRARY)
