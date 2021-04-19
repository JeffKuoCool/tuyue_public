package com.tuyue.common_sdk.image_edit

import android.graphics.Bitmap
import android.util.Log
import com.tuyue.common_sdk.model.PetternUndoRedoModel
import com.tuyue.common_sdk.model.SelectRecordModel
import com.tuyue.core.util.CheckUtils
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup
import java.util.*
import kotlin.collections.HashMap

/**
 * create by guojian
 * Date: 2020/12/31
 * 多管道滤镜帮助类实现类
 * @see [mImageFilterGroup] 操作滤镜栈的管理(包括撤销后的数据)
 * @see [mUndoFilterGroup] 重置撤销滤镜栈的管理
 *
 * 当调用 [undoFilter] 撤销滤镜时，[mImageFilterGroup] 栈中执行出栈操作，[mUndoFilterGroup] 进栈。
 * 当调用 [redoFilter] 重置一次撤销滤镜时，[mUndoFilterGroup] 栈中执行出栈操作，[mImageFilterGroup] 进栈。
 */
class UndoRedoHelperImpl : IFilterGroupHelper {

    /**
     * 当前操作队列
     */
    private var mCurrentQueue: PetternUndoRedoModel = PetternUndoRedoModel(PetternType.NORMAL)

    /**
     * 临时记录一次滤镜状态，用于还原操作
     */
    private val mRecordFilterGroups: LinkedList<PetternUndoRedoModel> = LinkedList()

    /**
     * 保存撤销重置
     */
    private val mUndoRecordFilterGroups: LinkedList<PetternUndoRedoModel> = LinkedList()

    /**
     * 当前操作状态
     */
    private var mPetternType: PetternType = PetternType.NORMAL

    private var mFilterGroup = hashSetOf<GPUImageFilter>()
    private var mAdjustGroup = hashSetOf<GPUImageFilter>()
    private var mTextureGroup = hashSetOf<GPUImageFilter>()
    private var mProgressQueue = hashMapOf<String, Int>()

    private var mPositionQueue = hashMapOf<PetternType, SelectRecordModel>()
    private var mCurrentPositions = hashMapOf<PetternType, SelectRecordModel>()

    override fun addFilter(filter: GPUImageFilter, petternType: PetternType, position: Int?, hasProgress: Boolean) {
        //滤镜和纹理只保留单管道
        position?.let {
            val model = SelectRecordModel(it, hasProgress)
            mCurrentPositions.put(petternType, model)
        }
        val filterSet = when (petternType) {
            PetternType.FILTER -> {
                mFilterGroup
            }
            PetternType.ADJUST -> {
                mAdjustGroup
            }
            PetternType.TEXTURE -> {
                mTextureGroup
            }
            else -> {
                mFilterGroup
            }
        }
        if (petternType == PetternType.FILTER || petternType == PetternType.TEXTURE ||petternType == PetternType.ADJUST) {
            filterSet.clear()
        }
        filterSet.add(filter)
        mCurrentQueue.type = petternType
        mCurrentQueue.filter.addFilter(filter)
        mCurrentQueue.filter.updateMergedFilters()
    }

    fun getLastPositionByPetternType(type: PetternType): SelectRecordModel?{
        return mPositionQueue[type]
    }

    /**
     * 记录进度条状态
     */
    fun recordProgress(filter: GPUImageFilter, progress: Int){
        mProgressQueue[filter.javaClass.simpleName] = progress
    }

    fun getFilterProgress(filter: GPUImageFilter): Int?{
        return mProgressQueue[filter.javaClass.simpleName]
    }

    fun isCanAddFilter(filter: GPUImageFilter, petternType: PetternType): Boolean{
        val filterSet = when (petternType) {
            PetternType.FILTER -> {
                mFilterGroup
            }
            PetternType.ADJUST -> {
                mAdjustGroup
            }
            PetternType.TEXTURE -> {
                mTextureGroup
                return true
            }
            else -> {
                mFilterGroup
            }
        }
        return filterSet.size == 0 || !filterSet.last().javaClass.name.equals(filter.javaClass.name)
    }

    override fun addBitmap(bitmap: Bitmap, petternType: PetternType) {

    }

    override fun getFilterGroup(): GPUImageFilterGroup {
        val filterGroup = GPUImageFilterGroup()
        mFilterGroup.forEach {
            filterGroup.addFilter(it)
        }
        mAdjustGroup.forEach {
            filterGroup.addFilter(it)
        }
        mTextureGroup.forEach {
            filterGroup.addFilter(it)
        }
        if (CheckUtils.isEmpty(filterGroup)) {
            return filterGroup
        }
        return filterGroup
    }

