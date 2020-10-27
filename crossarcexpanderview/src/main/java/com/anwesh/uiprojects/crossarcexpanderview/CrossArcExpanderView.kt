package com.anwesh.uiprojects.crossarcexpanderview

/**
 * Created by anweshmishra on 27/10/20.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

val lines : Int = 4
val parts : Int = 2 * lines
val scGap : Float = 0.02f / parts
val strokeFactor : Float = 90f
val sizeFactor : Float = 2.9f
val rFactor : Float = 8.9f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")
val colors : Array<Int> = arrayOf(
        "#F44336",
        "#03A9F4",
        "#FF9800",
        "#3F51B5",
        "#009688"
).map {
    Color.parseColor(it)
}.toTypedArray()
val rot : Float = 90f
val start : Float = 45f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawCrossArcExpander(i : Int, scale : Float, w : Float, h : Float, paint : Paint) {
    val r : Float = Math.min(w, h) / rFactor
    val size : Float = Math.min(w, h) / sizeFactor
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(i, parts)
    val sf2 : Float = sf.divideScale(lines + i, parts)
    val deg : Float = rot * sf2
    save()
    rotate(rot * i)
    drawLine(0f, 0f, size * sf1, size * sf1, paint)
    drawArc(RectF(-r, -r, r, r), -deg / 2, deg, true, paint)
    restore()
}

fun Canvas.drawCrossArcExpanders(scale : Float, w : Float, h : Float, paint : Paint) {
    save()
    translate(w / 2, h / 2)
    for (j in 0..3) {
        drawCrossArcExpander(j, scale, w, h, paint)
    }
    restore()
}

fun Canvas.drawCAENode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawCrossArcExpanders(scale, w, h, paint)
}

class CrossArcExpanderView(ctx : Context) : View(ctx) {

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

    data class CAENode(var i : Int, val state : State = State()) {

        private var prev : CAENode? = null
        private var next : CAENode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = CAENode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawCAENode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : CAENode {
            var curr : CAENode? = prev
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

    data class CrossArcExpander(var i : Int) {

        private var curr : CAENode = CAENode(0)
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

    data class Renderer(var view : CrossArcExpanderView) {

        private val cae : CrossArcExpander = CrossArcExpander(0)
        private val animator : Animator = Animator(view)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            cae.draw(canvas, paint)
            animator.animate {
                cae.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            cae.startUpdating {
                animator.start()
            }
        }
    }
}