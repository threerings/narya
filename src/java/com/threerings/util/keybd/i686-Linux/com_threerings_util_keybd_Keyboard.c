/*
 * $Id: com_threerings_util_keybd_Keyboard.c,v 1.2 2003/01/23 19:00:44 mdb Exp $
 */

#include <stdio.h>
#include <X11/Xlib.h>
#include <jni.h>
#include "com_threerings_util_keybd_Keyboard.h"

/* defines */
#define MESSAGE_LENGTH (256)

/* prototype definitions */
Display* getXDisplay (JNIEnv* env);

JNIEXPORT jboolean JNICALL
Java_com_threerings_util_keybd_Keyboard_init (
    JNIEnv* env, jclass class, jboolean enabled)
{
    Display* display = getXDisplay(env);
    if (display == NULL) {
        /* If we are unable to open a display, we can't function. */
        return JNI_FALSE;
    } else {
        XCloseDisplay(display);
        return JNI_TRUE;
    }
}

JNIEXPORT void JNICALL
Java_com_threerings_util_keybd_Keyboard_setKeyRepeat (
    JNIEnv* env, jclass class, jboolean enabled)
{
    Display* display = getXDisplay(env);
    if (display == NULL) {
        return;
    }

    /* set the desired key auto-repeat state. */
    if (enabled) {
        XAutoRepeatOn(display);
    } else {
        XAutoRepeatOff(display);
    }

    /* close the display to save our changes */
    XCloseDisplay(display);
}

JNIEXPORT jboolean JNICALL
Java_com_threerings_util_keybd_Keyboard_isKeyRepeatEnabled (
    JNIEnv* env, jclass class)
{
    XKeyboardState values;
    Display* display = getXDisplay(env);
    if (display == NULL) {
        /* for now, assume auto-repeat is enabled */
        return JNI_TRUE;
    }

    /* get the current keyboard control information */
    XGetKeyboardControl(display, &values);

    /* close the display */
    XCloseDisplay(display);

    return (values.global_auto_repeat) ? JNI_TRUE : JNI_FALSE;
}

/*
 * Returns a pointer to the X display, or null if an error occurred.
 */
Display*
getXDisplay (JNIEnv* env)
{
    char* disp = NULL;
    Display* dpy = XOpenDisplay(disp);
    if (dpy == NULL) {
        char message[MESSAGE_LENGTH];
        snprintf(message, MESSAGE_LENGTH,
                 "Unable to open display [display=%s].\n", XDisplayName(disp));
        return NULL;
    }

    /* printf("Opened display [disp=%s].\n", XDisplayName(disp)); */
    return dpy;
}
