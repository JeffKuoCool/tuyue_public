
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.util.Log
import android.util.Size
import android.view.Surface
import com.tuyue.common_sdk.camera.CameraLoader
import com.tuyue.core.util.ImageUtils
import java.util.*
import kotlin.jvm.Throws

class Camera2Loader(private val mActivity: Activity) : CameraLoader() {
    private val mCameraManager: CameraManager = mActivity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var mCharacteristics: CameraCharacteristics? = null
    private var mCameraDevice: CameraDevice? = null
    private var mCaptureSession: CameraCaptureSession? = null
    private var mImageReader: ImageReader? = null
    private var mCameraId: String? = null
    private var mCameraFacing = CameraCharacteristics.LENS_FACING_BACK
    private var mViewWidth = 0
    private var mViewHeight = 0
    private val mAspectRatio = 0.75f // 4:3
    override fun onResume(width: Int, height: Int) {
        mViewWidth = width
        mViewHeight = height
        setUpCamera()
    }

    override fun onPause() {
        releaseCamera()
    }

    override fun switchCamera() {
        mCameraFacing = mCameraFacing xor 1
        Log.d(TAG, "current camera facing is: $mCameraFacing")
        releaseCamera()
        setUpCamera()
    }

    override val cameraOrientation: Int
        get() {
            var degrees = mActivity.windowManager.defaultDisplay.rotation
            when (degrees) {
                Surface.ROTATION_0 -> degrees = 0
                Surface.ROTATION_90 -> degrees = 90
                Surface.ROTATION_180 -> degrees = 180
                Surface.ROTATION_270 -> degrees = 270
                else -> degrees = 0
            }
            var orientation = 0
            try {
                val cameraId = getCameraId(mCameraFacing)
                val characteristics = mCameraManager.getCameraCharacteristics(cameraId)
                orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)?: 0
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
            Log.d(
                TAG,
                "degrees: $degrees, orientation: $orientation, mCameraFacing: $mCameraFacing"
            )
            return if (mCameraFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                (orientation + degrees) % 360
            } else {
                (orientation - degrees) % 360
            }
        }

    override fun hasMultipleCamera(): Boolean {
        try {
            val size = mCameraManager.cameraIdList.size
            return size > 1
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return false
    }

    override val isFrontCamera: Boolean
        get() = mCameraFacing == CameraCharacteristics.LENS_FACING_FRONT

    @SuppressLint("MissingPermission")
    private fun setUpCamera() {
        try {
            mCameraId = getCameraId(mCameraFacing)
            mCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId!!)
            mCameraManager.openCamera(mCameraId!!, mCameraDeviceCallback, null)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Opening camera (ID: $mCameraId) failed.")
            e.printStackTrace()
        }
    }

    private fun releaseCamera() {
        if (mCaptureSession != null) {
            mCaptureSession!!.close()
            mCaptureSession = null
        }
        if (mCameraDevice != null) {
            mCameraDevice!!.close()
            mCameraDevice = null
        }
        if (mImageReader != null) {
            mImageReader!!.close()
            mImageReader = null
        }
    }

    @Throws(CameraAccessException::class)
    private fun getCameraId(facing: Int): String {
        for (cameraId: String in mCameraManager.cameraIdList) {
            if (mCameraManager.getCameraCharacteristics(cameraId)
                    .get(CameraCharacteristics.LENS_FACING) ==
                facing
            ) {
                return cameraId
            }
        }
        // default return
        return Integer.toString(facing)
    }

    private fun startCaptureSession() {
        val size = chooseOptimalSize()
        Log.d(TAG, "size: $size")
        mImageReader = ImageReader.newInstance(size.width, size.height, ImageFormat.YUV_420_888, 2)
        mImageReader!!.setOnImageAvailableListener(ImageReader.OnImageAvailableListener { reader ->
            if (reader != null) {
                val image = reader.acquireNextImage()
                if (image != null) {
                    if (mOnPreviewFrameListener != null) {
                        val data = ImageUtils.generateNV21Data(image)
                        mOnPreviewFrameListener!!.onPreviewFrame(data, image.width, image.height)
                    }
                    image.close()
                }
            }
        }, null)
        try {
            mCameraDevice!!.createCaptureSession(
                Arrays.asList(
                    mImageReader!!.surface
                ), mCaptureStateCallback, null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            Log.e(TAG, "Failed to start camera session")
        }
    }

    private fun chooseOptimalSize(): Size {
        Log.d(TAG, "viewWidth: $mViewWidth, viewHeight: $mViewHeight")
        if (mViewWidth == 0 || mViewHeight == 0) {
            return Size(0, 0)
        }
        val map = mCharacteristics!!.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val sizes = map!!.getOutputSizes(ImageFormat.YUV_420_888)
        val orientation = cameraOrientation
        val swapRotation = orientation == 90 || orientation == 270
        val width = if (swapRotation) mViewHeight else mViewWidth
        val height = if (swapRotation) mViewWidth else mViewHeight
        return getSuitableSize(sizes, width, height, mAspectRatio)
    }

    private fun getSuitableSize(
        sizes: Array<Size>,
        width: Int,
        height: Int,
        aspectRatio: Float
    ): Size {
        var minDelta = Int.MAX_VALUE
        var index = 0
        Log.d(TAG, "getSuitableSize. aspectRatio: $aspectRatio")
        for (i in sizes.indices) {
            val size = sizes[i]
            // 先判断比例是否相等
            if (size.width * aspectRatio == size.height.toFloat()) {
                val delta = Math.abs(width - size.width)
                if (delta == 0) {
                    return size
                }
                if (minDelta > delta) {
                    minDelta = delta
                    index = i
                }
            }
        }
        return sizes[index]
    }

    private val mCameraDeviceCallback: CameraDevice.StateCallback =
        object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                mCameraDevice = camera
                startCaptureSession()
            }

            override fun onDisconnected(camera: CameraDevice) {
                mCameraDevice!!.close()
                mCameraDevice = null
            }

            override fun onError(camera: CameraDevice, error: Int) {
                mCameraDevice!!.close()
                mCameraDevice = null
            }
        }
    private val mCaptureStateCallback: CameraCaptureSession.StateCallback =
        object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                if (mCameraDevice == null) {
                    return
                }
                mCaptureSession = session
                try {
                    val builder =
                        mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    builder.addTarget(mImageReader!!.surface)
                    builder.set(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                    )
                    builder.set(
                        CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                    )
                    session.setRepeatingRequest(builder.build(), null, null)
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                Log.e(TAG, "Failed to configure capture session.")
            }
        }

    companion object {
        private val TAG = "Camera2Loader"
    }

}