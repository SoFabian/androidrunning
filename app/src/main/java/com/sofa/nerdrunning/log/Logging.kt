package com.sofa.nerdrunning.log

import android.util.Log

const val DEBUG = false

fun logDebug(tag: String, msg: String) {
    if (DEBUG) {
        Log.d(tag, msg)
    }
}