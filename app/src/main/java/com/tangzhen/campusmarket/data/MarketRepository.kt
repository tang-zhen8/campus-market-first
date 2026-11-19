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

        // limit=0 表示拉取云端全部商品，再由白名单 + 价格上限过滤出校园闲置
        ApiClient.marketApi.getProducts(0).enqueue(object : Callback<ProductListResponse> {
            override fun onResponse(
                call: Call<ProductListResponse>,
                response: Response<ProductListResponse>
            ) {
                val products = response.body()?.products.orEmpty()
                val suitable = products
                    .filter { it.category in CAMPUS_CATEGORIES }
                    // 二手折价后再卡上限：学生转让的东西很少有超过几百块的
                    .filter { it.price * RATE_USD_CNY * USED_DISCOUNT <= MAX_CLOUD_PRICE_CNY }
                    .take(60)
                if (response.isSuccessful && suitable.isNotEmpty()) {
                    ItemCache.save(ctx, suitable)
                    replaceRemoteItems(suitable)
                    onResult(true, "云端同步成功，已获取 ${suitable.size} 件商品")
                } else {
                    onResult(false, "云端返回异常（${response.code()}），已显示本地缓存")
                }
            }

            override fun onFailure(call: Call<ProductListResponse>, t: Throwable) {
                onResult(false, "网络请求失败（${t.javaClass.simpleName}），已显示本地缓存")
            }
        })
    }

    /** 云端商品美元 -> 人民币汇率参考 */
    private const val RATE_USD_CNY = 7.0

    /**
     * 二手折价系数：学生转让的都是自用闲置，一般按原价五~六折出。
     * 用它算出来的价格既比原价便宜（符合二手逻辑），又能让贵的原价商品
     * 自然落到学生消费得起的价格区间。
     */
    private const val USED_DISCOUNT = 0.6

    /**
     * 云端商品折算成人民币后的价格上限（校园二手的价格合理范围）：
     * 超过 600 元的东西学生基本不会在校内二手群卖，直接丢弃。
     */
    private const val MAX_CLOUD_PRICE_CNY = 600.0

    /**
     * 云端品类白名单：只保留学生真正会交易的品类。
     * 数据源是通用电商演示库，直接照搬会出现化妆品、香水、生鲜肉、
     * 床、摩托车、名表、首饰等不符合校园二手场景的数据——
     * 白名单 + 价格上限双重过滤是最彻底的清洗方式。
     */
    private val CAMPUS_CATEGORIES = setOf(
        "smartphones", "tablets", "laptops", "mobile-accessories",  // 数码产品
        "sports-accessories",                                       // 运动器材
        "sunglasses",                                               // 数码配件（墨镜）
        "mens-shirts", "tops", "womens-dresses",                    // 衣物用品
        "mens-shoes", "womens-shoes",                               // 鞋
        "womens-bags",                                              // 包包
    )

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
    private fun ProductDto.toItem(): Item = Item(
        id = REMOTE_ID_BASE + id,
        // 列表标题中文化：从具体商品名库中按 id 取名（品名 + 型号/细节），
        // 命名风格与内置商品一致，避免"九成新鞋"这类一眼假的名字
        title = cloudTitle(category, id.toLong()),
        // dummyjson 价格为美元：按汇率折算后，再按"二手闲置"打六折，
        // 得到贴近校园实际的学生转让价
        price = (price * RATE_USD_CNY * USED_DISCOUNT).roundToInt().toDouble(),
        // 描述按学生发二手帖的口吻生成，详情页仍可查看云端数据源原文
        description = "自用闲置转让，${cloudCondition(id.toLong())}，校内可当面交易，" +
            "可送到宿舍楼下，可小刀，介意勿拍。",
        category = mapCategory(category),
        sellerName = "李同学",
        publishTime = now(),
        trustTags = listOf("本校学生认证", "支持当面验货"),
        imageUrl = thumbnail ?: images?.firstOrNull() ?: "",
        isFromCloud = true,
    )

    /** 品相描述词（按 id 轮换，用在描述里，让每条文案不完全一样） */
    private val CLOUD_CONDITIONS =
        listOf("九成新", "几乎全新", "仅用一学期", "轻度使用痕迹", "成色不错", "功能完好")

    private fun cloudCondition(id: Long): String =
        CLOUD_CONDITIONS[((id % CLOUD_CONDITIONS.size).toInt())]

    /**
     * 云端商品的具体商品名库：模仿学生真实发帖的命名（品名 + 型号/细节），
     * 按 id 轮换取名，保证同一个商品每次刷新名字稳定不变。
     */
    private val CLOUD_TITLES: Map<String, List<String>> = mapOf(
        "mobile-accessories" to listOf(
            "蓝牙耳机 半入耳式", "充电宝 2万毫安快充", "机械键盘 87键红轴", "无线游戏鼠标",
            "手机支架 桌面折叠", "迷你蓝牙音箱", "Type-C 有线耳机", "三合一数据线",
            "智能手环 运动计步款", "手机壳 硅胶防摔", "桌面小台灯 USB充电", "车充 双口快充",
        ),
        "sports-accessories" to listOf(
            "篮球 7号 室内外通用", "足球 5号 训练款", "羽毛球拍 单拍送手胶", "乒乓球拍 双拍套装",
            "计数跳绳", "加厚瑜伽垫", "运动护膝", "棒球手套", "握力器", "臂力棒",
            "羽毛球一筒 12只装", "网球拍 初学款", "篮球包 大容量", "排球 5号软式",
        ),
        "mens-shirts" to listOf(
            "纯棉白T恤 基础款", "长袖格子衬衫", "polo衫 休闲款", "加绒连帽卫衣",
            "水洗牛仔衬衫", "针织开衫 纯色",
        ),
        "tops" to listOf(
            "圆领卫衣 宽松版", "印花短袖 情侣款", "套头毛衣", "纯色长袖T恤",
            "连帽衫 宽松款", "衬衫 条纹款",
        ),
        "womens-dresses" to listOf(
            "碎花连衣裙", "针织长裙", "白衬衫裙", "长袖收腰连衣裙", "小个子连衣裙",
        ),
        "mens-shoes" to listOf(
            "帆布鞋 低帮", "休闲板鞋", "轻便跑鞋", "小白鞋 百搭款", "实战篮球鞋", "训练鞋 透气款",
        ),
        "womens-shoes" to listOf(
            "厚底小白鞋", "粉色帆布鞋", "运动跑鞋", "复古乐福鞋", "百搭板鞋",
        ),
        "womens-bags" to listOf(
            "双肩包 书包款", "托特包 大容量", "单肩小方包", "帆布斜挎包", "手提通勤包",
        ),
        "sunglasses" to listOf(
            "偏光墨镜 骑行款", "圆框太阳镜", "黑框墨镜 百搭", "金丝细框眼镜",
        ),
    )

    private const val FALLBACK_TITLE = "闲置好物"

    /** 按品类 + id 取一个具体的二手商品名（同一商品名字永远稳定） */
    private fun cloudTitle(category: String, id: Long): String {
        val pool = CLOUD_TITLES[category] ?: return FALLBACK_TITLE
        return pool[(id % pool.size).toInt()]
    }

    /** 云端分类映射到本 App 的六个分类 */
    private fun mapCategory(raw: String): String = when (raw) {
        "smartphones", "mobile-accessories", "laptops", "tablets" -> "数码产品"
        "furniture", "home-decoration", "kitchen-accessories" -> "生活电器"
        "sports-accessories", "motorcycle", "vehicle" -> "运动器材"
        "mens-shirts", "tops", "womens-dresses", "womens-bags",
        "mens-shoes", "womens-shoes", "sunglasses" -> "衣物用品"
        else -> "其他"
    }

    // 分类常量（供发布页下拉框使用）
    val CATEGORIES = listOf("教材书籍", "数码产品", "生活电器", "运动器材", "衣物用品", "其他")
}
