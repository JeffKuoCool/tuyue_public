package com.tuyue.core.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.tuyue.core.network.ApiService
import com.tuyue.core.network.RetrofitFactory
import com.tuyue.core.reqbody.Fiction
import com.tuyue.core.resbody.FrameResBody
import com.tuyue.core.resbody.TextureResBody
import dataConvert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * Create by guojian on 2021/2/25
 * Describe：
 **/
class PetternViewModel: ViewModel() {

    var mFrameList = MutableLiveData<List<FrameResBody>>()
    var mTextureList = MutableLiveData<List<TextureResBody>>()


    fun getFrameList(success: MutableLiveData<List<FrameResBody>>? = null) {
        viewModelScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    RetrofitFactory.instance.getService(ApiService::class.java)
                        .getFrameList()
                }
                mFrameList.value = data.data
                success?.value = data.data

            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("请求失败", "${e.message}")

            }


        }
    }

    fun getTextureList(success: MutableLiveData<List<TextureResBody>>? = null) {
        viewModelScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    RetrofitFactory.instance.getService(ApiService::class.java)
                        .getTextureList()
                }
                mTextureList.value = data.data
                success?.value = data.data

            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("请求失败", "${e.message}")

            }


        }
    }
}