#include <jni.h>
#include <stdio.h>
#include <stdio.h>
#include <syslog.h>
#include <fcntl.h>
#include <linux/fb.h>
#include <sys/mman.h>
#include <sys/ioctl.h>
#include <unistd.h>
#include "TestJNI.h"

int fbfd = -1;
char *fbp = nullptr;
struct fb_var_screeninfo vinfo;
struct fb_fix_screeninfo finfo;


JNIEXPORT void JNICALL Java_org_warp_picalculator_TestJNI_disposeDisplayBuffer(JNIEnv *env, jobject thisObj)
{
	if (fbp != nullptr) {
	    munmap(fbp, finfo.smem_len);
	    fbp = nullptr;
	}
	if (fbfd != -1) {
	    close(fbfd);
	    fbfd = -1;
	}
    return;
}

JNIEXPORT jobject JNICALL Java_org_warp_picalculator_TestJNI_getDisplayBuffer(JNIEnv *env, jobject thisObj)
{

    syslog(LOG_INFO, "[JNI NATIVE] INIT");

    fbfd = open("/dev/fb1", O_RDWR);
    if (fbfd == -1) {
        syslog(LOG_ERR, "Unable to open secondary display");
        return NULL;
    }
    if (ioctl(fbfd, FBIOGET_FSCREENINFO, &finfo)) {
        syslog(LOG_ERR, "Unable to get secondary display information");
        return NULL;
    }
    if (ioctl(fbfd, FBIOGET_VSCREENINFO, &vinfo)) {
        syslog(LOG_ERR, "Unable to get secondary display information");
        return NULL;
    }

    syslog(LOG_INFO, "Second display is %d x %d %dbps\n", vinfo.xres, vinfo.yres, vinfo.bits_per_pixel);

    fbp = (char*) mmap(0, finfo.smem_len, PROT_READ | PROT_WRITE, MAP_SHARED, fbfd, 0);
    if (fbp <= 0) {
        syslog(LOG_ERR, "Unable to create mamory mapping");
        close(fbfd);
        return NULL;
    }

	char* class_name = "org/warp/picalculator/MmapByteBuffer";
	jclass clz = env->FindClass(class_name);
	if (clz == NULL) {
		printf("Error, could not find class '%s'\n", class_name);
		return NULL;
	}
	char* signature = "(IIILjava/nio/ByteBuffer;)V";
	jmethodID constructor = env->GetMethodID( clz, "<init>", signature);
	if (constructor == NULL) {
		printf("Error, could not find constructor %s %s\n", class_name, signature);
		return NULL;
	}

    syslog(LOG_INFO, "[JNI NATIVE] DONE");
	return env->NewObject(clz, constructor, fbfd, fbp, finfo.smem_len,
			env->NewDirectByteBuffer(fbp, finfo.smem_len));
}
