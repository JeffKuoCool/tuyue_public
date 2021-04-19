package com.tuyue.common_sdk.image_edit

import android.graphics.Bitmap
import com.tuyue.common_sdk.model.PetternUndoRedoModel
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup

/**
 * create by guojian
 * Date: 2020/12/31
 * 多管道滤镜帮助类
 */
interface IFilterGroupHelper {

    /**
     * 顺序添加滤镜
     */
    fun addFilter(filter: GPUImageFilter, petternType: PetternType = PetternType.FILTER, position: Int? = null, hasProgress: Boolean = true)

    /**
     * 顺序添加位图
     */
    fun addBitmap(bitmap: Bitmap, petternType: PetternType = PetternType.NORMAL)

    /**
     * 获取管道
     */
    fun getFilterGroup(): GPUImageFilter

    /**
     * 清除滤镜
     */
    fun clearFilterByType(petternType: PetternType = PetternType.FILTER)

    /**
     * 重制撤销
     */
    fun redoFilter(petternType: PetternType = PetternType.FILTER)

    /**
     * 撤销
     */
    fun undoFilter(petternType: PetternType = PetternType.FILTER): Boolean

    /**
     * 是否可以重置重置一次操作
     */
    fun isRedoResetFilter(petternType: PetternType = PetternType.FILTER): Boolean

    /**
     * 是否可以重置一次操作
     */
    fun isResetFilter(petternType: PetternType = PetternType.FILTER): Boolean

    /**
     * 记录状态
     */
    fun recordFilterState(petternType: PetternType = PetternType.FILTER, bitmap: Bitmap? = null)

    /**
     * 还原上一次操作状态
     */
    fun resetLastFilterState(): PetternUndoRedoModel

}