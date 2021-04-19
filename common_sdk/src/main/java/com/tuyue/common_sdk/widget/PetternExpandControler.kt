package com.tuyue.common_sdk.widget

import RandomColorBg
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.divyanshu.draw.widget.DrawView
import com.tuyue.common_sdk.R
import com.tuyue.common_sdk.activity.ImagePetternActivity
import com.tuyue.common_sdk.activity.PetternModel
import com.tuyue.common_sdk.adapter.PetternAdapter
import com.tuyue.common_sdk.helper.GPUImageHelper
import com.tuyue.common_sdk.helper.PetternAssetsHelper
import com.tuyue.common_sdk.helper.PetternDataHelper
import com.tuyue.common_sdk.image_edit.PetternType
import com.tuyue.common_sdk.image_edit.UndoRedoHelperImpl
import com.tuyue.common_sdk.image_edit.provider.BaseEditorProvider
import com.tuyue.common_sdk.item_decoration.LinearListItemDecoration
import com.tuyue.common_sdk.model.FrameAssetsModel
import com.tuyue.common_sdk.model.StyleTransferAssetsModel
import com.tuyue.common_sdk.model.TextureAssetsModel
import com.tuyue.common_sdk.tflite.StyleTransferModelExecutor
import com.tuyue.common_sdk.tflite.model.ModelExecutionResult
import com.tuyue.common_sdk.tools.BitmapUtil
import com.tuyue.common_sdk.tools.GPUImageFilterTools
import com.tuyue.core.resbody.FrameResBody
import com.tuyue.core.resbody.TextureResBody
import com.tuyue.core.util.CheckUtils
import com.tuyue.core.viewmodel.PetternViewModel
import com.xinlan.imageeditlibrary.editimage.utils.BitmapUtils
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageAlphaBlendFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup
import kotlinx.android.synthetic.main.view_pettern_controler.view.*
import kotlinx.coroutines.*


/**
 * create by guojian
 * Date: 2020/12/14
 * 处理模版图像的控制器
 */
class PetternExpandControler(context: Context, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs) {

    /**
     * GPU渲染组件
     */
    private var mGpuImageLayout: GPUImageLayout = GPUImageLayout(context)
    private var mFilterAdjuster: GPUImageFilterTools.FilterAdjuster? = null
    private val mGpuImageFilterGroup: UndoRedoHelperImpl = UndoRedoHelperImpl()

    /**
     * 功能适配器
     */
    private val mAdapter = PetternExpandAdapter()

    /**
     * 模版适配器
     */
    private val mNormalAdapter = PetternAdapter()
    private var mPetternData = mutableListOf<PetternModel>()

    /**
     * 控制器当前模式
     */
    private var mPetternType = PetternType.NORMAL
    private var mLastPetternType = PetternType.NORMAL

    /**
     * 编辑裁剪一级数据
     */
    private var mCropData: MutableList<CropNode> = mutableListOf()

    /**
     * 滤镜一级数据
     */
    private var mFilterData: MutableList<SecondNode> = mutableListOf()

    /**
     * 调整一级数据
     */
    private var mAdjustData: MutableList<SecondNode> = mutableListOf()

    /**
     * 纹理一级数据
     */
    private var mTextureData: MutableList<SecondNode> = mutableListOf()

    /**
     * 焦点数据
     */
    private var mFocusData: MutableList<SecondNode> = mutableListOf()

    /**
     * 风格迁移一级数据
     */
    private var mStyleTransferData: MutableList<SecondNode> = mutableListOf()

    /**
     * 边框一级数据
     */
    private var mFrameData: MutableList<SecondNode> = mutableListOf()

    /**
     * 裁剪框
     */
    private var mCropImageLayout: CropImageLayout? = null

    /**
     * 边框组件
     */
    private var mImageFrameView: ImageFrameView? = null

    /**
     * 涂鸦组件
     */
    private var mDoodleView: DrawView? = null

    /**
     * 原图
     */
    private var mUriPath: String? = null

    private var mControlerStateLisener: OnContralerStateLisener? = null

    private lateinit var mStyleTransferModelExecutor: StyleTransferModelExecutor

    private var mCurrentGpuimageFilter: GPUImageFilter? = null
    //编辑状态
    private var isEdit = false

    init {

        initView()

        initFirstLevelData()
        initCropList()
        initFilterList()
        initAdjustList()
        initFocusList()
        initStyleTransferList()

        initEvent()
    }

    private fun initFocusList() {
        mFocusData = PetternDataHelper.initFocusData(context)
    }

