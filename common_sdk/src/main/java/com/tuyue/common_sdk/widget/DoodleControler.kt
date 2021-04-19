package com.tuyue.common_sdk.widget

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.divyanshu.draw.widget.DrawView
import com.hjq.toast.ToastUtils
import com.tuyue.common_sdk.R
import com.tuyue.common_sdk.activity.PetternModel
import com.tuyue.common_sdk.adapter.PetternAdapter
import com.tuyue.common_sdk.image_edit.PetternType
import com.tuyue.common_sdk.item_decoration.LinearListItemDecoration
import kotlinx.android.synthetic.main.view_doodle_controler.view.*
import kotlinx.android.synthetic.main.view_pettern_controler.view.*
import kotlinx.android.synthetic.main.view_pettern_controler.view.rv_controler


class DoodleControler(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    private val mNormalAdapter = PetternAdapter()
    private var mDoodleData: MutableList<PetternModel> = mutableListOf()
    private lateinit var mDoodleView: DrawView

    init {
        inflate(context, R.layout.view_doodle_controler, this)
        initBase()
        setEvent()
    }

    private fun setEvent() {
        iv_doodle_close.setOnClickListener {
            doodle_color_selector.visibility = View.GONE
            doodle_bottom.visibility = View.INVISIBLE
        }
        iv_clear.setOnClickListener {
            mDoodleView.clearCanvas()
            iv_doodle_undo.isEnabled = mDoodleView.isCanUndo()
        }
        bg_doodle_undo.setOnClickListener {
            mDoodleView.undo()
            iv_doodle_undo.isEnabled = mDoodleView.isCanUndo()
        }
        bg_doodle_redo.setOnClickListener {
            mDoodleView.redo()
        }
        mNormalAdapter.setOnItemClickListener { adapter, view, position ->
            when(mDoodleData[position].petternType){
                PetternType.DOODLE_COLOR -> {
                    doodle_color_selector.visibility = View.VISIBLE
                    doodle_bottom.visibility = View.VISIBLE
                }
                PetternType.DOODLE_SIZE -> {
                    tone_doodle_seekbar.visibility = View.VISIBLE
                }
            }
        }
        doodle_color_selector.colorSelectCallback(object : ColorSelectCallback{
            override fun onSelect(color: Int) {
                mDoodleView.setColor(color)
                doodle_color_selector.visibility = View.GONE
                doodle_bottom.visibility = View.INVISIBLE
            }
        })
        tone_doodle_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                mDoodleView.setStrokeWidth(p1.toFloat())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })

    }

    private fun initBase(){
        val manager = LinearLayoutManager(context)
        manager.orientation = LinearLayoutManager.HORIZONTAL
        rv_controler.layoutManager = manager
        rv_controler.adapter = mNormalAdapter
        rv_controler.addItemDecoration(LinearListItemDecoration("#1C1C1C", 1))

        mDoodleData.add(PetternModel(R.drawable.ic_ys, "颜色", PetternType.DOODLE_COLOR))
        mDoodleData.add(PetternModel(R.drawable.ic_dx, "大小", PetternType.DOODLE_SIZE))
//        mDoodleData.add(PetternModel(R.drawable.ic_yd, "硬度", PetternType.DOODLE))
        mNormalAdapter.setList(mDoodleData)
        doodle_color_selector.visibility = View.GONE
        doodle_bottom.visibility = View.INVISIBLE
        iv_doodle_undo.isEnabled = false
    }

    fun init(){
        this.visibility = View.VISIBLE
    }

    fun bindDoodleView(doodleView: DrawView){
        mDoodleView = doodleView
        mDoodleView.addTouchAction(object :DrawView.TouchAction{
            override fun event_up() {
                iv_doodle_undo.isEnabled = mDoodleView.isCanUndo()
            }
        })
    }

    fun outputBitmap(): Bitmap {
        iv_doodle_undo.isEnabled = false
        return mDoodleView.getBitmap()
    }

}