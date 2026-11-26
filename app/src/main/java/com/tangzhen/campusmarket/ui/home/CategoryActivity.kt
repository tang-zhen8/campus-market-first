package com.tangzhen.campusmarket.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.tangzhen.campusmarket.databinding.ActivityCategoryBinding
import com.tangzhen.campusmarket.data.MarketRepository
import com.tangzhen.campusmarket.ui.detail.ItemDetailActivity

/**
 * 分类商品列表页（首页第二级）：展示某个分类下的全部商品
 *
 * 数据仍来自 MarketRepository 的 LiveData，只做按分类过滤；
 * 仓库数据变化（发布 / 收藏 / 同步云端）时列表自动刷新。
 */
class CategoryActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CATEGORY = "extra_category"
    }

    private lateinit var binding: ActivityCategoryBinding
    private lateinit var adapter: ItemAdapter
    private val category: String by lazy {
        intent.getStringExtra(EXTRA_CATEGORY).orEmpty()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = category
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = ItemAdapter { item ->
            val intent = Intent(this, ItemDetailActivity::class.java)
            intent.putExtra(ItemDetailActivity.EXTRA_ITEM_ID, item.id)
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = null

        MarketRepository.items.observe(this) { list ->
            val filtered = list.filter { it.category == category }
            adapter.submitList(filtered)
            binding.emptyView.visibility =
                if (filtered.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
