package com.tuyue.common_sdk.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.tuyue.common_sdk.R

class SquareProcessView(private val mContext: Context, attrs: AttributeSet?) : View(
    mContext, attrs
) {
    private var paint: Paint? = null
    private var processPaint: Paint? = null
    private var textPaint: Paint? = null
    private var canvas: Canvas? = null
    private var currentPogress = 0
    private var strokeColor = Color.BLACK //正方形默认颜色
    private var strokeWith = 5.0f //正方形边框默认宽度
    private var progressColor = Color.RED //进度条默认颜色
    private var textColor = Color.BLUE //百分比文字默认颜色
    private var textSize = 10.0f //百分比文字默认字体大小
    private fun initValue(attrs: AttributeSet?) {
        val typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.SquareProcessView)
        currentPogress = typedArray.getInteger(R.styleable.SquareProcessView_currentPogress, 0)
        strokeColor = typedArray.getColor(
            R.styleable.SquareProcessView_strokeColor,
            ContextCompat.getColor(mContext, R.color.colorPrimary)
        )
        strokeWith = typedArray.getDimension(R.styleable.SquareProcessView_strokeWith, strokeWith)
        progressColor = typedArray.getColor(
            R.styleable.SquareProcessView_progressColor,
            ContextCompat.getColor(mContext, R.color.colorAccent)
        )
        textColor = typedArray.getColor(
            R.styleable.SquareProcessView_textColor,
            ContextCompat.getColor(mContext, R.color.colorAccent)
        )
        textSize = typedArray.getDimension(R.styleable.SquareProcessView_textSize, textSize)
        initPaints()
    }

    private fun initPaints() {
        paint = Paint()
        paint!!.color = strokeColor
        paint!!.strokeWidth = strokeWith
        paint!!.isAntiAlias = true
        paint!!.style = Paint.Style.STROKE
        initProcessPaint()
        initTextPaint()
    }

    private fun initProcessPaint() {
        processPaint = Paint()
        processPaint!!.color = progressColor
        processPaint!!.strokeWidth = strokeWith
        processPaint!!.isAntiAlias = true
        processPaint!!.style = Paint.Style.STROKE
    }

    private fun initTextPaint() {
        textPaint = Paint()
        textPaint!!.color = textColor
        textPaint!!.isAntiAlias = true
        textPaint!!.style = Paint.Style.STROKE
        textPaint!!.textSize = textSize
    }

    fun setCurrentPogress(currentPogress: Int) {
        this.currentPogress = currentPogress
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        this.canvas = canvas
        /*画正方形*/drawSquare()
        /*画进度条*/drawProcessSquare(currentPogress)
        /*画百分比*/drawPercent()
    }

    /**
     * 四条线组成一个方形
     */
    private fun drawSquare() {
        drawTopLine()
        drawRightLine()
        drawBottomLine()
        drawLeftLine()
    }

    private fun drawTopLine() {
        val path = Path()
        /*把坐标移动到坐上下角*/path.moveTo(0f, 0f)
        path.lineTo(canvas!!.width.toFloat(), 0f)
        canvas!!.drawPath(path, paint!!)
    }

    private fun drawRightLine() {
        val path = Path()
        /*把坐标移动到右上角*/path.moveTo(canvas!!.width.toFloat(), 0f)
        path.lineTo(canvas!!.width.toFloat(), canvas!!.height.toFloat())
        canvas!!.drawPath(path, paint!!)
    }

    private fun drawBottomLine() {
        val path = Path()
        /*把坐标移动到右下角*/path.moveTo(canvas!!.width.toFloat(), canvas!!.height.toFloat())
        path.lineTo(0f, canvas!!.height.toFloat())
        canvas!!.drawPath(path, paint!!)
    }

    private fun drawLeftLine() {
        val path = Path()
        /*把坐标移动到坐上下角*/path.moveTo(0f, 0f)
        path.lineTo(0f, canvas!!.height.toFloat())
        canvas!!.drawPath(path, paint!!)
    }

    /**
     * 画进度
     */
    private fun drawProcessSquare(progress: Int) {
        var topProcess = 0f
        var rightProcess = 0f
        var bottomProcess = 0f
        var leftProcess = 0f
        if (progress <= PER_LINE_MAX_PROCESS) { //进度值小于等于一条边的进度   画 top进度线条
            topProcess = progress.toFloat()
        } else if (progress <= PER_LINE_MAX_PROCESS * 2) { //进度值小于等于两条边的进度 top 进度为线条最大值；right进度值为progress-一条线条的总进度(减去top 的值)
            topProcess = PER_LINE_MAX_PROCESS
            rightProcess = progress - PER_LINE_MAX_PROCESS
        } else if (progress <= PER_LINE_MAX_PROCESS * 3) { //进度值小于等于三条边的进度 top 进度为线条最大值;right 进度为线条最大值；bottom进度值为progress-两条线条的总进度(减去top与right的值)
            topProcess = PER_LINE_MAX_PROCESS
            rightProcess = PER_LINE_MAX_PROCESS
            bottomProcess = progress - PER_LINE_MAX_PROCESS * 2
        } else if (progress <= MAX_PROGRESS) { //进度值小于等于四条边的进度 top 进度为线条最大值;right 进度为线条最大值;bottom 进度为线条最大值；left进度值为progress-三条线条的总进度(减去top、right、bottom的值)
            topProcess = PER_LINE_MAX_PROCESS
            rightProcess = PER_LINE_MAX_PROCESS
            bottomProcess = PER_LINE_MAX_PROCESS
            leftProcess = progress - PER_LINE_MAX_PROCESS * 3
        }
        drawProgressTopLine(topProcess)
        drawProgressRightLine(rightProcess)
        drawProgressBottomLine(bottomProcess)
        drawProgressLeftLine(leftProcess)
    }

    private fun drawProgressTopLine(progress: Float) {
        val path = Path()
        /*把坐标移动到左上角*/path.moveTo(0f, 0f)
        path.lineTo(canvas!!.width / PER_LINE_MAX_PROCESS * progress, 0f)
        canvas!!.drawPath(path, processPaint!!)
    }

    private fun drawProgressRightLine(progress: Float) {
        val path = Path()
        /*把坐标移动到右上角*/path.moveTo(canvas!!.width.toFloat(), 0f)
        path.lineTo(canvas!!.width.toFloat(), canvas!!.height / PER_LINE_MAX_PROCESS * progress)
        canvas!!.drawPath(path, processPaint!!)
    }

    /*比较特殊  lineTo是从左往右画的 进度是从右往左走的  所以取 当前line 的进度 减去总 line的总长度的 绝对值*/
    private fun drawProgressBottomLine(progress: Float) {
        val path = Path()
        /*把坐标移动到右下角*/path.moveTo(canvas!!.width.toFloat(), canvas!!.height.toFloat())
        path.lineTo(
            canvas!!.height / PER_LINE_MAX_PROCESS * Math.abs(progress - PER_LINE_MAX_PROCESS),
            canvas!!.height.toFloat()
        )
        canvas!!.drawPath(path, processPaint!!)
    }

    private fun drawProgressLeftLine(progress: Float) {
        val path = Path()
        /*把坐标移动到左下角*/path.moveTo(0f, canvas!!.height.toFloat())
        path.lineTo(
            0f,
            canvas!!.height / PER_LINE_MAX_PROCESS * Math.abs(progress - PER_LINE_MAX_PROCESS)
        )
        canvas!!.drawPath(path, processPaint!!)
    }

    private fun drawPercent() {
        val width = canvas!!.width
        val height = canvas!!.height
        textPaint!!.textAlign = Paint.Align.CENTER
        val fontMetrics = textPaint!!.fontMetrics
        val top = fontMetrics.top
        val bottom = fontMetrics.bottom
        canvas!!.drawText(
            "$currentPogress%",
            (width / 2).toFloat(),
            height / 2 - top / 2 - bottom / 2,
            textPaint!!
        )
    }

    companion object {
        private const val MAX_PROGRESS = 100 //最大进度
        private const val PER_LINE_MAX_PROCESS = (100 / 4 //正方形每条边的最大进度
                ).toFloat()
    }

    init {
        initValue(attrs)
    }
}