
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import java.util.*

/**
 * 背景颜色随机色
 */
object RandomColorBg {
    private val colors = intArrayOf(-0x101001, -0x21002, -0x10511, -0x100402)
    private val random = Random()
    private val color: Int
        get() = colors[random.nextInt(colors.size)]

    val colorDrawable: Drawable
        get() {
            val drawable = GradientDrawable()
            drawable.setColor(color)
            return drawable
        }

    fun getColorDrawable(radius: Float): Drawable {
        val drawable = GradientDrawable()
        drawable.setColor(color)
        drawable.cornerRadius = radius
        return drawable
    }
}