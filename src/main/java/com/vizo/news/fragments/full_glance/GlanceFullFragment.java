package com.vizo.news.fragments.full_glance;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.vizo.news.R;
import com.vizo.news.activities.GlanceFullActivity;
import com.vizo.news.database.DatabaseConstants;
import com.vizo.news.domain.VizoCategory;
import com.vizo.news.domain.VizoGlance;
import com.vizo.news.fragments.base.BaseFragment;
import com.vizo.news.service.ProfileSyncService;
import com.vizo.news.ui.PlayPauseDrawable;
import com.vizo.news.ui.ProgressWheel;
import com.vizo.news.ui.VizoDynamicImageView;
import com.vizo.news.ui.VizoFitTextView;
import com.vizo.news.ui.VizoScrollView;
import com.vizo.news.utils.CommonUtils;
import com.amplitude.api.Amplitude;
/**
 * Custom fragment class which corresponds to the page of view pager in Full Glance
 *
 * @author nine3_marks
 */
public class GlanceFullFragment extends BaseFragment {

    /**
     * Member variable which holds the instance of delegate activity
     */
    private GlanceFullActivity delegate;

    private View view;
    private VizoGlance glance;
    private VizoDynamicImageView ivGlanceImage;
    private VizoFitTextView tvGlanceDescription;
    private ImageView ivDescriptionTint;

    private ImageView ivArrowUp;
    private ImageView ivPlayButton;
    private ProgressWheel progressWheel;
    private VizoScrollView scrollView;
    private TextView tvGlanceDateTime;
    private TextView tvImageCredit;

    private boolean needFullShow = false;
    private PlayPauseDrawable playPauseDrawable;

    /**
     * Member variable which refers click event handler of the fragment
     */
    private View.OnClickListener mClickListener;

    /**
     * Holds flag whether to show glanced indicator at the beginning of glance description
     */
    private boolean needGlancedMark = false;

    public static GlanceFullFragment newInstance(VizoGlance glance) {
        GlanceFullFragment fragment = new GlanceFullFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("glance", glance);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * Set value for needGlancedMark
     *
     * @param needGlancedMark if true, display glanced indicator if glanced
     *                        if false, do not display yellow indicator even though it's glanced
     */
    public void setNeedGlancedMark(boolean needGlancedMark) {
        this.needGlancedMark = needGlancedMark;
    }

    public GlanceFullFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.glance = getArguments().getParcelable("glance");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.full_glance_item, container, false);

        // Map view elements to class members
        ivGlanceImage = (VizoDynamicImageView) this.view.findViewById(R.id.iv_glance_image);
        tvGlanceDescription = (VizoFitTextView) this.view.findViewById(R.id.tv_glance_description);
        ivArrowUp = (ImageView) this.view.findViewById(R.id.iv_arrow_up);
        ivPlayButton = (ImageView) this.view.findViewById(R.id.iv_play_button);
        progressWheel = (ProgressWheel) this.view.findViewById(R.id.pw_tts_progress);

        ivDescriptionTint = (ImageView) this.view.findViewById(R.id.iv_description_tint);

        tvGlanceDateTime = (TextView) this.view.findViewById(R.id.preview_glance_date_time);
        tvImageCredit = (TextView) view.findViewById(R.id.tv_image_credit);

        // Populate view content with glance data
        VizoCategory category = VizoCategory.findCategoryById(glance.category_id, baseActivity);
        ivGlanceImage.loadImage(glance.image_url,
                category.getPlaceholderImage(baseActivity, false));

        // Get actual description text size which fits bounds
        tvGlanceDescription.setText(glance.description);

        // resizeText() takes some time, so it should be done in runnable
        tvGlanceDescription.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = tvGlanceDescription.getLayoutParams();
                params.height = scrollView.getHeight() * 80 / 100;
                tvGlanceDescription.setLayoutParams(params);

