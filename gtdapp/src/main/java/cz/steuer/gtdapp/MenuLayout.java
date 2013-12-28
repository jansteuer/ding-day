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
import android.view.Window;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

// DK Trick
// Only the content is moving. Attach an View.OnTouchListener to it to support fling and drag & drop gesture
// The menu stays still
public class MenuLayout extends LinearLayout {

    public static final String TAG = "MenuLayout";
    public static final boolean DEBUG = true;

    // Duration of sliding animation, in miliseconds
    private static final int SLIDING_DURATION = 500;

    // Query Scroller every 16 miliseconds
    private static final int QUERY_INTERVAL = 16;

    private static final int MARGIN_WIDTH = 30;

    // MainLayout width
    int mainLayoutWidth;

    // Sliding menu
    private View menu;

    // Main content
    private View content;

    // menu does not occupy some right space
    // This should be updated correctly later in onMeasure
    private static int menuRightMargin = 150;

    // The state of menu
    private enum MenuState {
        HIDING,
        HIDDEN,
        SHOWING,
        SHOWN,
    };

    // content will be layouted based on this X offset
    // Normally, contentXOffset = menu.getLayoutParams().width = this.getWidth - menuRightMargin
    private int contentXOffset;

    // menu is hidden initially
    private MenuState currentMenuState = MenuState.HIDDEN;

    // Scroller is used to facilitate animation
    private Scroller menuScroller = new Scroller(this.getContext(),
            new EaseInInterpolator());

    // Used to query Scroller about scrolling position
    // Note: The 3rd paramter to startScroll is the distance
    private Runnable menuRunnable = new MenuRunnable();
    private Handler menuHandler = new Handler();

    // Previous touch position
    int prevX = 0;

    // Is user dragging the content
    boolean isDragging = false;

    // Used to facilitate ACTION_UP 
    int lastDiffX = 0;

    public View getMenu() {
        return menu;

    }

    public View getContent() {
        return content;
    }

    // Constructor

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
//        menu = this.getChildAt(0);
//        content = this.getChildAt(1);

        // Attach View.OnTouchListener
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return MenuLayout.this.onContentTouch(v, event);
            }
        });

        // Initially hide the menu
//        menu.setVisibility(View.GONE);
    }

    public void setMenu(int layoutId) {
        this.menu = LayoutInflater.from(getContext()).inflate(layoutId, null);
        this.addView(this.menu, 0);
        menu.setVisibility(View.GONE);

        this.forceLayout();
    }

    // Overriding LinearLayout core methods

    // Ask all children to measure themselves and compute the measurement of this
    // layout based on the children
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mainLayoutWidth = MeasureSpec.getSize(widthMeasureSpec);
//        menuRightMargin = mainLayoutWidth * 10 / 100;
//        menuRightMargin = 300;
        // Nothing to do, since we only care about how to layout
    }

    // This is called when MainLayout is attached to window
    // At this point it has a Surface and will start drawing. 
    // Note that this function is guaranteed to be called before onDraw
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

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
        this.content = decorChild;
