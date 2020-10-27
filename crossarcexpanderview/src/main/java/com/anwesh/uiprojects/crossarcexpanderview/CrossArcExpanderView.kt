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

val parts : Int = 8
val scGap : Float = 0.02f / parts
val strokeFactor : Float = 90f
val sizeFactor : Float = 2.9f
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
