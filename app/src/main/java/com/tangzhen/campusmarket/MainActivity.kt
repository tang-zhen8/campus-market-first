package com.tangzhen.campusmarket

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.tangzhen.campusmarket.data.MarketRepository
import com.tangzhen.campusmarket.databinding.ActivityMainBinding
import com.tangzhen.campusmarket.ui.home.HomeFragment
import com.tangzhen.campusmarket.ui.message.MessageFragment
import com.tangzhen.campusmarket.ui.mine.MineFragment
import com.tangzhen.campusmarket.ui.publish.PublishFragment
import com.tangzhen.campusmarket.widget.StarredWidgetProvider

/**
 * 主界面：单 Activity + 底部导航 + 四个 Fragment
 *
 * 架构说明：
 *  ui/home     首页 - 商品列表
 *  ui/publish  发布 - 上架二手商品
 *  ui/message  消息 - 留言消息汇总
 *  ui/mine     我的 - 收藏与发布
 *  data/       数据仓库层
 *  model/      数据模型层
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // 1. 初始化数据层：先读取云端商品的本地离线缓存（断网也能浏览）
        MarketRepository.init(applicationContext)

        // 2. 数据一变（发布 / 收藏 / 留言 / 盯盘切换 / 云端同步）就把盯盘快照推到桌面小组件，
        //    保证桌面内容永远跟 App 内状态一致，不会停留在旧数据上
        MarketRepository.onDataChanged = {
            StarredWidgetProvider.pushUpdate(applicationContext)
        }
        StarredWidgetProvider.refreshAll(applicationContext)

        if (savedInstanceState == null) {
            switchTo(HomeFragment(), "首页")
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> switchTo(HomeFragment(), "首页")
                R.id.nav_publish -> switchTo(PublishFragment(), "发布闲置")
                R.id.nav_message -> switchTo(MessageFragment(), "消息")
                R.id.nav_mine -> switchTo(MineFragment(), "我的")
            }
            true
        }
    }

    private fun switchTo(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        binding.toolbar.title = title
    }
}
