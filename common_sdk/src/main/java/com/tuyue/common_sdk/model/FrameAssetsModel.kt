package com.tuyue.common_sdk.model

import com.tuyue.common_sdk.image_edit.PetternType
import com.tuyue.common_sdk.widget.SecondNode


/**
 * Create by guojian on 2021/2/28
 * Describe：网络下载，本地解压边框资源
 **/
class FrameAssetsModel(type: PetternType,var frameIcon: String = "",
                       var frameTitle: String = "",
                       var frameZipUrl: String = "",
                       var frameZipMd5: String = "",
                       var frameZip: String = "",
                       var up: String = "", var down: String = "",
                       var left: String = "", var right: String = "",
                       var up_left: String = "", var up_right: String = "",
                       var down_left: String = "", var down_right: String = "",
                       var imageUrl: String = "", var frameId: Int = 0
                       )
    :SecondNode ("", null, 0, type)