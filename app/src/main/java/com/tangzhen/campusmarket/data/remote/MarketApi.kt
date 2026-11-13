package com.tangzhen.campusmarket.data.remote

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 云端商品接口（Retrofit 声明式网络请求）
 *
 * 强制技术点「网络」的核心：
 * 用注解描述 HTTP 接口，Retrofit + OkHttp 在底层完成请求、序列化、
 * 线程切换与错误处理，比手写 HttpURLConnection 更贴近企业开发方式。
 */
interface MarketApi {

    /**
     * 拉取云端商品列表
     * @param limit 一次拉取多少条（演示用 20 条足够，避免浪费流量）
     */
    @GET("products")
    fun getProducts(@Query("limit") limit: Int): Call<ProductListResponse>
}
