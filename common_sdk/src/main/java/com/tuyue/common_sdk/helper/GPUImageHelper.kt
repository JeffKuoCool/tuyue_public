package com.tuyue.common_sdk.helper

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseNodeAdapter
import com.tuyue.common_sdk.image_edit.Constants
import com.tuyue.common_sdk.model.FrameAssetsModel
import com.tuyue.common_sdk.model.TextureAssetsModel
import com.tuyue.common_sdk.tools.GPUImageFilterTools
import com.tuyue.common_sdk.tools.Tools
import com.tuyue.common_sdk.tools.ZipFileUtil
import com.tuyue.common_sdk.widget.DownloadItem
import com.tuyue.common_sdk.widget.GPUImageLayout
import com.tuyue.common_sdk.widget.ImageFrameView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageAlphaBlendFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


/**
 * Create by guojian on 2021/3/30
 * Describe：
 **/
object GPUImageHelper {

    /**
     * 下载边框，解压更新
     */
    fun updateFrame(adapter: BaseNodeAdapter?, position : Int,context: Context, model: FrameAssetsModel, imageFrameView: ImageFrameView?, loadingView: DownloadItem){
        val assetsID = model.frameId.toString()
        val assetsZip = model.frameZipUrl
        val assetsMd5 = model.frameZipMd5
        val fileFolder = Constants.FRAME_FOLDER
        loadingView.loadingState(true)
        PetternAssetsHelper(context).download(
            fileFolder,
            "${assetsID}.zip",
            assetsZip,
            object : PetternAssetsHelper.OnDownloadListener {
                override fun onDownloadSuccess(file: File) {
                    //下载之后进行MD5校验，无效文件删除
                    val fileMD5 = Tools.getFileMD5(file)
                    if (!TextUtils.equals(fileMD5, assetsMd5)) {
                        Tools.deleteFile(file.path)
                    }else{
                        //解压
                        val output = file.path.split(".zip")[0]
                        val path = ZipFileUtil.upZipFile(file, output)
                        Log.e("path", path.toString())
                        val folder = File(output)
                        val newModel: FrameAssetsModel = PetternAssetsHelper(context).getFrameAssetsInstance(folder.listFiles(), file.path, model)
                        imageFrameView?.setFrameAssets(newModel)
                        adapter?.notifyItemChanged(position, newModel)
                    }
                    loadingView.loadingState(false)
                    loadingView.downloadFinish()
                }

                override fun onDownloading(progress: Int) {

                }

                override fun onDownloadFailed() {
                    Log.e("onDownloadFailed", "")
                    loadingView.loadingState(false)
                }
            })
    }

    fun updateTexture(adapter: BaseNodeAdapter?, position : Int, context: Context, textureModel: TextureAssetsModel, loadingView: DownloadItem, observer: Observer){
        val assetsID = textureModel.textureId.toString()
        val assetsZip = textureModel.textureZip
        val assetsMd5 = textureModel.textureZipMd5
        val fileFolder = Constants.TEXTURE_FOLDER
        loadingView.loadingState(true)
        PetternAssetsHelper(context).download(
            fileFolder,
            "${assetsID}.zip",
            assetsZip,
            object : PetternAssetsHelper.OnDownloadListener {
                override fun onDownloadSuccess(file: File) {
                    //下载之后进行MD5校验，无效文件删除
                    val fileMD5 = Tools.getFileMD5(file)
                    if (!TextUtils.equals(fileMD5, assetsMd5)) {
                        Tools.deleteFile(file.path)
                    }else{
                        //解压
                        val output = file.path.split(".zip")[0]
                        val path = ZipFileUtil.upZipFile(file, output)
                        Log.e("path", path.toString())
                        val folder = File(output)
                        val model: TextureAssetsModel = PetternAssetsHelper(context).getTextureAssetsInstance(folder.listFiles(), file.path)
                        val filter = transferFilterByPath(context, model.path)
                        textureModel.path = model.path
                        textureModel.filter = filter
                        GlobalScope.launch {
                            withContext(Dispatchers.Main){
                                observer.just(textureModel)
                                adapter?.notifyItemChanged(position, textureModel)
                            }
                        }
                    }
                    loadingView.loadingState(false)
                    loadingView.downloadFinish()
                }

                override fun onDownloading(progress: Int) {

                }

                override fun onDownloadFailed() {
                    Log.e("onDownloadFailed", "")
                    loadingView.loadingState(false)
                }
            })
    }

    fun transferFilterByPath(context: Context, path: String): GPUImageFilter{
        val bitmap = Glide.with(context).asBitmap().load(path)
            .submit().get()
        val filter = GPUImageFilterTools.createBlendFilter(
            context,
            GPUImageAlphaBlendFilter::class.java,
            bitmap
        )
        return filter
    }

    interface Observer{
        fun just(data: Any)
    }
}