    /**
     * 初始化边框数据
     */
    private fun initFrameList() {
        mFrameData.clear()
        mFrameData.add(FrameNode("无边框", null, R.drawable.ic_no_frame, PetternType.FRAME, null))
        val mFrameList = MutableLiveData<List<FrameResBody>>()
        val netViewModel =
            ViewModelProvider(context as ImagePetternActivity).get(PetternViewModel::class.java)
        netViewModel.getFrameList(mFrameList)
        mFrameList.observe(context as ImagePetternActivity, Observer { list ->
            var assets: MutableList<FrameAssetsModel>?
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    list?.forEach {
                        val assetsModel = FrameAssetsModel(PetternType.FRAME)
                        assetsModel.imageUrl = it.frameIcon
                        assetsModel.frameId = it.frameID
                        assetsModel.frameZipMd5 = it.frameZipMd5
                        assetsModel.frameZipUrl = it.frameZip
                        mFrameData.add(assetsModel)
                    }

                    assets = PetternAssetsHelper(context).parseFrameAssets()
                    assets?.let { list ->
                        list.forEach { asset ->
                            mFrameData.forEach {
                                if (it is FrameAssetsModel && asset.left.contains(it.frameId.toString())) {
                                    it.left = asset.left
                                    it.right = asset.right
                                    it.up = asset.up
                                    it.down = asset.down
                                    it.up_left = asset.up_left
                                    it.up_right = asset.up_right
                                    it.down_left = asset.down_left
                                    it.down_right = asset.down_right
                                    it.frameZip = asset.frameZip
                                }
                            }
                        }
                    }

                }
                withContext(Dispatchers.Main) {
                    mAdapter.setNewInstance(mFrameData as MutableList<BaseNode>)
                }
            }
        })
    }

    /**
     * 初始化纹理数据
     */
    private fun initTextureList() {
        mTextureData.clear()
        mTextureData.add(SecondNode("无", null, null, PetternType.TEXTURE))
        val netViewModel =
            ViewModelProvider(context as ImagePetternActivity).get(PetternViewModel::class.java)
        val mTextureList = MutableLiveData<List<TextureResBody>>()
        netViewModel.getTextureList(mTextureList)
        mTextureList.observe(context as ImagePetternActivity, Observer { list ->
            var assets: MutableList<TextureAssetsModel>?
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    list?.forEach {
                        val assetsModel = TextureAssetsModel(PetternType.TEXTURE)
                        assetsModel.textureTitle = it.textureTitle
                        assetsModel.iconUrl = it.textureIcon
                        assetsModel.textureId = it.textureID
                        assetsModel.textureZip = it.textureZip
                        assetsModel.textureZipMd5 = it.textureZipMd5
                        mTextureData.add(assetsModel)
                    }
                    assets = PetternAssetsHelper(context).parseTextureAssets()
                    assets?.let { list ->
                        list.forEach { asset ->
                            mTextureData.forEach {
                                if (it is TextureAssetsModel && asset.path.contains(it.textureId.toString())) {
                                    it.path = asset.path
                                }
                            }
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    mAdapter.setNewInstance(mTextureData as MutableList<BaseNode>)
                }
            }
        })
    }

    /**
     * 初始化风格迁移数据
     */
    private fun initStyleTransferList() {
        GlobalScope.launch {
            withContext(Dispatchers.IO){
                mStyleTransferModelExecutor = StyleTransferModelExecutor(context, false)
            }
        }
        mStyleTransferData.clear()
        mStyleTransferData =
            PetternAssetsHelper(context).parseStyleTransferAssets() as MutableList<SecondNode>
    }

    /**
     * 调整数据初始化
     */
    private fun initAdjustList() {
        mAdjustData = PetternDataHelper.initAdjustData(context)
    }

    /**
     * 编辑数据初始化
     */
    private fun initCropList() {
        mCropData = PetternDataHelper.initCropData()
    }

    /**
     * 滤镜数据初始化
     */
    private fun initFilterList() {
        mFilterData = PetternDataHelper.initFilterData_v2(context)
    }

    /**
     * 一级node数据
     */
    private fun initFirstLevelData() {
        mPetternData = PetternDataHelper.initFirstLevelData()
        mNormalAdapter.setNewInstance(mPetternData)
    }

    private fun initEvent() {
        //选择模式
        mNormalAdapter.setOnItemClickListener { _, _, position ->
            mPetternType = mPetternData[position].petternType
            tv_center_title.text = mPetternData[position].title

            checkUndoRedoVisible(false)
            checkUpdateButton(true)
            expand_header.visibility =
                if (mPetternType == PetternType.EDIT) View.VISIBLE else View.GONE

            //还原上一次记录选中的位置
            mGpuImageFilterGroup.getLastPositionByPetternType(mPetternType)?.let {
                mAdapter.setSecondPosition(it.position.plus(1))
                if(it.hasProgress){
                    tone_seekbar.visibility = View.VISIBLE
                }
            }?:let {
                mAdapter.setSecondPosition(0)
            }

            when (mPetternType) {
                PetternType.EDIT -> {
                    rv_controler.adapter = mAdapter
                    mAdapter.setList(mCropData)
                    mCropImageLayout?.setImageBitmap(mGpuImageLayout.capture())
                    onClickEdit(0)
                    mAdapter.setSecondPosition(1)
                }
                PetternType.FILTER -> {
                    rv_controler.adapter = mAdapter
                    mAdapter.setList(mFilterData)
                }
                PetternType.ADJUST -> {
                    rv_controler.adapter = mAdapter
                    mAdapter.setList(mAdjustData)
                }
                PetternType.TEXTURE -> {
                    initTextureList()
                    rv_controler.adapter = mAdapter
                    mAdapter.setList(mTextureData)
                }
                PetternType.FRAME -> {
                    initFrameList()
                    rv_controler.adapter = mAdapter
                    mAdapter.setList(mFrameData)
                    onClickFrame()
                }
                PetternType.DOODLE -> {
                    doodle_controler.init()
                    mDoodleView?.visibility = View.VISIBLE
                    mDoodleView?.setBitmap(mGpuImageLayout.capture())
                    mGpuImageLayout.visibility = View.GONE
                }
                PetternType.FOCUS -> {
                    rv_controler.adapter = mAdapter
                    mAdapter.setList(mFocusData)
                }
                PetternType.STYLE_TRANSFER -> {
                    rv_controler.adapter = mAdapter
                    mAdapter.setList(mStyleTransferData)
                }
            }

            mLastPetternType = mPetternType
        }
        //一级事件处理
        mAdapter.addLevelItemCallback(object : FirstLevelClickCallback {
            override fun onLevelClick(view: View, data: BaseNode, position: Int) {
                when (mPetternType) {
                    PetternType.FRAME -> {
                        if (data is FirstNode) {
                            data.icon?.let {
                                mImageFrameView?.setFrame(it)
                            }
                        }
                    }
                }
            }
        })
        //二级Node添加事件
        mAdapter.addNodeItemCallback(object : NodeItemClickCallback {
            override fun onNodeItemClick(
                lastView: View?,
                currentView: View,
                data: BaseNode,
                position: Int,
                extra: Any?
            ) {
                extra?.let {
                    if (it is GPUImageFilter) {
                        onSelectFilter(it, position)
                        mCurrentGpuimageFilter = it
                    }
                } ?: let {
                    tone_seekbar.visibility = View.GONE
                }
                isEdit = true
                when (mPetternType) {
                    PetternType.EDIT -> {
                        if (position < 0) {
                            mUriPath?.let {
                                mCropImageLayout?.setImageBitmap(it)
                                onClickEdit(0)
                            }
                        } else {
                            onClickEdit(position)
                        }
                    }
                    PetternType.FILTER, PetternType.TEXTURE, PetternType.ADJUST -> {
                        //重置滤镜
                        if (position < 0) {
                            mGpuImageFilterGroup.clearFilterByType(mPetternType)
                            renderFilter()
                        }
                    }
                    PetternType.FRAME -> {
                        mImageFrameView?.let { image_frame_view ->
                            mFrameData[position.plus(1)].let {
                                when (it) {
                                    is FrameResNode -> {
                                        image_frame_view.setFrameResouce(it)
                                    }
                                    is FrameAssetsModel -> {
                                        if (CheckUtils.isEmpty(it.left)) {
                                            updateFrameItem(position)
                                            if(CheckUtils.isEmpty(it.left)){
                                                //手动下载资源
                                                val loadingView = currentView.findViewById<DownloadItem>(R.id.download_item) as DownloadItem
                                                GPUImageHelper.updateFrame(mAdapter, position, context, it, mImageFrameView, loadingView)
                                            }else {
                                                image_frame_view.setFrameAssets(
                                                    mFrameData[position.plus(
                                                        1
                                                    )] as FrameAssetsModel
                                                )
                                            }
                                            return@let
                                        }
                                        image_frame_view.setFrameAssets(it)
                                    }
                                    else -> {
                                        image_frame_view.setFrameResouce(null)
                                    }
                                }
                            }
                        }
                    }
                    PetternType.STYLE_TRANSFER -> {
                        (context as ImagePetternActivity).startLoading()

                        GlobalScope.launch {
                            var result : ModelExecutionResult? = null
                            val model = mStyleTransferData[position.plus(1)]
                            if(model is StyleTransferAssetsModel) {
                                withContext(Dispatchers.IO) {
                                    mStyleTransferModelExecutor.close()
                                    mStyleTransferModelExecutor =
                                        StyleTransferModelExecutor(context, false)
                                    result = mStyleTransferModelExecutor.execute(
                                        mGpuImageLayout.capture(),
                                        model.path,
                                        context
                                    )
                                }
                                withContext(Dispatchers.Main) {
                                    (context as ImagePetternActivity).stopLoading()
                                    result?.let {
                                        mGpuImageLayout.styleTransfer(it.styledImage)
                                    }
                                }
                            }
                        }
                    }
                }

                onLevelSelect(currentView, lastView, data as SecondNode)
            }
        })
        tone_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                mFilterAdjuster?.let {
                    it.adjust(progress)
                    mGpuImageLayout.requestRender()
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                mCurrentGpuimageFilter?.let {
                    mGpuImageFilterGroup.recordProgress(it, tone_seekbar.progress)
                }
                isEdit = true
            }
        })
        tv_finish.setOnClickListener { saveSnapshot() }
        tv_update.setOnClickListener {
            when (mPetternType) {
                PetternType.EDIT -> {
                    mCropImageLayout?.getCropBitmap()?.let {
                        mGpuImageFilterGroup.recordFilterState(mPetternType, it)
                        mGpuImageLayout.mGPUImageView.gpuImage.deleteImage()
                        mGpuImageLayout.setImageResetMatrix(it)
                        renderFilter()
                    }
                }
                PetternType.FRAME -> {
                    mGpuImageLayout.postFrame(
                        mImageFrameView?.result(),
                        mImageFrameView?.getFrameOffset()
                    )
                }
                PetternType.DOODLE -> {
                    mDoodleView?.let {
                        // 撤销队列中的滤镜已输出修改，替换原始数据
                        mGpuImageFilterGroup.updateLastRecord(GPUImageFilterGroup(), mGpuImageLayout.mGPUImageView.capture())
                        mGpuImageLayout.mGPUImageView.gpuImage.deleteImage()
                        val bitmap = doodle_controler.outputBitmap()
                        mGpuImageLayout.setImageResetMatrix(bitmap)
                        //原图已修改，在滤镜输出后再渲染，效果就错了
                        mGpuImageLayout.clearFilter()
                        mGpuImageFilterGroup.clearAllFilter()
                        mGpuImageFilterGroup.recordFilterState(mPetternType, bitmap)
                    }
                }
                PetternType.STYLE_TRANSFER -> {
                    mGpuImageLayout.confirmStyleTransfer()
                }
                else -> {
                    if(isEdit) {
                        mGpuImageFilterGroup.recordFilterState(mPetternType)
                    }
                }
            }
            onBackInit()
            resetUndoRedoState()

            isEdit = false
        }
        //返回操作
        iv_close.setOnClickListener {
            exit()
        }
        //撤销
        iv_undo.setOnClickListener {
            tone_seekbar.visibility = View.GONE
            mGpuImageFilterGroup.clearAllFilter()
            val undeRedoModel = mGpuImageFilterGroup.resetLastFilterState()
            when(undeRedoModel.type){
                PetternType.NORMAL -> {
                    mGpuImageLayout.mGPUImageView.gpuImage.deleteImage()
                    val bitmap = BitmapUtils.parseBitmapFromUri(context, mUriPath)
                    mGpuImageLayout.setImageResetMatrix(bitmap)

                }
                else -> {
                    undeRedoModel.bitmap?.let {
                        mGpuImageLayout.mGPUImageView.gpuImage.deleteImage()
                        mGpuImageLayout.setImageResetMatrix(it)
                    }
                }
            }
            val gpuImageFilter = if(undeRedoModel.filter.filters.size==0) GPUImageFilter() else undeRedoModel.filter
            mGpuImageLayout.mGPUImageView.filter = gpuImageFilter
            mGpuImageLayout.requestRender()

            resetUndoRedoState()
        }
        //重置撤销
        iv_redo.setOnClickListener {
            tone_seekbar.visibility = View.GONE
            val undeRedoModel = mGpuImageFilterGroup.redoResetLastFilterState()
            undeRedoModel.bitmap?.let {
                mGpuImageLayout.mGPUImageView.gpuImage.deleteImage()
                mGpuImageLayout.setImageResetMatrix(it)
            }
            val gpuImageFilter = if(undeRedoModel.filter.filters.size==0) GPUImageFilter() else undeRedoModel.filter
            mGpuImageLayout.mGPUImageView.filter = gpuImageFilter
            mGpuImageLayout.requestRender()

            resetUndoRedoState()
        }
        //逆时针旋转90度
        iv_crop_rotate.setOnClickListener {
            mCropImageLayout?.cropRotate()
        }
        //水平翻转
        iv_crop_overturn.setOnClickListener {
            mCropImageLayout?.overturnBitmap()
        }
    }

    fun exit(){
        isEdit = false
        clearFilterByType()
        onBackInit()
        mControlerStateLisener?.cancel()
    }

    /**
     * 更新边框
     */
    private fun updateFrameItem(position: Int) {
        mFrameData[position.plus(1)].let {
            if (it is FrameAssetsModel) {
                val model = PetternAssetsHelper(context).parseFrameModel(it.frameId.toString())
                model?.let { asset ->
                    it.left = asset.left
                    it.right = asset.right
                    it.up = asset.up
                    it.down = asset.down
                    it.up_left = asset.up_left
                    it.up_right = asset.up_right
                    it.down_left = asset.down_left
                    it.down_right = asset.down_right
                    it.frameZip = asset.frameZip
                }
            }
        }
    }

    /**
     * 清除已修改的滤镜
     */
    private fun clearFilterByType() {
        if (mPetternType != PetternType.NORMAL) {
            mGpuImageFilterGroup.clearRecordPositions()
            mGpuImageFilterGroup.clearAllFilter()
            val redoUndoModel = mGpuImageFilterGroup.getLastCancelFilterGroup()
            redoUndoModel?.let {
                val filter = it.filter
                if(CheckUtils.isEmpty(filter.filters)){
                    mGpuImageLayout.mGPUImageView.filter = GPUImageFilter()
                }else{
                    mGpuImageLayout.mGPUImageView.filter = filter
                }
            }?:let {
                mGpuImageLayout.mGPUImageView.filter = GPUImageFilter()
            }
        }
        mGpuImageLayout.requestRender()
    }

    /**
     * 重制视图
     */
    private fun resetBitmap() {
        val bitmap = BitmapUtils.parseBitmapFromUri(context, mUriPath)
        mGpuImageLayout.mGPUImageView.gpuImage.deleteImage()
        mGpuImageLayout.mGPUImageView.setImage(bitmap)
    }

    /**
     * 刷新撤销重置撤销状态
     */
    private fun resetUndoRedoState() {
        iv_undo.isEnabled = mGpuImageFilterGroup.isResetFilter(mPetternType)
        iv_redo.isEnabled = mGpuImageFilterGroup.isRedoResetFilter(mPetternType)
    }

    /**
     * 显示/隐藏撤销
     */
    private fun checkUndoRedoVisible(visible: Boolean) {
        bg_undo.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        bg_redo.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    /**
     * 选中滤镜
     */
    private fun onSelectFilter(it: GPUImageFilter? = null, position: Int) {
        it?.let {
            val recordProgress = mGpuImageFilterGroup.getFilterProgress(it)?:50
            if(mGpuImageFilterGroup.isCanAddFilter(it, mPetternType)) {
                mFilterAdjuster = GPUImageFilterTools.FilterAdjuster(it)
                mGpuImageFilterGroup.addFilter(it, mPetternType, position, mFilterAdjuster!=null)
                mFilterAdjuster?.adjust(recordProgress)
                tone_seekbar.progress = recordProgress
                tone_seekbar.visibility = View.VISIBLE
                renderFilter()
            }
        } ?: let {
            resetBitmap()
            tone_seekbar.visibility = View.GONE
        }

    }

    /**
     * 渲染滤镜
     */
    private fun renderFilter() {
        val filter = mGpuImageFilterGroup.getFilterGroup()
        if (CheckUtils.isEmpty(filter.filters)) {
            mGpuImageLayout.mGPUImageView.filter = GPUImageFilter()
        } else {
            mGpuImageLayout.mGPUImageView.filter = filter
        }
        mGpuImageLayout.requestRender()
    }

    /**
     * 二级套娃选中
     */
    private fun onLevelSelect(currentView: View, lastView: View?, secondNode: SecondNode) {
        currentView.setBackgroundResource(R.drawable.bg_pettern_item_selected)
        lastView?.let { it.background = null }
    }

    /**
     * 重置底部居中选中的文字
     */
    private fun resetBottomCenterText() {
        tv_center_title.text = ""
    }

    /**
     * 返回操作
     */
    private fun onBackInit() {
        if (mPetternType == PetternType.NORMAL) {
            val dialog = AlertDialog.Builder(context)
                .setTitle("退出编辑？")
                .setMessage("如果退出，您所做的修改将被丢弃")
                .setPositiveButton("取消"
                ) { _, _ -> }
                .setNegativeButton("确认"
                ) { _, _ -> (context as Activity).finish() }.create()
            if(iv_undo.isEnabled){
                dialog.show()
            }else{
                (context as Activity).finish()
            }
        }
        checkUndoRedoVisible(true)
        rv_controler.adapter = mNormalAdapter
        mNormalAdapter.setNewInstance(mPetternData)
        mCropImageLayout?.visibility = View.GONE
        mImageFrameView?.visibility = View.GONE
        mGpuImageLayout.visibility = View.VISIBLE
        mGpuImageLayout.finish()
        tone_seekbar.visibility = View.GONE
        doodle_controler.visibility = View.GONE
        mDoodleView?.visibility = View.GONE
        checkUpdateButton(false)
        mPetternType = PetternType.NORMAL
        resetBottomCenterText()
        //隐藏裁剪头部
        expand_header.visibility = View.GONE
    }

    /**
     * 点击编辑
     */
    private fun onClickEdit(position: Int) {
        mCropImageLayout?.let { cropImageLayout ->
            cropImageLayout.visibility = View.VISIBLE
            mGpuImageLayout.visibility = View.GONE
            cropImageLayout.checkCropMode(mCropData[position.plus(1)].ratio)
        }

    }

    /**
     * 点击边框
     */
    private fun onClickFrame() {
        mImageFrameView?.let {
            it.visibility = View.VISIBLE
            mGpuImageLayout.visibility = View.GONE
            it.setPetternBitmap(mGpuImageLayout.capture())
            //每次进入边框编辑，清除缓存边框
            it.setFrame(null)
        }
    }

    private fun initView() {
        View.inflate(context, R.layout.view_pettern_controler, this)

        val manager = LinearLayoutManager(context)
        manager.orientation = LinearLayoutManager.HORIZONTAL
        rv_controler.layoutManager = manager
        rv_controler.adapter = mNormalAdapter
        rv_controler.addItemDecoration(LinearListItemDecoration("#1C1C1C", 1))

        iv_undo.isEnabled = false
        iv_redo.isEnabled = false
    }

    /**
     * 绑定渲染组件
     * @param gpuImageLayout
     */
    fun bindGpuImageView(gpuImageLayout: GPUImageLayout, uri: String? = null) {
        mGpuImageLayout = gpuImageLayout
        mUriPath = uri
        mAdapter.setImageUri(gpuImageLayout.context, uri)

    }

    /**
     * 绑定裁剪组件
     */
    fun bindCropImageLayout(cropImageLayout: CropImageLayout) {
        mCropImageLayout = cropImageLayout
        mUriPath?.let {
            mCropImageLayout?.setImageBitmap(it)
        }
    }

    /**
     * 绑定边框组件
     */
    fun bindFrameView(imageFrameView: ImageFrameView) {
        mImageFrameView = imageFrameView
    }

    /**
     * 绑定
     */
    fun bindDoodleView(doodleView: DrawView) {
        mDoodleView = doodleView
        mDoodleView?.let {
            doodle_controler.bindDoodleView(it)
        }
    }

    /**
     * 保存图片
     */
    private fun saveSnapshot() {
        val saveBitmap = BitmapUtil.convertViewToBitmap(mGpuImageLayout)
        saveBitmap?.let {
            mControlerStateLisener?.finish(it)
        }
    }

    /**
     * @param isChecked true显示更新操作，false显示完成操作
     * 切换更新按钮
     */
    private fun checkUpdateButton(isChecked: Boolean) {
        tv_update.visibility = if (isChecked) View.VISIBLE else View.INVISIBLE
        tv_finish.visibility = if (isChecked) View.INVISIBLE else View.VISIBLE
    }

    fun setControlerStateListener(l: OnContralerStateLisener) {
        mControlerStateLisener = l
    }

}

