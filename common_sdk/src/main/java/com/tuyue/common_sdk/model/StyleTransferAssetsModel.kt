package com.tuyue.common_sdk.model

import com.tuyue.common_sdk.image_edit.PetternType
import com.tuyue.common_sdk.widget.SecondNode
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter


/**
 * Create by guojian on 2021/3/2
 * Describe：
 **/
class StyleTransferAssetsModel (
    type: PetternType, var styleTransferId: Int = 0, var path: String = "", var styleTransferTitle: String = "", var textureFilter: GPUImageFilter? = null, var iconUrl: String = ""
        )
    : SecondNode(styleTransferTitle, textureFilter, 0, type)