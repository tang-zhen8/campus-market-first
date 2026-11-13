package com.tangzhen.campusmarket.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tangzhen.campusmarket.data.remote.ProductDto
import java.io.File

/**
 * 云端商品本地缓存（离线缓存策略）
 *
 * 策略：网络优先，本地兜底
 *  1. 每次从云端拉到商品后，把 JSON 写入应用私有目录（filesDir）；
 *  2. App 冷启动时先读缓存，用户立刻能看到上次的商品（断网也能浏览）；
 *  3. 下拉刷新触发真实网络请求，成功后覆盖缓存。
 *
 * 为什么不用 Room？课程第一版先用文件缓存演示"云端->本地"的数据流，
 * 后续迭代把 ItemCache 换成 Room 的 DAO 即可，仓库层之上的代码不用改。
 */
object ItemCache {

    private const val FILE_NAME = "cached_products.json"
    private val gson = Gson()

    private fun cacheFile(context: Context): File =
        File(context.filesDir, FILE_NAME)

    /** 读取缓存（首次运行 / 缓存损坏时返回空列表） */
    fun load(context: Context): List<ProductDto> = try {
        val file = cacheFile(context)
        if (file.exists()) {
            val type = object : TypeToken<List<ProductDto>>() {}.type
            gson.fromJson(file.readText(), type) ?: emptyList()
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        emptyList()
    }

    /** 写入缓存 */
    fun save(context: Context, products: List<ProductDto>) {
        try {
            cacheFile(context).writeText(gson.toJson(products))
        } catch (_: Exception) {
            // 缓存写入失败不影响主流程
        }
    }

    /** 清空缓存 */
    fun clear(context: Context) {
        cacheFile(context).delete()
    }
}
