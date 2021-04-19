package com.tuyue.common_sdk.widget
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
//import com.ldoublem.loadingviewlib.view.LVCircularZoom
import com.tuyue.common_sdk.model.FrameAssetsModel
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import resetSize

/**
 * create by guojian
 * Date: 2020/12/24
 */
class GPUImageLayout : FrameLayout {

    //边框id
    private var mFrameId: Int? = null
    private var mFrameView: ImageView = ImageView(context)
    private var mImageFrameView: ImageFrameView = ImageFrameView(context)
    var mGPUImageView: GPUImageView = GPUImageView(context)
    private var mStyleTransferView: ImageView = ImageView(context)
    var mFrameOffset: Int = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    init {
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        mGPUImageView.layoutParams = params
        addView(mGPUImageView)

        mStyleTransferView.layoutParams = params
        addView(mStyleTransferView)

        mFrameView.layoutParams = params
        mFrameView.scaleType = ImageView.ScaleType.FIT_XY
        addView(mFrameView)

        mImageFrameView.layoutParams = params
        mImageFrameView.setScaleType(ImageView.ScaleType.FIT_XY)
        addView(mImageFrameView)
    }

    /**
     * 设置边框
     * @param secondNode 边框资源模型
     * @param frameOffset 偏移量
     */
    fun postFrame(secondNode: SecondNode?, frameOffset: Int?){
        secondNode?.let {
            if(it is FrameResNode){
                mImageFrameView.setFrameResouce(it)
            }
            if(it is FrameAssetsModel){
                mImageFrameView.setFrameAssets(it)
            }
        }

        val params = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        frameOffset?.let {
            mFrameOffset = frameOffset
            params.setMargins(frameOffset, frameOffset, frameOffset, frameOffset)
        }?:let {
            mFrameOffset = 0
            params.setMargins(0, 0, 0, 0)
        }
        mGPUImageView.layoutParams = params
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val ract = Rect(0, 0, width, height)
        mFrameId?.let {
            val frameBitmap = BitmapFactory.decodeResource(resources, it)
            canvas?.drawBitmap(frameBitmap, ract, ract, Paint())
        }
    }

    /**
     * 预览风格迁移效果
     */
    fun styleTransfer(bitmap: Bitmap){
        mStyleTransferView.visibility = View.VISIBLE
        mGPUImageView.visibility = View.INVISIBLE
        mStyleTransferView.setImageBitmap(bitmap)
    }

    fun finish(){
        mStyleTransferView.visibility = View.INVISIBLE
        mGPUImageView.visibility = View.VISIBLE
    }


    /**
     * 设置
     */
    fun setImageResetMatrix(bitmap: Bitmap) {

        var resetWidght = 0
        var resetHeight = 0
        val dm: DisplayMetrics = resources.displayMetrics

        val widgetWidth = dm.widthPixels
        val widgetHeight = dm.heightPixels

        val scale = widgetWidth.div(widgetHeight)
        if (scale > bitmap.width.div(bitmap.height)) {
            resetHeight = widgetHeight
            resetWidght = resetHeight.times(bitmap.width).div(bitmap.height)
        } else {
            resetWidght = widgetWidth
            resetHeight = resetWidght.times(bitmap.height).div(bitmap.width)
        }
        resetSize(resetWidght, resetHeight)

        mGPUImageView.setImage(bitmap)
    }

    fun confirmStyleTransfer(){
        val style = (mStyleTransferView.drawable as BitmapDrawable).bitmap
        mGPUImageView.gpuImage.deleteImage()
        setImageResetMatrix(style)
    }

    fun capture(): Bitmap{
        return mGPUImageView.capture()
    }

    fun requestRender(){
        mGPUImageView.requestRender()
    }

    fun clearFilter(){
        mGPUImageView.filter = GPUImageFilter()
        mGPUImageView.requestRender()
    }
}