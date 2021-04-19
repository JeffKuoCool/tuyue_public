package com.tuyue.common_sdk.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tuyue.common_sdk.R
import com.tuyue.common_sdk.item_decoration.LinearListItemDecoration
import kotlinx.android.synthetic.main.view_doodle_selector.view.*


/**
 * class DoodleColorSelector
 **/
class DoodleColorSelector(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    private val mAdapter = Adapter()
    var mColor = Color.parseColor("#000000")
    private val colors = mutableListOf<Int>()
    private var mCallback: ColorSelectCallback? = null

    init {
        inflate(context, R.layout.view_doodle_selector, this)

        val manager = LinearLayoutManager(context)
        manager.orientation = LinearLayoutManager.HORIZONTAL
        rv_select_color.layoutManager = manager
        rv_select_color.adapter = mAdapter
        rv_select_color.addItemDecoration(LinearListItemDecoration("#1C1C1C", 1))

        colors.add(Color.parseColor("#FFFFFF"))
        colors.add(Color.parseColor("#7D7D7D"))
        colors.add(Color.parseColor("#000000"))
        colors.add(Color.parseColor("#66CCFF"))
        colors.add(Color.parseColor("#6687FF"))
        colors.add(Color.parseColor("#8766FF"))
        colors.add(Color.parseColor("#DE66FF"))
        colors.add(Color.parseColor("#FC66CC"))
        colors.add(Color.parseColor("#E54F4F"))
        colors.add(Color.parseColor("#F28754"))
        colors.add(Color.parseColor("#FECC66"))
        colors.add(Color.parseColor("#FFF763"))
        colors.add(Color.parseColor("#CCFF66"))
        colors.add(Color.parseColor("#54FF87"))
        colors.add(Color.parseColor("#54FFEB"))
        mAdapter.setNewInstance(colors)

        initEvent()
    }

    private fun initEvent() {
        mAdapter.setOnItemClickListener { adapter, view, position ->
            mColor = colors[position]
            mCallback?.onSelect(mColor)
        }
    }

    fun colorSelectCallback(callback: ColorSelectCallback){
        mCallback = callback
    }

}
class Adapter() : BaseQuickAdapter<Int, BaseViewHolder>(R.layout.item_doodle_color_selector) {
    override fun convert(holder: BaseViewHolder, item: Int) {
        holder.setBackgroundColor(R.id.image, item)
    }

}

interface ColorSelectCallback{
    fun onSelect(color: Int)
}