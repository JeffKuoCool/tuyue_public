package com.tuyue.core.util

import android.text.TextUtils
import com.tuyue.core.util.CheckUtils
import java.util.HashMap
import java.util.regex.Pattern

/**
 * 检查校验工具类
 */
object CheckUtils {
    /**
     * 检查一个Map是否是空的
     *
     * @param map 对象
     * @return true：空对象和没有值
     */
    fun isEmpty(map: Map<*, *>?): Boolean {
        return map == null || map.size == 0
    }

    /**
     * 校验一个集合是否为空的
     *
     * @return true：空对象和没有值
     */
    fun isEmpty(collection: Collection<*>?): Boolean {
        return collection == null || collection.isEmpty()
    }

    /**
     * 校验一个数组是否为空
     */
    fun <T> isEmpty(vararg objects: T): Boolean {
        return objects == null || objects.size == 0
    }

    /**
     * 校验一个对象是否为空
     */
    fun isEmpty(`object`: Any?): Boolean {
        return `object` == null || `object` == ""
    }

    /**
     * 校验源String是否为空，为空返回默认值
     *
     * @param source 源
     * @param def    默认值
     */
    fun emptyDef(source: String?, def: String): String {
        return if (TextUtils.isEmpty(source)) def else source!!
    }

    /**
     * 校验trim后的字符串
     */
    fun trimEmptyDef(source: String?, def: String): String {
        return if (source == null) def else emptyDef(source.trim { it <= ' ' }, def)
    }

    /**
     * 返回一个不为空的HashMap
     */
    fun <K, V> nonNullHashMap(map: Map<K, V>?): Map<K, V> {
        return map ?: HashMap()
    }

    /**
     * 获取数组的长度
     *
     * @param objects
     * @return
     */
    fun getLength(vararg objects: Any?): Int {
        return objects.size
    }

    fun getLength(collection: Collection<*>?): Int {
        return collection?.size ?: 0
    }

    fun getLength(map: Map<*, *>?): Int {
        return map?.size ?: 0
    }

    /**
     * 校验是否是手机号
     *
     * @param number 手机号
     * @return
     */
    fun isPhoneNumber(number: String?): Boolean {
        val regex = "^1[0-9]{10}$"
        return Pattern.compile(regex).matcher(number).matches()
    }

    /**
     * 校验是否是正整数
     */
    fun isNumber(number: String?): Boolean {
        if (TextUtils.isEmpty(number)) {
            return false
        }
        val regex = "^\\+?[1-9]\\d*$"
        return Pattern.compile(regex).matcher(number).matches()
    }

    /**
     * 限制最大值
     *
     * @param collection 集合
     * @param max        最大值
     */
    fun maxLimit(collection: Collection<*>?, max: Int): Int {
        val length = getLength(collection)
        return Math.min(length, max)
    }

    /**
     * 是否是验证码登录
     *
     * @param code 验证码
     */
    fun isLoginVerificationCode(code: String?): Boolean {
        return !TextUtils.isEmpty(code)
    }
}