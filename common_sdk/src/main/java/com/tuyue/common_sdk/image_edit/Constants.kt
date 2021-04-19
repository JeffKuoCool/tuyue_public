package com.tuyue.common_sdk.image_edit

import com.tuyue.common_sdk.tools.BaseControlCenter


/**
 * Create by guojian on 2021/3/1
 * Describe：
 **/
object Constants {

    //边框资源路径
    val FRAME_FOLDER = BaseControlCenter.getContext().filesDir.absolutePath + "/frame"

    //纹理资源路径
    val TEXTURE_FOLDER = BaseControlCenter.getContext().filesDir.absolutePath + "/texture"

    //风格迁移资源路径
    val STYLE_TRANSFER_FOLDER = BaseControlCenter.getContext().filesDir.absolutePath + "/thumbnails"
}