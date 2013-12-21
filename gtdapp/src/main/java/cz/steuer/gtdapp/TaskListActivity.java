package cz.steuer.gtdapp;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.animation.Interpolator;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import cz.steuer.gtdapp.enums.TaskCategory;


/**
 * An activity representing a list of Tasks. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link TaskDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link TaskListFragment} and the item details
 * (if present) is a {@link TaskDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link TaskListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class TaskListActivity extends FragmentActivity
        implements TaskListFragment.Callbacks, MenuFragment.Callbacks {

    private static final String TAG_FRAGMENT_TASKS = "task_list";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_task_list);

        Bundle arguments = new Bundle();
        arguments.putString(TaskListFragment.ARG_CATEGORY, TaskCategory.INBOX.toString());
        TaskListFragment fragment = new TaskListFragment();
        fragment.setArguments(arguments);
        getFragmentManager().beginTransaction()
                .add(R.id.task_list_container, fragment, TAG_FRAGMENT_TASKS)
                .commit();

        if (findViewById(R.id.task_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((TaskListFragment) getFragmentManager()
                    .findFragmentById(R.id.task_list))
                    .setActivateOnItemClick(true);
        }

        if(findViewById(R.id.menu_fragment) == null) {
            // configure the SlidingMenu
            SlidingMenu menu = new SlidingMenu(this, SlidingMenu.SLIDING_WINDOW);
            menu.setMode(SlidingMenu.LEFT);
            menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
            menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
            menu.setFadeDegree(0.35f);
            menu.setMenu(R.layout.menu_frame);
            menu.setBehindScrollScale(0.0f);

            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.menu_frame, new MenuFragment())
                    .commit();
        }


    }

    /**
     * Callback method from {@link TaskListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(TaskDetailFragment.ARG_ITEM_ID, id);
            TaskDetailFragment fragment = new TaskDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.task_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, TaskDetailActivity.class);
            detailIntent.putExtra(TaskDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    @Override
    public void onCategorySelected(TaskCategory category) {
        Bundle arguments = new Bundle();
        arguments.putString(TaskListFragment.ARG_CATEGORY, category.toString());
        TaskListFragment fragment = new TaskListFragment();
        fragment.setArguments(arguments);
        getFragmentManager().beginTransaction()
                .replace(R.id.task_list_container, fragment, TAG_FRAGMENT_TASKS)
                .commit();

    }
}
