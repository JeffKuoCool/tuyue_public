package com.tuyue.common_sdk.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.hjq.toast.ToastUtils
import com.orhanobut.logger.Logger
import com.tuyue.common_sdk.model.FrameAssetsModel
import com.tuyue.common_sdk.tools.DensityUtil


/**
 * class ImageFrameView
 **/
class ImageFrameView : View {

    var mFrameId: Int? = null
    private var mPetternImage: Bitmap? = null
    //边框宽度
    private var mFrameOffset: Int = DensityUtil.dp2Px(44f)
    //四个角矩形
    private lateinit var mAngleLeftTopRect: Rect
    private lateinit var mAngleRightTopRect: Rect
    private lateinit var mAngleLeftBottomRect: Rect
    private lateinit var mAngleRightBottomRect: Rect
    //四条边矩形
    private lateinit var mFrameLeftRect: Rect
    private lateinit var mFrameTopRect: Rect
    private lateinit var mFrameRightRect: Rect
    private lateinit var mFrameBottomRect: Rect

    private val mBitmapPaint = Paint()
    private val mFramePaint = Paint()
    //绘制边界
    private lateinit var mBounds: Rect
    //边框的数据
    private var mFrameNode: FrameResNode? = null
    private var mFrameAssets: FrameAssetsModel? = null
    //缩放类型
    private var mScaleType = ImageView.ScaleType.MATRIX
    private var mType = Type.NORMAL

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        measureBounds()
        //计算bounds后计算边角矩阵坐标
        mAngleLeftTopRect = Rect(mBounds.left, mBounds.top, mBounds.left.plus(mFrameOffset), mBounds.top.plus(mFrameOffset))
        mAngleRightTopRect = Rect(mBounds.right.minus(mFrameOffset), mBounds.top, mBounds.right, mBounds.top.plus(mFrameOffset))
        mAngleLeftBottomRect = Rect(mBounds.left, mBounds.bottom.minus(mFrameOffset), mBounds.left.plus(mFrameOffset), mBounds.bottom)
        mAngleRightBottomRect = Rect(mBounds.right.minus(mFrameOffset), mBounds.bottom.minus(mFrameOffset), mBounds.right, mBounds.bottom)

        mFrameLeftRect = Rect(mBounds.left, mBounds.top.plus(mFrameOffset), mBounds.left.plus(mFrameOffset), mBounds.bottom.minus(mFrameOffset))
        mFrameTopRect = Rect(mBounds.left.plus(mFrameOffset), mBounds.top, mBounds.right.minus(mFrameOffset), mBounds.top.plus(mFrameOffset))
        mFrameRightRect = Rect(mBounds.right.minus(mFrameOffset), mBounds.top.plus(mFrameOffset), mBounds.right, mBounds.bottom.minus(mFrameOffset))
        mFrameBottomRect = Rect(mBounds.left.plus(mFrameOffset), mBounds.bottom.minus(mFrameOffset), mBounds.right.minus(mFrameOffset), mBounds.bottom)

