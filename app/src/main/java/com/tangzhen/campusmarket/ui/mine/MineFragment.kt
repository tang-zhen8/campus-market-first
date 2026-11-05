package com.tangzhen.campusmarket.ui.mine

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.tangzhen.campusmarket.data.MarketRepository
import com.tangzhen.campusmarket.databinding.FragmentMineBinding
import com.tangzhen.campusmarket.ui.detail.ItemDetailActivity
import com.tangzhen.campusmarket.ui.home.ItemAdapter

/**
 * 我的：我的收藏（必做功能入口） + 我的发布
 */
class MineFragment : Fragment() {

    private var _binding: FragmentMineBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ItemAdapter { item ->
            val intent = Intent(requireContext(), ItemDetailActivity::class.java)
            intent.putExtra(ItemDetailActivity.EXTRA_ITEM_ID, item.id)
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = null

        binding.chipFavorites.isChecked = true
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            loadData()
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val list =
            if (binding.chipFavorites.isChecked) MarketRepository.getFavoriteItems()
            else MarketRepository.getMyItems()
        adapter.submitList(list)
        binding.emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        binding.emptyView.text =
            if (binding.chipFavorites.isChecked) "还没有收藏，去首页看看吧"
            else "还没有发布过闲置"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
