package com.tuyue.common_sdk.activity
import BaseActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.tuyue.common_sdk.R
import com.tuyue.common_sdk.tools.GPUImageFilterTools
import com.tuyue.core.util.FileUtils
import com.xinlan.imageeditlibrary.editimage.utils.BitmapUtils
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import kotlinx.android.synthetic.main.activity_gallery.*

class GalleryActivity : BaseActivity(), View.OnClickListener {
    private val mNoImageFilter = GPUImageFilter()
    private var mCurrentImageFilter: GPUImageFilter? = mNoImageFilter
    private var mFilterAdjuster: GPUImageFilterTools.FilterAdjuster? = null

    private var imageWidth: Int = 0
    private var imageHeight: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        initView()
        openGallery()
    }

    private fun initView() {
        val metrics = resources.displayMetrics
        imageWidth = metrics.widthPixels / 2
        imageHeight = metrics.heightPixels / 2

        gpuimage.setOnClickListener(this)
        filter_name_tv.setOnClickListener(this)
        tone_seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener)
        findViewById<View>(R.id.compare_iv).setOnTouchListener(mOnTouchListener)
        findViewById<View>(R.id.close_iv).setOnClickListener(this)
        findViewById<View>(R.id.save_iv).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.gpuimage -> openGallery()
            R.id.filter_name_tv -> GPUImageFilterTools.showDialog(
                this,
                mOnGpuImageFilterChosenListener
            )
            R.id.close_iv -> finish()
            R.id.save_iv -> saveImage()
        }
    }

    private fun openGallery() {
//        PictureSelector.create(this).openGallery(PictureMimeType.ofImage())
//            .imageEngine(com.tuyue.sdk.PictureSelectorImageLoader())
//            .forResult(object : OnResultCallbackListener<LocalMedia> {
//                override fun onResult(result: MutableList<LocalMedia>?) {
//                    result?.let {
//                        val file = BitmapUtils.getSampledBitmap(it[0].realPath, imageWidth, imageHeight)
//                        gpuimage.setImage(file)
//                        gpuimage.filter = mCurrentImageFilter
//                    }
//                }
//
//                override fun onCancel() {
//
//                }
//            })
    }

    private fun saveImage() {
        val fileName = System.currentTimeMillis().toString() + ".jpg"
        gpuimage.saveToPictures("GPUImage", fileName, mOnPictureSavedListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            gpuimage.setImage(data.data)
            gpuimage.filter = mCurrentImageFilter
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @SuppressLint("ClickableViewAccessibility")
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
        val filePath = FileUtils.getRealFilePath(this@GalleryActivity, uri)
        Toast.makeText(this@GalleryActivity, "Saved: $filePath", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_PICK_IMAGE = 0x001
    }
}