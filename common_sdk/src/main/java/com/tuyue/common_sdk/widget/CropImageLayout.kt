package com.tuyue.common_sdk.widget
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.hjq.toast.ToastUtils
import com.tuyue.common_sdk.R
import com.xinlan.imageeditlibrary.editimage.utils.BitmapUtils
import com.xinlan.imageeditlibrary.editimage.utils.Matrix3
import com.xinlan.imageeditlibrary.editimage.view.imagezoom.ImageViewTouchBase
import kotlinx.android.synthetic.main.view_crop_layout.view.*

/**
 * create by guojian
 * Date: 2020/12/18
 * 裁剪控件
 */
class CropImageLayout(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    /**
     * 原图
     */
    private var mUri: String? = null
    private var mBitmap: Bitmap? = null

    /**
     * 调整后的临时图片
     */
    private var adjustBitmap: Bitmap? = null

    private var mCropRatio: Float = 0f

    init {
        View.inflate(context, R.layout.view_crop_layout, this)
    }

    /**
     * 设置原图
     */
    fun setImageBitmap(uri: String){
        mUri = uri
        adjustBitmap = BitmapUtils.parseBitmapFromUri(context, uri)
        mBitmap = BitmapUtils.parseBitmapFromUri(context, uri)
    }

    fun setImageBitmap(bitmap: Bitmap){
        adjustBitmap = bitmap
        mBitmap?.let {
            if(it.width == adjustBitmap?.width)return
            val scaleWidth: Float = adjustBitmap!!.width / it.width.toFloat()
            val scaleHeight: Float = adjustBitmap!!.height / it.height.toFloat()
            val matrix = Matrix()
            matrix.postScale(scaleWidth, scaleHeight)
            val newBitmap: Bitmap = Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, false)
            mBitmap = newBitmap
        }
    }

    /**
     * 切换裁剪模式
     */
    fun checkCropMode(ratio: Float? = null){
        ratio?.let {
            mCropRatio = ratio
        }
        adjustBitmap?.let {
            main_image.displayType = ImageViewTouchBase.DisplayType.FIT_TO_SCREEN
            main_image.setScaleEnabled(true)
            main_image.setImageBitmap(it)

            main_image.post {
                crop_image_view.setRatioCropRect(main_image.bitmapRect, mCropRatio)
            }
        }
    }

    /**
     * 逆时针旋转90度
     */
    fun cropRotate(){
        adjustBitmap?.let {
            adjustBitmap = BitmapUtils.rotateBitmap(-90, it)
            checkCropMode(mCropRatio)
        }
    }

    /**
     * 水平翻转
     */
    fun overturnBitmap(){
        adjustBitmap?.let {
            adjustBitmap = BitmapUtils.overturnBitmap(it, true, false)
            checkCropMode(mCropRatio)
        }
    }

    fun getCropBitmap():Bitmap?{
        mBitmap?.let {
            val cropRect: RectF = crop_image_view.getCropRect() // 剪切区域矩形

            val touchMatrix: Matrix = main_image.imageViewMatrix
            val data = FloatArray(9)
            touchMatrix.getValues(data) // 底部图片变化记录矩阵原始数据

            val cal = Matrix3(data) // 辅助矩阵计算类

            val inverseMatrix = cal.inverseMatrix() // 计算逆矩阵

            val m = Matrix()
            m.setValues(inverseMatrix.values)
            m.mapRect(cropRect) // 变化剪切矩形
            try {
                val width = cropRect.width().toInt()
                val height = cropRect.height().toInt()
                val newBitmap = Bitmap.createBitmap(
                    it,
                    cropRect.left.toInt(), cropRect.top.toInt(),
                    width, height
                )
                mBitmap = newBitmap
                return newBitmap
            }catch (e: Exception){
                Log.e("createBitmap", e.message.toString())
                ToastUtils.show("裁剪失败")
            }
        }
        return null
    }

    /**
     * 重置
     */
    fun reset(){
        mUri?.let {
            setImageBitmap(it)
            checkCropMode()
        }

    }

}