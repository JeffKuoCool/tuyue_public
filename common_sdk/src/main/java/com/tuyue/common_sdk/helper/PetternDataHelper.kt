
package com.tuyue.common_sdk.helper

import android.content.Context
import android.graphics.PointF
import com.chad.library.adapter.base.entity.node.BaseNode
import com.tuyue.common_sdk.R
import com.tuyue.common_sdk.activity.PetternModel
import com.tuyue.common_sdk.image_edit.PetternType
import com.tuyue.common_sdk.tools.GPUImageFilterTools
import com.tuyue.common_sdk.widget.*
import jp.co.cyberagent.android.gpuimage.filter.*
import java.util.*

/**
 * create by guojian
 * Date: 2020/12/17
 * 配置模版数据帮助类
 */
object PetternDataHelper {

    fun initFilterData_v2(context: Context): MutableList<SecondNode>{
        val filterData :MutableList<SecondNode> = mutableListOf()
        filterData.add(SecondNode("无", null, null, PetternType.FILTER))
        filterData.add(SecondNode("单色", GPUImageMonochromeFilter(1.0f, floatArrayOf(0.6f, 0.45f, 0.3f, 1.0f)), null, PetternType.FILTER))
        filterData.add(SecondNode("漩涡", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.SWIRL), null, PetternType.FILTER))
        filterData.add(SecondNode("球形折射", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.SPHERE_REFRACTION), null, PetternType.FILTER))
//        filterData.add(SecondNode("反相", GPUImageColorInvertFilter(), null, PetternType.FILTER))
//        filterData.add(SecondNode("像素化", GPUImagePixelationFilter(), null, PetternType.FILTER))
        filterData.add(SecondNode("褐色（怀旧）", GPUImageSepiaToneFilter(), null, PetternType.FILTER))
//        filterData.add(SecondNode("灰度", GPUImageGrayscaleFilter(), null, PetternType.FILTER))
//        filterData.add(SecondNode("Sobel边缘检测", GPUImageThresholdEdgeDetectionFilter(), null, PetternType.FILTER))
        val convolution = GPUImage3x3ConvolutionFilter()
        convolution.setConvolutionKernel(
            floatArrayOf(
                -1.0f, 0.0f, 1.0f,
                -2.0f, 0.0f, 2.0f,
                -1.0f, 0.0f, 1.0f
            )
        )
//        filterData.add(SecondNode("3x3卷积", convolution, null, PetternType.FILTER))
        filterData.add(SecondNode("浮雕", GPUImageEmbossFilter(), null, PetternType.FILTER))
        filterData.add(SecondNode("色调分离", GPUImagePosterizeFilter(), null, PetternType.FILTER))
        val filters: MutableList<GPUImageFilter> =
            LinkedList()
        filters.add(GPUImageContrastFilter())
        filters.add(GPUImageDirectionalSobelEdgeDetectionFilter())
        filters.add(GPUImageGrayscaleFilter())
//        filterData.add(SecondNode("组合滤镜（对比度+灰度+Sobel边缘检测）", GPUImageFilterGroup(filters), null, PetternType.FILTER))
//        filterData.add(SecondNode("提亮阴影", GPUImageHighlightShadowFilter(0.0f, 1.0f), null, PetternType.FILTER))
//        filterData.add(SecondNode("不透明度", GPUImageOpacityFilter(1.0f), null, PetternType.FILTER))
        filterData.add(SecondNode("RGB", GPUImageRGBFilter(1.0f, 1.0f, 1.0f), null, PetternType.FILTER))
        filterData.add(SecondNode("白平衡", GPUImageWhiteBalanceFilter(5000.0f, 0.0f), null, PetternType.FILTER))
        val centerPoint = PointF()
        centerPoint.x = 0.5f
        centerPoint.y = 0.5f
        filterData.add(SecondNode("晕影", GPUImageVignetteFilter(centerPoint, floatArrayOf(0.0f, 0.0f, 0.0f), 0.3f, 0.75f), null, PetternType.FILTER))
        val toneCurveFilter = GPUImageToneCurveFilter()
        toneCurveFilter.setFromCurveFileInputStream(
            context.resources.openRawResource(R.raw.tone_cuver_sample)
        )
//        filterData.add(SecondNode("色调曲线", toneCurveFilter, null, PetternType.FILTER))