    /**
     * 重置滤镜
     * @param petternType 类型
     */
    override fun clearFilterByType(petternType: PetternType) {
        when (petternType) {
            PetternType.FILTER -> {
                mFilterGroup.clear()
            }
            PetternType.ADJUST -> {
                mAdjustGroup.clear()
            }
            PetternType.TEXTURE -> {
                mTextureGroup.clear()
            }
            else -> {
                return
            }
        }
    }

    /**
     * 清除所有滤镜
     */
    fun clearAllFilter(){
        mFilterGroup.clear()
        mAdjustGroup.clear()
        mTextureGroup.clear()
        mPositionQueue.clear()
    }

    /**
     * 取消一次操作
     */
    fun removeLast(){
        if(mRecordFilterGroups.size > 0){
            mRecordFilterGroups.removeLast()
        }
    }

    /**
     * 撤销滤镜
     * @return 返回是否可以渲染滤镜的状态，false 不可渲染重制视图
     */
    override fun undoFilter(petternType: PetternType): Boolean {
        resetLastFilterState()
        return isResetFilter()
    }

    /**
     * 重置一次撤销
     */
    override fun redoFilter(petternType: PetternType) {
        redoResetLastFilterState()
    }

    /**
     * 是否可以重置
     */
    override fun isRedoResetFilter(petternType: PetternType): Boolean {
        return mUndoRecordFilterGroups.isNotEmpty()
    }

    override fun isResetFilter(petternType: PetternType): Boolean {
        return mRecordFilterGroups.size > 0
    }

    /**
     * 修改队列中最后一个元素参数
     */
    fun updateLastRecord(filter: GPUImageFilterGroup, bitmap: Bitmap){
        if(mRecordFilterGroups.isEmpty())return
        mRecordFilterGroups.last.filter = filter
        mRecordFilterGroups.last.bitmap = bitmap
        //替换原图后清除记录的位置信息
        mPositionQueue.clear()
    }

    /**
     * 记录撤销，将数据加入操作序列
     */
    override fun recordFilterState(petternType: PetternType, bitmap: Bitmap?) {
        mPetternType = petternType
        val filter = getFilterGroup()
        if(mRecordFilterGroups.size > 0 && mRecordFilterGroups.last.filter == filter){
            //没有做修改不需要记录操作
            return
        }
        val undoRedoModel = PetternUndoRedoModel(mPetternType)
        undoRedoModel.bitmap = bitmap
        undoRedoModel.filter = filter

        mRecordFilterGroups.add(undoRedoModel)
        mPositionQueue.putAll(mCurrentPositions)
        mCurrentPositions.clear()
        Log.e("mRecordFilterGroups", mRecordFilterGroups.toString())
    }

    /**
     * 还原一次操作
     */
    override fun resetLastFilterState(): PetternUndoRedoModel {
        mUndoRecordFilterGroups.add(mRecordFilterGroups.last)
        mRecordFilterGroups.removeLast()
        val undoRedoModel = PetternUndoRedoModel(PetternType.NORMAL)
        if (!isResetFilter()) {
            undoRedoModel.type = PetternType.NORMAL

        } else {
            undoRedoModel.type = mRecordFilterGroups.last.type
            undoRedoModel.filter = mRecordFilterGroups.last.filter
            undoRedoModel.bitmap = mRecordFilterGroups.last.bitmap
        }
        return undoRedoModel
    }

    /**
     * 重置一次还原
     */
    fun redoResetLastFilterState(): PetternUndoRedoModel {
        mRecordFilterGroups.add(mUndoRecordFilterGroups.last)
        mUndoRecordFilterGroups.removeLast()
        val undoRedoModel = PetternUndoRedoModel(PetternType.NORMAL)
        undoRedoModel.type = mRecordFilterGroups.last.type
        undoRedoModel.filter = mRecordFilterGroups.last.filter
        undoRedoModel.bitmap = mRecordFilterGroups.last.bitmap
        return undoRedoModel
    }

    fun clearRecordPositions(){
        mCurrentPositions.clear()
    }

    /**
     * 记录取消
     */
    fun getLastCancelFilterGroup(): PetternUndoRedoModel? {
        if (CheckUtils.isEmpty(mRecordFilterGroups)) {
            return null
        }
        return mRecordFilterGroups.last
    }

}