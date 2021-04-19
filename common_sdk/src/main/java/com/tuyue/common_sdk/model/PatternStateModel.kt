package com.tuyue.common_sdk.model

import com.tuyue.common_sdk.image_edit.PetternType
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter


/**
 * Create by guojian on 2021/4/6
 * Describe：
 **/
class PatternStateModel {
    private val filterLastState: State = State()
    private val adjustLastState: State = State()

    fun setLastPosition(petternType: PetternType, position:Int){
        getState(petternType)?.mLastPosition = position
    }

    fun setLastFilter(petternType: PetternType, filter: GPUImageFilter){
        getState(petternType)?.mLastFilter = filter
    }

    fun setLastProgress(petternType: PetternType, progress: Int){
        getState(petternType)?.mLastProgress = progress
    }

    fun getLastPosition(petternType: PetternType): Int{
        return getState(petternType)?.mLastPosition?: 0
    }

    fun getLastFilter(petternType: PetternType): GPUImageFilter?{
        return getState(petternType)?.mLastFilter
    }

    fun getLastProgress(petternType: PetternType): Int{
        return getState(petternType)?.mLastProgress?:50
    }

    private fun getState(petternType: PetternType): State?{
        when(petternType){
            PetternType.FILTER -> {
                return filterLastState
            }
            PetternType.ADJUST -> {
                return adjustLastState
            }
        }
        return null
    }
}

class State(
     var mLastPosition: Int = 0,
             var mLastFilter: GPUImageFilter? = null,
             var mLastProgress: Int = 50
)