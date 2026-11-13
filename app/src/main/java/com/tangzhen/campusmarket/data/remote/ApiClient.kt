package com.tangzhen.campusmarket.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

/**
 * 网络客户端（单例）
 *
 * 统一的 Retrofit 配置入口：baseUrl、超时、JSON 转换器都在这里，
 * 全 App 共用一个实例，避免重复创建线程池与连接池。
 */
object ApiClient {

    /** 免注册的公开商品数据接口（返回真实商品 JSON，适合课程演示） */
    private const val BASE_URL = "https://dummyjson.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val marketApi: MarketApi = retrofit.create(MarketApi::class.java)
}
