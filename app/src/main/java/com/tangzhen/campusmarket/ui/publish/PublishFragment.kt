package com.tangzhen.campusmarket.ui.publish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.tangzhen.campusmarket.R
import com.tangzhen.campusmarket.data.MarketRepository
import com.tangzhen.campusmarket.databinding.FragmentPublishBinding

/**
 * 发布页：填写商品信息后上架
 */
class PublishFragment : Fragment() {

    private var _binding: FragmentPublishBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPublishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 创新点三：一键快捷发布（底部弹窗）
        binding.btnQuickPublish.setOnClickListener {
            QuickPublishSheet().show(parentFragmentManager, "quick_publish")
        }

        // 分类下拉框
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            MarketRepository.CATEGORIES
        )
        binding.spinnerCategory.setAdapter(adapter)
        binding.spinnerCategory.setText(MarketRepository.CATEGORIES[0], false)

        // 创新点四：交易地点下拉框（预设校园点位）
        val locationAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            MarketRepository.DEAL_LOCATIONS
        )
        binding.spinnerLocation.setAdapter(locationAdapter)
        binding.spinnerLocation.setText(MarketRepository.DEAL_LOCATIONS[0], false)

        binding.btnPublish.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val priceText = binding.etPrice.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val category = binding.spinnerCategory.text.toString()
            // 创新点四：交易类型 + 交易地点
            val dealType = when (binding.toggleDealType.checkedButtonId) {
                R.id.btnDealBuy -> "求购"
                R.id.btnDealSwap -> "交换"
                else -> "出售"
            }
            val location = binding.spinnerLocation.text.toString()
                .takeIf { it != "不限" } ?: ""

            // 简单表单校验
            if (title.isEmpty()) {
                binding.tilTitle.error = "请填写标题"
                return@setOnClickListener
            }
            binding.tilTitle.error = null

            val price = priceText.toDoubleOrNull()
            if (price == null || price < 0) {
                binding.tilPrice.error = if (dealType == "求购") "请填写你的预算" else "请填写正确的价格"
                return@setOnClickListener
            }
            binding.tilPrice.error = null

            MarketRepository.addItem(title, price, description, category, dealType, location)
            Toast.makeText(requireContext(), "发布成功", Toast.LENGTH_SHORT).show()
            clearForm()
        }
    }

    private fun clearForm() {
        binding.etTitle.setText("")
        binding.etPrice.setText("")
        binding.etDescription.setText("")
        binding.spinnerCategory.setText(MarketRepository.CATEGORIES[0], false)
        binding.spinnerLocation.setText(MarketRepository.DEAL_LOCATIONS[0], false)
        binding.toggleDealType.check(R.id.btnDealSell)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
