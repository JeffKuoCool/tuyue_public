package com.tuyue.common_sdk.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View


/**
 * class PointDrawer
 **/
class PointDrawer(context: Context?) : View(context) {

    private var mRadius = 100f
    private val mPaint = Paint()
    private var mColor = Color.parseColor("#000000")

    init {
        mPaint.color = mColor
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawCircle(mRadius, mRadius, mRadius, mPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(mRadius.times(2).toInt(), mRadius.times(2).toInt())
    }

    fun setColor(color: Int){
        mColor = color
        mPaint.color = mColor
    }

    fun adjust(radiu: Float){
        mRadius = radiu
        invalidate()
    }
}