        canvas?.let { c ->
            //绘制边框，四个角，四条边
            mFrameNode?.let {
                drawBitmap(it.frameLeftRes, mFrameLeftRect, c, mFramePaint)
                drawBitmap(it.frameTopRes, mFrameTopRect, c, mFramePaint)
                drawBitmap(it.frameRightRes, mFrameRightRect, c, mFramePaint)
                drawBitmap(it.frameBottomRes, mFrameBottomRect, c, mFramePaint)
                drawBitmap(it.angleLeftTopRes, mAngleLeftTopRect, c, mFramePaint)
                drawBitmap(it.angleRightTopRes, mAngleRightTopRect, c, mFramePaint)
                drawBitmap(it.angleLeftBottomRes, mAngleLeftBottomRect, c, mFramePaint)
                drawBitmap(it.angleRightBottomRes, mAngleRightBottomRect, c, mFramePaint)
            }
            mFrameAssets?.let {
                drawBitmap(it.left, mFrameLeftRect, c, mFramePaint)
                drawBitmap(it.up, mFrameTopRect, c, mFramePaint)
                drawBitmap(it.right, mFrameRightRect, c, mFramePaint)
                drawBitmap(it.down, mFrameBottomRect, c, mFramePaint)
                drawBitmap(it.up_left, mAngleLeftTopRect, c, mFramePaint)
                drawBitmap(it.up_right, mAngleRightTopRect, c, mFramePaint)
                drawBitmap(it.down_left, mAngleLeftBottomRect, c, mFramePaint)
                drawBitmap(it.down_right, mAngleRightBottomRect, c, mFramePaint)
                Log.e("mFrameAssets", it.toString())
            }

            //绘制原图
            mPetternImage?.let {
                val bitmapRect = Rect(mBounds.left.plus(mFrameOffset), mBounds.top.plus(mFrameOffset),
                        mBounds.right.minus(mFrameOffset), mBounds.bottom.minus(mFrameOffset))
                c.drawBitmap(it, Rect(0, 0, it.width, it.height),
                        bitmapRect, mBitmapPaint)

                Logger.e("mBounds = ${this.mBounds} bitmapRect = $bitmapRect")
            }

        }
    }

    private fun measureBounds() {
        when(mScaleType){
            ImageView.ScaleType.MATRIX -> {
                if (mPetternImage == null) return
                var newWidth = 0
                var newHeight = 0
                mPetternImage?.let {
                    if (it.width.toFloat().div(it.height.toFloat()) > width.toFloat().div(height.toFloat())) {
                        newWidth = width
                        newHeight = newWidth.times(it.height).div(it.width)
                    } else {
                        newHeight = height
                        newWidth = newHeight.times(it.width).div(it.height)
                    }
                }
                val left = width.minus(newWidth).div(2)
                val right = left.plus(newWidth)
                val top = height.minus(newHeight).div(2)
                val bottom = top.plus(newHeight)
                mBounds = Rect(left, top, right, bottom)
            }
            else -> {
                val left = 0
                val right = width
                val top = 0
                val bottom = height
                mBounds = Rect(left, top, right, bottom)
            }
        }

    }

    /**
     * 在目标矩阵里绘制图片
     * @param resId 资源id
     * @param targetRect 目标矩形矩阵
     * @param c 画布
     */
    private fun drawBitmap(resId: Int, targetRect: Rect, c: Canvas, paint: Paint) {
        val bitmap = (ContextCompat.getDrawable(context, resId) as BitmapDrawable).bitmap
        bitmap?.let {
            val rect = Rect(0, 0, it.width, it.height)
            c.drawBitmap(it, rect, targetRect, paint)
        }
    }

    private fun drawBitmap(path: String, targetRect: Rect, c: Canvas, paint: Paint) {
        val bitmap = BitmapFactory.decodeFile(path)
        bitmap?.let {
            val rect = Rect(0, 0, it.width, it.height)
            c.drawBitmap(it, rect, targetRect, paint)
        }
    }

    /**
     * 设置边框数据
     */
    fun setFrameResouce(frameResNode: FrameResNode?) {
        mFrameNode = frameResNode
        frameResNode?.let {
            mType = Type.RESOURCE
            mFramePaint.color = Color.BLACK
        }?:let {
            mType = Type.NORMAL
            mFramePaint.color = Color.TRANSPARENT
        }
        invalidate()
    }

    /**
     * 设置边框数据
     */
    fun setFrameAssets(frameAssetsModel: FrameAssetsModel?) {
        mFrameAssets = frameAssetsModel
        mFrameAssets?.let {
            if(TextUtils.isEmpty(it.left)){
//                ToastUtils.show("资源下载中～")
                return
            }
            mType = Type.ASSETS
            mFramePaint.color = Color.BLACK
        }?:let {
            mType = Type.NORMAL
            mFramePaint.color = Color.TRANSPARENT
        }
        invalidate()
    }

    /**
     * 设置边框
     */
    @Deprecated("")
    fun setFrame(frameId: Int?) {
        mFrameId = frameId
    }

    /**
     * 设置模版
     */
    fun setPetternBitmap(bitmap: Bitmap) {
        mPetternImage = bitmap
    }

    /**
     * 设置缩放类型
     */
    fun setScaleType(scaleType: ImageView.ScaleType){
        mScaleType = scaleType
    }

    fun getFrameOffset(): Int{
        return if(mFrameNode != null) mFrameOffset else 0
    }

    fun result(): SecondNode?{
        return when(mType){
            Type.RESOURCE -> {
                mFrameNode
            }
            Type.ASSETS -> {
                mFrameAssets
            }
            else -> {
                null
            }
        }
    }

    enum class Type{
        NORMAL, RESOURCE, ASSETS
    }

}