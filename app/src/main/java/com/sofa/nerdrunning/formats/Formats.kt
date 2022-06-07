package com.sofa.nerdrunning.formats

import java.time.Duration

fun Duration.format(): String =
    String.format("%02d:%02d", seconds / 60, seconds % 60)
