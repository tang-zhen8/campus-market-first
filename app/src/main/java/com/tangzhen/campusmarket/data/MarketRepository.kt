package com.tangzhen.campusmarket.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tangzhen.campusmarket.data.remote.ApiClient
import com.tangzhen.campusmarket.data.remote.ProductDto
import com.tangzhen.campusmarket.data.remote.ProductListResponse
import com.tangzhen.campusmarket.model.Comment
import com.tangzhen.campusmarket.model.Item
import com.tangzhen.campusmarket.utils.NetUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 数据仓库层（框架核心）
 *
 * 第一版使用内存模拟数据 + LiveData 对外暴露，
 * 后续版本只需把内部实现替换为 Room / 云端接口，
 * UI 层代码无需改动 —— 这就是分层架构的意义。
 */
object MarketRepository {

    private var nextId = 100L
    private var nextCommentId = 1000L
    private val currentUserName = "唐珍"

    private val timeFormat = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)

    private fun now(): String = timeFormat.format(Date())

    // 内存数据源：模拟商品列表（trustTags = 校园信任标签创新点）
    private val itemList: MutableList<Item> = mutableListOf(
        Item(
            id = 1, title = "高等数学教材（第七版）上下册",
            price = 15.0, description = "大一上学期用的，笔记不多，九成新，考研的同学也可以参考。",
            category = "教材书籍", sellerName = "李同学", publishTime = "11-03 14:20",
            trustTags = listOf("本校学生认证", "可送到宿舍楼下"),
        ),
        Item(
            id = 2, title = "小米台灯 Pro（宿舍神器）",
            price = 59.0, description = "用了一年，功能完好，灯珠无损坏，搬宿舍低价出。",
            category = "生活电器", sellerName = "王同学", publishTime = "11-03 20:05",
            trustTags = listOf("本校学生认证", "支持当面验货"),
        ),
        Item(
            id = 3, title = "蓝牙机械键盘 87键 红轴",
            price = 89.0, description = "编程写作业都很舒服，换了客制化键盘所以出掉。",
            category = "数码产品", sellerName = "张同学", publishTime = "11-04 09:12",
            isStarred = true,  // 预置盯盘数据，方便演示桌面小组件
            trustTags = listOf("本校学生认证", "同栋宿舍楼"),
        ),
        Item(
            id = 4, title = "羽毛球拍 尤尼克斯入门款",
            price = 45.0, description = "体育课结束后用不上了，送手胶和拍套。",
            category = "运动器材", sellerName = "刘同学", publishTime = "11-04 18:40",
            trustTags = listOf("本校学生认证", "支持当面验货"),
        ),
        Item(
            id = 5, title = "CET-4/6 真题试卷一套（含解析）",
            price = 10.0, description = "只写了前两套，几乎全新，附赠答题卡。",
            category = "教材书籍", sellerName = "陈同学", publishTime = "11-05 08:30",
            trustTags = listOf("本校学生认证", "可送到宿舍楼下"),
        ),
        Item(
            id = 6, title = "自行车 变速通勤款",
            price = 150.0, description = "毕业学长留下的，刹车已检修，校内代步首选。",
            category = "其他", sellerName = "赵同学", publishTime = "11-05 12:00",
            trustTags = listOf("本校学生认证", "同栋宿舍楼", "支持当面验货"),
        ),
    )

    // 预置两条留言，演示效果
    init {
        itemList[0].comments.add(Comment(nextCommentId++, "赵同学", "请问可以刀一点吗？", "11-03 15:00"))
        itemList[2].comments.add(Comment(nextCommentId++, "李同学", "有线还是只有蓝牙？", "11-04 10:21"))
    }

    // ===== 对 UI 层暴露的 LiveData =====
    private val _items = MutableLiveData<List<Item>>()
    val items: LiveData<List<Item>> = _items

    /**
     * 数据变更回调（供桌面小组件联动）
     *
     * 只要仓库内任何数据发生变化（发布 / 收藏 / 留言 / 盯盘），
     * 都会回调这里，由 MainActivity 注册后把最新盯盘快照推送到桌面小组件，
     * 避免"小组件内容停留在旧数据"的问题。
     */
    var onDataChanged: (() -> Unit)? = null

    private fun notifyChanged() {
        _items.value = itemList.toList()
        onDataChanged?.invoke()
    }

    /** 首页：全部商品（倒序，最新发布在前） */
    fun refresh() = notifyChanged()

    fun getItemById(id: Long): Item? = itemList.find { it.id == id }

    /** 发布商品（发布页） */
    fun addItem(title: String, price: Double, description: String, category: String) {
        val item = Item(
            id = nextId++,
            title = title,
            price = price,
            description = description,
            category = category,
            sellerName = currentUserName,
            publishTime = now(),
        )
        itemList.add(0, item)
        notifyChanged()
    }

    /** 收藏 / 取消收藏（必做功能：收藏） */
    fun toggleFavorite(itemId: Long): Boolean? {
        val item = getItemById(itemId) ?: return null
        item.isFavorited = !item.isFavorited
        notifyChanged()
        return item.isFavorited
    }

    /** 我的收藏 */
    fun getFavoriteItems(): List<Item> = itemList.filter { it.isFavorited }

    /** 我的发布 */
    fun getMyItems(): List<Item> = itemList.filter { it.sellerName == currentUserName }

    /** 留言（必做功能：留言） */
    fun addComment(itemId: Long, content: String): Boolean {
        val item = getItemById(itemId) ?: return false
        item.comments.add(Comment(nextCommentId++, currentUserName, content, now()))
        notifyChanged()
        return true
    }

    /** 盯盘（创新点：桌面小组件实时展示） */
    fun toggleStar(itemId: Long): Boolean? {
        val item = getItemById(itemId) ?: return null
        item.isStarred = !item.isStarred
        // 记录盯盘时间：最近盯盘的排在小组件最前面
        item.starredAt = if (item.isStarred) System.currentTimeMillis() else 0L
        notifyChanged()
        return item.isStarred
    }

    /** 所有盯盘商品，按"最近盯盘"倒序（桌面小组件按此顺序展示） */
    fun getStarredItems(): List<Item> =
        itemList.filter { it.isStarred }.sortedByDescending { it.starredAt }

    /** 最近一个盯盘中的商品 */
    fun getLatestStarred(): Item? = getStarredItems().firstOrNull()

    // ==========================================================
    // 云端数据流（强制技术点「网络」）：Retrofit 拉取 + 本地离线缓存
    // ==========================================================

    /** 云端商品的 id 统一从 2000 起，和本地 id（1~99 内置、100+ 用户发布）互不冲突 */
    private const val REMOTE_ID_BASE = 2000L

    private var appContext: Context? = null

    /**
     * 初始化（MainActivity 启动时调用一次）：
     * 先把上次的云端商品缓存读出来，保证断网/冷启动也能立刻看到商品。
     */
    fun init(context: Context) {
        appContext = context.applicationContext
        val cached = ItemCache.load(appContext!!)
        if (cached.isNotEmpty()) {
            replaceRemoteItems(cached)
        } else {
            // 无缓存也要发一次初始数据，避免首页短暂空白
            notifyChanged()
        }
    }

    /** 当前是否已经有云端商品（首页判断要不要静默触发首次网络请求） */
    fun hasCloudData(): Boolean = itemList.any { it.isFromCloud }

    /** 消息页：把别人在我发布的商品下的留言汇总为消息 */
    fun getCommentMessages(): List<Comment> =
        getMyItems().flatMap { it.comments }.sortedByDescending { it.time }

    /**
     * 下拉刷新：向云端发起真实网络请求（Retrofit 异步回调）
     *
     * 策略：网络优先，本地兜底
     *  - 成功：更新列表 + 覆盖本地缓存（断网后仍可浏览）
     *  - 失败：保留现有数据，并通过 callback 告知 UI 提示用户
     */
    fun refreshFromNetwork(onResult: (success: Boolean, message: String) -> Unit) {
        val ctx = appContext
        if (ctx == null) {
            onResult(false, "数据层尚未初始化")
            return
        }
        if (!NetUtils.isOnline(ctx)) {
            onResult(false, "当前无网络连接，已显示本地缓存")
            return
        }

        ApiClient.marketApi.getProducts(20).enqueue(object : Callback<ProductListResponse> {
            override fun onResponse(
                call: Call<ProductListResponse>,
                response: Response<ProductListResponse>
            ) {
                val products = response.body()?.products.orEmpty()
                if (response.isSuccessful && products.isNotEmpty()) {
                    ItemCache.save(ctx, products)
                    replaceRemoteItems(products)
                    onResult(true, "云端同步成功，已获取 ${products.size} 件商品")
                } else {
                    onResult(false, "云端返回异常（${response.code()}），已显示本地缓存")
                }
            }

            override fun onFailure(call: Call<ProductListResponse>, t: Throwable) {
                onResult(false, "网络请求失败（${t.javaClass.simpleName}），已显示本地缓存")
            }
        })
    }

    /** 用云端商品替换列表里的旧云端数据（保留用户对它们的收藏 / 盯盘 / 留言状态） */
    private fun replaceRemoteItems(products: List<ProductDto>) {
        val oldStates = itemList.filter { it.id >= REMOTE_ID_BASE }.associateBy { it.id }

        itemList.removeAll { it.id >= REMOTE_ID_BASE }

        val mapped = products.map { dto ->
            val fresh = dto.toItem()
            oldStates[fresh.id]?.let { old ->
                fresh.isFavorited = old.isFavorited
                fresh.isStarred = old.isStarred
                fresh.starredAt = old.starredAt
                fresh.comments.addAll(old.comments)
            }
            fresh
        }

        // 云端商品插在本机发布的商品之后、内置示例之前
        val insertIndex = itemList.indexOfFirst { it.id >= 100L }
        val at = if (insertIndex == -1) 0 else insertIndex + 1
        itemList.addAll(at, mapped)

        notifyChanged()
    }

    /** DTO -> Item：外部数据结构转换为 App 内部模型（解耦：换接口只改这里） */
    private fun ProductDto.toItem(): Item {
        val chineseCategory = mapCategory(category)
        return Item(
            id = REMOTE_ID_BASE + id,
            // 列表标题中文化：云端原始英文标题收进详情页，界面保持校园二手的中文语境
            title = "来自云端的好物 #$id（$chineseCategory）",
            // dummyjson 价格为美元，按 1:7 折算为人民币参考价
            price = (price * 7).roundToInt().toDouble(),
            // 详情页保留云端原文（含品牌），证明数据来自真实网络请求
            description = "该商品来自云端商品库，以下为数据源原文描述：\n$description",
            category = chineseCategory,
            sellerName = "☁ 云端自营",
            publishTime = "今日同步",
            trustTags = listOf("本校学生认证", "云端数据源"),
            imageUrl = thumbnail ?: images?.firstOrNull() ?: "",
            isFromCloud = true,
        )
    }

    /** 云端分类映射到本 App 的六个分类 */
    private fun mapCategory(raw: String): String = when (raw) {
        "smartphones", "mobile-accessories", "laptops", "tablets" -> "数码产品"
        "furniture", "home-decoration", "kitchen-accessories" -> "生活电器"
        "sports-accessories", "motorcycle", "vehicle" -> "运动器材"
        "tops", "mens-shirts", "womens-dresses", "womens-bags", "womens-jewellery" -> "衣物用品"
        else -> "其他"
    }

    // 分类常量（供发布页下拉框使用）
    val CATEGORIES = listOf("教材书籍", "数码产品", "生活电器", "运动器材", "衣物用品", "其他")
}
