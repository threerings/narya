/*
 * $Id: com_threerings_util_unsafe_Unsafe.c,v 1.2 2003/05/09 18:40:07 mdb Exp $
 */

#include <stdio.h>
#include <jni.h>
#include <jvmpi.h>

#include <errno.h>
#include <sys/types.h>
#include <time.h>
#include <unistd.h>

#include "com_threerings_util_unsafe_Unsafe.h"

/* global jvmpi interface pointer */
static JVMPI_Interface* jvmpi;

/** A sleep method that uses select(). This seems to have about 10ms
 * granularity where nanosleep() has about 20ms. Sigh. */
static int select_sleep (int millisecs)
{
    fd_set dummy;
    struct timeval toWait;
    FD_ZERO(&dummy);
    toWait.tv_sec = millisecs / 1000;
    toWait.tv_usec = (millisecs % 1000) * 1000;
    return select(0, &dummy, NULL, NULL, &toWait);
}

JNIEXPORT void JNICALL
Java_com_threerings_util_unsafe_Unsafe_enableGC (JNIEnv* env, jclass clazz)
{
    jvmpi->EnableGC();
}

JNIEXPORT void JNICALL
Java_com_threerings_util_unsafe_Unsafe_disableGC (JNIEnv* env, jclass clazz)
{
    jvmpi->DisableGC();
}

JNIEXPORT void JNICALL
Java_com_threerings_util_unsafe_Unsafe_nativeSleep (
    JNIEnv* env, jclass clazz, jint millis)
{
/*     struct timespec tmspec; */
/*     tmspec.tv_sec = millis/1000; */
/*     tmspec.tv_nsec = (millis%1000)*1000000; */
/*     if (nanosleep(&tmspec, NULL) < 0) { */
/*         fprintf(stderr, "nanosleep() failed: %s\n", strerror(errno)); */
/*     } */
    if (select_sleep(millis) < 0) {
        fprintf(stderr, "select_sleep() failed: %s\n", strerror(errno));
    }
}

JNIEXPORT jboolean JNICALL
Java_com_threerings_util_unsafe_Unsafe_init (JNIEnv* env, jclass clazz)
{
    JavaVM* jvm;

    if ((*env)->GetJavaVM(env, &jvm) > 0) {
        fprintf(stderr, "Failed to get JavaVM from env.\n");
        return JNI_FALSE;
    }

    /* get jvmpi interface pointer */
    if (((*jvm)->GetEnv(jvm, (void**)&jvmpi, JVMPI_VERSION_1)) < 0) {
        fprintf(stderr, "Failed to get JVMPI from JavaVM.\n");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}