//        filterData.add(SecondNode("差异混合", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_DIFFERENCE), null, PetternType.FILTER))
//        filterData.add(SecondNode("源混合", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_SOURCE_OVER), null, PetternType.FILTER))
//        filterData.add(SecondNode("色彩加深混合", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_COLOR_BURN), null, PetternType.FILTER))
//        filterData.add(SecondNode("色彩减淡混合", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_COLOR_DODGE), null, PetternType.FILTER))
//        filterData.add(SecondNode("加深混合", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_DARKEN), null, PetternType.FILTER))
//        filterData.add(SecondNode("溶解", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_DISSOLVE), null, PetternType.FILTER))
//        filterData.add(SecondNode("排除混合", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_EXCLUSION), null, PetternType.FILTER))
//        filterData.add(SecondNode("强光混合", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_HARD_LIGHT), null, PetternType.FILTER))
//        filterData.add(SecondNode("减淡混合", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_LIGHTEN), null, PetternType.FILTER))
//        filterData.add(SecondNode("加法混合", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_ADD), null, PetternType.FILTER))
//        filterData.add(SecondNode("分割混合", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_DIVIDE), null, PetternType.FILTER))
//        filterData.add(SecondNode("正片叠底", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_MULTIPLY), null, PetternType.FILTER))
//        filterData.add(SecondNode("叠加", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_OVERLAY), null, PetternType.FILTER))
//        filterData.add(SecondNode("屏幕包裹", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_SCREEN), null, PetternType.FILTER))
//        filterData.add(SecondNode("透明混合", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_ALPHA), null, PetternType.FILTER))
//        filterData.add(SecondNode("颜色混合", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_COLOR), null, PetternType.FILTER))
//        filterData.add(SecondNode("色度混合", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_HUE), null, PetternType.FILTER))
//        filterData.add(SecondNode("饱和度混合", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_SATURATION), null, PetternType.FILTER))
//        filterData.add(SecondNode("明度混合", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_LUMINOSITY), null, PetternType.FILTER))
//        filterData.add(SecondNode("线性加深", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_LINEAR_BURN), null, PetternType.FILTER))
//        filterData.add(SecondNode("柔光", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_SOFT_LIGHT), null, PetternType.FILTER))
//        filterData.add(SecondNode("差值混合", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_SUBTRACT), null, PetternType.FILTER))
//        filterData.add(SecondNode("色度键混合", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_CHROMA_KEY), null, PetternType.FILTER))
//        filterData.add(SecondNode("正常混合", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BLEND_NORMAL), null, PetternType.FILTER))
//
//        filterData.add(SecondNode("颜色查找表（Amatorka）", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.LOOKUP_AMATORKA), null, PetternType.FILTER))
//        filterData.add(SecondNode("交叉线阴影", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.CROSSHATCH), null, PetternType.FILTER))
//        filterData.add(SecondNode("CGA色彩滤镜", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.CGA_COLORSPACE), null, PetternType.FILTER))
//        filterData.add(SecondNode("素描", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.SKETCH), null, PetternType.FILTER))
        filterData.add(SecondNode("卡通", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.TOON), null, PetternType.FILTER))
//        filterData.add(SecondNode("平滑卡通", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.SMOOTH_TOON), null, PetternType.FILTER))
//        filterData.add(SecondNode("点染", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.HALFTONE), null, PetternType.FILTER))
        filterData.add(SecondNode("鱼眼", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BULGE_DISTORTION), null, PetternType.FILTER))
//        filterData.add(SecondNode("水晶球", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.GLASS_SPHERE), null, PetternType.FILTER))
        filterData.add(SecondNode("拉普拉斯变换", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.LAPLACIAN), null, PetternType.FILTER))
//        filterData.add(SecondNode("非最大抑制", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.NON_MAXIMUM_SUPPRESSION), null, PetternType.FILTER))
//        filterData.add(SecondNode("假色", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.FALSE_COLOR), null, PetternType.FILTER))
        filterData.add(SecondNode("颜色平衡", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.COLOR_BALANCE), null, PetternType.FILTER))
        filterData.add(SecondNode("色阶", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.LEVELS_FILTER_MIN), null, PetternType.FILTER))
//        filterData.add(SecondNode("形状变化（2D）", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.TRANSFORM2D), null, PetternType.FILTER))

//        filterData.add(SecondNode("高斯模糊", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.GAUSSIAN_BLUR), null, PetternType.FILTER))
//        filterData.add(SecondNode("盒状模糊", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BOX_BLUR), null, PetternType.FILTER))
//        filterData.add(SecondNode("扩展边缘模糊，变黑白", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.DILATION), null, PetternType.FILTER))
//        filterData.add(SecondNode("Kuwahara滤波", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.KUWAHARA), null, PetternType.FILTER))
//        filterData.add(SecondNode("RGB扩展边缘模糊，有色彩", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.RGB_DILATION), null, PetternType.FILTER))
        filterData.add(SecondNode("薄雾", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.HAZE), null, PetternType.FILTER))
