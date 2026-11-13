package com.tangzhen.campusmarket.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.tangzhen.campusmarket.MainActivity
import com.tangzhen.campusmarket.R
import com.tangzhen.campusmarket.data.MarketRepository
import java.util.Locale

/**
 * 「桌面星标闪送」小组件（伴生模块 P1 + 创新点一）
 *
 * 把用户"盯盘"中的商品钉在桌面上：解锁手机即可看到标题与价格，
 * 实现"信息找用户"，无需反复打开 App 刷新。
 *
 * 数据同步方式：
 *  App 内切换盯盘状态 -> WidgetSyncHelper.push() 写入 SharedPreferences
 *  并立即刷新所有桌面小组件实例；系统周期/重启更新时同样从
 *  SharedPreferences 读取，保证跨进程可见。
 */
class StarredWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // 首次添加小组件：若从未同步过，直接从仓库读取（onUpdate 运行在 App 自身进程内）
        ensureData(context)
        render(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        private const val PREFS = "starred_widget"
        private const val KEY_TITLE = "title"
        private const val KEY_PRICE = "price"
        private const val KEY_STARRED = "has_starred"

        /** 从仓库读取最新盯盘商品并写入 SharedPreferences（跨进程可见的持久化） */
        private fun syncFromRepository(context: Context): Pair<String?, String?> {
            val item = MarketRepository.getLatestStarred()
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val title = item?.title ?: ""
            val price = item?.price?.let { String.format(Locale.CHINA, "¥%.2f", it) } ?: ""
            prefs.edit()
                .putString(KEY_TITLE, title)
                .putString(KEY_PRICE, price)
                .putBoolean(KEY_STARRED, item != null)
                .apply()
            return title to price
        }

        /** App 内数据变化时调用：持久化 + 立即刷新桌面小组件 */
        fun pushUpdate(context: Context) {
            syncFromRepository(context)
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, StarredWidgetProvider::class.java))
            if (ids.isNotEmpty()) {
                StarredWidgetProvider().render(context, manager, ids)
            }
        }

        private fun ensureData(context: Context) {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            if (!prefs.contains(KEY_STARRED)) syncFromRepository(context)
        }
    }

    private fun render(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val hasStarred = prefs.getBoolean(KEY_STARRED, false)
        val title = if (hasStarred) prefs.getString(KEY_TITLE, "") else context.getString(R.string.widget_empty)
        val price = if (hasStarred) prefs.getString(KEY_PRICE, "") else ""

        // 点击小组件打开 App 首页
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_starred).apply {
                setTextViewText(R.id.tvWidgetTitle, title)
                setTextViewText(R.id.tvWidgetPrice, price)
                setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)
            }
            manager.updateAppWidget(id, views)
        }
    }
}
