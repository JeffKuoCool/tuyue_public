package com.tuyue.core.util

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

object AssetsUtil {

    /**
     * 复制Assets目录文件到安装包
     */
    fun copyAssetsFiles(context: Context, folder: String){
        val files = context.assets.list(folder)
        if(CheckUtils.isEmpty(files)) return
        files!!.forEach {
            copyAssetsFile(context, folder, it)
        }
    }

    /**
     * 复制文件
     */
    fun copyAssetsFile(context: Context, folder: String, assertName: String) {
        val filePath: String = context.filesDir.absolutePath
        val assetManager: AssetManager = context.assets
        try {
            val file = File(filePath)
            if (!file.exists()) {
                file.mkdirs()
            }
            val folderFile = File("${filePath}/${folder}")
            if (!folderFile.exists()) {
                folderFile.mkdirs()
            }
            //保存到本地的文件夹下的文件
            val inputStream: InputStream = assetManager.open("$folder/$assertName")
            val fileOutputStream = FileOutputStream("$filePath/$folder/$assertName")
            val buffer = ByteArray(1024)
            var count = 0
            while (inputStream.read(buffer).also { count = it } > 0) {
                fileOutputStream.write(buffer, 0, count)
            }
            fileOutputStream.flush()
            fileOutputStream.close()
            inputStream.close()
            Log.e("copyAssetsFiles", filePath)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("copyAssetsFiles", e.message.toString())
        }
    }



}