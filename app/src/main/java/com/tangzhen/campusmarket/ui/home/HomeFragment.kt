package com.tangzhen.campusmarket.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.tangzhen.campusmarket.R
import com.tangzhen.campusmarket.data.MarketRepository
import com.tangzhen.campusmarket.databinding.FragmentHomeBinding

/**
 * 首页：分类宫格（第一级）
 *
 * 打开 App 先看到六个分类卡片（教材书籍 / 数码产品 / 生活电器 / 运动器材 /
 * 衣物用品 / 其他），点进分类后才是具体的商品列表（CategoryActivity）。
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CategoryAdapter

    /** 六个分类与对应的配图（与 MarketRepository.CATEGORIES 保持一致） */
    private val categoryIcons = mapOf(
        "教材书籍" to R.drawable.img_cat_textbook,
        "数码产品" to R.drawable.img_cat_digital,
        "生活电器" to R.drawable.img_cat_appliance,
        "运动器材" to R.drawable.img_cat_sports,
        "衣物用品" to R.drawable.img_cat_clothes,
        "其他" to R.drawable.img_cat_other,
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = CategoryAdapter { category ->
            // 点分类卡片 -> 进入该分类的商品列表
            val intent = Intent(requireContext(), CategoryActivity::class.java)
            intent.putExtra(CategoryActivity.EXTRA_CATEGORY, category)
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = null

        binding.swipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.primary)
        )
        // 强制技术点「网络」：下拉刷新向云端发起真实 HTTP 请求
        binding.swipeRefresh.setOnRefreshListener {
            loadFromCloud()
        }

        // 观察仓库数据，统计每个分类的在售件数（LiveData 单向数据流）
        MarketRepository.items.observe(viewLifecycleOwner) { list ->
            val uiList = categoryIcons.map { (name, icon) ->
                CategoryUi(name, icon, list.count { it.category == name })
            }
            adapter.submitList(uiList)
            binding.emptyView.visibility =
                if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        // 首次进入：云端商品缓存为空时静默触发一次网络请求
        if (!MarketRepository.hasCloudData()) {
            binding.swipeRefresh.isRefreshing = true
            loadFromCloud()
        }
    }

    /** 请求云端商品列表；失败时提示并保留本地缓存数据 */
    private fun loadFromCloud() {
        MarketRepository.refreshFromNetwork { success, message ->
            if (_binding == null) return@refreshFromNetwork
            binding.swipeRefresh.isRefreshing = false
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
