package com.sofa.nerdrunning.log

const val DEBUG = false

fun logDebug(tag: String, msg: String) {
    if (DEBUG) {
        logDebug(tag, msg)
    }
}