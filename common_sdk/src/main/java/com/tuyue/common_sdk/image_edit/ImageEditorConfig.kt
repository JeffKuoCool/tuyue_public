package com.tuyue.common_sdk.image_edit

import java.io.Serializable


/**
 * create by guojian
 * Date: 2020/12/7
 */
internal class ImageEditorConfig: Serializable {

    /**
     * 本地uri
     */
    var uri: String? = null

    /**
     * 处理图片的类型
     */
    var mType : ImageEditor.EditorType? = ImageEditor.EditorType.EDIT

    /**
     * 支持沉浸式状态栏
     */
    var mIsImmersionBar = false

    /**
     * 导出路径
     */
    var mExportDir: String? = null
}