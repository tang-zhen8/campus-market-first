package com.tangzhen.campusmarket.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tangzhen.campusmarket.databinding.ItemProductBinding
import com.tangzhen.campusmarket.model.Item
import java.util.Locale

/**
 * 商品列表适配器：DiffUtil 只刷新变化的卡片，避免闪烁
 */
class ItemAdapter(
    private val onItemClick: (Item) -> Unit
) : ListAdapter<Item, ItemAdapter.ItemViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Item, newItem: Item) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class ItemViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item, onItemClick: (Item) -> Unit) {
            binding.tvTitle.text = item.title
            binding.tvPrice.text = String.format(Locale.CHINA, "¥%.2f", item.price)
            binding.tvDescription.text = item.description
            binding.tvCategory.text = item.category
            binding.tvSeller.text = "${item.sellerName} · ${item.publishTime}"
            binding.tvCommentCount.text = "${item.comments.size} 条留言"
            binding.ivFavorite.visibility = if (item.isFavorited) android.view.View.VISIBLE else android.view.View.INVISIBLE
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}