/**
 * 可扩展的适配器（套娃数据支持）
 */
class PetternExpandAdapter : BaseNodeAdapter() {

    private val mFirstProvider = FirstProvider()
    private val mSecondProvider = SecondProvider()

    init {
        addNodeProvider(mFirstProvider)
        addNodeProvider(mSecondProvider)
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        val node = data[position]
        if (node is FirstNode) {
            return 1
        } else if (node is SecondNode) {
            return 2
        }
        return -1
    }

    /**
     * 回调二级事件
     */
    fun addNodeItemCallback(callback: NodeItemClickCallback) {
        mSecondProvider.addNodeItemCallback(callback)
    }

    fun addLevelItemCallback(callback: FirstLevelClickCallback) {
        mFirstProvider.addLevelItemCallback(callback)
    }

    fun setImageUri(context: Context, uri: String?) {
        mFirstProvider.setImageUri(context, uri)
        mSecondProvider.setImageUri(context, uri)
    }

    fun setSecondPosition(position: Int){
        mSecondProvider.setLastPosition(position)
    }

}

/**
 * 一级数据提供者
 */
class FirstProvider : BaseEditorProvider() {

    private var mFirstCallback: FirstLevelClickCallback? = null

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        if (item is FirstNode) {
            helper.setText(R.id.tv_title, item.title)
            item.icon?.let {
                helper.setImageResource(R.id.iv_source, it)
            } ?: let {
                val imageView = helper.getView<ImageView>(R.id.iv_source)
                Glide.with(imageView).load(mImageBitmap).into(imageView)
            }
        }
    }

    override val itemViewType: Int
        get() = 1

    override val layoutId: Int
        get() = R.layout.item_pettern_expand_first

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        getAdapter()?.expandOrCollapse(position)
        mFirstCallback?.onLevelClick(view, data, position)
    }

    fun addLevelItemCallback(callback: FirstLevelClickCallback) {
        mFirstCallback = callback
    }

}

