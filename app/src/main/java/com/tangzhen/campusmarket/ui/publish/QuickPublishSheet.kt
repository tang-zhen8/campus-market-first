package com.tangzhen.campusmarket.ui.publish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tangzhen.campusmarket.R
import com.tangzhen.campusmarket.data.MarketRepository
import com.tangzhen.campusmarket.databinding.SheetQuickPublishBinding

/**
 * 创新点三：一键快捷发布（底部弹窗）
 *
 * 场景：期末季卖闲置又多又急，完整表单太繁琐。
 * 只需「商品名 + 价格」即可上架，并提供校园高频词快捷填充，
 * 将发布流程压缩到 3 秒内；详细描述可走下方完整表单。
 */
class QuickPublishSheet : BottomSheetDialogFragment() {

    private var _binding: SheetQuickPublishBinding? = null
    private val binding get() = _binding!!

    // 校园高频词：点击自动填充标题与价格
    private val quickWords = mapOf(
        R.id.chipMath to ("高等数学教材" to 15.0),
        R.id.chipBike to ("自行车 通勤款" to 150.0),
        R.id.chipLamp to ("宿舍台灯" to 59.0),
        R.id.chipRacket to ("羽毛球拍" to 45.0),
        R.id.chipKeyboard to ("机械键盘" to 89.0),
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = SheetQuickPublishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        quickWords.forEach { (id, pair) ->
            binding.root.findViewById<View>(id)?.setOnClickListener {
                binding.etQuickTitle.setText(pair.first)
                binding.etQuickPrice.setText(pair.second.toString())
            }
        }

        binding.btnQuickSubmit.setOnClickListener {
            val title = binding.etQuickTitle.text.toString().trim()
            val price = binding.etQuickPrice.text.toString().trim().toDoubleOrNull()

            if (title.isEmpty()) {
                binding.tilQuickTitle.error = "请填写商品名"
                return@setOnClickListener
            }
            binding.tilQuickTitle.error = null
            if (price == null || price < 0) {
                binding.tilQuickPrice.error = "请填写正确的价格"
                return@setOnClickListener
            }
            binding.tilQuickPrice.error = null

            // 快捷发布默认：分类"其他"，描述用占位文案（详细描述走完整表单）
            MarketRepository.addItem(
                title = title,
                price = price,
                description = "（快捷发布）具体成色与细节请留言咨询~",
                category = "其他"
            )
            Toast.makeText(context, "⚡ 已上架，全程不到 3 秒", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
