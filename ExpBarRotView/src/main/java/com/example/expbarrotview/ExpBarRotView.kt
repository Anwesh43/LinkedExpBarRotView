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
                    view.invalidate()
                    Thread.sleep(delay)
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

    data class EBRNode(var i : Int, val state : State = State()) {

        private var next : EBRNode? = null
        private var prev : EBRNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = EBRNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawEBRNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : EBRNode {
            var curr : EBRNode? = prev
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

    data class ExpBarRot(var i : Int) {

        private var curr : EBRNode = EBRNode(0)
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

    data class Renderer(var view : ExpBarRotView) {

        private val animator : Animator = Animator(view)
        private val expBarRot : ExpBarRot = ExpBarRot(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            expBarRot.draw(canvas, paint)
            animator.animate {
                expBarRot.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            expBarRot.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : ExpBarRotView {
            val view : ExpBarRotView = ExpBarRotView(activity)
            activity.setContentView(view)
            return view
        }
    }
}
