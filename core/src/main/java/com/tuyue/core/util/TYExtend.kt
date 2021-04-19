
import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import com.tuyue.core.network.BaseResp

/**
 * create by guojian
 * Date: 2020/12/24
 * 扩展函数库
 */

/**
 * 重置控件宽高
 * @param width 重置宽度
 * @param height 重置高度
 * @param gravity 位置
 * 注意：父控件类型只支持 [ConstraintLayout]
 */
@SuppressLint("RtlHardcoded")
fun View.resetSize(width: Int, height: Int, gravity: Int = Gravity.CENTER) {
    val params = ConstraintLayout.LayoutParams(width, height)
    when (gravity) {
        Gravity.LEFT, Gravity.START -> {
            params.startToStart = PARENT_ID
        }
        Gravity.RIGHT, Gravity.END -> {
            params.endToEnd = PARENT_ID
        }
        Gravity.TOP -> {
            params.topToTop = PARENT_ID
        }
        Gravity.BOTTOM -> {
            params.endToEnd = PARENT_ID
        }
        else -> {
            params.topToTop = PARENT_ID
            params.bottomToBottom = PARENT_ID
            params.startToStart = PARENT_ID
            params.endToEnd = PARENT_ID
        }
    }
    this.layoutParams = params
}

/*数据解析扩展函数*/
fun <T> BaseResp<T>.dataConvert(): T {

    if (code == 200) {
        return data
    } else {
//        throw Exception(msg)
        return data
    }


}