/**
 * 二级数据提供者
 */
class SecondProvider : BaseEditorProvider() {

    private var mCallback: NodeItemClickCallback? = null
    private var mLastView: View? = null
    private var mListPosition = 0

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        if (item is SecondNode) {
            helper.setIsRecyclable(item.type == PetternType.FILTER)
            helper.setText(R.id.tv_title, item.title)

            when (item.type) {
                PetternType.ADJUST, PetternType.EDIT, PetternType.NORMAL, PetternType.FOCUS -> {
                    item.icon?.let {
                        helper.setImageResource(R.id.iv_source, it)
                    }
                }
                PetternType.FILTER, PetternType.TEXTURE-> {
                    //滤镜类型 预览效果
                    mImageBitmap.let { image ->
                        if (item is TextureAssetsModel) {
                            val imageView = helper.getView<ImageView>(R.id.item_gpu_image)
                            var bitmap: Bitmap? = null
                            helper.setVisible(R.id.download_item, CheckUtils.isEmpty(item.path))
                            GlobalScope.launch {
                                withContext(Dispatchers.IO) {
                                    if (!TextUtils.isEmpty(item.path)) {
                                        bitmap = Glide.with(imageView).asBitmap().placeholder(RandomColorBg.colorDrawable).load(item.path)
                                            .submit().get()
                                        item.filter = GPUImageFilterTools.createBlendFilter(
                                            context,
                                            GPUImageAlphaBlendFilter::class.java,
                                            bitmap
                                        )
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    Glide.with(imageView).load(item.iconUrl).placeholder(RandomColorBg.colorDrawable).into(imageView)
                                }
                            }
                        } else {
                            GlobalScope.launch {

                                var newBitmap: Bitmap? = null
                                withContext(Dispatchers.IO) {
                                    val gpuImage = GPUImage(context)
                                    gpuImage.setImage(image)
                                    gpuImage.setFilter(item.filter)

                                    val bitmap =
                                        if (item.filter != null) gpuImage.bitmapWithFilterApplied
                                        else image

                                    bitmap?.let {
                                        newBitmap = Bitmap.createScaledBitmap(it, 100, 100, true)
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    val imageView = helper.getView<ImageView>(R.id.item_gpu_image)
                                    imageView.setImageBitmap(newBitmap)
                                }
                            }
                        }
                    }
                }
                PetternType.FRAME -> {
                    when (item) {
                        is FrameResNode -> {
                            val imageView = helper.getView<ImageView>(R.id.item_gpu_image)
                            imageView.scaleType = ImageView.ScaleType.FIT_XY
                            Glide.with(imageView).load(item.icon).placeholder(RandomColorBg.colorDrawable).into(imageView)
                            helper.setVisible(R.id.item_gpu_image, true)
                            helper.setVisible(R.id.iv_source, false)
                        }
                        is FrameAssetsModel -> {
                            val imageView = helper.getView<ImageView>(R.id.item_gpu_image)
                            imageView.scaleType = ImageView.ScaleType.FIT_XY
                            Glide.with(imageView).load(item.imageUrl).placeholder(RandomColorBg.colorDrawable).into(imageView)
                            helper.setVisible(R.id.item_gpu_image, true)
                            helper.setVisible(R.id.iv_source, false)
                            helper.setVisible(R.id.download_item, CheckUtils.isEmpty(item.left))
                        }
                        else -> {
                            val imageView = helper.getView<ImageView>(R.id.iv_source)
                            imageView.setImageResource(item.icon!!)
                            helper.setVisible(R.id.item_gpu_image, false)
                            helper.setVisible(R.id.iv_source, true)
                            helper.setVisible(R.id.download_item, false)
                        }
                    }
                }
                PetternType.STYLE_TRANSFER -> {
                    if(item is StyleTransferAssetsModel) {
                        val imageView = helper.getView<ImageView>(R.id.item_gpu_image)
                        imageView.scaleType = ImageView.ScaleType.FIT_XY
                        Glide.with(imageView).load(item.path).placeholder(RandomColorBg.colorDrawable).into(imageView)
                        helper.setVisible(R.id.item_gpu_image, true)
                        helper.setVisible(R.id.iv_source, false)
                    }
                }
            }
        }

        if(mListPosition ==helper.adapterPosition){
            helper.itemView.setBackgroundResource(R.drawable.bg_pettern_item_selected)
            mLastView = helper.itemView
        }else{
            helper.itemView.setBackgroundResource(0)
        }
    }

    override val itemViewType: Int
        get() = 2

    override val layoutId: Int
        get() = R.layout.item_pettern_expand_second

    private var mEventTime:Long = 0
    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        super.onClick(helper, view, data, position)
        val currentTime = System.currentTimeMillis()
        if (data is SecondNode && mListPosition != position) {
            //风格迁移2秒内禁止连续切换
            if(data.type == PetternType.STYLE_TRANSFER && currentTime.minus(mEventTime) < 1500){
                return
            }
            mEventTime = currentTime

            val filter = if (data is TextureNode) data.lessFilter else data.filter
            if(data is TextureAssetsModel && filter ==null ){
                val loadingView = view.findViewById<DownloadItem>(R.id.download_item)
                GPUImageHelper.updateTexture(getAdapter(), position, context, data, loadingView, object :GPUImageHelper.Observer{
                    override fun just(juster: Any) {
                        if(juster is TextureAssetsModel){
                            mCallback?.onNodeItemClick(mLastView, view, juster, position.minus(1), juster.filter)
                            mLastView = view
                            mListPosition = position
                        }
                    }
                })
            }else {
                mCallback?.onNodeItemClick(mLastView, view, data, position.minus(1), filter)
                mLastView = view
                mListPosition = position
            }

        }
    }

