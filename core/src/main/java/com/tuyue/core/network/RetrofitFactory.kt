package com.tuyue.core.network

import android.util.Log
import com.google.gson.Gson

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class RetrofitFactory private constructor() {

    private val retrofit: Retrofit
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .addInterceptor(initLogInterceptor())
        .build()

    init {
        val gson = Gson().newBuilder()
            .setLenient()
            .serializeNulls()
            .create()

        retrofit = Retrofit.Builder()
            .baseUrl("https://app.aituyue.cn/api/photo-editor/")
            .client(getOkhttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    companion object {
        val instance: RetrofitFactory by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            RetrofitFactory()
        }

    }

    fun getOkhttpClient(): OkHttpClient {
        return httpClient
    }



    /**
     * 日志拦截器
     */
    private fun initLogInterceptor(): HttpLoggingInterceptor {

        val interceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Log.i("Retrofit", message)
            }
        })

        interceptor.level = HttpLoggingInterceptor.Level.BODY

        return interceptor
    }

    /**
     * 具体服务实例化
     */
    fun <T> getService(service: Class<T>): T {

        return retrofit.create(service)
    }
}



