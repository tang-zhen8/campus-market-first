package com.tangzhen.campusmarket.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.tangzhen.campusmarket.R
import com.tangzhen.campusmarket.data.MarketRepository
import com.tangzhen.campusmarket.databinding.FragmentHomeBinding
import com.tangzhen.campusmarket.ui.detail.ItemDetailActivity

/**
 * 首页：商品列表
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ItemAdapter { item ->
            // 点击卡片 -> 商品详情
            val intent = Intent(requireContext(), ItemDetailActivity::class.java)
            intent.putExtra(ItemDetailActivity.EXTRA_ITEM_ID, item.id)
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = null

        binding.swipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.primary)
        )
        // 强制技术点「网络」：下拉刷新向云端发起真实 HTTP 请求
        binding.swipeRefresh.setOnRefreshListener {
            loadFromCloud()
        }

        // 观察仓库数据，自动刷新列表（LiveData 单向数据流）
        MarketRepository.items.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
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
