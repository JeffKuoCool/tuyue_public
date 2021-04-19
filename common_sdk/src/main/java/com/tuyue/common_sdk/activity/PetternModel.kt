package com.tuyue.common_sdk.activity

import com.tuyue.common_sdk.image_edit.PetternType

/**
 * create by guojian
 * Date: 2020/12/12
 */
data class PetternModel (
    val icon: Int? = null,
    val title: String,
    val petternType: PetternType
)