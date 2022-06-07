package com.sofa.nerdrunning.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sofa.nerdrunning.R

val longhaul = FontFamily(Font(R.font.longhaul))

// Set of Material typography styles to start with
val Typography = Typography(
    body1 = TextStyle(
        fontFamily = longhaul,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        color = DarkBlue,
    ),
    body2 = TextStyle(
        fontFamily = longhaul,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = DarkBlue,
    ),
    subtitle1 = TextStyle(
        fontFamily = longhaul,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = DarkBlue,
    ),
    subtitle2 = TextStyle(
        fontFamily = longhaul,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        color = DarkBlue,
    ),
    caption = TextStyle(
        fontFamily = longhaul,
        fontWeight = FontWeight.Normal,
        fontSize = 8.sp,
    )
)