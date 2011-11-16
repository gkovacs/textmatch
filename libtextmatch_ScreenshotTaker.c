#include <jni.h>
#include <X11/Xlib.h>
#include "textmatch_ScreenshotTaker.h"

XWindowAttributes attributes;
XWindowAttributes child_attributes;
Display *display;

/*
Window get_toplevel_parent(Window window)
{
     Window parent;
     Window root;
     Window * children;
     unsigned int num_children;

     while (1) {
         if (0 == XQueryTree(display, window, &root,
                   &parent, &children, &num_children)) {
             //fprintf(stderr, "XQueryTree error\n");
             //abort(); //change to whatever error handling you prefer
         }
         if (children) { //must test for null
             XFree(children);
         }
         if (window == root || parent == root) {
             return window;
         }
         else {
             window = parent;
         }
     }
}
*/

void get_toplevel_parent(Window window, Window *active_ret, Window *previous_ret)
{
     Window parent;
     Window root;
     Window * children;
     Window previous = window;
     unsigned int num_children;

     while (1) {
         if (0 == XQueryTree(display, window, &root,
                   &parent, &children, &num_children)) {
             //fprintf(stderr, "XQueryTree error\n");
             //abort(); //change to whatever error handling you prefer
         }
         if (children) { //must test for null
             XFree(children);
         }
         if (window == root || parent == root) {
             *active_ret = window;
             *previous_ret = previous;
             return;
         }
         else {
             previous = window;
             window = parent;
         }
     }
}

JNIEXPORT void JNICALL Java_textmatch_ScreenshotTaker_initializeXInteraction
  (JNIEnv *e, jclass c) {
display = XOpenDisplay(NULL);
}

JNIEXPORT void JNICALL Java_textmatch_ScreenshotTaker_refreshInfo
  (JNIEnv *e, jclass c) {
Window active_window;
int focus_state;
XGetInputFocus(display, &active_window, &focus_state);
Window active_window_act, active_window_child;
get_toplevel_parent(active_window, &active_window_act, &active_window_child);
XGetWindowAttributes(display, active_window_act, &attributes);
XGetWindowAttributes(display, active_window_child, &child_attributes);
}


JNIEXPORT jint JNICALL Java_textmatch_ScreenshotTaker_getX
  (JNIEnv *e, jclass c) {
  return attributes.x + (attributes.width - child_attributes.width);
}


JNIEXPORT jint JNICALL Java_textmatch_ScreenshotTaker_getY
  (JNIEnv *e, jclass c) {
  // used to exclude window decorations
  return attributes.y + (attributes.height - child_attributes.height);
}


JNIEXPORT jint JNICALL Java_textmatch_ScreenshotTaker_getWidth
  (JNIEnv *e, jclass c) {
  return child_attributes.width;
}


JNIEXPORT jint JNICALL Java_textmatch_ScreenshotTaker_getHeight
  (JNIEnv *e, jclass c) {
  return child_attributes.height;
}

