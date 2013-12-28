package cz.steuer.gtdapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;


public class MenuLayout extends LinearLayout {

    private static final String TAG = "MenuLayout";
    private static final boolean DEBUG = true;

    // Duration of sliding animation, in miliseconds
    private static final int SLIDING_DURATION = 500;

    // Query Scroller every 16 miliseconds
    private static final int QUERY_INTERVAL = 16;

    private static final int MARGIN_WIDTH = 30;

    // MainLayout width
//    int mMainLayoutWidth;

    // Sliding mMenu
    private View mMenu;

    // Main mContent
    private View mContent;

    // mMenu does not occupy some right space
    // This should be updated correctly later in onMeasure
    private static int mMenuRightMargin = 150;

    // The state of mMenu
    private enum MenuState {
        HIDING,
        HIDDEN,
        SHOWING,
        SHOWN,
    };

    // mContent will be layouted based on this X offset
    // Normally, mContentXOffset = mMenu.getLayoutParams().width = this.getWidth - mMenuRightMargin
    private int mContentXOffset;

    // mMenu is hidden initially
    private MenuState mCurrentMenuState = MenuState.HIDDEN;

    // Scroller is used to facilitate animation
    private Scroller mMenuScroller = new Scroller(this.getContext(),
            new EaseInInterpolator());

    // Used to query Scroller about scrolling position
    // Note: The 3rd paramter to startScroll is the distance
    private Runnable mMenuRunnable = new MenuRunnable();
    private Handler mMenuHandler = new Handler();

    // Previous touch position
    int mPrevX = 0;

    // Is user dragging the mContent
    boolean mIsDragging = false;

    // Used to facilitate ACTION_UP 
    int mLastDiffX = 0;


    // 3 parameters constructor seems to be unavailable in 2.3
    public MenuLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        makeWindow();
    }

//    public MenuLayout(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }

    public MenuLayout(Activity activity) {
        super(activity);

        makeWindow();


        // Get our 2 child View
//        mMenu = this.getChildAt(0);
//        mContent = this.getChildAt(1);

        // Attach View.OnTouchListener
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return MenuLayout.this.onContentTouch(v, event);
            }
        });

        // Initially hide the mMenu
//        mMenu.setVisibility(View.GONE);
    }

    public View getMenu() {
        return mMenu;

    }

    public View getContent() {
        return mContent;
    }

    public void setMenu(int layoutId) {
        this.mMenu = LayoutInflater.from(getContext()).inflate(layoutId, null);
        this.addView(this.mMenu, 0);
        mMenu.setVisibility(View.GONE);

        this.forceLayout();
    }

