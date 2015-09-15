package com.vizo.news.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vizo.news.R;
import com.vizo.news.utils.Constants;

/**
 * Custom view class which corresponds to account view item
 *
 * @author nine3_marks
 */
public class VizoAccountItemView extends RelativeLayout {

    // member variables which hold view elements
    private ImageView accountIcon;
    private TextView accountName;
    private ImageView connectIcon;
    private TextView actionButton;
    private LinearLayout topContentView;

    private int accountId;
    private String[] accountTitles;
    private int[] accountImageIds;

    private OnActionListener actionListener;

    public VizoAccountItemView(Context context) {
        super(context);
        init();
    }

    public VizoAccountItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VizoAccountItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.vizo_account_item, this);

//        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
//        mTouchSlop = configuration.getScaledTouchSlop();

        // fetch account info from resource
        accountTitles = getResources().getStringArray(R.array.account_names);
        TypedArray images = getResources().obtainTypedArray(R.array.account_image_ids);
        accountImageIds = new int[images.length()];
        for (int i = 0; i < images.length(); i++) {
            accountImageIds[i] = images.getResourceId(i, -1);
        }
        images.recycle();

        // map view elements to class members
        accountIcon = (ImageView) findViewById(R.id.iv_account_image);
        accountName = (TextView) findViewById(R.id.tv_account_name);
        connectIcon = (ImageView) findViewById(R.id.iv_status_button);
        actionButton = (TextView) findViewById(R.id.tv_disconnect_button);
        topContentView = (LinearLayout) findViewById(R.id.ll_top_content_view);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (accountId == Constants.ADD_ACCOUNT) {
                    if (actionListener != null)
                        actionListener.onActionClicked(accountId);
                } else {
                    toggleActionButton();
                }
            }
        });

        actionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionListener != null)
                    actionListener.onActionClicked(accountId);
            }
        });
    }

    /**
     * Configure view with account info
     *
     * @param accountId The id of account (Refer Constants.java)
     */
    public void configureWithAccount(int accountId, boolean isConnected) {

        this.accountId = accountId;

        accountIcon.setImageResource(accountImageIds[accountId]);
        accountName.setText(accountTitles[accountId]);

        if (accountId == Constants.ADD_ACCOUNT) {
            accountName.setTextColor(getResources().getColor(R.color.grey));
        } else {
            accountName.setTextColor(getResources().getColor(R.color.white));
        }

        if (isConnected) {
            actionButton.setText(R.string.Disconnect);
            if (accountId != Constants.ADD_ACCOUNT)
                connectIcon.setImageResource(R.drawable.icon_cross);
        } else {
            actionButton.setText(R.string.Add_Account);
            if (accountId != Constants.ADD_ACCOUNT)
                connectIcon.setImageResource(R.drawable.icon_plus);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        topContentView.setAlpha(0);
        if (position == ACTION_BUTTON_POSITION.LEFT) {
            topContentView.setX(0 - actionButton.getWidth());
        } else {
            topContentView.setX(actionButton.getWidth());
        }
        isShowing = false;
    }

    private boolean isShowing = false;

    /**
     * show/hide action button for connect/disconnect
     */
    public void toggleActionButton() {
        if (!isShowing) {
            isShowing = true;
            topContentView.animate().alpha(1).x(0);
        } else {
            isShowing = false;
            if (position == ACTION_BUTTON_POSITION.LEFT) {
                topContentView.animate().alpha(0).x(0 - actionButton.getWidth());
            } else {
                topContentView.animate().alpha(0).x(actionButton.getWidth());
            }
        }
    }

    /**
     * Set action listener
     *
     * @param actionListener action listener object
     */
    public void setOnActionListener(OnActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public interface OnActionListener {
        void onActionClicked(int accountId);
    }

    private ACTION_BUTTON_POSITION position;

    public void setActionButtonPos(ACTION_BUTTON_POSITION position) {
        this.position = position;
    }

    public enum ACTION_BUTTON_POSITION {
        LEFT, RIGHT
    }

}
