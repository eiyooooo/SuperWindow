package com.eiyooooo.superwindow.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RSInvalidStateException;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import timber.log.Timber;

public class BlurUtil {

    private static RenderScript mRenderScript;
    private static ScriptIntrinsicBlur mBlur;
    private static Allocation mInput;
    private static Allocation mOutput;
    private static BitmapDrawable lastFrameDrawable;

    public static Drawable blurForeground(Context context, Bitmap image) {
        if (image == null || image.getWidth() <= 1 || image.getHeight() == 0 || image.isRecycled()) {
            if (lastFrameDrawable != null && lastFrameDrawable.getBitmap() != null && !lastFrameDrawable.getBitmap().isRecycled()) {
                Timber.w("TextureView获取Bitmap为空，返回最后一次缓存的bitmapDrawable");
                return lastFrameDrawable;
            } else {
                Timber.w("TextureView获取Bitmap为空");
                return null;
            }
        }

        Timber.d("虚化bitmap 宽:" + image.getWidth() + ",高:" + image.getHeight());
        Bitmap blurBitmap;
        try {
            blurBitmap = blurBitmap(context, image, 10f, 15);
        } catch (Exception e) {
            Timber.w("获取模糊bitmap异常:%s", e.getMessage());
            if (lastFrameDrawable != null && lastFrameDrawable.getBitmap() != null && !lastFrameDrawable.getBitmap().isRecycled()) {
                Timber.w("获取模糊bitmap异常,返回最后一次的模糊bitmap");
                return lastFrameDrawable;
            } else {
                Timber.w("获取模糊bitmap异常");
                return null;
            }
        }

        if (blurBitmap.isRecycled()) {
            Timber.w("模糊bitmap无效");
            return null;
        }

        lastFrameDrawable = new BitmapDrawable(context.getResources(), blurBitmap);
        return lastFrameDrawable;
    }

    private static Bitmap blurBitmap(Context context, Bitmap originBitmap, float blurRadius, int scaleRatio) {
        if (blurRadius <= 0) {
            blurRadius = 1;
        }
        if (blurRadius > 25f) {
            blurRadius = 25f;
        }
        int width = originBitmap.getWidth() / scaleRatio;
        int height = originBitmap.getHeight() / scaleRatio;
        Bitmap bitmap = Bitmap.createScaledBitmap(originBitmap, width, height, false);
        if (mRenderScript == null) {
            mRenderScript = RenderScript.create(context);
        }
        if (mBlur == null) {
            mBlur = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
        }
        mInput = Allocation.createFromBitmap(mRenderScript, bitmap);
        mOutput = Allocation.createTyped(mRenderScript, mInput.getType());
        mBlur.setRadius(blurRadius);
        mBlur.setInput(mInput);
        mBlur.forEach(mOutput);
        mOutput.copyTo(bitmap);
        return bitmap;
    }

    public static synchronized void destroy() {
        try {
            if (mRenderScript != null) {
                mRenderScript.destroy();
                mRenderScript = null;
            }
        } catch (RSInvalidStateException e) {
            // Log the exception or handle it as necessary
            mRenderScript = null;
        }
        try {
            if (mBlur != null) {
                mBlur.destroy();
                mBlur = null;
            }
        } catch (RSInvalidStateException e) {
            mBlur = null;
        }
        try {
            if (mInput != null) {
                mInput.destroy();
                mInput = null;
            }
        } catch (RSInvalidStateException e) {
            mInput = null;
        }
        try {
            if (mOutput != null) {
                mOutput.destroy();
                mOutput = null;
            }
        } catch (RSInvalidStateException e) {
            mOutput = null;
        }
        if (lastFrameDrawable != null && lastFrameDrawable.getBitmap() != null) {
            lastFrameDrawable.getBitmap().recycle();
            lastFrameDrawable = null;
        }
    }
}
