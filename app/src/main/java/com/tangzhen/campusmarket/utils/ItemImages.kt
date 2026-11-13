package com.tangzhen.campusmarket.utils

import com.tangzhen.campusmarket.R
import com.tangzhen.campusmarket.model.Item

/**
 * 本地商品的分类配图
 *
 * 云端商品自带真实网络图片（Glide 下载，满足「网络 + 图片处理」技术点）；
 * 本地内置示例和用户发布的商品没有图源，统一使用柔和的分类示意图，
 * 保证列表里每一张卡片都有图可看，不会出现灰底占位框。
 */
object ItemImages {

    fun resFor(item: Item): Int = when {
        // 内置示例第 6 件是自行车，分类虽是"其他"，给它专属配图
        item.id == 6L -> R.drawable.img_bicycle
        item.category == "教材书籍" -> R.drawable.img_cat_textbook
        item.category == "数码产品" -> R.drawable.img_cat_digital
        item.category == "生活电器" -> R.drawable.img_cat_appliance
        item.category == "运动器材" -> R.drawable.img_cat_sports
        item.category == "衣物用品" -> R.drawable.img_cat_clothes
        else -> R.drawable.img_cat_other
    }
}