//    // Overriding LinearLayout core methods
//
//    // Ask all children to measure themselves and compute the measurement of this
//    // layout based on the children
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//
//        mMainLayoutWidth = MeasureSpec.getSize(widthMeasureSpec);
//
//    }

    public void makeWindow() {
        Activity activity = (Activity) getContext();


        TypedArray a = activity.getTheme().obtainStyledAttributes(new int[] {android.R.attr.windowBackground});
        int background = a.getResourceId(0, 0);
        a.recycle();


        ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
        ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
        // save ActionBar themes that have transparent assets
        decorChild.setBackgroundResource(background);


        decor.removeView(decorChild);
        decor.addView(this);
//        this.addView(new TextView(activity));
        this.addView(decorChild);
        this.mContent = decorChild;
//        setContent(decorChild);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int nLeft = getPaddingLeft() + left;
        int nRight = getPaddingRight() + right;
        int nTop = getPaddingTop() + top;
        int nBottom = getPaddingBottom() + bottom;

        // True if MainLayout 's size and position has changed
        // If true, calculate child views size
        if(changed) {
            mMenuRightMargin = getWidth() * 30 / 100;

            // mContent View occupies the full height and width
            LayoutParams contentLayoutParams = (LayoutParams) mContent.getLayoutParams();
            contentLayoutParams.height = this.getHeight();
            contentLayoutParams.width = this.getWidth();

            // mMenu View occupies the full height, but certain width
            LayoutParams menuLayoutParams = (LayoutParams) mMenu.getLayoutParams();
            menuLayoutParams.height = this.getHeight();
            menuLayoutParams.width = this.getWidth() - mMenuRightMargin;

        }

        // Layout the child views
        mMenu.layout(nLeft, nTop, nRight - mMenuRightMargin, nBottom);
        mContent.layout(nLeft + mContentXOffset, nTop, nRight + mContentXOffset, nBottom);

    }

    public void toggleMenu() {
        switch(mCurrentMenuState) {
            case HIDDEN:
               showMenu();
                break;
            case SHOWN:
                hideMenu();
                break;
        }
    }

    public void showMenu() {
        if(mCurrentMenuState == MenuState.HIDDEN) {
            mCurrentMenuState = MenuState.SHOWING;
            mMenu.setVisibility(View.VISIBLE);
            mMenuScroller.startScroll(0, 0, mMenu.getLayoutParams().width, 0, SLIDING_DURATION);
            mMenuHandler.postDelayed(mMenuRunnable, QUERY_INTERVAL);
            this.invalidate();
        }
    }

    public void hideMenu() {
        if(mCurrentMenuState == MenuState.SHOWN) {
            mCurrentMenuState = MenuState.HIDING;
            mMenuScroller.startScroll(mContentXOffset, 0, -mContentXOffset, 0, SLIDING_DURATION);
            mMenuHandler.postDelayed(mMenuRunnable, QUERY_INTERVAL);
            this.invalidate();
        }
    }

    public boolean isMenuShown() {
        return mCurrentMenuState == MenuState.SHOWN;
    }

    // Query Scroller
    protected class MenuRunnable implements Runnable {
        @Override
        public void run() {
            boolean isScrolling = mMenuScroller.computeScrollOffset();
            adjustContentPosition(isScrolling);
        }
    }

    // Adjust mContent View position to match sliding animation
    private void adjustContentPosition(boolean isScrolling) {
        int scrollerXOffset = mMenuScroller.getCurrX();

        //Log.d("MainLayout.java adjustContentPosition()", "scrollerOffset " + scrollerOffset);

        // Translate mContent View accordingly
        mContent.offsetLeftAndRight(scrollerXOffset - mContentXOffset);

        mContentXOffset = scrollerXOffset;

        // Invalite this whole MainLayout, causing onLayout() to be called
        this.invalidate();

        // Check if animation is in progress
        if (isScrolling)
            mMenuHandler.postDelayed(mMenuRunnable, QUERY_INTERVAL);
        else
            this.onMenuSlidingComplete();
    }

    // Called when sliding is complete
    private void onMenuSlidingComplete() {
        switch (mCurrentMenuState) {
            case SHOWING:
                mCurrentMenuState = MenuState.SHOWN;
                break;
            case HIDING:
                mCurrentMenuState = MenuState.HIDDEN;
                mMenu.setVisibility(View.GONE);
                break;
            default:
                return;
        }
    }

    // Make scrolling more natural. Move more quickly at the end
    // See the formula here http://cyrilmottier.com/2012/05/22/the-making-of-prixing-fly-in-app-mMenu-part-1/
    protected class EaseInInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float t) {
            return (float)Math.pow(t-1, 5) + 1;
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        int curX = (int) ev.getRawX();

        if(mCurrentMenuState == MenuState.HIDING || mCurrentMenuState == MenuState.SHOWING) {
            return true;
        }

        if(mCurrentMenuState == MenuState.SHOWN && curX > mContentXOffset) {
            return true;
        }


        if(mIsDragging) {
            return true;
        }

        if(mCurrentMenuState == MenuState.HIDDEN && curX <= MARGIN_WIDTH) {
            return true;
        }

        return false;
    }



    // Handle touch event on mContent View
    public boolean onContentTouch(View v, MotionEvent event) {
        System.out.println(mContentXOffset);
        // Do nothing if sliding is in progress
        if(mCurrentMenuState == MenuState.HIDING || mCurrentMenuState == MenuState.SHOWING)
            return false;

        // getRawX returns X touch point corresponding to screen
        // getX sometimes returns screen X, sometimes returns mContent View X
        int curX = (int) event.getRawX();
        int diffX = 0;

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(mCurrentMenuState == MenuState.SHOWN && curX < this.mContentXOffset) {
                    return false;
                }

                if(DEBUG) Log.d(TAG, "Down x " + curX);
                mPrevX = curX;
                return true;

            case MotionEvent.ACTION_MOVE:
                if(DEBUG) Log.d(TAG, "Move x " + curX + " prev x " + mPrevX);

                // Set mMenu to Visible when user start dragging the mContent View
                if(!mIsDragging) {
                    mIsDragging = true;
                    mMenu.setVisibility(View.VISIBLE);
                }

                // How far we have moved since the last position
                diffX = curX - mPrevX;

                // Prevent user from dragging beyond border
                if(mContentXOffset + diffX <= 0) {
                    // Don't allow dragging beyond left border
                    // Use diffX will make mContent cross the border, so only translate by -mContentXOffset
                    diffX = -mContentXOffset;
                } else if(mContentXOffset + diffX > getWidth() - mMenuRightMargin) {
                    // Don't allow dragging beyond mMenu width
                    diffX = getWidth() - mMenuRightMargin - mContentXOffset;
                }

                // Translate mContent View accordingly
                mContent.offsetLeftAndRight(diffX);

                mContentXOffset += diffX;

                // Invalite this whole MainLayout, causing onLayout() to be called
                this.invalidate();

                mPrevX = curX;
                mLastDiffX = diffX;
                return true;

            case MotionEvent.ACTION_UP:
                //Log.d("MainLayout.java onContentTouch()", "Up x " + curX);

                Log.d("MainLayout.java onContentTouch()", "Up mLastDiffX " + mLastDiffX);

                // Start scrolling
                // Remember that when mContent has a chance to cross left border, mLastDiffX is set to 0
                if(mLastDiffX > 0 || (mLastDiffX == 0 && mIsDragging && mCurrentMenuState == MenuState.HIDDEN)) {
                    // User wants to show mMenu
                    mCurrentMenuState = MenuState.SHOWING;

                    // No need to set to Visible, because we have set to Visible in ACTION_MOVE
                    //mMenu.setVisibility(View.VISIBLE);

                    //Log.d("MainLayout.java onContentTouch()", "Up mContentXOffset " + mContentXOffset);

                    // Start scrolling from mContentXOffset
                    mMenuScroller.startScroll(mContentXOffset, 0, mMenu.getLayoutParams().width - mContentXOffset,
                            0, SLIDING_DURATION);
                } else if(mLastDiffX < 0 || (mLastDiffX == 0 && mCurrentMenuState == MenuState.SHOWN)) {
                    // User wants to hide mMenu
                    mCurrentMenuState = MenuState.HIDING;
                    mMenuScroller.startScroll(mContentXOffset, 0, -mContentXOffset,
                            0, SLIDING_DURATION);
                }

                // Begin querying
                mMenuHandler.postDelayed(mMenuRunnable, QUERY_INTERVAL);

                // Invalite this whole MainLayout, causing onLayout() to be called
                this.invalidate();

                // Done dragging
                mIsDragging = false;
                mPrevX = 0;
                mLastDiffX = 0;
                return true;

            default:
                break;
        }

        return false;
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        int leftPadding = insets.left;
        int rightPadding = insets.right;
        int topPadding = insets.top;
        int bottomPadding = insets.bottom;

        setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
        return true;
    }
}