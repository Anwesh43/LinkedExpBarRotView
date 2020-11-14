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

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawExpBarRot(scale : Float, w : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val barSize : Float = Math.min(w, h) / barSizeFactor
    save()
    translate(w / 2, barSize / 2 + (h * 0.5f - barSize / 2) * sf.divideScale(1, parts))
    rotate(90f * sf.divideScale(2, parts))
    for (j in 0..1) {
        save()
        scale(1f - 2 * j, 1f)
        drawRect(
            RectF(w / 2 - w * 0.5f * sf.divideScale(0, parts), -barSize / 2, w / 2, barSize / 2),
            paint
        )
        restore()
    }
    restore()
}

fun Canvas.drawEBRNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawExpBarRot(scale, w, h, paint)
}

class ExpBarRotView(ctx : Context) : View(ctx) {

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
