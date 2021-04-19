package com.tuyue.core.network


/**
 * 返回数据基础类
 **/
data class BaseResp<T>(
    var code: Int = 0,
    var message: String = "",
    var data: T
)