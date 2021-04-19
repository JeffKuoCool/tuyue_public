package com.tuyue.sdk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import com.tencent.bugly.crashreport.CrashReport
import com.tuyue.common_sdk.image_edit.ImageEditor
import com.tuyue.core.util.PermissionsUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CrashReport.initCrashReport(applicationContext, "048951fdf0", false)

        btn_sdk.setOnClickListener {

            PermissionsUtil.verifyStoragePermissions(this)
            PictureSelector.create(this).openGallery(PictureMimeType.ofImage())
                .imageEngine(PictureSelectorImageLoader())
                .forResult(object : OnResultCallbackListener<LocalMedia> {
                    override fun onResult(result: MutableList<LocalMedia>?) {
                        result?.let { mutableList ->
                            ImageEditor.get(this@MainActivity)
                                .isImmersionBar(true)
                                .start(mutableList[0].path)
                        }
                    }

                    override fun onCancel() {

                    }
                })
        }

    }

}