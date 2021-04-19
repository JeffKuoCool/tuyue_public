package com.tuyue.common_sdk.model

import com.tuyue.common_sdk.image_edit.PetternType
import com.tuyue.common_sdk.widget.SecondNode
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter


/**
 * Create by guojian on 2021/3/2
 * Describe：
 **/
class TextureAssetsModel (
    type: PetternType,
    var textureId: Int = 0,
    var path: String = "",
    var textureZip: String = "",
    var textureZipMd5: String = "",
    var textureTitle: String = "",
    var textureFilter: GPUImageFilter? = null,
    var iconUrl: String = ""
        )
    : SecondNode(textureTitle, textureFilter, 0, type)