//        setContent(decorChild);
    }

    // Called from layout when this view should assign a size and position to each of its children
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int nLeft = getPaddingLeft() + left;
        int nRight = getPaddingRight() + right;
        int nTop = getPaddingTop() + top;
        int nBottom = getPaddingBottom() + bottom;

        // True if MainLayout 's size and position has changed
        // If true, calculate child views size
        if(changed) {
            // Note: LayoutParams are used by views to tell their parents how they want to be laid out

            //Log.d("MainLayout.java onLayout()", "changed " + changed);

            // content View occupies the full height and width
            LayoutParams contentLayoutParams = (LayoutParams)content.getLayoutParams();
            contentLayoutParams.height = this.getHeight();
            contentLayoutParams.width = this.getWidth();

            // menu View occupies the full height, but certain width
            LayoutParams menuLayoutParams = (LayoutParams)menu.getLayoutParams();
            menuLayoutParams.height = this.getHeight();
            menuLayoutParams.width = this.getWidth() - menuRightMargin;
        }

        // Layout the child views
        menu.layout(nLeft, nTop, nRight - menuRightMargin, nBottom);
        content.layout(nLeft + contentXOffset, nTop, nRight + contentXOffset, nBottom);

    }

    // Custom methods for MainLayout

    // Used to show/hide menu accordingly
    public void toggleMenu() {
        // Do nothing if sliding is in progress
        if(currentMenuState == MenuState.HIDING || currentMenuState == MenuState.SHOWING)
            return;

        switch(currentMenuState) {
            case HIDDEN:
                currentMenuState = MenuState.SHOWING;
                menu.setVisibility(View.VISIBLE);
                menuScroller.startScroll(0, 0, menu.getLayoutParams().width,
                        0, SLIDING_DURATION);
                break;
            case SHOWN:
                currentMenuState = MenuState.HIDING;
                menuScroller.startScroll(contentXOffset, 0, -contentXOffset,
                        0, SLIDING_DURATION);
                break;
            default:
                break;
        }

        // Begin querying
        menuHandler.postDelayed(menuRunnable, QUERY_INTERVAL);

        // Invalite this whole MainLayout, causing onLayout() to be called
        this.invalidate();
    }

    public void showMenu() {
        if(currentMenuState == MenuState.HIDDEN) {
            currentMenuState = MenuState.SHOWING;
            menu.setVisibility(View.VISIBLE);
            menuScroller.startScroll(0, 0, menu.getLayoutParams().width,
                    0, SLIDING_DURATION);
        }

        menuHandler.postDelayed(menuRunnable, QUERY_INTERVAL);
        this.invalidate();
    }

    public void hideMenu() {
        if(currentMenuState == MenuState.SHOWN) {
            currentMenuState = MenuState.HIDING;
            menuScroller.startScroll(contentXOffset, 0, -contentXOffset,
                    0, SLIDING_DURATION);
        }

        menuHandler.postDelayed(menuRunnable, QUERY_INTERVAL);
        this.invalidate();
    }

    // Query Scroller
    protected class MenuRunnable implements Runnable {
        @Override
        public void run() {
            boolean isScrolling = menuScroller.computeScrollOffset();
            adjustContentPosition(isScrolling);
        }
    }

    // Adjust content View position to match sliding animation
    private void adjustContentPosition(boolean isScrolling) {
        int scrollerXOffset = menuScroller.getCurrX();

        //Log.d("MainLayout.java adjustContentPosition()", "scrollerOffset " + scrollerOffset);

        // Translate content View accordingly
        content.offsetLeftAndRight(scrollerXOffset - contentXOffset);

        contentXOffset = scrollerXOffset;

        // Invalite this whole MainLayout, causing onLayout() to be called
        this.invalidate();

        // Check if animation is in progress
        if (isScrolling)
            menuHandler.postDelayed(menuRunnable, QUERY_INTERVAL);
        else
            this.onMenuSlidingComplete();
    }

    // Called when sliding is complete
    private void onMenuSlidingComplete() {
        switch (currentMenuState) {
            case SHOWING:
                currentMenuState = MenuState.SHOWN;
                break;
            case HIDING:
                currentMenuState = MenuState.HIDDEN;
                menu.setVisibility(View.GONE);
                break;
            default:
                return;
        }
    }

    // Make scrolling more natural. Move more quickly at the end
    // See the formula here http://cyrilmottier.com/2012/05/22/the-making-of-prixing-fly-in-app-menu-part-1/
    protected class EaseInInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float t) {
            return (float)Math.pow(t-1, 5) + 1;
        }

    }

    // Is menu completely shown
    public boolean isMenuShown() {
        return currentMenuState == MenuState.SHOWN;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        int curX = (int) ev.getRawX();

        if(currentMenuState == MenuState.HIDING || currentMenuState == MenuState.SHOWING) {
            return true;
        }

        if(currentMenuState == MenuState.SHOWN && curX > contentXOffset) {
            return true;
        }


        if(isDragging) {
            return true;
        }

        if(currentMenuState == MenuState.HIDDEN && curX <= MARGIN_WIDTH) {
            return true;
        }

        return false;
    }



    // Handle touch event on content View
    public boolean onContentTouch(View v, MotionEvent event) {
        // Do nothing if sliding is in progress
        if(currentMenuState == MenuState.HIDING || currentMenuState == MenuState.SHOWING)
            return false;

        // getRawX returns X touch point corresponding to screen
        // getX sometimes returns screen X, sometimes returns content View X
        int curX = (int) event.getRawX();
        int diffX = 0;

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(currentMenuState == MenuState.SHOWN && curX < this.contentXOffset) {
                    return false;
                }

                if(DEBUG) Log.d(TAG, "Down x " + curX);
                prevX = curX;
                return true;

            case MotionEvent.ACTION_MOVE:
                if(DEBUG) Log.d(TAG, "Move x " + curX + " prev x " + prevX);

                // Set menu to Visible when user start dragging the content View
                if(!isDragging) {
                    isDragging = true;
                    menu.setVisibility(View.VISIBLE);
                }

                // How far we have moved since the last position
                diffX = curX - prevX;

                // Prevent user from dragging beyond border
                if(contentXOffset + diffX <= 0) {
                    // Don't allow dragging beyond left border
                    // Use diffX will make content cross the border, so only translate by -contentXOffset
                    diffX = -contentXOffset;
                } else if(contentXOffset + diffX > mainLayoutWidth - menuRightMargin) {
                    // Don't allow dragging beyond menu width
                    diffX = mainLayoutWidth - menuRightMargin - contentXOffset;
                }

                // Translate content View accordingly
                content.offsetLeftAndRight(diffX);

                contentXOffset += diffX;

                // Invalite this whole MainLayout, causing onLayout() to be called
                this.invalidate();

                prevX = curX;
                lastDiffX = diffX;
                return true;

            case MotionEvent.ACTION_UP:
                //Log.d("MainLayout.java onContentTouch()", "Up x " + curX);

                Log.d("MainLayout.java onContentTouch()", "Up lastDiffX " + lastDiffX);

                // Start scrolling
                // Remember that when content has a chance to cross left border, lastDiffX is set to 0
                if(lastDiffX > 0 || (lastDiffX == 0 && isDragging && currentMenuState == MenuState.HIDDEN)) {
                    // User wants to show menu
                    currentMenuState = MenuState.SHOWING;

                    // No need to set to Visible, because we have set to Visible in ACTION_MOVE
                    //menu.setVisibility(View.VISIBLE);

                    //Log.d("MainLayout.java onContentTouch()", "Up contentXOffset " + contentXOffset);

                    // Start scrolling from contentXOffset
                    menuScroller.startScroll(contentXOffset, 0, menu.getLayoutParams().width - contentXOffset,
                            0, SLIDING_DURATION);
                } else if(lastDiffX < 0 || (lastDiffX == 0 && currentMenuState == MenuState.SHOWN)) {
                    // User wants to hide menu
                    currentMenuState = MenuState.HIDING;
                    menuScroller.startScroll(contentXOffset, 0, -contentXOffset,
                            0, SLIDING_DURATION);
                }

                // Begin querying
                menuHandler.postDelayed(menuRunnable, QUERY_INTERVAL);

                // Invalite this whole MainLayout, causing onLayout() to be called
                this.invalidate();

                // Done dragging
                isDragging = false;
                prevX = 0;
                lastDiffX = 0;
                return true;

            default:
                break;
        }

        return false;
    }

    @SuppressLint("NewApi")
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