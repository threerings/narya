/**
 * $Id$
 */

#include <signal.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>

#include <jni.h>
#include "com_threerings_util_signal_SignalManager.h"

static int writefd;

typedef void (*sighandler_t)(int);
static sighandler_t old_handlers[64];
static sighandler_t handlers[64];

static void
signal_handler (int signo)
{
    write(writefd, &signo, sizeof(signo));
    signal(SIGINT, signal_handler);
}

/**
 * Class:     com_threerings_util_signal_SignalManager
 * Method:    activateHandler
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_com_threerings_util_signal_SignalManager_activateHandler (
    JNIEnv* env, jclass clazz, jint signo)
{
    old_handlers[signo] = signal(signo, signal_handler);
    if (old_handlers[signo] == SIG_ERR) {
        fprintf(stderr, "Error setting signal handler (%d): %s\n",
                signo, strerror(errno));
        old_handlers[signo] = 0;
    } else {
        handlers[signo] = signal_handler;
    }
}

/**
 * Class:     com_threerings_util_signal_SignalManager
 * Method:    deactivateHandler
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_com_threerings_util_signal_SignalManager_deactivateHandler (
    JNIEnv* env, jclass clazz, jint signo)
{
    if (old_handlers[signo] != 0) {
        signal(signo, old_handlers[signo]);
        old_handlers[signo] = 0;
        handlers[signo] = 0;
    }
}

/**
 * Class:     com_threerings_util_signal_SignalManager
 * Method:    dispatchSignals
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_com_threerings_util_signal_SignalManager_dispatchSignals (
    JNIEnv* env, jclass clazz)
{
    int filedes[2], readfd;
    jint signo;
    jmethodID mid = (*env)->GetStaticMethodID(
        env, clazz, "signalReceived", "(I)V");

    if (pipe(filedes) < 0) {
        fprintf(stderr, "Failed to create signal pipe: %s\n", strerror(errno));
        return;
    }
    readfd = filedes[0];
    writefd = filedes[1];

    while (1) {
        int got = read(readfd, &signo, sizeof(int));
        if (got < 0) {
            fprintf(stderr, "Signal pipe read failed: %s\n", strerror(errno));
        } else if (got == 0) {
            fprintf(stderr, "Signal pipe read returned zero bytes.\n");
        } else {
            if (signo < 0 || signo >= 64 || handlers[signo] == 0) {
                fprintf(stderr, "Received bogus signal (%d).\n", signo);
            } else {
                (*env)->CallStaticVoidMethod(env, clazz, mid, signo);
            }
        }
    }
}
