package com.tuyue.common_sdk.camera

abstract class CameraLoader {
    @JvmField
    protected var mOnPreviewFrameListener: OnPreviewFrameListener? = null
    abstract fun onResume(width: Int, height: Int)
    abstract fun onPause()
    abstract fun switchCamera()
    abstract val cameraOrientation: Int
    abstract fun hasMultipleCamera(): Boolean
    abstract val isFrontCamera: Boolean
    fun setOnPreviewFrameListener(onPreviewFrameListener: OnPreviewFrameListener?) {
        mOnPreviewFrameListener = onPreviewFrameListener
    }

    interface OnPreviewFrameListener {
        fun onPreviewFrame(data: ByteArray?, width: Int, height: Int)
    }
}