LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := keymaster
LOCAL_LDLIBS := -lkeymaster
include $(PREBUILT_SHARED_LIBRARY)
