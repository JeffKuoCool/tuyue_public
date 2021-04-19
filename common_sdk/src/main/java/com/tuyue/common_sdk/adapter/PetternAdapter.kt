package com.tuyue.common_sdk.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tuyue.common_sdk.R
import com.tuyue.common_sdk.activity.PetternModel

/**
 * create by guojian
 * Date: 2020/12/11
 * 模版的适配器
 */
class PetternAdapter() : BaseQuickAdapter<PetternModel, BaseViewHolder>(R.layout.item_pettern) {

    override fun convert(holder: BaseViewHolder, item: PetternModel) {
        holder.setText(R.id.tv_pettern_title, item.title)
        item.icon?.let {
            holder.setImageResource(R.id.iv_pettern_icon, it)
        }
    }
}