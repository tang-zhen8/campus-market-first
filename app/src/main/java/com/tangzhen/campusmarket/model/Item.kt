package com.tangzhen.campusmarket.model

/**
 * 商品数据模型
 * 对应校园二手交易（无支付）场景：仅展示与沟通，不涉及支付
 *
 * isStarred  : 盯盘状态（创新点：桌面小组件实时展示用户最关心的商品）
 * starredAt  : 盯盘时间戳，用于"最近盯盘的排最前"，避免按发布时间排序导致小组件内容锁死
 * trustTags  : 校园信任标签（创新点：区别于社会平台的熟人信任机制）
 * dealType   : 交易类型（创新点：出售 / 求购 / 交换，校园特色交易模式）
 * location   : 交易地点（预设校园点位，方便当面交易）
 * priceDropped : 本次同步是否发生降价（创新点：盯盘降价提醒）
 */
data class Item(
    val id: Long,
    val title: String,
    /** 价格会随"卖家降价"变动（盯盘降价提醒），所以是 var */
    var price: Double,
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
    val isFromCloud: Boolean = false,
    /** 交易类型：出售（默认）/ 求购 / 交换 */
    val dealType: String = "出售",
    /** 交易地点：预设校园点位，空 = 不限 */
    val location: String = "",
    /** 本次同步是否降价（用于小组件降价角标，降价发生后保持标记） */
    var priceDropped: Boolean = false
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
