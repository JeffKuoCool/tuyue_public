package com.tuyue.common_sdk.image_edit
import android.app.Activity
import android.content.Context
import android.content.Intent
import com.tuyue.common_sdk.activity.ImagePetternActivity

/**
 * create by guojian
 * Date: 2020/12/7
 */
class ImageEditor private constructor(private val mActivity: Activity) {

    private val mConfig: ImageEditorConfig = ImageEditorConfig()
    private var mResultCode = 0

    /**
     * 设置类型
     */
    fun ofType(type: EditorType): ImageEditor {
        mConfig.mType = type
        return this
    }

    /**
     * 沉浸式状态栏
     */
    fun isImmersionBar(immersionBar: Boolean): ImageEditor{
        mConfig.mIsImmersionBar = immersionBar
        return this
    }

    fun start(uri: String, exportDir: String? = null){
        mConfig.uri = uri
        exportDir?.let {
            mConfig.mExportDir = it
        }
        intoImageEditor()
    }

    fun resultCode(code: Int): ImageEditor{
        mResultCode = code
        return this
    }

    private fun intoImageEditor(){
        val intent = Intent(mActivity, ImagePetternActivity::class.java)
        mConfig.uri?.let {
            intent.putExtra("uri", it)
        }
        mConfig.mExportDir?.let {
            intent.putExtra("exportDir", it)
        }
        intent.putExtra("extra", mConfig)
        mActivity.startActivityForResult(intent, mResultCode)
    }

    companion object {
        fun get(activity: Activity): ImageEditor {
            return ImageEditor(activity)
        }
    }

    enum class EditorType {
        /**
         * 编辑
         */
        EDIT,

        /**
         * 滤镜
         */
        FILTER
    }

}