package com.xinlan.imageeditlibrary.editimage.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.xinlan.imageeditlibrary.R
import com.xinlan.imageeditlibrary.editimage.utils.PaintUtil

/**
 * 剪切图片
 *
 * @author 潘易
 */
class CropImageView : View {
    private val CIRCLE_WIDTH = 65
    private var mContext: Context? = null
    private var oldx = 0f
    private var oldy = 0f
    private var status = STATUS_IDLE
    private var selectedControllerCicle = 0
    private val backUpRect = RectF() // 上
    private val backLeftRect = RectF() // 左
    private val backRightRect = RectF() // 右
    private val backDownRect = RectF() // 下
    private val cropRect = RectF() // 剪切矩形
    private lateinit var mBackgroundPaint // 背景Paint
            : Paint
    private lateinit var mAnglePaint: Paint//边角画笔
    private lateinit var mDividerPaint: Paint//分割线画笔
    private lateinit var mCropBoundsPaint: Paint//边界画笔
    private lateinit var circleBit: Bitmap
    private val circleRect = Rect()
    private lateinit var leftTopCircleRect: RectF
    private lateinit var rightTopCircleRect: RectF
    private lateinit var leftBottomRect: RectF
    private lateinit var rightBottomRect: RectF
    private val imageRect = RectF() // 存贮图片位置信息
    private val tempRect = RectF() // 临时存贮矩形数据
    var ratio = -1f // 剪裁缩放比率

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        mContext = context
        mBackgroundPaint = PaintUtil.newBackgroundPaint(context)
        mAnglePaint = PaintUtil.newRotateBottomImagePaint()
        mDividerPaint = PaintUtil.newCustomPaint("#60FFFFFF", 1f)
        mCropBoundsPaint = PaintUtil.newCustomPaint("#60FFFFFF", 3f)

