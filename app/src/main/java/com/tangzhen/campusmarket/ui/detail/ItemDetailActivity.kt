package com.tangzhen.campusmarket.ui.detail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.tangzhen.campusmarket.R
import com.tangzhen.campusmarket.data.MarketRepository
import com.tangzhen.campusmarket.databinding.ActivityItemDetailBinding
import java.util.Locale

/**
 * 商品详情页：展示信息 + 收藏 + 留言（两个必做功能都在这里）
 */
class ItemDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ITEM_ID = "item_id"
    }

    private lateinit var binding: ActivityItemDetailBinding
    private var itemId: Long = -1
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1)
        val item = MarketRepository.getItemById(itemId)
        if (item == null) {
            finish()
            return
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        commentAdapter = CommentAdapter()
        binding.recyclerComments.layoutManager = LinearLayoutManager(this)
        binding.recyclerComments.adapter = commentAdapter

        binding.btnSend.setOnClickListener {
            val content = binding.etComment.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(this, "留言内容不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            MarketRepository.addComment(itemId, content)
            binding.etComment.setText("")
            Toast.makeText(this, "留言成功", Toast.LENGTH_SHORT).show()
        }

        binding.btnFavorite.setOnClickListener { toggleFavorite() }
        binding.btnStar.setOnClickListener { toggleStar() }

        MarketRepository.items.observe(this) { render() }
        render()
    }

    private fun render() {
        val item = MarketRepository.getItemById(itemId) ?: return
        binding.tvTitle.text = item.title
        binding.tvPrice.text = String.format(Locale.CHINA, "¥%.2f", item.price)
        binding.tvCategory.text = item.category
        binding.tvTrust.text = item.trustTags.joinToString(" · ")
        binding.tvSeller.text = "卖家：${item.sellerName}"
        binding.tvTime.text = "发布时间：${item.publishTime}"
        binding.tvDescription.text = item.description

        updateFavoriteButton(item.isFavorited)
        updateStarButton(item.isStarred)

        commentAdapter.submitList(item.comments.toList())
        binding.tvCommentHeader.text = "留言（${item.comments.size}）"
        binding.tvNoComment.visibility =
            if (item.comments.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateFavoriteButton(favorited: Boolean) {
        binding.btnFavorite.text = if (favorited) "★ 已收藏" else "☆ 收藏"
        binding.btnFavorite.setIconResource(
            if (favorited) R.drawable.ic_star_filled else R.drawable.ic_star_outline
        )
    }

    private fun toggleFavorite() {
        val result = MarketRepository.toggleFavorite(itemId)
        if (result == true) {
            Toast.makeText(this, "已加入收藏", Toast.LENGTH_SHORT).show()
        } else if (result == false) {
            Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 创新点一：盯盘（桌面小组件联动）
     * 状态变化后立即把最新盯盘商品推送到桌面小组件
     */
    private fun toggleStar() {
        val result = MarketRepository.toggleStar(itemId)
        if (result == true) {
            Toast.makeText(this, "已设为盯盘，去桌面看小组件吧", Toast.LENGTH_LONG).show()
        } else if (result == false) {
            Toast.makeText(this, "已取消盯盘", Toast.LENGTH_SHORT).show()
        }
        com.tangzhen.campusmarket.widget.StarredWidgetProvider.pushUpdate(this)
        render()
    }

    private fun updateStarButton(starred: Boolean) {
        binding.btnStar.text = if (starred) "📌 盯盘中 · 已钉在桌面" else "📌 盯盘到桌面"
        binding.btnStar.setIconResource(
            if (starred) R.drawable.ic_star_filled else R.drawable.ic_pin
        )
    }
}
