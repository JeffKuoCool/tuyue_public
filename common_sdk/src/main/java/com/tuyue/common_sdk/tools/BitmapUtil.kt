package com.tuyue.common_sdk.tools

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.tuyue.common_sdk.widget.GPUImageLayout
import jp.co.cyberagent.android.gpuimage.GPUImageView
import java.io.*


object BitmapUtil {

    /**
     * 保存图片
     */
    private fun saveImage(context: Context, fileName: String, image: Bitmap, expordDir: String? = null): String? {
        val path =
        expordDir?.let {
            File(it)
        }?:let {
            if(Build.VERSION.SDK_INT < 29){
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            }else {
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            }
        }
        val file = File(path, fileName)
        return try {
            file.parentFile?.mkdirs()
            image.compress(Bitmap.CompressFormat.JPEG, 80, FileOutputStream(file))
            MediaScannerConnection.scanFile(context, arrayOf(
                file.toString()
            ), null
            ) { savePath, uri ->
                Log.e("saveImage", savePath)

            }
            file.path
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 复合gpuimage原图和相框
     */
    fun convertViewToBitmap(view: GPUImageLayout): Bitmap? {

        val bitmap = Bitmap.createBitmap(view.width, view.height,
                Bitmap.Config.ARGB_8888)

        //利用bitmap生成画布
        val canvas1 = Canvas(bitmap)
        //把view中的内容绘制在画布上
        view.draw(canvas1)

        val newmap = Bitmap
                .createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newmap)
        canvas.drawBitmap(view.capture(), view.mFrameOffset.toFloat(), view.mFrameOffset.toFloat(), null)
        canvas.drawBitmap(bitmap, 0f,
                0f, null)
        canvas.save()
        canvas.restore()

        return newmap
    }

    /*
    * 保存文件，文件名为当前日期
    */
    fun saveBitmap(context: Context, bitmap: Bitmap, bitName: String, expordDir: String? = null): String? {
        return if(Build.VERSION.SDK_INT < 29 || expordDir!=null){
            saveImage(context, bitName, bitmap, expordDir)
        }else{
            saveSignImage(context, bitName, bitmap)
        }
    }


    //将文件保存到公共的媒体文件夹
    //这里的filepath不是绝对路径，而是某个媒体文件夹下的子路径，和沙盒子文件夹类似
    //这里的filename单纯的指文件名，不包含路径
    private fun saveSignImage( context: Context,
            fileName: String?, bitmap: Bitmap) : String?{
        try {
            //设置保存参数到ContentValues中
            val contentValues = ContentValues()
            //设置文件名
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            //兼容Android Q和以下版本
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //android Q中不再使用DATA字段，而用RELATIVE_PATH代替
                //RELATIVE_PATH是相对路径不是绝对路径
                //DCIM是系统文件夹，关于系统文件夹可以到系统自带的文件管理器中查看，不可以写没存在的名字
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/")
                //contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Music/signImage");
            } else {
                contentValues.put(MediaStore.Images.Media.DATA, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path)
            }
            //设置文件类型
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/JPEG")
            //执行insert操作，向系统文件夹中添加文件
            //EXTERNAL_CONTENT_URI代表外部存储器，该值不变
            val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                //若生成了uri，则表示该文件添加成功
                //使用流将内容写入该uri中即可
                val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    outputStream.flush()
                    outputStream.close()
                }
                return Tools.getFilePathFromContentUri(uri, context.contentResolver)
            }
            return null
        } catch (e: java.lang.Exception) {
            return null
        }
    }


}