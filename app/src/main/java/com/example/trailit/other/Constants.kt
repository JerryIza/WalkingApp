package com.example.trailit.other

import android.graphics.Color

object Constants {

    //implement some of these into xml resources

    const val RUNNING_DATABASE_NAME = "running_db"

    const val REQUEST_CODE_LOCATION_PERMISSION = 0

    const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
    const val ACTION_START_RESUME_TRACKING_SERVICE = "ACTION_START_RESUME_TRACKING_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

    const val TIMER_UPDATE_INTERVAL = 50L

    const val SHARED_PREFERENCES_NAME = "sharePref"

    const val KEY_FIRST_TIME_TOGGLE = "KEY_FIRST_TIME_TOGGLE"
    const val KEY_NAME = "KEY_NAME"
    const val KEY_WEIGHT = "KEY_WEIGHT"

    const val HIGHEST_UPDATE_INTERVAL = 5000L
    const val LOWEST_UPDATE_INTERVAL = 2000L

    const val MAP_ZOOM = 15f

    const val TRAILLINE_COLOR = Color.GREEN
    const val TRAILLINE_WIDTH = 7f


    const val CHANNEL_ID = "tracking_channel"
    const val CHANNEL_NAME = "Tracking"
    //don't set to zero, it will not work
    const val NOTIFICATION_ID = 1

}