package com.tangzhen.campusmarket.data.remote

import com.google.gson.annotations.SerializedName

/**
 * 云端商品数据模型（DTO：Data Transfer Object）
 *
 * 对应 https://dummyjson.com/products 返回的 JSON 结构。
 * 注意：DTO 只负责"传输"，不直接给 UI 用；转换成 model/Item 后才进入仓库层，
 * 这样以后换接口（比如换自建服务器）只要改这个文件，UI 层完全不用动。
 */
data class ProductListResponse(
    @SerializedName("products") val products: List<ProductDto> = emptyList(),
    @SerializedName("total") val total: Int = 0
)

data class ProductDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: Double,
    @SerializedName("category") val category: String,
    @SerializedName("thumbnail") val thumbnail: String? = null,
    @SerializedName("images") val images: List<String>? = null,
    @SerializedName("brand") val brand: String? = null,
    @SerializedName("stock") val stock: Int = 0,
    @SerializedName("rating") val rating: Double = 0.0
)