        circleBit = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.sticker_rotate
        )
        circleRect[0, 0, circleBit.getWidth()] = circleBit.getHeight()
        leftTopCircleRect = RectF(0f, 0f, CIRCLE_WIDTH.toFloat(), CIRCLE_WIDTH.toFloat())
        rightTopCircleRect = RectF(leftTopCircleRect)
        leftBottomRect = RectF(leftTopCircleRect)
        rightBottomRect = RectF(leftTopCircleRect)
    }

    /**
     * 重置剪裁面
     *
     * @param rect
     */
    fun setCropRect(rect: RectF?) {
        if (rect == null) return
        imageRect.set(rect)
        cropRect.set(rect)
        scaleRect(cropRect, 1f)
        invalidate()
    }

    fun setRatioCropRect(rect: RectF, r: Float) {
        ratio = r
        if (r < 0) {
            setCropRect(rect)
            return
        }
        imageRect.set(rect)
        cropRect.set(rect)
        // 调整Rect
        val h: Float
        val w: Float
        if (cropRect.width().div(cropRect.height()).toFloat() >= ratio ) { // w>=h
            h = cropRect.height()
            w = ratio * h
        } else { // w<h
            w = rect.width()
            h = w / ratio
        } // end if
        val scaleX = w / cropRect.width()
        val scaleY = h / cropRect.height()
        scaleRect(cropRect, scaleX, scaleY)
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val w = width
        val h = height
        if (w <= 0 || h <= 0) return

        // 绘制黑色背景
        backUpRect[0f, 0f, w.toFloat()] = cropRect.top
        backLeftRect[0f, cropRect.top, cropRect.left] = cropRect.bottom
        backRightRect[cropRect.right, cropRect.top, w.toFloat()] = cropRect.bottom
        backDownRect[0f, cropRect.bottom, w.toFloat()] = h.toFloat()
        canvas.drawRect(backUpRect, mBackgroundPaint)
        canvas.drawRect(backLeftRect, mBackgroundPaint)
        canvas.drawRect(backRightRect, mBackgroundPaint)
        canvas.drawRect(backDownRect, mBackgroundPaint)

        // 绘制四个控制点
        val radius = CIRCLE_WIDTH shr 1
        leftTopCircleRect[cropRect.left - radius, cropRect.top - radius, cropRect.left + radius] =
            cropRect.top + radius
        rightTopCircleRect[cropRect.right - radius, cropRect.top - radius, cropRect.right + radius] =
            cropRect.top + radius
        leftBottomRect[cropRect.left - radius, cropRect.bottom - radius, cropRect.left + radius] =
            cropRect.bottom + radius
        rightBottomRect[cropRect.right - radius, cropRect.bottom - radius, cropRect.right + radius] =
            cropRect.bottom + radius
//        canvas.drawBitmap(circleBit, circleRect, leftTopCircleRect, null)
//        canvas.drawBitmap(circleBit, circleRect, rightTopCircleRect, null)
//        canvas.drawBitmap(circleBit, circleRect, leftBottomRect, null)
//        canvas.drawBitmap(circleBit, circleRect, rightBottomRect, null)

        val paintOffset = mAnglePaint.strokeWidth.div(2)
        //左上边角
        canvas.drawLine(leftTopCircleRect.centerX().minus(paintOffset), leftTopCircleRect.centerY(), leftTopCircleRect.right, leftTopCircleRect.centerY(), mAnglePaint)
        canvas.drawLine(leftTopCircleRect.centerX(), leftTopCircleRect.centerY().minus(paintOffset), leftTopCircleRect.centerX(), leftTopCircleRect.bottom, mAnglePaint)
        //右上边角
        canvas.drawLine(rightTopCircleRect.left, rightTopCircleRect.centerY(), rightTopCircleRect.centerX().plus(paintOffset), rightTopCircleRect.centerY(), mAnglePaint)
        canvas.drawLine(rightTopCircleRect.centerX(), rightTopCircleRect.centerY().minus(paintOffset), rightTopCircleRect.centerX(), rightTopCircleRect.bottom, mAnglePaint)
        //左下边角
        canvas.drawLine(leftBottomRect.centerX(), leftBottomRect.top, leftBottomRect.centerX(), leftBottomRect.centerY().plus(paintOffset), mAnglePaint)
        canvas.drawLine(leftBottomRect.centerX().minus(paintOffset), leftBottomRect.centerY(), leftBottomRect.right, leftBottomRect.centerY(), mAnglePaint)
        //右下边角
        canvas.drawLine(rightBottomRect.left, rightBottomRect.centerY(), rightBottomRect.centerX().plus(paintOffset), rightBottomRect.centerY(), mAnglePaint)
        canvas.drawLine(rightBottomRect.centerX(), rightBottomRect.centerY().plus(paintOffset), rightBottomRect.centerX(), rightBottomRect.top, mAnglePaint)

        //边界
        canvas.drawLine(cropRect.left, cropRect.top, cropRect.left, cropRect.bottom, mCropBoundsPaint)
        canvas.drawLine(cropRect.left, cropRect.top, cropRect.right, cropRect.top, mCropBoundsPaint)
        canvas.drawLine(cropRect.right, cropRect.top, cropRect.right, cropRect.bottom, mCropBoundsPaint)
        canvas.drawLine(cropRect.left, cropRect.bottom, cropRect.right, cropRect.bottom, mCropBoundsPaint)

        //分割线
        val baseX = cropRect.right.minus(cropRect.left).div(3)
        val baseY = cropRect.bottom.minus(cropRect.top).div(3)
        canvas.drawLine(cropRect.left.plus(baseX), cropRect.top, cropRect.left.plus(baseX), cropRect.bottom, mDividerPaint)
        canvas.drawLine(cropRect.left.plus(baseX.times(2)), cropRect.top, cropRect.left.plus(baseX.times(2)), cropRect.bottom, mDividerPaint)
        canvas.drawLine(cropRect.left, cropRect.top.plus(baseY), cropRect.right, cropRect.top.plus(baseY), mDividerPaint)
        canvas.drawLine(cropRect.left, cropRect.top.plus(baseY.times(2)), cropRect.right, cropRect.top.plus(baseY.times(2)), mDividerPaint)
    }

    /**
     * 触摸事件处理
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var ret = super.onTouchEvent(event) // 是否向下传递事件标志 true为消耗
        val action = event.action
        val x = event.x
        val y = event.y
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                val selectCircle = isSeletedControllerCircle(x, y)
                if (selectCircle > 0) { // 选择控制点
                    ret = true
                    selectedControllerCicle = selectCircle // 记录选中控制点编号
                    status = STATUS_SCALE // 进入缩放状态
                } else if (cropRect.contains(x, y)) { // 选择缩放框内部
                    ret = true
                    status = STATUS_MOVE // 进入移动状态
                } else { // 没有选择
                } // end if
            }
            MotionEvent.ACTION_MOVE -> if (status == STATUS_SCALE) { // 缩放控制
                // System.out.println("缩放控制");
                scaleCropController(x, y)
            } else if (status == STATUS_MOVE) { // 移动控制
                // System.out.println("移动控制");
                translateCrop(x - oldx, y - oldy)
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> status = STATUS_IDLE // 回归空闲状态
        }

        // 记录上一次动作点
        oldx = x
        oldy = y
        return ret
    }

    /**
     * 移动剪切框
     *
     * @param dx
     * @param dy
     */
    private fun translateCrop(dx: Float, dy: Float) {
        tempRect.set(cropRect) // 存贮原有数据，以便还原
        translateRect(cropRect, dx, dy)
        // 边界判定算法优化
        val mdLeft = imageRect.left - cropRect.left
        if (mdLeft > 0) {
            translateRect(cropRect, mdLeft, 0f)
        }
        val mdRight = imageRect.right - cropRect.right
        if (mdRight < 0) {
            translateRect(cropRect, mdRight, 0f)
        }
        val mdTop = imageRect.top - cropRect.top
        if (mdTop > 0) {
            translateRect(cropRect, 0f, mdTop)
        }
        val mdBottom = imageRect.bottom - cropRect.bottom
        if (mdBottom < 0) {
            translateRect(cropRect, 0f, mdBottom)
        }
        this.invalidate()
    }

    /**
     * 操作控制点 控制缩放
     *
     * @param x
     * @param y
     */
    private fun scaleCropController(x: Float, y: Float) {
        tempRect.set(cropRect) // 存贮原有数据，以便还原

        var moveX = x
        var moveY = y
        if(x<imageRect.left){
            moveX = imageRect.left
        }
        if(x> imageRect.right){
            moveX = imageRect.right
        }
        if(y<imageRect.top){
            moveY = imageRect.top
        }
        if(y>imageRect.bottom){
            moveY = imageRect.bottom
        }

        if (ratio < 0) { // 任意缩放比
            when (selectedControllerCicle) {
                1 -> {
                    cropRect.left = x
                    cropRect.top = y
                }
                2 -> {
                    cropRect.right = x
                    cropRect.top = y
                }
                3 -> {
                    cropRect.left = x
                    cropRect.bottom = y
                }
                4 -> {
                    cropRect.right = x
                    cropRect.bottom = y
                }
            }
            // 边界条件检测
            validateCropRect()
            invalidate()
        } else {
            // 更新剪切矩形长宽
            // 确定不变点
            when (selectedControllerCicle) {
                1 -> {
                    if(cropRect.right.minus(moveX).div(cropRect.bottom.minus(moveY))> ratio){
                        cropRect.top = moveY
                        val width = cropRect.bottom.minus(cropRect.top).times(ratio)
                        cropRect.left = cropRect.right.minus(width)
                    }else{
                        cropRect.left = moveX
                        val height = cropRect.right.minus(cropRect.left).div(ratio)
                        cropRect.top = cropRect.bottom.minus(height)
                    }
                }
                2 -> {
                    if(moveX.minus(cropRect.left).div(cropRect.bottom.minus(moveY))> ratio){
                        cropRect.top = moveY
                        val width = cropRect.bottom.minus(cropRect.top).times(ratio)
                        cropRect.right = cropRect.left.plus(width)
                    }else{
                        cropRect.right = moveX
                        val height = cropRect.right.minus(cropRect.left).div(ratio)
                        cropRect.top = cropRect.bottom.minus(height)
                    }
                }
                3 -> {
                    if(cropRect.right.minus(moveX).div(moveY.minus(cropRect.top))> ratio){
                        cropRect.bottom = moveY
                        val width = cropRect.bottom.minus(cropRect.top).times(ratio)
                        cropRect.left = cropRect.right.minus(width)
                    }else{
                        cropRect.left = moveX
                        val height = cropRect.right.minus(cropRect.left).div(ratio)
                        cropRect.bottom = cropRect.top.plus(height)
                    }
                }
                4 -> {
                    if(moveX.minus(cropRect.left).div(moveY.minus(cropRect.top)) > ratio){
                        cropRect.bottom = moveY
                        val width = cropRect.bottom.minus(cropRect.top).times(ratio)
                        cropRect.right = cropRect.left.plus(width)
                    }else{
                        cropRect.right = moveX
                        val height = cropRect.right.minus(cropRect.left).div(ratio)
                        cropRect.bottom = cropRect.top.plus(height)
                    }
                }
            }
            invalidate()
        }
    }

    /**
     * 边界条件检测
     *
     */
    private fun validateCropRect() {
        if (cropRect.width() < CIRCLE_WIDTH) {
            cropRect.left = tempRect.left
            cropRect.right = tempRect.right
        }
        if (cropRect.height() < CIRCLE_WIDTH) {
            cropRect.top = tempRect.top
            cropRect.bottom = tempRect.bottom
        }
        if (cropRect.left < imageRect.left) {
            cropRect.left = imageRect.left
        }
        if (cropRect.right > imageRect.right) {
            cropRect.right = imageRect.right
        }
        if (cropRect.top < imageRect.top) {
            cropRect.top = imageRect.top
        }
        if (cropRect.bottom > imageRect.bottom) {
            cropRect.bottom = imageRect.bottom
        }
    }

    /**
     * 是否选中控制点
     *
     * -1为没有
     *
     * @param x
     * @param y
     * @return
     */
    private fun isSeletedControllerCircle(x: Float, y: Float): Int {
        if (leftTopCircleRect.contains(x, y)) // 选中左上角
            return 1
        if (rightTopCircleRect.contains(x, y)) // 选中右上角
            return 2
        if (leftBottomRect.contains(x, y)) // 选中左下角
            return 3
        return if (rightBottomRect.contains(x, y)) 4 else -1
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    /**
     * 返回剪切矩形
     *
     * @return
     */
    fun getCropRect(): RectF {
        return RectF(cropRect)
    }

    companion object {
        private const val STATUS_IDLE = 1 // 空闲状态
        private const val STATUS_MOVE = 2 // 移动状态
        private const val STATUS_SCALE = 3 // 缩放状态

        /**
         * 移动矩形
         *
         * @param rect
         * @param dx
         * @param dy
         */
        private fun translateRect(rect: RectF, dx: Float, dy: Float) {
            rect.left += dx
            rect.right += dx
            rect.top += dy
            rect.bottom += dy
        }

        /**
         * 缩放指定矩形
         *
         * @param rect
         */
        private fun scaleRect(rect: RectF, scaleX: Float, scaleY: Float) {
            val w = rect.width()
            val h = rect.height()
            val newW = scaleX * w
            val newH = scaleY * h
            val dx = (newW - w) / 2
            val dy = (newH - h) / 2
            rect.left -= dx
            rect.top -= dy
            rect.right += dx
            rect.bottom += dy
        }

        /**
         * 缩放指定矩形
         *
         * @param rect
         * @param scale
         */
        private fun scaleRect(rect: RectF, scale: Float) {
            scaleRect(rect, scale, scale)
        }
    }
}
