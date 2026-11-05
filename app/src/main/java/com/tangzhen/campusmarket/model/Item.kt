package com.tangzhen.campusmarket.model

/**
 * 商品数据模型
 * 对应校园二手交易（无支付）场景：仅展示与沟通，不涉及支付
 */
data class Item(
    val id: Long,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val sellerName: String,
    val publishTime: String,
    var isFavorited: Boolean = false,
    val comments: MutableList<Comment> = mutableListOf()
)

/**
 * 留言/评论模型（必做功能：留言）
 */
data class Comment(
    val id: Long,
    val user: String,
    val content: String,
    val time: String
)
