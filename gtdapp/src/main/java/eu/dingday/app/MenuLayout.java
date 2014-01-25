package eu.dingday.app;

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

    private enum MenuState {
        HIDING, HIDDEN, SHOWING, SHOWN, PEEPED
    }

    // Make scrolling more natural. Move more quickly at the end
    // See the formula here http://cyrilmottier.com/2012/05/22/the-making-of-prixing-fly-in-app-mMenu-part-1/
    protected class EaseInInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float t) {
            return (float) Math.pow(t-1, 5) + 1;
        }
    }

    protected class MenuRunnable implements Runnable {
        @Override
        public void run() {
            boolean isScrolling = mMenuScroller.computeScrollOffset();
            adjustContentPosition(isScrolling);
        }
    }

    private static final String TAG = "MenuLayout";
    private static final boolean DEBUG = true;

    private static final int SLIDING_DURATION = 500; //miliseconds
    private static final int QUERY_INTERVAL = 16; //miliseconds
    private static final int MARGIN_WIDTH = 30; //pixels

    private View mContent;
    private View mMenu = null;

    private int mMenuRightMargin = 150; //pixels
    private int mMenuPeepWidth = 50; //pixels

    private MenuState mCurMenuState = MenuState.HIDDEN;
    private int mCurContentXOffset; //pixels
    private boolean mIsDragging = false;
    private boolean mIsPeeped = false;

    private Scroller mMenuScroller = new Scroller(getContext(), new EaseInInterpolator());
    private Runnable mMenuRunnable = new MenuRunnable();
    private Handler mMenuHandler = new Handler();

    int mPrevX = 0;
    int mLastDiffX = 0;

    public MenuLayout(Activity activity, int menuResource, AttributeSet attrs, int defStyle) {
        super(activity, attrs, defStyle);
        makeAsToplevel(activity);
        setMenu(menuResource);
    }

    public MenuLayout(Activity activity, int menuResource, AttributeSet attrs) {
        super(activity, attrs);
        makeAsToplevel(activity);
        setMenu(menuResource);
    }

    public MenuLayout(Activity activity, int menuResource) {
        super(activity);
        makeAsToplevel(activity);
        setMenu(menuResource);
    }

    /**
     * Make as direct child of Window.
     * It's done that way because we want to slide action bar when displaying menu.
     */
    private void makeAsToplevel(Activity activity) {
        TypedArray a = activity.getTheme().obtainStyledAttributes(new int[] {android.R.attr.windowBackground});
        int background = a.getResourceId(0, 0);
        a.recycle();

        ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
        ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);

        decorChild.setBackgroundResource(background);
        decor.removeView(decorChild);
        decor.addView(this);
        this.addView(decorChild);

        this.mContent = decorChild;
    }

    public View getMenu() {
        return mMenu;
    }

    public View getContent() {
        return mContent;
    }

    private void setMenu(int resourceId) {
        mMenu = LayoutInflater.from(getContext()).inflate(resourceId, null);
        addView(mMenu, 0);
        mMenu.setVisibility(View.GONE);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int nLeft = getPaddingLeft() + left;
        int nRight = getPaddingRight() + right;
        int nTop = getPaddingTop() + top;
        int nBottom = getPaddingBottom() + bottom;

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

        mMenu.layout(nLeft, nTop, nRight - mMenuRightMargin, nBottom);
        mContent.layout(nLeft + mCurContentXOffset, nTop, nRight + mCurContentXOffset, nBottom);
    }

    public void toggleMenu() {
        switch(mCurMenuState) {
            case HIDDEN:
               showMenu();
                break;
            case SHOWN:
                hideMenu();
                break;
        }
    }

    public void peepMenu() {
        if(mCurMenuState == MenuState.HIDDEN) {
            mIsPeeped = true;
            mMenu.setVisibility(View.VISIBLE);
            mMenuScroller.startScroll(0, 0, mMenuPeepWidth, 0, SLIDING_DURATION);
            mMenuHandler.postDelayed(mMenuRunnable, QUERY_INTERVAL);
            this.invalidate();
        }
    }

    public void showMenu() {

        if(mCurMenuState == MenuState.HIDDEN || mCurMenuState == MenuState.PEEPED) {
            int startX = (mCurMenuState == MenuState.HIDDEN) ? 0 : mMenuPeepWidth;
            mCurMenuState = MenuState.SHOWING;
            mMenu.setVisibility(View.VISIBLE);
            mMenuScroller.startScroll(startX, 0, mMenu.getLayoutParams().width - startX, 0, SLIDING_DURATION);
            mMenuHandler.postDelayed(mMenuRunnable, QUERY_INTERVAL);
            this.invalidate();
        }
    }

    public void hideMenu() {
        hideMenu(true);
    }

    public void hideMenu(boolean force) {
        if(mCurMenuState == MenuState.SHOWN || (force && mCurMenuState == MenuState.PEEPED)) {
            int distance = -mCurContentXOffset;
            if(!force && mIsPeeped) {
                distance += mMenuPeepWidth;
            } else {
                mIsPeeped = false;
            }

            mCurMenuState = MenuState.HIDING;
            mMenuScroller.startScroll(mCurContentXOffset, 0, distance, 0, SLIDING_DURATION);
            mMenuHandler.postDelayed(mMenuRunnable, QUERY_INTERVAL);
            this.invalidate();
        }
    }

    public boolean isMenuShown() {
        return mCurMenuState == MenuState.SHOWN;
    }

    // Adjust mContent View position to match sliding animation
    private void adjustContentPosition(boolean isScrolling) {
        int scrollerXOffset = mMenuScroller.getCurrX();

        //Log.d("MainLayout.java adjustContentPosition()", "scrollerOffset " + scrollerOffset);

        // Translate mContent View accordingly
        mContent.offsetLeftAndRight(scrollerXOffset - mCurContentXOffset);

        mCurContentXOffset = scrollerXOffset;

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
        switch (mCurMenuState) {
            case SHOWING:
                if(mCurContentXOffset == mMenuPeepWidth) {
                    mCurMenuState = MenuState.PEEPED;
                } else {
                    mCurMenuState = MenuState.SHOWN;
                }
                break;
            case HIDING:
                if(mCurContentXOffset > 0) {
                    mCurMenuState = MenuState.PEEPED;
                } else {
                    mCurMenuState = MenuState.HIDDEN;
                    mMenu.setVisibility(View.GONE);
                }
                break;
            default:
                return;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        int curX = (int) ev.getRawX();

        if(mCurMenuState == MenuState.HIDING || mCurMenuState == MenuState.SHOWING) {
            return true;
        }

        if(mCurMenuState == MenuState.SHOWN && curX > mCurContentXOffset) {
            return true;
        }


        if(mIsDragging) {
            return true;
        }

        if(mCurMenuState == MenuState.HIDDEN && curX <= MARGIN_WIDTH) {
            return true;
        }

        return false;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Do nothing if sliding is in progress
        if(mCurMenuState == MenuState.HIDING || mCurMenuState == MenuState.SHOWING)
            return false;

        // getRawX returns X touch point corresponding to screen
        // getX sometimes returns screen X, sometimes returns mContent View X
        int curX = (int) event.getRawX();
        int diffX = 0;

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(mCurMenuState == MenuState.SHOWN && curX < this.mCurContentXOffset) {
                    return false;
                }

                mPrevX = curX;
                return true;

            case MotionEvent.ACTION_MOVE:
                // Set mMenu to Visible when user start dragging the mContent View
                if(!mIsDragging) {
                    mIsDragging = true;
                    mMenu.setVisibility(View.VISIBLE);
                }

                // How far we have moved since the last position
                diffX = curX - mPrevX;

                // Prevent user from dragging beyond border
                if(mCurContentXOffset + diffX <= 0) {
                    // Don't allow dragging beyond left border
                    // Use diffX will make mContent cross the border, so only translate by -mCurContentXOffset
                    diffX = -mCurContentXOffset;
                } else if(mCurContentXOffset + diffX > getWidth() - mMenuRightMargin) {
                    // Don't allow dragging beyond mMenu width
                    diffX = getWidth() - mMenuRightMargin - mCurContentXOffset;
                }

                // Translate mContent View accordingly
                mContent.offsetLeftAndRight(diffX);

                mCurContentXOffset += diffX;

                // Invalite this whole MainLayout, causing onLayout() to be called
                this.invalidate();

                mPrevX = curX;
                mLastDiffX = diffX;
                return true;

            case MotionEvent.ACTION_UP:
                // Start scrolling
                // Remember that when mContent has a chance to cross left border, mLastDiffX is set to 0
                if(mLastDiffX > 0 || (mLastDiffX == 0 && mIsDragging && mCurMenuState == MenuState.HIDDEN)) {
                    // User wants to show mMenu
                    mCurMenuState = MenuState.SHOWING;

                    // No need to set to Visible, because we have set to Visible in ACTION_MOVE
                    //mMenu.setVisibility(View.VISIBLE);

                    //Log.d("MainLayout.java onContentTouch()", "Up mCurContentXOffset " + mCurContentXOffset);

                    // Start scrolling from mCurContentXOffset
                    mMenuScroller.startScroll(mCurContentXOffset, 0, mMenu.getLayoutParams().width - mCurContentXOffset,
                            0, SLIDING_DURATION);
                } else if(mLastDiffX < 0 || (mLastDiffX == 0 && mCurMenuState == MenuState.SHOWN)) {
                    // User wants to hide mMenu
                    mCurMenuState = MenuState.HIDING;
                    mMenuScroller.startScroll(mCurContentXOffset, 0, -mCurContentXOffset,
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