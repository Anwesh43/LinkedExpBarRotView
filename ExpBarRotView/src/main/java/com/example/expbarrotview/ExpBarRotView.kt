package com.example.expbarrotview

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Color
import android.content.Context
import android.app.Activity

val colors : Array<Int> = arrayOf(
    "#F44336",
    "#3F51B5",
    "#009688",
    "#2196F3",
    "#FF5722"
).map {
    Color.parseColor(it)
}.toTypedArray()
val parts : Int = 3
val scGap : Float = 0.02f / parts
val strokeFactor : Float = 90f
val barSizeFactor : Float = 12.2f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")
