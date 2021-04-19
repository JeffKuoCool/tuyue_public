package com.tuyue.common_sdk.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.tuyue.common_sdk.R
import kotlinx.android.synthetic.main.item_dowload.view.*
import kotlinx.coroutines.*


/**
 * Create by guojian on 2021/3/31
 * Describe：
 **/
class DownloadItem(context: Context, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs) {

    init {
        inflate(context, R.layout.item_dowload, this)
    }

    fun loadingState(state: Boolean){
        GlobalScope.launch {
            withContext(Dispatchers.Main){
                loading_view.visibility = if(state) View.VISIBLE else View.INVISIBLE
                icon.visibility = if(state) View.VISIBLE else View.INVISIBLE
            }
        }

    }

    fun downloadFinish(boolean: Boolean = true){
        GlobalScope.launch {
            withContext(Dispatchers.Main){
                this@DownloadItem.visibility = View.GONE
                icon.visibility = View.GONE
            }
        }
    }
}