//        filterData.add(SecondNode("弱像素包含", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.WEAK_PIXEL_INCLUSION), null, PetternType.FILTER))
//        filterData.add(SecondNode("双边模糊", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BILATERAL_BLUR), null, PetternType.FILTER))


        return filterData
    }

    fun initFocusData(context: Context):MutableList<SecondNode>{
        val mFocusData = mutableListOf<SecondNode>()
        mFocusData.add(SecondNode("无", null, R.drawable.ic_jd_no, PetternType.FOCUS))
        mFocusData.add(SecondNode("高斯模糊", GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.GAUSSIAN_BLUR), R.drawable.ic_jd_gs, PetternType.FOCUS))
        return mFocusData
    }

    /**
     * 初始化调整数据
     */
    fun initAdjustData(context: Context): MutableList<SecondNode>{
        val mAdjustData = arrayListOf<SecondNode>()
        mAdjustData.add(SecondNode("重置", null, R.drawable.ic_reset))
        mAdjustData.add(
                SecondNode(
                        "亮度",
                        GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.BRIGHTNESS), R.drawable.ic_adjust_brightness,
                        PetternType.ADJUST
                )
        )
        mAdjustData.add(
                SecondNode(
                        "对比度",
                        GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.CONTRAST), R.drawable.ic_adjust_contrast,
                        PetternType.ADJUST
                )
        )
        mAdjustData.add(
                SecondNode(
                        "饱和度",
                        GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.SATURATION), R.drawable.ic_adjust_saturation,
                        PetternType.ADJUST
                )
        )
        mAdjustData.add(
                SecondNode(
                        "阴影",
                        GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.HIGHLIGHT_SHADOW), R.drawable.ic_adjust_shadow,
                        PetternType.ADJUST
                )
        )
        mAdjustData.add(
                SecondNode(
                        "曝光",
                        GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.EXPOSURE), R.drawable.ic_adjust_exposure,
                        PetternType.ADJUST
                )
        )
        mAdjustData.add(
                SecondNode(
                        "伽马",
                        GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.GAMMA), R.drawable.ic_adjust_gamma,
                        PetternType.ADJUST
                )
        )
        mAdjustData.add(
                SecondNode(
                        "色温",
                        GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.HUE), R.drawable.ic_adjust_colorful,
                        PetternType.ADJUST
                )
        )
        mAdjustData.add(
                SecondNode(
                        "锐度",
                        GPUImageFilterTools.createFilterForType(context, GPUImageFilterTools.FilterType.SHARPEN), R.drawable.ic_adjust_sharpness,
                        PetternType.ADJUST
                )
        )
        return mAdjustData
    }

    /**
     * 初始化控制器一级数据
     */
    fun initFirstLevelData(): MutableList<PetternModel>{
        val levelList = mutableListOf<PetternModel>()
        levelList.add(PetternModel(R.drawable.ic_pettern_crop, "裁剪", PetternType.EDIT))
        levelList.add(PetternModel(R.drawable.ic_pettern_filter, "滤镜", PetternType.FILTER))
        levelList.add(PetternModel(R.drawable.ic_pettern_adjust, "调整", PetternType.ADJUST))
        levelList.add(PetternModel(R.drawable.ic_pettern_frame, "边框", PetternType.FRAME))
        levelList.add(PetternModel(R.drawable.ic_pettern_texture, "纹理", PetternType.TEXTURE))
        levelList.add(PetternModel(R.drawable.ic_pettern_paint, "笔刷", PetternType.DOODLE))
        levelList.add(PetternModel(R.drawable.ic_pettern_focus, "焦点", PetternType.FOCUS))
        levelList.add(PetternModel(R.drawable.ic_ailj, "AI滤镜", PetternType.STYLE_TRANSFER))
        return levelList
    }

    /**
     * 初始化裁剪数据
     */
    fun initCropData(): MutableList<CropNode>{
        val cropData: MutableList<CropNode> = mutableListOf()
        val cropArrays = arrayOf("重置", "自由", "正方形", "16:9", "9:16", "4:3", "3:4", "3:2", "2:3")
        val cropRatios = arrayOf(-1f, -1f, 1f, 16 / 9f, 9/16f, 4 / 3f, 3/4f, 3 / 2f, 2/3f)
        val cropIcons = arrayOf(R.drawable.ic_reset, R.drawable.ic_crop_ratio_freedom, R.drawable.ic_crop_ratio_1_1, R.drawable.ic_crop_ratio_16_9,R.drawable.ic_crop_ratio_9_16,
            R.drawable.ic_crop_ratio_4_3, R.drawable.ic_crop_ratio_3_4, R.drawable.ic_crop_ratio_3_2, R.drawable.ic_crop_ratio_2_3)
        for (i in 0..cropArrays.lastIndex) {
            cropData.add(CropNode(cropArrays[i], cropRatios[i], cropIcons[i], PetternType.EDIT))
        }
        return cropData
    }
}