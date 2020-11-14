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
val scGap : Float = 0.02f / parts

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

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class SBNode(var i : Int, val state : State = State()) {

        private var next : SBNode? = null
        private var prev : SBNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = SBNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSBNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SBNode {
            var curr: SBNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr

            }
            cb()
            return this
        }
    }

    data class ShrinkingBar(var i : Int) {

        private var curr : SBNode = SBNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : ShrinkingBarView) {

        private val animator : Animator = Animator(view)
        private val sb : ShrinkingBar = ShrinkingBar(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            sb.draw(canvas, paint)
            animator.animate {
                sb.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            sb.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : ShrinkingBarView {
            val view : ShrinkingBarView = ShrinkingBarView(activity)
            activity.setContentView(view)
            return view
        }
    }
}