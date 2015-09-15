package com.vizo.news.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import com.vizo.news.utils.BitmapUtils;

public class VizoRoundedImageView extends ImageView {

    private float borderWidth;

    public VizoRoundedImageView(Context context) {
        super(context);
        sharedConstructor();
    }

    public VizoRoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructor();
    }

    public VizoRoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        sharedConstructor();
    }

    private void sharedConstructor() {
        borderWidth = 0.0f;
    }

    public void setBorderWidth(float border) {
        borderWidth = border;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Drawable drawable = getDrawable();

        if (drawable == null) {
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        // This is source bitmap
        Bitmap b = ((BitmapDrawable) drawable).getBitmap();

        // This is target bitmap which should be drawn
        Bitmap bitmap = null;

        // This file should temporally save the bytes
        File file = new File(Environment.getExternalStorageDirectory(), "/vizo_cache/bm_cache.txt");
        file.getParentFile().mkdirs();

        // Open RandomAccess File
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

            // Get the width and height of the source bitmap
            int width = b.getWidth();
            int height = b.getHeight();

            // Copy the bytes to the file
            // assuming the source bitmap is loaded using options.inPreferredConfig = Config.ARGB_8888
            FileChannel channel = randomAccessFile.getChannel();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, width * height * 4);
            b.copyPixelsToBuffer(map);

            // Recycle the source bitmap
            b.recycle();

            // Create new bitmap to load the bitmap again
            bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            map.position(0);

            // Load it back from temporary
            bitmap.copyPixelsFromBuffer(map);

            // Close the temporary file and channel, then delete that also
            channel.close();
            randomAccessFile.close();
        } catch (Exception e) {
        }

        int w = getWidth();
        // int h = getHeight();

        if (bitmap != null) {
            Bitmap roundBitmap = getCroppedBitmap(bitmap, w, borderWidth);
            canvas.drawBitmap(roundBitmap, 0, 0, null);
        }

    }

    public static Bitmap getCroppedBitmap(Bitmap bmp, int radius,
                                          float borderWidth) {
        Bitmap sbmp;
        if (bmp.getWidth() != radius || bmp.getHeight() != radius) {
            sbmp = BitmapUtils.createScaledBitmap(bmp, radius, radius);
        } else {
            sbmp = bmp;
        }

        Bitmap output = Bitmap.createBitmap(radius, radius, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        // final int color = 0xffa19774;
        final Paint paint = new Paint();
        final Paint stroke = new Paint();

        final Rect rect = new Rect(0, 0, radius, radius);

        paint.setAntiAlias(true);
        stroke.setAntiAlias(true);

        paint.setFilterBitmap(true);
        stroke.setFilterBitmap(true);

        paint.setDither(true);
        stroke.setDither(true);

        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        stroke.setColor(Color.parseColor("#ffffff"));
        stroke.setStyle(Style.STROKE);
        stroke.setStrokeWidth(borderWidth);
        canvas.drawCircle(radius / 2, radius / 2, radius / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);

//		canvas.drawCircle(radius / 2, radius / 2,
//				radius / 2 - stroke.getStrokeWidth() / 2, stroke);

        return output;
    }

}
