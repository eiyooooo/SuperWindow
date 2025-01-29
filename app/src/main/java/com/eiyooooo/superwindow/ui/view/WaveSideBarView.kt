package com.eiyooooo.superwindow.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import com.eiyooooo.superwindow.R
import com.eiyooooo.superwindow.util.dp2px
import com.eiyooooo.superwindow.util.sp2px
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class WaveSideBarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val ANGLE = Math.PI * 45 / 180
        private const val ANGLE_R = Math.PI * 90 / 180
    }

    private var letterChangeListener: ((Char) -> Unit)? = null

    private val mLetters: List<String> by lazy { listOf<String>(*context.resources.getStringArray(R.array.side_bar_letters)) }

    private var mChoose = -1
    private var oldChoose = 0
    private var newChoose = 0

    private val mSelectingLetter: MutableStateFlow<Char?> by lazy { MutableStateFlow(null) }
    val selectingLetter: StateFlow<Char?> = mSelectingLetter

    private val mRatioAnimator: ValueAnimator = ValueAnimator()

    private val mLettersPaint = Paint()
    private val mFramePaint = Paint()
    private val mTextPaint = Paint()
    private val mWavePaint = Paint()

    private var mMaxTextSize = 0f
    private var mLargeTextSize = 0f

    private var mOnBackgroundColor = 0
    private var mBackgroundColor = 0
    private var mWaveColor = 0
    private var mChooseTextColor = 0

    private var mWidth = 0
    private var mHeight = 0
    private var mItemHeight = 0
    private var mTextSize = 0f

    private val mWavePath = Path()
    private val mBallPath = Path()

    private var mCenterY = 0
    private var mRadius = 0
    private var mBallRadius = 0

    private var mRatio = 0f
    private var mPosX = 0f
    private var mChoosePosY = 0f
    private var mBallCentreX = 0f

    private var mFirstLetterPosY = 0f
    private var mLastLetterPosY = 0f

    init {
        mOnBackgroundColor = Color.parseColor("#969696")
        mBackgroundColor = Color.parseColor("#F9F9F9")
        mWaveColor = Color.parseColor("#1A73E8")
        mChooseTextColor = Color.parseColor("#FFFFFF")
        mMaxTextSize = context.sp2px(10).toFloat()
        mLargeTextSize = context.sp2px(32).toFloat()
        mRadius = context.dp2px(20)
        mBallRadius = context.dp2px(24)
        if (attrs != null) {
            val a = getContext().obtainStyledAttributes(attrs, R.styleable.WaveSideBarView)
            mOnBackgroundColor = a.getColor(R.styleable.WaveSideBarView_sidebarOnBackgroundColor, mOnBackgroundColor)
            mBackgroundColor = a.getColor(R.styleable.WaveSideBarView_sidebarBackgroundColor, mBackgroundColor)
            mWaveColor = a.getColor(R.styleable.WaveSideBarView_sidebarWaveColor, mWaveColor)
            mChooseTextColor = a.getColor(R.styleable.WaveSideBarView_sidebarChooseTextColor, mChooseTextColor)
            mMaxTextSize = a.getDimension(R.styleable.WaveSideBarView_sidebarMaxTextSize, mMaxTextSize)
            mLargeTextSize = a.getDimension(R.styleable.WaveSideBarView_sidebarLargeTextSize, mLargeTextSize)
            mRadius = a.getDimension(R.styleable.WaveSideBarView_sidebarRadius, mRadius.toFloat()).toInt()
            mBallRadius = a.getDimension(R.styleable.WaveSideBarView_sidebarBallRadius, mBallRadius.toFloat()).toInt()
            a.recycle()
        }
        mWavePaint.apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = mWaveColor
        }
        mTextPaint.apply {
            isAntiAlias = true
            color = mChooseTextColor
            style = Paint.Style.FILL
            textSize = mLargeTextSize
            textAlign = Paint.Align.CENTER
        }
        isHapticFeedbackEnabled = true
    }

    fun setLetterChangeListener(listener: (Char) -> Unit) {
        letterChangeListener = listener
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        oldChoose = mChoose
        newChoose = (event.y / mHeight * mLetters.size).toInt()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (event.x < mWidth - 2 * mRadius) {
                    return false
                }
                mRatioAnimator.cancel()
                mRatio = 1.0f
                mCenterY = event.y.toInt().coerceIn(mFirstLetterPosY.toInt(), mLastLetterPosY.toInt())
                if (oldChoose != newChoose && newChoose in mLetters.indices) {
                    mChoose = newChoose
                    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    letterChangeListener?.invoke(mLetters[newChoose][0])
                    mSelectingLetter.update { mLetters[newChoose][0] }
                }
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                mCenterY = event.y.toInt().coerceIn(mFirstLetterPosY.toInt(), mLastLetterPosY.toInt())
                if (oldChoose != newChoose && newChoose in mLetters.indices) {
                    mChoose = newChoose
                    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    letterChangeListener?.invoke(mLetters[newChoose][0])
                    mSelectingLetter.update { mLetters[newChoose][0] }
                }
                invalidate()
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                mRatioAnimator.cancel()
                mRatioAnimator.setFloatValues(mRatio, 0f)
                mRatioAnimator.addUpdateListener {
                    mRatio = it.animatedValue as Float
                    invalidate()
                }
                mRatioAnimator.start()
                mChoose = -1
                mSelectingLetter.update { null }
            }
        }
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mHeight = MeasureSpec.getSize(heightMeasureSpec)
        mWidth = MeasureSpec.getSize(widthMeasureSpec)
        mItemHeight = mHeight / mLetters.size
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        initLetterPaint()
        drawFrame(canvas)
        drawLetters(canvas)
        drawWavePath(canvas)
        drawBallPath(canvas)
        drawChooseText(canvas)
    }

    private fun initLetterPaint() {
        mLettersPaint.reset()
        mLettersPaint.color = mOnBackgroundColor
        mLettersPaint.isAntiAlias = true
        mLettersPaint.textAlign = Paint.Align.CENTER
        mTextSize = mLettersPaint.scaleTextSizeToFitHeight(mHeight / mLetters.size, mMaxTextSize)
        mPosX = mWidth - 1.6f * mTextSize
    }

    private fun Paint.scaleTextSizeToFitHeight(targetHeight: Int, maxTextSize: Float? = null): Float {
        textSize = 100f
        val metrics = fontMetrics
        val newSize = 100f * targetHeight / (metrics.bottom - metrics.top)
        val finalTextSize = maxTextSize?.coerceAtMost(newSize) ?: newSize
        textSize = finalTextSize
        return finalTextSize
    }

    private fun drawFrame(canvas: Canvas) {
        val rectF = RectF()
        rectF.left = mPosX - mTextSize
        rectF.right = mPosX + mTextSize
        rectF.top = 0f
        rectF.bottom = mHeight.toFloat()

        mFramePaint.reset()
        mFramePaint.style = Paint.Style.FILL
        mFramePaint.color = mBackgroundColor
        mFramePaint.isAntiAlias = true
        canvas.drawRoundRect(rectF, mTextSize, mTextSize, mFramePaint)

        mFramePaint.reset()
        mFramePaint.style = Paint.Style.STROKE
        mFramePaint.color = mOnBackgroundColor
        mFramePaint.isAntiAlias = true
        canvas.drawRoundRect(rectF, mTextSize, mTextSize, mFramePaint)
    }

    private fun drawLetters(canvas: Canvas) {
        val fontMetrics = mLettersPaint.fontMetrics
        val baselineOffset = (mItemHeight - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top
        for (i in mLetters.indices) {
            val posY = mItemHeight * i + baselineOffset

            if (i == mChoose) {
                mChoosePosY = posY
            } else {
                canvas.drawText(mLetters[i], mPosX, posY, mLettersPaint)
            }

            if (i == 0) {
                mFirstLetterPosY = posY
            } else if (i == mLetters.size - 1) {
                mLastLetterPosY = posY
            }
        }
    }

    private fun drawWavePath(canvas: Canvas) {
        mWavePath.reset()
        mWavePath.moveTo(mWidth.toFloat(), (mCenterY - 3 * mRadius).toFloat())
        val controlTopY = mCenterY - 2 * mRadius

        val endTopX = (mWidth - mRadius * cos(ANGLE) * mRatio).toInt()
        val endTopY = (controlTopY + mRadius * sin(ANGLE)).toInt()
        mWavePath.quadTo(mWidth.toFloat(), controlTopY.toFloat(), endTopX.toFloat(), endTopY.toFloat())

        val controlCenterX = (mWidth - 1.8f * mRadius * sin(ANGLE_R) * mRatio).toInt()
        val controlCenterY = mCenterY

        val controlBottomY = mCenterY + 2 * mRadius
        val endBottomY = (controlBottomY - mRadius * cos(ANGLE)).toInt()
        mWavePath.quadTo(controlCenterX.toFloat(), controlCenterY.toFloat(), endTopX.toFloat(), endBottomY.toFloat())

        mWavePath.quadTo(mWidth.toFloat(), controlBottomY.toFloat(), mWidth.toFloat(), (controlBottomY + mRadius).toFloat())

        mWavePath.close()
        canvas.drawPath(mWavePath, mWavePaint)
    }

    private fun drawBallPath(canvas: Canvas) {
        mBallCentreX = (mWidth + mBallRadius) - (2.0f * mRadius + 2.0f * mBallRadius) * mRatio

        mBallPath.reset()
        mBallPath.addCircle(mBallCentreX, mCenterY.toFloat(), mBallRadius.toFloat(), Path.Direction.CW)
        mBallPath.op(mWavePath, Path.Op.DIFFERENCE)

        mBallPath.close()
        canvas.drawPath(mBallPath, mWavePaint)
    }

    private fun drawChooseText(canvas: Canvas) {
        if (mChoose != -1) {
            mLettersPaint.reset()
            mLettersPaint.color = mChooseTextColor
            mLettersPaint.textSize = mTextSize
            mLettersPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(mLetters[mChoose], mPosX, mChoosePosY, mLettersPaint)

            if (mRatio >= 0.4f) {
                val target = mLetters[mChoose]
                val fontMetrics = mTextPaint.fontMetrics
                val baseline = abs((-fontMetrics.bottom - fontMetrics.top).toDouble()).toFloat()
                val x = mBallCentreX
                val y = mCenterY + baseline / 2
                canvas.drawText(target, x, y, mTextPaint)
            }
        }
    }
}
