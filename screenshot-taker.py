#!/usr/bin/python

import gtk
import gc
import os
import glob

def take_screenshot(seen_screenshots, series_num, screenshot_num):
    # Calculate the size of the whole screen
    screenw = gtk.gdk.screen_width()
    screenh = gtk.gdk.screen_height()

    # Get the root and active window
    root = gtk.gdk.screen_get_default()

    if root.supports_net_wm_hint("_NET_ACTIVE_WINDOW") and root.supports_net_wm_hint("_NET_WM_WINDOW_TYPE"):
        active = root.get_active_window()
        # You definately do not want to take a screenshot of the whole desktop, see entry 23.36 for that
        # Returns something like ('ATOM', 32, ['_NET_WM_WINDOW_TYPE_DESKTOP'])
        if active.property_get("_NET_WM_WINDOW_TYPE")[-1][0] == '_NET_WM_WINDOW_TYPE_DESKTOP':
            return False

        # Calculate the size of the wm decorations
        relativex, relativey, winw, winh, d = active.get_geometry() 
        w = winw + (relativex*2)
        h = winh + (relativey+relativex)

        # Calculate the position of where the wm decorations start (not the window itself)
        screenposx, screenposy = active.get_root_origin()
    else:
        return False

    screenshot = gtk.gdk.Pixbuf.get_from_drawable(gtk.gdk.Pixbuf(gtk.gdk.COLORSPACE_RGB, True, 8, w, h),
            gtk.gdk.get_default_root_window(),
            gtk.gdk.colormap_get_system(),
            screenposx, screenposy, 0, 0, w, h)

    pixels = screenshot.get_pixels()
    if pixels in seen_screenshots:
        return False
    seen_screenshots.add(pixels)

    # Either "png" or "jpeg" (case matters)
    format = "png"

    # Pixbuf's have a save method 
    # Note that png doesnt support the quality argument. 
    screenshot.save("screenshot" + str(series_num) + '_' + str(screenshot_num) + "." + format, format)
    del screenshot
    gc.collect()
    return True

seen_screenshots = set()
series_num = 0
while len(glob.glob('screenshot' + str(series_num) + '*.png')) > 0:
    series_num += 1
screenshot_num = 0
while True:
    if take_screenshot(seen_screenshots, series_num, screenshot_num):
        screenshot_num += 1

