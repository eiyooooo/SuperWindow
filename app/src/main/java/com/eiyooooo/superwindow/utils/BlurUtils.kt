package com.eiyooooo.superwindow.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.View
import androidx.core.graphics.drawable.toDrawable

object BlurUtils {

    private var mRenderScript: RenderScript? = null
    private var mBlur: ScriptIntrinsicBlur? = null
    private var mInput: Allocation? = null
    private var mOutput: Allocation? = null

    fun blurView(input: View, blurRadius: Float = 10F, scaleRatio: Int = 15): Drawable? {
        val inputBitmap = input.getBitmap()
        if (inputBitmap == null || inputBitmap.width <= 1 || inputBitmap.height <= 1 || inputBitmap.isRecycled) {
            return null
        }
        try {
            val output = blurBitmap(input.context, inputBitmap, blurRadius, scaleRatio)
            return if (output == null || output.isRecycled) null else output.toDrawable(input.context.resources)
        } catch (e: Exception) {
            return null
        }
    }

    private fun blurBitmap(context: Context, input: Bitmap?, blurRadius: Float = 10F, scaleRatio: Int = 15): Bitmap? {
        if (input == null || input.width <= 1 || input.height <= 1 || input.isRecycled) {
            return null
        }
        try {
            var blurRadiusLimited = blurRadius
            if (blurRadiusLimited <= 0) {
                blurRadiusLimited = 1f
            }
            if (blurRadiusLimited > 25f) {
                blurRadiusLimited = 25f
            }
            val width = input.width / scaleRatio
            val height = input.height / scaleRatio
            val bitmap = Bitmap.createScaledBitmap(input, width, height, false)
            if (mRenderScript == null) {
                mRenderScript = RenderScript.create(context)
            }
            if (mBlur == null) {
                mBlur = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript))
            }
            mInput = Allocation.createFromBitmap(mRenderScript, bitmap)
            mOutput = Allocation.createTyped(mRenderScript, mInput!!.type)
            mBlur!!.setRadius(blurRadiusLimited)
            mBlur!!.setInput(mInput)
            mBlur!!.forEach(mOutput)
            mOutput!!.copyTo(bitmap)
            return if (bitmap.isRecycled) null else bitmap
        } catch (e: Exception) {
            return null
        }
    }

    @Synchronized
    fun destroy() {
        try {
            if (mRenderScript != null) {
                mRenderScript!!.destroy()
                mRenderScript = null
            }
        } catch (e: Exception) {
            mRenderScript = null
        }

        try {
            if (mBlur != null) {
                mBlur!!.destroy()
                mBlur = null
            }
        } catch (e: Exception) {
            mBlur = null
        }

        try {
            if (mInput != null) {
                mInput!!.destroy()
                mInput = null
            }
        } catch (e: Exception) {
            mInput = null
        }

        try {
            if (mOutput != null) {
                mOutput!!.destroy()
                mOutput = null
            }
        } catch (e: Exception) {
            mOutput = null
        }
    }
}
