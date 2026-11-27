package com.tangzhen.campusmarket.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tangzhen.campusmarket.R
import com.tangzhen.campusmarket.data.MarketRepository
import java.util.Locale

/**
 * 盯盘降价提醒（创新点：让"盯盘"从静态展示变成真正的监控）
 *
 * 云端同步后如果发现盯盘中的商品降价了，发一条系统通知：
 * "篮球 7号 ¥63.00 → ¥52.00"。
 *
 * 权限：Android 13+ 通知属于运行时权限（POST_NOTIFICATIONS），
 * MainActivity 启动时会请求；未授权则静默跳过，不影响主流程。
 */
object PriceAlertNotifier {

    private const val CHANNEL_ID = "price_drop_alert"
    private const val NOTIFICATION_ID = 2001

    /** 创建通知渠道（Android 8.0+ 必须，重复创建无副作用） */
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "盯盘降价提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "盯盘中的商品降价时提醒，帮助你及时捡漏"
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    /** 同步后发现降价时调用；只对"盯盘中"的商品提醒 */
    fun notifyDrops(context: Context, drops: List<MarketRepository.PriceDrop>) {
        val starred = drops.filter { it.isStarred }
        if (starred.isEmpty()) return

        val money = { v: Double -> String.format(Locale.CHINA, "¥%.2f", v) }
        val lines = starred.joinToString("\n") { drop ->
            "${drop.title}  ${money(drop.oldPrice)} → ${money(drop.newPrice)}"
        }
        val title = if (starred.size == 1) "📌 盯盘商品降价啦！" else "📌 ${starred.size} 件盯盘商品降价啦！"

        // Android 13+ 通知是运行时权限；未授权时优雅降级为应用内提醒，
        // 功能不缺失，用户开启通知后即可收到系统推送
        val canNotify: Boolean =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

        if (canNotify) {
            ensureChannel(context)
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_pin)
                .setContentTitle(title)
                .setContentText(starred[0].title + "  " + money(starred[0].oldPrice) + " → " + money(starred[0].newPrice))
                .setStyle(NotificationCompat.BigTextStyle().bigText(lines))
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            android.util.Log.d("PriceAlert", "已发送系统通知")
        } else {
            android.util.Log.d("PriceAlert", "无通知权限，降级为应用内提醒")
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(
                    context,
                    "$title\n$lines",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
