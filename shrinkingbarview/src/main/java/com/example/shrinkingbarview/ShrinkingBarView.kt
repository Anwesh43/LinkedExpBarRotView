package com.example.shrinkingbarview

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

val parts : Int = 4
val strokeFactor : Float = 90f
val wSizeFactor : Float = 12.4f
val delay : Long = 20
val rot : Float = 90f
val backColor : Int = Color.parseColor("#BDBDBD")
val colors : Array<Int> = arrayOf(
    "#F44336",
    "#673AB7",
    "#4CAF50",
    "#2196F3",
    "#FF5722"
).map {
    Color.parseColor(it)
}.toTypedArray()

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawShrinkingBar(scale : Float, w : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf2 : Float = sf.divideScale(2, parts)
    val barSize : Float = Math.min(w, h) / wSizeFactor
    save()
    translate(w / 2, h / 2)
    rotate(rot * sf2)
    for (j in 0..1) {
        save()
        scale(1f - 2 * j, 1f)
        translate((w / 2 - barSize / 2) * (1 - sf.divideScale(1, parts)), 0f)
        drawRect(
            RectF(
                -barSize / 2,
                0f,
                barSize / 2,
                (h * 0.5f * sf.divideScale(0, parts) - (h / 2 - w / 2) * sf2) * (1f - 2 * j)
            ),
            paint)
        restore()
    }
    restore()
}

fun Canvas.drawSBNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawShrinkingBar(scale, w, h, paint)
}

class ShrinkingBarView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}