    fun addNodeItemCallback(callback: NodeItemClickCallback) {
        mCallback = callback
    }

    fun setLastPosition(position: Int){
        mListPosition = position
    }

}

/**
 * 一级Node数据类
 */
class FirstNode(
    private val mChildNodeList: MutableList<BaseNode>?,
    val title: String, val icon: Int? = null, val type: PetternType = PetternType.NORMAL
) : BaseExpandNode() {

    init {
        //默认不展开
        isExpanded = false
    }

    override val childNode: MutableList<BaseNode>?
        get() = mChildNodeList
}

/**
 * 裁剪二级数据类
 */
class CropNode(title: String, val ratio: Float, cropIcon: Int? = null, type: PetternType) :
    SecondNode(title, null, cropIcon, type)

/**
 * 纹理二级数据
 */
class TextureNode(
    title: String,
    filter: GPUImageFilter?,
    textureIcon: Int?,
    type: PetternType,
    val lessFilter: GPUImageFilter
) :
    SecondNode(title, filter, textureIcon, type)

class FrameNode(
    title: String,
    filter: GPUImageFilter?,
    frameIcon: Int?,
    type: PetternType,
    val frameId: Int?
) :
    SecondNode(title, filter, frameIcon, type)

class FrameResNode(
    title: String, filter: GPUImageFilter?, frameIcon: Int?, type: PetternType,
    val angleLeftTopRes: Int, val angleLeftBottomRes: Int,
    val angleRightTopRes: Int, val angleRightBottomRes: Int,
    val frameLeftRes: Int, val frameTopRes: Int, val frameRightRes: Int, val frameBottomRes: Int
) :
    SecondNode(title, filter, frameIcon, type)

/**
 * 二级Node数据类
 */
open class SecondNode(
    val title: String,
    var filter: GPUImageFilter?,
    val icon: Int? = null,
    val type: PetternType = PetternType.NORMAL
) : BaseNode() {
    //套娃数据，不需要继续嵌套返回null就可以了
    override val childNode: MutableList<BaseNode>?
        get() = null
}

/**
 * 可扩展Node事件回传
 */
interface NodeItemClickCallback {

    /**
     * @param data 数据
     * @param position 当前索引。注意：二级Node索引从1开始计算。
     * @param extra 开放一个扩展参数，用于回传滤镜
     */
    fun onNodeItemClick(
        lastView: View? = null,
        currentView: View,
        data: BaseNode,
        position: Int,
        extra: Any? = null
    )
}

interface FirstLevelClickCallback {

    fun onLevelClick(view: View, data: BaseNode, position: Int)
}

interface OnContralerStateLisener {
    fun finish(bitmap: Bitmap)
    fun cancel()
}
