package com.alpenraum.shimstack.ui.base

import android.content.Context
import android.content.res.TypedArray
import android.util.TypedValue

fun Context.resolveColorAttr(attr: Int): Int {
    val typedValue = TypedValue()
    val a: TypedArray = this.obtainStyledAttributes(typedValue.data, intArrayOf(attr))
    val color = a.getColor(0, 0)
    a.recycle()
    return color
}