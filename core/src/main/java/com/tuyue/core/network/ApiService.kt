package com.tuyue.core.network

import com.tuyue.core.resbody.FrameResBody
import com.tuyue.core.resbody.TextureResBody
import retrofit2.http.GET


/**
 * interface ApiService
 **/
interface ApiService {

    @GET("framelist")
    suspend fun getFrameList(): BaseResp<List<FrameResBody>>

    @GET("texturelist")
    suspend fun getTextureList(): BaseResp<List<TextureResBody>>
}