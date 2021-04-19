package com.tuyue.common_sdk.item_decoration

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tuyue.common_sdk.tools.DensityUtil

class LinearListItemDecoration : RecyclerView.ItemDecoration {
    private var paint: Paint
    private var dividerHeight: Int
    private var isNeedBottomDivider = true

    constructor(color: String?, heightDp: Int) {
        paint = Paint()
        paint.color = Color.parseColor(color)
        paint.style = Paint.Style.FILL
        dividerHeight = DensityUtil.dp2Px(heightDp.toFloat())
    }

    constructor(color: Int, heightDp: Int) {
        paint = Paint()
        paint.color = color
        paint.style = Paint.Style.FILL
        dividerHeight = DensityUtil.dp2Px(heightDp.toFloat())
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = dividerHeight
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        val childCount = parent.childCount
        for (i in 0 until childCount - 1 - if (!isNeedBottomDivider && childCount > 1) 1 else 0) {
            val view = parent.getChildAt(i)
            val top = view.bottom.toFloat()
            val bottom = (view.bottom + dividerHeight).toFloat()
            val left = view.left
            val right = view.right
            c.drawRect(left.toFloat(), top, right.toFloat(), bottom, paint)
        }
    }

    fun hasBottomDivider(hasBottom: Boolean) {
        isNeedBottomDivider = hasBottom
    }
}