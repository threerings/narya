/*
 * $Id$
 */

#include <stdio.h>
#include <jni.h>
#include <windows.h>
#include "com_threerings_util_keybd_Keyboard.h"

/* prototype definitions */
LRESULT CALLBACK KeyboardProc (int nCode, WPARAM wParam, LPARAM lParam);
LRESULT CALLBACK LowLevelKeyboardProc (int nCode, WPARAM wParam, LPARAM lParam);

/* static global variables */
static HHOOK gKeyHook;
static HINSTANCE gHInst;

BOOL WINAPI
DllMain (HANDLE hinstDLL, DWORD dwReason, LPVOID lpvReserved)
{
    /* save off our instance handle */
    fprintf(stderr, "In DllMain.\n");
    gHInst = (HINSTANCE)hinstDLL;
}

JNIEXPORT void JNICALL
Java_com_threerings_util_keybd_Keyboard_setKeyRepeat (
    JNIEnv* env, jclass class, jboolean enabled)
{
    if (enabled) {
        if (gKeyHook != NULL) {
            fprintf(stderr, "Removing windows keyboard hook.\n");
            /* remove the keyboard hook */
            UnhookWindowsHookEx(gKeyHook);
            gKeyHook = NULL;
        }

    } else {
        /* install the hook with which we usurp all keyboard events */
/*         gKeyHook = SetWindowsHookEx( */
/*             WH_KEYBOARD_LL, LowLevelKeyboardProc, hinstExe, 0); */

        fprintf(stderr, "Setting windows keyboard hook.\n");
        gKeyHook = SetWindowsHookEx(
            WH_KEYBOARD, KeyboardProc, gHInst, 0);
    }
}

JNIEXPORT jboolean JNICALL
Java_com_threerings_util_keybd_Keyboard_isKeyRepeatEnabled (
    JNIEnv* env, jclass class)
{
    /* since windows has no global key repeat enable/disable facility, we
     * simply always return true here so that we'll be sure to "re-enable"
     * key repeat, which will result in our properly removing the keyboard
     * hook with which we trap key events. */
    fprintf(stderr, "isKeyRepeatEnabled stderr.\n");
    printf("isKeyRepeatEnabled stdout.\n");
    return JNI_TRUE;
}

/*
 * The keyboard event hook that eats all repeated keystrokes.
 */
LRESULT CALLBACK
KeyboardProc (int nCode, WPARAM wParam, LPARAM lParam)
{
    int repeatCount = (lParam & KF_REPEAT);
    fprintf(stderr, "Key down [key=%d, repeatCount=%d].\n",
            wParam, repeatCount);
    return (repeatCount > 1) ? 1 : CallNextHookEx(NULL, nCode, wParam, lParam);
}

#if 0

/*
 * The low-level keyboard event hook that eats all keystrokes.
 */
LRESULT CALLBACK
LowLevelKeyboardProc (int nCode, WPARAM wParam, LPARAM lParam)
{
    BOOL fEatKeystroke = FALSE;

    if (nCode == HC_ACTION) {
        switch (wParam) {
        case WM_KEYDOWN:
        case WM_SYSKEYDOWN:
        case WM_KEYUP:
        case WM_SYSKEYUP: 
            PKBDLLHOOKSTRUCT p = (PKBDLLHOOKSTRUCT) lParam;
            fEatKeystroke = 
                ((p->vkCode == VK_TAB) && ((p->flags & LLKHF_ALTDOWN) != 0)) ||
                ((p->vkCode == VK_ESCAPE) && 
                 ((p->flags & LLKHF_ALTDOWN) != 0)) ||
                ((p->vkCode == VK_ESCAPE) && ((GetKeyState(VK_CONTROL) & 
                                               0x8000) != 0));
            break;
        }
    }

    return (fEatKeystroke) ? 1 : CallNextHookEx(NULL, nCode, wParam, lParam);
}

#endif