                tvGlanceDescription.resizeText();
                if (glance.isGlanced(baseActivity) && needGlancedMark) {
                    tvGlanceDescription.setText(generateSpannable(glance.getShortDescription()));
                } else {
                    tvGlanceDescription.setText(glance.getShortDescription());
                }
            }
        });

        scrollView = (VizoScrollView) this.view.findViewById(R.id.sv_description_area);
        scrollView.setScrollViewListener(mListener);

        scrollView.post(new Runnable() {

            @Override
            public void run() {
                if (needFullShow) {
                    scrollView.setSmoothScrollingEnabled(false);
                    scrollView.scrollTo(0, getResources()
                            .getDimensionPixelSize(R.dimen.vizo_scrollview_page_size));
                    scrollView.setSmoothScrollingEnabled(true);
                    mListener.onScrolledToFull(scrollView);
                    needFullShow = false;
                } else {
                    tvGlanceDateTime.setText(
                            CommonUtils.getInstance().getVizoTimeString(glance.modified_date));
                    scrollView.setSmoothScrollingEnabled(false);
                    scrollView.scrollTo(0, 0);
                    scrollView.setSmoothScrollingEnabled(true);
                }
            }
        });

        // Need to show image credit at the bottom right corner
        if (glance.image_credit != null) {
            tvImageCredit.setText(glance.image_credit);
        }

        playPauseDrawable = new PlayPauseDrawable(
                CommonUtils.getInstance().dpToPx(3),
                getResources().getColor(R.color.white),
                getResources().getColor(R.color.white),
                300);
        ivPlayButton.setImageDrawable(playPauseDrawable);

        view.findViewById(R.id.ll_content_area).setOnClickListener(mClickListener);
        view.findViewById(R.id.rl_tts_indicator).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (delegate.getTTSEngine() != null) {
                    if (playPauseDrawable.isPlaying()) {

                        Amplitude.getInstance().logEvent("TEXT_TO_SPEECH_ACTIVE");

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            delegate.getTTSEngine()
                                    .speak(glance.description, TextToSpeech.QUEUE_FLUSH, null, null);
                        } else {
                            delegate.getTTSEngine()
                                    .speak(glance.description, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    } else {
                        delegate.getTTSEngine().stop();
                    }
                }

                playPauseDrawable.toggle();
            }
        });

        return this.view;
    }

    public void refresh() {
        ivPlayButton.post(new Runnable() {
            @Override
            public void run() {
                playPauseDrawable.animatePlay();
            }
        });

        scrollView.post(new Runnable() {

            @Override
            public void run() {
                if (needFullShow) {
                    scrollView.setSmoothScrollingEnabled(false);
                    scrollView.scrollTo(0, getResources()
                            .getDimensionPixelSize(R.dimen.vizo_scrollview_page_size));
                    scrollView.setSmoothScrollingEnabled(true);
                    mListener.onScrolledToFull(scrollView);
                    needFullShow = false;
                } else {
                    tvGlanceDateTime.setText(
                            CommonUtils.getInstance().getVizoTimeString(glance.modified_date));
                    scrollView.setSmoothScrollingEnabled(false);
                    scrollView.scrollTo(0, 0);
                    scrollView.setSmoothScrollingEnabled(true);
                }
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        delegate = (GlanceFullActivity) getActivity();
    }

    /**
     * Generate spannable with yellow indicator (yellow dot at the beginning)
     *
     * @param word The normal text
     * @return generated spannable
     */
    private Spannable generateSpannable(String word) {

        Spannable indicator = new SpannableString(" ● " + word);
        indicator.setSpan(
                new ForegroundColorSpan(getResources().getColor(R.color.vizo_yellow)),
                0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        indicator.setSpan(
                new ForegroundColorSpan(getResources().getColor(R.color.white)),
                3, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        return indicator;
    }

    /**
     * Custom VizoScrollViewListener which handles scrolling event in full glance screen
     */
    private VizoScrollView.VizoScrollViewListener mListener = new VizoScrollView.VizoScrollViewListener() {

        @Override
        public void onScrollChanged(VizoScrollView scrollView, int x, int y, int oldX, int oldY) {

            //evans code
            ViewGroup.LayoutParams params = tvGlanceDescription.getLayoutParams();
            params.height = scrollView.getHeight() * 80 / 100;
            tvGlanceDescription.setLayoutParams(params);
            scrollView.setSmoothScrollingEnabled(true);
            tvGlanceDescription.resizeText();
            //end

            if (y > 0) {
                tvGlanceDateTime.setText("");
                if (glance.isGlanced(getActivity()) && needGlancedMark) {
                    tvGlanceDescription.setText(generateSpannable(glance.description));
                } else {
                    tvGlanceDescription.setText(glance.description);
                }
            } else {
                tvGlanceDateTime.setText(CommonUtils.getInstance().getVizoTimeString(glance.modified_date));

                if (glance.isGlanced(getActivity()) && needGlancedMark) {
                    tvGlanceDescription.setText(generateSpannable(glance.getShortDescription()));
                } else {
                    tvGlanceDescription.setText(glance.getShortDescription());
                }
            }

            // Rotate arrow along the scrolling of glance text
            float alpha = (float) y / getResources()
                    .getDimensionPixelSize(R.dimen.vizo_scrollview_page_size);
            ivDescriptionTint.setAlpha(Math.min(1.0f, alpha));
            ivArrowUp.setRotation(Math.min(180, 180 * alpha));

            ivPlayButton.setVisibility(View.GONE);
            ivArrowUp.setVisibility(View.VISIBLE);
            progressWheel.setProgress(0);

            if (delegate.getTTSEngine() != null && delegate.getTTSEngine().isSpeaking()) {
                delegate.getTTSEngine().stop();
            }
        }

        @Override
        public void onScrolledToFull(VizoScrollView scrollView) {

            // if scrolled to full glance, this glance is saved to database as a glanced item
            if (!glance.isGlanced(getActivity())) {
                glance.syncState = DatabaseConstants.UPLOAD_REQUIRED;
                glance.addAsGlanced(getActivity());

                // Start sync process
                Intent intent = new Intent(getActivity(), ProfileSyncService.class);
                getActivity().startService(intent);
            }

            ivArrowUp.setRotation(180);

            // Animate TTS progress
            // We disable TTS engine for hebrew version
            if (localStorage.loadAppLanguage().equals("he"))
                return;

            Thread thread = new Thread(new Runnable() {

                private int i = 0;

                @Override
                public void run() {
                    while (i < 361) {
                        i += 5;
                        progressWheel.setProgress(i);

                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    baseActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ivArrowUp.setVisibility(View.GONE);
                            ivPlayButton.setVisibility(View.VISIBLE);
                            ivPlayButton.post(new Runnable() {
                                @Override
                                public void run() {
                                    playPauseDrawable.animatePlay();
                                }
                            });
                        }
                    });
                }
            });
            thread.start();
        }

        @Override
        public void onScrolledToPreview(VizoScrollView scrollView) {
            ivArrowUp.setRotation(0);
        }
    };

    public void showFull() {
        needFullShow = true;
    }

    /**
     * Save glance image to external storage
     */
    public boolean saveGlanceImageToStorage() {
        BitmapDrawable drawable = (BitmapDrawable) ivGlanceImage.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        File saveFile = new File(Environment.getExternalStorageDirectory(), glance.title + ".png");

        boolean success = false;
        // Encode a file as a PNG image
        FileOutputStream outStream;
        try {

            outStream = new FileOutputStream(saveFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);

            outStream.flush();
            outStream.close();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return success;
    }

    /**
     * Get glance image as Bitmap object
     *
     * @return Bitmap object for glance image
     */
    public Bitmap getGlanceImage() {
        BitmapDrawable drawable = (BitmapDrawable) ivGlanceImage.getDrawable();
        return drawable.getBitmap();
    }

    /**
     * Get glance object
     *
     * @return VizoGlance object
     */
    public VizoGlance getGlance() {
        return this.glance;
    }

    /**
     * Set custom click listener which intercepts click event of fragment
     *
     * @param onClickListener Custom OnClickListener object
     */
    public void setOnClickListener(View.OnClickListener onClickListener) {

        // Set member value
        mClickListener = onClickListener;
    }
}
