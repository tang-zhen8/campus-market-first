package com.tangzhen.campusmarket.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.tangzhen.campusmarket.MainActivity
import com.tangzhen.campusmarket.R
import com.tangzhen.campusmarket.data.MarketRepository
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

/**
 * 「桌面星标闪送」小组件（伴生模块 P1 + 创新点一）
 *
 * 把用户"盯盘"中的商品钉在桌面上：解锁手机即可看到标题与价格，
 * 实现"信息找用户"，无需反复打开 App 刷新。
 *
 * ── 数据同步机制（本次修复重点）──────────────────────────────
 * 1. 单一数据源：仓库层 MarketRepository 是唯一真相，数据一变就回调；
 * 2. 快照持久化：把最多 3 条盯盘商品以 JSON 写入 SharedPreferences，
 *    应用进程被杀 / 手机重启后小组件依然有内容；
 * 3. 排序按「盯盘时间」倒序（不是发布时间），
 *    这样新盯的商品一定排在第一位，不会出现"内容锁死不变"；
 * 4. 每次数据变更 + 系统周期刷新（30 分钟）+ 点击刷新，都会重绘小组件。
 */
class StarredWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        ensureSnapshot(context)
        render(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_REFRESH) {
            pushUpdate(context)
        } else {
            super.onReceive(context, intent)
        }
    }

    companion object {
        /** 手动刷新（也用于 App 内数据变化后广播刷新） */
        const val ACTION_REFRESH = "com.tangzhen.campusmarket.action.WIDGET_REFRESH"

        private const val PREFS = "starred_widget"
        private const val KEY_SNAPSHOT = "snapshot"
        private const val MAX_ROWS = 3

        /**
         * App 内数据变化时调用：把最新盯盘列表写成快照并立即重绘小组件。
         * 发布、收藏、留言、盯盘切换都会走到这里（由 MainActivity 注册的回调触发）。
         */
        fun pushUpdate(context: Context) {
            val app = context.applicationContext
            val items = MarketRepository.getStarredItems().take(MAX_ROWS)
            val array = JSONArray()
            items.forEach { item ->
                array.put(
                    JSONObject().apply {
                        put("title", item.title)
                        put("price", item.price)
                    }
                )
            }
            app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SNAPSHOT, array.toString())
                .apply()

            refreshAll(app)
        }

        /** 只重绘，不改数据（onUpdate / 点击刷新时用） */
        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, StarredWidgetProvider::class.java))
            if (ids.isNotEmpty()) {
                StarredWidgetProvider().render(context, manager, ids)
            }
        }

        /** 首次添加小组件且从未同步过时，先用仓库当前状态兜底 */
        private fun ensureSnapshot(context: Context) {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            if (!prefs.contains(KEY_SNAPSHOT)) {
                pushUpdate(context)
            }
        }

        /** 读取快照：返回 (标题, 价格) 列表 */
        private fun readSnapshot(context: Context): List<Pair<String, String>> {
            val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_SNAPSHOT, null) ?: return emptyList()
            return try {
                val array = JSONArray(raw)
                (0 until array.length()).map { i ->
                    val obj = array.getJSONObject(i)
                    obj.getString("title") to String.format(Locale.CHINA, "¥%.2f", obj.getDouble("price"))
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun render(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val snapshot = readSnapshot(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val refreshPendingIntent = PendingIntent.getBroadcast(
            context, 1,
            Intent(context, StarredWidgetProvider::class.java).apply { action = ACTION_REFRESH },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val rowIds = listOf(
            Triple(R.id.row1, R.id.tvTitle1, R.id.tvPrice1),
            Triple(R.id.row2, R.id.tvTitle2, R.id.tvPrice2),
            Triple(R.id.row3, R.id.tvTitle3, R.id.tvPrice3),
        )

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_starred)

            views.setTextViewText(
                R.id.tvWidgetCount,
                if (snapshot.isEmpty()) "" else "共 ${snapshot.size} 件盯盘中"
            )

            rowIds.forEachIndexed { index, (rowId, titleId, priceId) ->
                val entry = snapshot.getOrNull(index)
                if (entry == null) {
                    views.setViewVisibility(rowId, View.GONE)
                } else {
                    views.setViewVisibility(rowId, View.VISIBLE)
                    views.setTextViewText(titleId, entry.first)
                    views.setTextViewText(priceId, entry.second)
                }
            }

            views.setViewVisibility(
                R.id.tvWidgetEmpty,
                if (snapshot.isEmpty()) View.VISIBLE else View.GONE
            )

            views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)
            // 点"共 N 件盯盘中"立即刷新（手动兜底，避免误以为卡住）
            views.setOnClickPendingIntent(R.id.tvWidgetCount, refreshPendingIntent)
            manager.updateAppWidget(id, views)
        }
    }
}
