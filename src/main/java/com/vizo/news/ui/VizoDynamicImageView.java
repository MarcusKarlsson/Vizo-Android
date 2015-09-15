package com.vizo.news.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import com.vizo.news.R;
import com.vizo.news.utils.CommonUtils;

/**
 * Custom ImageView class which has interface callbacks to load images from rest
 *
 * @author nine3_marks
 */
public class VizoDynamicImageView extends ImageView {

    public VizoDynamicImageView(Context context) {
        super(context);
    }

    public VizoDynamicImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VizoDynamicImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void loadRoundedImage(String imageUrl) {
        Picasso.with(getContext().getApplicationContext())
                .load(imageUrl)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .placeholder(R.drawable.news_placeholder)
                .transform(new RoundedTransformation(20, 0))
                .into(this);
    }

    public void loadImage(String imageUrl, int placeholder) {
//        Picasso.with(getContext().getApplicationContext())
//                .load(imageUrl)
//                .placeholder(R.drawable.news_placeholder)
//                .into(this);

        CommonUtils.getInstance()
                .loadImage(this, imageUrl, placeholder);
    }

    public void loadImage(String imageUrl) {
        CommonUtils.getInstance()
                .loadImage(this, imageUrl, R.drawable.news_placeholder);
    }

    public class RoundedTransformation implements
            com.squareup.picasso.Transformation {
        private final int radius;
        private final int margin; // dp

        // radius is corner radii in dp
        // margin is the board in dp
        public RoundedTransformation(final int radius, final int margin) {
            this.radius = radius;
            this.margin = margin;
        }

        @Override
        public Bitmap transform(final Bitmap source) {
            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP,
                    Shader.TileMode.CLAMP));

            try {
                Bitmap output = Bitmap.createBitmap(source.getWidth(),
                        source.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(output);
                canvas.drawRoundRect(new RectF(margin, margin, source.getWidth()
                        - margin, source.getHeight() - margin), radius, radius, paint);

                if (source != output) {
                    source.recycle();
                }

                return output;
            } catch (Exception e) {
                e.printStackTrace();
                return source;
            }
        }

        @Override
        public String key() {
            return "rounded";
        }
    }
}
