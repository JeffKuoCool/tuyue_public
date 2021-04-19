package com.tuyue.common_sdk.activity
import BaseActivity
import Camera2Loader
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.view.ViewCompat
import com.tuyue.common_sdk.camera.CameraLoader.OnPreviewFrameListener
import com.tuyue.common_sdk.R
import com.tuyue.common_sdk.tools.GPUImageFilterTools
import com.tuyue.core.util.FileUtils
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.util.Rotation
import kotlinx.android.synthetic.main.activity_camera.*

class CameraActivity : BaseActivity(), View.OnClickListener {
    private val mNoImageFilter = GPUImageFilter()
    private var mCurrentImageFilter: GPUImageFilter? = mNoImageFilter
    private var mFilterAdjuster: GPUImageFilterTools.FilterAdjuster? = null
    private lateinit var mCameraLoader: Camera2Loader
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        initView()
        initCamera()
    }

    private fun initView() {
        filter_name_tv.setOnClickListener(this)
        tone_seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener)
        findViewById<View>(R.id.compare_iv).setOnTouchListener(mOnTouchListener)
        findViewById<View>(R.id.close_iv).setOnClickListener(this)
        findViewById<View>(R.id.save_iv).setOnClickListener(this)
        findViewById<View>(R.id.switch_camera_iv).setOnClickListener(this)
    }

    private fun initCamera() {
        mCameraLoader = Camera2Loader(this)
        mCameraLoader.setOnPreviewFrameListener(object : OnPreviewFrameListener {
            override fun onPreviewFrame(data: ByteArray?, width: Int, height: Int) {
                gpuimage.updatePreviewFrame(data, width, height)
            }
        })
        gpuimage.setRatio(0.75f) // 固定使用 4:3 的尺寸
        updateGPUImageRotate()
        gpuimage.setRenderMode(GPUImageView.RENDERMODE_CONTINUOUSLY)
    }

    private fun updateGPUImageRotate() {
        val rotation = getRotation(mCameraLoader!!.cameraOrientation)
        var flipHorizontal = false
        var flipVertical = false
        if (mCameraLoader.isFrontCamera) { // 前置摄像头需要镜像
            if (rotation == Rotation.NORMAL || rotation == Rotation.ROTATION_180) {
                flipHorizontal = true
            } else {
                flipVertical = true
            }
        }
        gpuimage.gpuImage.setRotation(rotation, flipHorizontal, flipVertical)
    }

    override fun onResume() {
        super.onResume()
        if (ViewCompat.isLaidOut(gpuimage) && !gpuimage.isLayoutRequested) {
            mCameraLoader.onResume(gpuimage.width, gpuimage.height)
        } else {
            gpuimage.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                override fun onLayoutChange(
                    v: View,
                    left: Int,
                    top: Int,
                    right: Int,
                    bottom: Int,
                    oldLeft: Int,
                    oldTop: Int,
                    oldRight: Int,
                    oldBottom: Int
                ) {
                    gpuimage.removeOnLayoutChangeListener(this)
                    mCameraLoader.onResume(gpuimage.width, gpuimage.height)
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        mCameraLoader.onPause()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.filter_name_tv -> GPUImageFilterTools.showDialog(
                this,
                mOnGpuImageFilterChosenListener
            )
            R.id.close_iv -> finish()
            R.id.save_iv -> saveSnapshot()
            R.id.switch_camera_iv -> {
                gpuimage.gpuImage.deleteImage()
                mCameraLoader!!.switchCamera()
                updateGPUImageRotate()
            }
        }
    }

    private fun saveSnapshot() {
        val fileName = System.currentTimeMillis().toString() + ".jpg"
        gpuimage.saveToPictures("GPUImage", fileName, mOnPictureSavedListener)
    }

    private val mOnTouchListener = View.OnTouchListener { v, event ->
        if (v.id == R.id.compare_iv) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> gpuimage.filter = mNoImageFilter
                MotionEvent.ACTION_UP -> gpuimage.filter = mCurrentImageFilter
            }
        }
        true
    }
    private val mOnGpuImageFilterChosenListener =
        GPUImageFilterTools.OnGpuImageFilterChosenListener { filter, filterName ->
            switchFilterTo(filter)
            filter_name_tv.text = filterName
        }

    private fun switchFilterTo(filter: GPUImageFilter?) {
        if (mCurrentImageFilter == null
            || filter != null && mCurrentImageFilter!!.javaClass != filter.javaClass
        ) {
            mCurrentImageFilter = filter
            gpuimage.filter = mCurrentImageFilter
            mFilterAdjuster = GPUImageFilterTools.FilterAdjuster(mCurrentImageFilter)
            tone_seekbar.visibility =
                if (mFilterAdjuster!!.canAdjust()) View.VISIBLE else View.GONE
        } else {
            tone_seekbar.visibility = View.GONE
        }
    }

    private val mOnSeekBarChangeListener: SeekBar.OnSeekBarChangeListener =
        object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mFilterAdjuster != null) {
                    mFilterAdjuster!!.adjust(progress)
                }
                gpuimage.requestRender()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        }
    private val mOnPictureSavedListener = GPUImageView.OnPictureSavedListener { uri ->
        val filePath = FileUtils.getRealFilePath(this@CameraActivity, uri)
        Toast.makeText(this@CameraActivity, "Saved: $filePath", Toast.LENGTH_SHORT).show()
    }

    private fun getRotation(orientation: Int): Rotation {
        return when (orientation) {
            90 -> Rotation.ROTATION_90
            180 -> Rotation.ROTATION_180
            270 -> Rotation.ROTATION_270
            else -> Rotation.NORMAL
        }
    }
}