package com.tangzhen.campusmarket.model

/**
 * 商品数据模型
 * 对应校园二手交易（无支付）场景：仅展示与沟通，不涉及支付
 *
 * isStarred  : 盯盘状态（创新点：桌面小组件实时展示用户最关心的商品）
 * starredAt  : 盯盘时间戳，用于"最近盯盘的排最前"，避免按发布时间排序导致小组件内容锁死
 * trustTags  : 校园信任标签（创新点：区别于社会平台的熟人信任机制）
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
    var isStarred: Boolean = false,
    var starredAt: Long = 0L,
    val trustTags: List<String> = listOf("本校学生认证"),
    val comments: MutableList<Comment> = mutableListOf(),
    /** 网络图片地址（Glide 加载，空表示无图，显示占位图） */
    val imageUrl: String = "",
    /** 是否来自云端（true = Retrofit 拉取的网络数据；false = 本机发布/内置数据） */
    val isFromCloud: Boolean = false
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
