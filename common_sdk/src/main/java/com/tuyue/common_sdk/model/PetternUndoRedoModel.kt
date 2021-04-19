package com.tuyue.common_sdk.model

import android.graphics.Bitmap
import com.tuyue.common_sdk.image_edit.PetternType
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup


/**
 * Create by guojian on 2021/4/8
 * Describe：
 **/
class PetternUndoRedoModel (
    var type: PetternType,
    var filter: GPUImageFilterGroup = GPUImageFilterGroup(),
    var bitmap: Bitmap? = null
        )
