package com.tangzhen.campusmarket.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tangzhen.campusmarket.R
import com.tangzhen.campusmarket.databinding.ItemCategoryBinding

/** 首页宫格里的一个分类卡片（分类名 + 配图 + 在售件数） */
data class CategoryUi(
    val name: String,
    val iconRes: Int,
    val count: Int,
)

/**
 * 分类宫格适配器：首页第一级入口，点卡片进入 CategoryActivity
 */
class CategoryAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<CategoryUi, CategoryAdapter.CategoryViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CategoryUi>() {
            override fun areItemsTheSame(oldItem: CategoryUi, newItem: CategoryUi) =
                oldItem.name == newItem.name

            override fun areContentsTheSame(oldItem: CategoryUi, newItem: CategoryUi) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryUi, onItemClick: (String) -> Unit) {
            binding.tvName.text = category.name
            binding.tvCount.text =
                binding.root.context.getString(R.string.on_sale_count, category.count)
            Glide.with(binding.ivIcon)
                .load(category.iconRes)
                .centerCrop()
                .into(binding.ivIcon)
            binding.root.setOnClickListener { onItemClick(category.name) }
        }
    }
}
