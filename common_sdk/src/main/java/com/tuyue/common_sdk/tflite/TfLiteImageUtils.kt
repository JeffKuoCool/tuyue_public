/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tuyue.common_sdk.tflite

import android.content.Context
import android.graphics.*
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 * Collection of image reading and manipulation utilities in the form of static functions.
 * TODO: this class should be moved to the common code in the future
 */
abstract class TfLiteImageUtils {
  companion object {

    /**
     * Helper function used to convert an EXIF orientation enum into a transformation matrix
     * that can be applied to a bitmap.
     *
     * @param orientation - One of the constants from [ExifInterface]
     */
    private fun decodeExifOrientation(orientation: Int): Matrix {
      val matrix = Matrix()

      // Apply transformation corresponding to declared EXIF orientation
      when (orientation) {
        ExifInterface.ORIENTATION_NORMAL, ExifInterface.ORIENTATION_UNDEFINED -> Unit
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90F)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180F)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270F)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1F, 1F)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1F, -1F)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
          matrix.postScale(-1F, 1F)
          matrix.postRotate(270F)
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
          matrix.postScale(-1F, 1F)
          matrix.postRotate(90F)
        }

        // Error out if the EXIF orientation is invalid
        else -> throw IllegalArgumentException("Invalid orientation: $orientation")
      }

      // Return the resulting matrix
      return matrix
    }

    /**
     * 将原图按宽高比缩放到一个方形内，返回方形
     */
    fun scaleBitmap(bitmap: Bitmap, bounds: Int): Bitmap{
      var newWidth = 0f
      var newHeight = 0f
      if(bitmap.width >= bitmap.height){
        newWidth = bounds.toFloat()
        newHeight = newWidth.times(bitmap.height).div(bitmap.width)
      }else{
        newHeight = bounds.toFloat()
        newWidth = newHeight.times(bitmap.width).div(bitmap.height)
      }
      val width: Int = bitmap.width
      val height = bitmap.height
      val matrix = Matrix()
      matrix.postScale(newWidth.div(bitmap.width), newHeight.div(bitmap.height))
      val newBitmap: Bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false)

      val result = Bitmap.createBitmap(bounds, bounds, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(result)
      canvas.drawBitmap(newBitmap, 0f, 0f, null)
      canvas.save()
      return result
    }

    /**
     * 裁剪
     * @param bitmap 注意：原图是方形，按原图尺寸从左上角裁剪。
     * @return 裁剪后的图像
     */
    fun cropBitmap(bitmap: Bitmap, cropWidth: Int, cropHeight: Int): Bitmap {
      val cropWidth = if (cropWidth > bitmap.width) bitmap.width else cropWidth
      val cropHeight = if(cropHeight > bitmap.height) bitmap.height else cropHeight
      return Bitmap.createBitmap(bitmap, 0, 0, cropWidth, cropHeight, null, false)
    }

    /**
     * sets the Exif orientation of an image.
     * this method is used to fix the exit of pictures taken by the camera
     *
     * @param filePath - The image file to change
     * @param value - the orientation of the file
     */
    fun setExifOrientation(
      filePath: String,
      value: String
    ) {
      val exif = ExifInterface(filePath)
      exif.setAttribute(
        ExifInterface.TAG_ORIENTATION, value
      )
      exif.saveAttributes()
    }

    /** Transforms rotation and mirroring information into one of the [ExifInterface] constants */
    fun computeExifOrientation(rotationDegrees: Int, mirrored: Boolean) = when {
      rotationDegrees == 0 && !mirrored -> ExifInterface.ORIENTATION_NORMAL
      rotationDegrees == 0 && mirrored -> ExifInterface.ORIENTATION_FLIP_HORIZONTAL
      rotationDegrees == 180 && !mirrored -> ExifInterface.ORIENTATION_ROTATE_180
      rotationDegrees == 180 && mirrored -> ExifInterface.ORIENTATION_FLIP_VERTICAL
      rotationDegrees == 270 && mirrored -> ExifInterface.ORIENTATION_TRANSVERSE
      rotationDegrees == 90 && !mirrored -> ExifInterface.ORIENTATION_ROTATE_90
      rotationDegrees == 90 && mirrored -> ExifInterface.ORIENTATION_TRANSPOSE
      rotationDegrees == 270 && mirrored -> ExifInterface.ORIENTATION_ROTATE_270
      rotationDegrees == 270 && !mirrored -> ExifInterface.ORIENTATION_TRANSVERSE
      else -> ExifInterface.ORIENTATION_UNDEFINED
    }

    /**
     * Decode a bitmap from a file and apply the transformations described in its EXIF data
     *
     * @param file - The image file to be read using [BitmapFactory.decodeFile]
     */
    fun decodeBitmap(file: File): Bitmap {
      // First, decode EXIF data and retrieve transformation matrix
      val exif = ExifInterface(file.absolutePath)
      val transformation =
        decodeExifOrientation(
          exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90
          )
        )

      // Read bitmap using factory methods, and transform it using EXIF data
      val bitmap = BitmapFactory.decodeFile(file.absolutePath)
      return Bitmap.createBitmap(
        BitmapFactory.decodeFile(file.absolutePath),
        0, 0, bitmap.width, bitmap.height, transformation, true
      )
    }

    fun scaleBitmapAndKeepRatio(
      targetBmp: Bitmap,
      reqWidthInPixels: Int,
      reqHeightInPixels: Int,
    ): Bitmap {
      if (targetBmp.height == reqHeightInPixels && targetBmp.width == reqWidthInPixels) {
        return targetBmp
      }
      val matrix = Matrix()
      matrix.setRectToRect(
        RectF(
          0f, 0f,
          targetBmp.width.toFloat(),
          targetBmp.width.toFloat()
        ),
        RectF(
          0f, 0f,
          reqWidthInPixels.toFloat(),
          reqHeightInPixels.toFloat()
        ),
        Matrix.ScaleToFit.CENTER
      )
      return Bitmap.createBitmap(
        targetBmp, 0, 0,
        targetBmp.width,
        targetBmp.width, matrix, true
      )
    }

    fun bitmapToByteBuffer(
      bitmapIn: Bitmap,
      width: Int,
      height: Int,
      mean: Float = 0.0f,
      std: Float = 255.0f
    ): ByteBuffer {
      val bitmap = scaleBitmapAndKeepRatio(bitmapIn, width, height)
      val inputImage = ByteBuffer.allocateDirect(1 * width * height * 3 * 4)
      inputImage.order(ByteOrder.nativeOrder())
      inputImage.rewind()

      val intValues = IntArray(width * height)
      bitmap.getPixels(intValues, 0, width, 0, 0, width, height)
      var pixel = 0
      for (y in 0 until height) {
        for (x in 0 until width) {
          val value = intValues[pixel++]

          // Normalize channel values to [-1.0, 1.0]. This requirement varies by
          // model. For example, some models might require values to be normalized
          // to the range [0.0, 1.0] instead.
          inputImage.putFloat(((value shr 16 and 0xFF) - mean) / std)
          inputImage.putFloat(((value shr 8 and 0xFF) - mean) / std)
          inputImage.putFloat(((value and 0xFF) - mean) / std)
        }
      }

      inputImage.rewind()
      return inputImage
    }

    fun createEmptyBitmap(imageWidth: Int, imageHeigth: Int, color: Int = 0): Bitmap {
      val ret = Bitmap.createBitmap(imageWidth, imageHeigth, Bitmap.Config.RGB_565)
      if (color != 0) {
        ret.eraseColor(color)
      }
      return ret
    }

    fun loadBitmapFromResources(context: Context, path: String): Bitmap {
      val inputStream = context.assets.open(path)
      return BitmapFactory.decodeStream(inputStream)
    }

    fun convertArrayToBitmap(
      imageArray: Array<Array<Array<FloatArray>>>,
      imageWidth: Int,
      imageHeight: Int
    ): Bitmap {
      val conf = Bitmap.Config.ARGB_8888 // see other conf types
      val styledImage = Bitmap.createBitmap(imageWidth, imageHeight, conf)

      for (x in imageArray[0].indices) {
        for (y in imageArray[0][0].indices) {
          val color = Color.rgb(
            ((imageArray[0][x][y][0] * 255).toInt()),
            ((imageArray[0][x][y][1] * 255).toInt()),
            (imageArray[0][x][y][2] * 255).toInt()
          )

          // this y, x is in the correct order!!!
          styledImage.setPixel(y, x, color)
        }
      }
      return styledImage
    }
  }

}
