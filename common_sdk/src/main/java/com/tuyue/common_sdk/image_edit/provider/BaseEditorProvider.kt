package com.tuyue.common_sdk.image_edit.provider

import android.content.Context
import android.graphics.Bitmap
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.xinlan.imageeditlibrary.editimage.utils.BitmapUtils


abstract class BaseEditorProvider: BaseNodeProvider() {
    var mImageBitmap: Bitmap? = null

    fun setImageUri(context: Context, uri: String?) {
        uri?.let {
            mImageBitmap = BitmapUtils.parseBitmapFromUri(context, it)
        }
    }

}