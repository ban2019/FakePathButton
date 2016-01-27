package io.github.xialincn.fakepathbutton;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Arrays;

/**
 * Created by lin on 2016/1/22.
 */
public class FakePathButton extends RelativeLayout {
    private final String TAG = "FakePathButton";

    private ImageView mMainButton;
    private ImageView mLeftButton;
    private ImageView mLeftMidButton;
    private ImageView mMidButton;
    private ImageView mMidRightButton;
    private ImageView mRightButton;

    private LayoutInflater mInflater;

    private ImageView mSubButtons[];
    private PathButtonOnClickListener mExternalListeners[];

    private int baseAngle = 30;
    private int offset = 30;

    private int getLaunchAngle(int index) {
        return baseAngle + index * offset;
    }

    private class Delta {
        public Delta(float x, float y) {
            this.x = x;
            this.y = y;
        }

        float x;
        float y;
    }

    private final float LAUNCH_DISTANCE = 220;
    private final long LONG_DURATION = 350;
    private final long SHORT_DURATION = 100;
    private final float ROTATE_ANGLE = 1440f;
    // OvershootInterpolator's parameter
    private final float TENSION = 2f;

    private boolean isEven = false;

    private RelativeLayout mContainer;

    public FakePathButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        init();
    }

    public FakePathButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mInflater = LayoutInflater.from(context);
        init();
    }

    private int colorBackup;
    private View mParentRootLayout;

    public void init() {
        mInflater.inflate(R.layout.button_fake_path, this, true);
        mMainButton = (ImageView) findViewById(R.id.main_button);
        mLeftButton = (ImageView) findViewById(R.id.left_button);
        mLeftMidButton = (ImageView) findViewById(R.id.left_mid_button);
        mMidButton = (ImageView) findViewById(R.id.mid_button);
        mMidRightButton = (ImageView) findViewById(R.id.mid_right_button);
        mRightButton = (ImageView) findViewById(R.id.right_button);
        mSubButtons = new ImageView[]{
                mLeftButton, mLeftMidButton, mMidButton,
                mMidRightButton, mRightButton
        };
        mExternalListeners = new PathButtonOnClickListener[mSubButtons.length];

        mParentRootLayout = ((ActionBarActivity) getContext()).getWindow().getDecorView().getRootView();
        colorBackup = ((ColorDrawable) mParentRootLayout.getBackground()).getColor();

        mContainer = (RelativeLayout) findViewById(R.id.container);

        setPathButtonListeners();
    }

    private Delta getTranslationDelta(int i) {
        float deltaX, deltaY;
        deltaY = (float) (LAUNCH_DISTANCE * Math.sin(Math.toRadians(getLaunchAngle(i))));
        deltaX = (float) (LAUNCH_DISTANCE * Math.cos(Math.toRadians(getLaunchAngle(i))));
        return new Delta(-deltaX, -deltaY);
    }

    private void setPathButtonListeners() {
        mMainButton.setOnClickListener(new ButtonMainOnClickListener());
        for (int i = 0; i < mSubButtons.length; i++) {
            mSubButtons[i].setOnClickListener(mSubButtonListener);
        }
    }

    private class LayoutOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d(TAG, v.toString());
            mMainButton.performClick();
        }
    }
    private LayoutOnClickListener mLayoutOnClickListener = new LayoutOnClickListener();

    class ButtonMainOnClickListener implements ImageView.OnClickListener {
        @Override
        public void onClick(View v) {
            float angle1 = 0;
            float angle2 = 45f;
            Animator animatorMain;
            if (!isEven) {
                animatorMain = ObjectAnimator.ofFloat(v, "Rotation", angle1, angle2);
            } else {
                animatorMain = ObjectAnimator.ofFloat(v, "Rotation", angle2, angle1);
            }
            animatorMain.setInterpolator(new LinearInterpolator());
            animatorMain.setDuration(SHORT_DURATION);

            AnimatorSet animatorSub = new AnimatorSet();
            animatorSub.setDuration(LONG_DURATION);
            animatorSub.playTogether(
                    !isEven ? getSubButtonPopupAnimations(0) : getSubButtonBackAnimations(0),
                    !isEven ? getSubButtonPopupAnimations(1) : getSubButtonBackAnimations(1),
                    !isEven ? getSubButtonPopupAnimations(2) : getSubButtonBackAnimations(2),
                    !isEven ? getSubButtonPopupAnimations(3) : getSubButtonBackAnimations(3),
                    !isEven ? getSubButtonPopupAnimations(4) : getSubButtonBackAnimations(4)
            );
            if (Utils.hasLollipop()) {
                if (!isEven) {
                    animatorSub.playTogether(
                            ObjectAnimator.ofArgb(mParentRootLayout, "BackgroundColor",
                                    ((ColorDrawable) mParentRootLayout.getBackground()).getColor(), Color.parseColor("#818181")));
                    mContainer.setOnClickListener(mLayoutOnClickListener);
                } else {
                    animatorSub.playTogether(
                            ObjectAnimator.ofArgb(mParentRootLayout, "BackgroundColor",
                                    ((ColorDrawable) mParentRootLayout.getBackground()).getColor(), colorBackup));
                    mContainer.setOnClickListener(null);
                }
            } else {
                if (!isEven) {
                    mParentRootLayout.setBackgroundColor(Color.parseColor("#818181"));
                } else {
                    mParentRootLayout.setBackgroundColor(colorBackup);
                }
            }

            AnimatorSet animatorWhole = new AnimatorSet();
            animatorWhole.playTogether(animatorMain, animatorSub);
            animatorWhole.start();
            isEven = !isEven;
        }
    }

    private AnimatorSet getSubButtonPopupAnimations(int index) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(LONG_DURATION);
        animatorSet.setInterpolator(new OvershootInterpolator(TENSION));
        final float endAngle = ROTATE_ANGLE;

        Delta delta = getTranslationDelta(index);
        animatorSet.play(
                ObjectAnimator.ofFloat(mSubButtons[index], "TranslationX", 0, delta.x)).with(
                ObjectAnimator.ofFloat(mSubButtons[index], "TranslationY", 0, delta.y)).with(
                ObjectAnimator.ofFloat(mSubButtons[index], "Rotation", 0f, endAngle)
        );

        return animatorSet;
    }

    private AnimatorSet getSubButtonBackAnimations(int index) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(LONG_DURATION);
        animatorSet.setInterpolator(new AnticipateInterpolator(TENSION));
        final float endAngle = -ROTATE_ANGLE;

        Delta delta = getTranslationDelta(index);
        animatorSet.play(
                ObjectAnimator.ofFloat(mSubButtons[index], "TranslationX", delta.x, 0)).with(
                ObjectAnimator.ofFloat(mSubButtons[index], "TranslationY", delta.y, 0)).with(
                ObjectAnimator.ofFloat(mSubButtons[index], "Rotation", 0f, endAngle)
        );

        return animatorSet;
    }

    private AnimatorSet getFadeZoomTogetherAnimations(int index) {
        int length = mSubButtons.length;
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(LONG_DURATION);
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(mSubButtons[index], "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(mSubButtons[index], "ScaleX", 1f, 3f),
                ObjectAnimator.ofFloat(mSubButtons[index], "ScaleY", 1f, 3f),

                ObjectAnimator.ofFloat(mMainButton, "ScaleX", 1f, 0f),
                ObjectAnimator.ofFloat(mMainButton, "ScaleY", 1f, 0f),

                ObjectAnimator.ofFloat(mSubButtons[(index + 1) % length], "ScaleX", 1f, 0.001f),
                ObjectAnimator.ofFloat(mSubButtons[(index + 1) % length], "ScaleY", 1f, 0.001f),

                ObjectAnimator.ofFloat(mSubButtons[(index + 2) % length], "ScaleX", 1f, 0f),
                ObjectAnimator.ofFloat(mSubButtons[(index + 2) % length], "ScaleY", 1f, 0f),

                ObjectAnimator.ofFloat(mSubButtons[(index + 3) % length], "ScaleX", 1f, 0f),
                ObjectAnimator.ofFloat(mSubButtons[(index + 3) % length], "ScaleY", 1f, 0f),

                ObjectAnimator.ofFloat(mSubButtons[(index + 4) % length], "ScaleX", 1f, 0f),
                ObjectAnimator.ofFloat(mSubButtons[(index + 4) % length], "ScaleY", 1f, 0f)
        );

        return animatorSet;
    }

    private ButtonPopOnClickListener mSubButtonListener = new ButtonPopOnClickListener();

    private class ButtonPopOnClickListener implements ImageView.OnClickListener {

        @Override
        public void onClick(View v) {
            int i = Arrays.asList(mSubButtons).indexOf(v);
            getFadeZoomTogetherAnimations(i).start();
            mExternalListeners[i].onClick(v);
        }
    }

    public void reset() {
        mMainButton.setScaleX(1f);
        mMainButton.setScaleY(1f);
        mMainButton.setRotation(0);

        for (int i = 0; i < mSubButtons.length; i++) {
            mSubButtons[i].setScaleX(1f);
            mSubButtons[i].setScaleY(1f);

            mSubButtons[i].setAlpha(1f);

            mSubButtons[i].setTranslationX(0);
            mSubButtons[i].setTranslationY(0);
        }

        mParentRootLayout.setBackgroundColor(colorBackup);
        isEven = false;
    }

    public void gather() {
        if (isEven) {
            mMainButton.performClick();
        }
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        throw new RuntimeException("Please use setmLayoutOnClickListener instead");
    }

    public void setOnClickListener(int index, PathButtonOnClickListener listener) {
        mExternalListeners[index] = listener;
    }
}
