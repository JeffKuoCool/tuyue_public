package com.tuyue.common_sdk.helper

import android.content.Context
import android.util.Log
import com.tuyue.common_sdk.image_edit.Constants
import com.tuyue.common_sdk.image_edit.PetternType
import com.tuyue.common_sdk.model.FrameAssetsModel
import com.tuyue.common_sdk.model.StyleTransferAssetsModel
import com.tuyue.common_sdk.model.TextureAssetsModel
import com.tuyue.core.network.RetrofitFactory
import com.tuyue.core.util.CheckUtils
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class PetternAssetsHelper(val context: Context) {

    private val TAG = javaClass.simpleName

    fun parseStyleTransferAssets(): MutableList<StyleTransferAssetsModel> {
        val assets = mutableListOf<StyleTransferAssetsModel>()
        val folder = File(Constants.STYLE_TRANSFER_FOLDER)
        //所有一级资源
        val files = folder.listFiles()
        if (CheckUtils.isEmpty(files)) {
            return assets
        }
        files?.let { arrayFiles ->
            arrayFiles.forEach {
                val model = StyleTransferAssetsModel(PetternType.STYLE_TRANSFER)
//                if (it.path.endsWith("png") || it.path.endsWith("jpg")) {
                    model.path = it.path
//                }
                assets.add(model)
            }
        }
        return assets
    }

    /**
     * 读取纹理assets目录
     */
    fun parseTextureAssets(): MutableList<TextureAssetsModel> {
        val assets = mutableListOf<TextureAssetsModel>()
        val folder = File(Constants.TEXTURE_FOLDER)
        //所有一级资源
        val files = folder.listFiles()
        if (CheckUtils.isEmpty(files)) {
            return assets
        }
        for (i in 0..files!!.lastIndex) {
            if (files[i].isDirectory) {
                val assetsItems = files[i].listFiles()
                assetsItems?.let {
                    val model = TextureAssetsModel(PetternType.TEXTURE)
                    for (j in 0..it.lastIndex) {
                        if (it[j].path.endsWith("texture_assets.png")) {
                            model.path = it[j].path
                        }
                    }
                    assets.add(model)
                }
            } else {
                //zip压缩包
            }
        }
        return assets
    }

    /**
     * 通过边框id查询本地资源信息
     */
    fun parseFrameModel(frameId: String): FrameAssetsModel? {
        val folder = File(Constants.FRAME_FOLDER)
        //所有一级资源
        val files = folder.listFiles()
        if (CheckUtils.isEmpty(files)) {
            return null
        }
        for (i in 0..files!!.lastIndex) {
            if (files[i].isDirectory && files[i].path.contains(frameId)) {
                val assetsItems = files[i].listFiles()
                return getFrameAssetsInstance(assetsItems, "${files[i].path}.zip")

            } else {
                //zip压缩包
            }
        }
        return null
    }

    fun getTextureAssetsInstance(files: Array<File>, zipPath: String): TextureAssetsModel{
        val secondNode = TextureAssetsModel(PetternType.TEXTURE)
        files.let {
            for (j in 0..it.lastIndex) {
                if (it[j].path.endsWith("texture_assets.png")) {
                    secondNode.path = it[j].path
                }
            }
        }
        return secondNode
    }

    /**
     * 根据文件夹内容获取边框实例
     */
    fun getFrameAssetsInstance(files: Array<File>, zipPath: String, model: FrameAssetsModel? = null): FrameAssetsModel{
        val secondNode = model ?: FrameAssetsModel(PetternType.FRAME)
        files.let {
            for (j in 0..it.lastIndex) {
                if (it[j].path.endsWith("icon_frame.png")) {
                    secondNode.frameIcon = it[j].path
                }
                if (it[j].path.endsWith("up.png")) {
                    secondNode.up = it[j].path
                }
                if (it[j].path.endsWith("down.png")) {
                    secondNode.down = it[j].path
                }
                if (it[j].path.endsWith("left.png") && !it[j].path.contains("down")) {
                    secondNode.left = it[j].path
                }
                if (it[j].path.endsWith("right.png") && !it[j].path.contains("up")) {
                    secondNode.right = it[j].path
                }
                if (it[j].path.endsWith("up_left.png")) {
                    secondNode.up_left = it[j].path
                }
                if (it[j].path.endsWith("up_right.png")) {
                    secondNode.up_right = it[j].path
                }
                if (it[j].path.endsWith("down_left.png")) {
                    secondNode.down_left = it[j].path
                }
                if (it[j].path.endsWith("down_right.png")) {
                    secondNode.down_right = it[j].path
                }
            }
        }
        secondNode.frameZip = zipPath
        return secondNode
    }

    /**
     * 读取边框assets目录
     */
    fun parseFrameAssets(): MutableList<FrameAssetsModel> {
        val targetList: MutableList<FrameAssetsModel> = mutableListOf()
        val folder = File(Constants.FRAME_FOLDER)
        //所有一级资源
        val files = folder.listFiles()
        if (CheckUtils.isEmpty(files)) {
            return targetList
        }
        for (i in 0..files!!.lastIndex) {
            if (files[i].isDirectory) {
                val assetsItems = files[i].listFiles()
                val secondNode  = getFrameAssetsInstance(assetsItems, "${files[i].path}.zip")
                targetList.add(secondNode)

            } else {
                //zip压缩包
            }
        }
        return targetList
    }

    /**
     * @param url 下载连接
     * @param listener 下载监听
     */
    fun download(
        savePath: String,
        saveName: String,
        url: String,
        listener: OnDownloadListener? = null
    ) {
        val downloadPath = File(savePath)
        if (!downloadPath.mkdirs()) {
            downloadPath.createNewFile()
        }
        val file = File(downloadPath.absoluteFile, saveName)
        if (file.exists()) {
            Log.e("download", "文件已存在")
            return
        }
        val request: Request = Request.Builder().url(url).build()
        RetrofitFactory.instance.getOkhttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                listener?.onDownloadFailed()
            }

            override fun onResponse(call: Call, response: Response) {
                var inputStream: InputStream? = null
                val buf = ByteArray(2048)
                var len = 0
                var fos: FileOutputStream? = null
                try {
                    inputStream = response.body?.byteStream()
                    val total: Long = response.body?.contentLength()!!
                    Log.w(TAG, "最终路径：$file")
                    fos = FileOutputStream(file)
                    var sum: Long = 0
                    while (inputStream?.read(buf).also { len = it!! } != -1) {
                        fos.write(buf, 0, len)
                        sum += len.toLong()
                        val progress = (sum * 1.0f / total * 100).toInt()
                        listener?.onDownloading(progress)
                    }
                    fos.flush()
                    listener?.onDownloadSuccess(file)
                } catch (e: Exception) {
                    listener?.onDownloadFailed()
                } finally {
                    try {
                        inputStream?.close()
                        fos?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }


    interface OnDownloadListener {

        /**
         * 下载成功
         */
        fun onDownloadSuccess(file: File)

        /**
         * @param progress
         * 下载进度
         */
        fun onDownloading(progress: Int)

        /**
         * 下载失败
         */
        fun onDownloadFailed()
    }

}