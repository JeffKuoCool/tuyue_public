package com.tuyue.common_sdk.activity

import BaseActivity
import android.Manifest
import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.hjq.toast.ToastUtils
import com.tuyue.common_sdk.R
import com.tuyue.common_sdk.helper.PetternAssetsHelper
import com.tuyue.common_sdk.image_edit.Constants
import com.tuyue.common_sdk.image_edit.ImageEditorConfig
import com.tuyue.common_sdk.tools.*
import com.tuyue.common_sdk.widget.OnContralerStateLisener
import com.tuyue.core.resbody.FrameResBody
import com.tuyue.core.resbody.TextureResBody
import com.tuyue.core.util.AssetsUtil
import com.tuyue.core.viewmodel.PetternViewModel
import com.xinlan.imageeditlibrary.editimage.utils.BitmapUtils
import kotlinx.android.synthetic.main.activity_image_pettern.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


/**
 * create by guojian
 * Date: 2020/12/7
 * 编辑模版页面
 */
class ImagePetternActivity : BaseActivity() {
    /**
     * 图片本地路径
     */
    private var mUri: String? = null
    private var mConfig: ImageEditorConfig? = null
    private var mExportDir: String? = null
    private var mBitmap: Bitmap? = null
    private lateinit var netViewModel: PetternViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ToastUtils.init(this.application)
        BaseControlCenter.init(this.application, false)

        setContentView(R.layout.activity_image_pettern)
        copyAssets()
        initData()
//        initAssets()
        initView()
        setEvent()
    }

    private fun setEvent() {
        pettern_expand_controler.setControlerStateListener(object : OnContralerStateLisener {
            override fun finish(bitmap: Bitmap) {
                val fileName = System.currentTimeMillis().toString() + ".jpg"
                val path = BitmapUtil.saveBitmap(
                    this@ImagePetternActivity,
                    bitmap,
                    fileName,
                    mExportDir
                )
                path?.let {
//                    ToastUtils.show("图片已保存$it")
                    intent.putExtra("image", it)
                    setResult(Activity.RESULT_OK, intent)
                }

                finish()
            }

            override fun cancel() {
                setResult(Activity.RESULT_CANCELED)
            }
        })
    }

    private fun initAssets() {
        netViewModel = ViewModelProvider(this).get(PetternViewModel::class.java)
        netViewModel.getFrameList()
        netViewModel.mFrameList.observe(this, Observer { list ->
            //下载文件
            val fileFolder = Constants.FRAME_FOLDER
            downLoadAssets(list, fileFolder)
        })

        netViewModel.getTextureList()
        netViewModel.mTextureList.observe(this, Observer { list ->
            //下载文件
            val fileFolder = Constants.TEXTURE_FOLDER
            requestPermission(list, fileFolder)
        })
    }

    private fun requestPermission(list: List<Any>, fileFolder: String){
        val permission = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                !== PermissionChecker.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                !== PermissionChecker.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, permission, 101)
            } else {
                downLoadAssets(list, fileFolder)
            }
        } else {
            downLoadAssets(list, fileFolder)
        }
    }

    private fun copyAssets(){
        AssetsUtil.copyAssetsFiles(this, "thumbnails")
    }

    private fun downLoadAssets(list: List<Any>, fileFolder: String){
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                list.forEach {
                    var assetsID = ""
                    var assetsZip = ""
                    var assetsMd5 = ""
                    when (it) {
                        is FrameResBody -> {
                            assetsID = it.frameID.toString()
                            assetsZip = it.frameZip
                            assetsMd5 = it.frameZipMd5
                        }
                        is TextureResBody -> {
                            assetsID = it.textureID.toString()
                            assetsZip = it.textureZip
                            assetsMd5 = it.textureZipMd5
                        }
                        else -> {
                            return@forEach
                        }
                    }
                    PetternAssetsHelper(this@ImagePetternActivity).download(
                        fileFolder,
                        "${assetsID}.zip",
                        assetsZip,
                        object : PetternAssetsHelper.OnDownloadListener {
                            override fun onDownloadSuccess(file: File) {
                                //下载之后进行MD5校验，无效文件删除
                                val fileMD5 = Tools.getFileMD5(file)
                                if (!TextUtils.equals(fileMD5, assetsMd5)) {
                                    Tools.deleteFile(file.path)
                                }
                            }

                            override fun onDownloading(progress: Int) {

                            }

                            override fun onDownloadFailed() {
                                Log.e("onDownloadFailed", "")
                            }
                        })
                }
            }
            val fileTree = File(fileFolder).listFiles()
            fileTree?.let { arrayOfFiles ->
                withContext(Dispatchers.IO) {
                    arrayOfFiles.forEach {
                        if (it.isFile &&
                            it.path.endsWith(
                                ".zip"
                            )
                        ) {
                            val output = it.path.split(".zip")[0]
                            ZipFileUtil.upZipFile(it, output)
                        }
                    }
                }
                Log.e("fileTree", arrayOfFiles.toString())
            }

        }
    }

    private fun initData() {
        mUri = intent.getStringExtra("uri")
        mExportDir = intent.getStringExtra("exportDir")
        mUri?.let { uri ->
            mBitmap = BitmapUtils.parseBitmapFromUri(this, uri)
            mBitmap?.let {
                gpuimage.setImageResetMatrix(it)
            }
        }
        val uri = Tools.getImageContentUri(this, File(mUri))
        mConfig = intent.getSerializableExtra("extra") as ImageEditorConfig
    }

    private fun initView() {
        mConfig?.mIsImmersionBar?.let {
            if (it) {
                lifecycle.addObserver(BlackStateBarWindowAdjustPanLifecycle(this))
            }
        }
        pettern_expand_controler.bindGpuImageView(gpuimage, mUri)
        pettern_expand_controler.bindCropImageLayout(crop_image_layout)
        pettern_expand_controler.bindFrameView(image_frame_view)
        pettern_expand_controler.bindDoodleView(draw_view)
    }

    fun startLoading(){

    }

    fun stopLoading(){

    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            pettern_expand_controler.exit()
            true
        } else false
    }

}