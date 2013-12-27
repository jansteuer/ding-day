package cz.steuer.gtdapp;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import cz.steuer.gtdapp.enums.TaskCategory;
import cz.steuer.gtdapp.model.TaskContract;


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
 * {@link TaskItemAdapter.Callbacks} interface
 * to listen for item selections.
 */
public class TaskListActivity extends FragmentActivity
        implements TaskItemAdapter.Callbacks, MenuFragment.Callbacks {

    private static final String TAG_FRAGMENT_TASKS = "task_list";
    public static final String ARG_TASK_CATEGORY = "task_category";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private SlidingMenu menu = null;
    private TaskCategory currentCategory = null;
    private TextView mViewTitle;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_add_task:
                Intent detailIntent = new Intent(this, NewTaskActivity.class);
                detailIntent.putExtra(NewTaskActivity.ARG_TASK_CATEGORY, currentCategory);
                startActivity(detailIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setCustomView(R.layout.actionbar_list);
        mViewTitle = (TextView) getActionBar().getCustomView().findViewById(R.id.actionbar_title);

        currentCategory = (TaskCategory) getIntent().getSerializableExtra(ARG_TASK_CATEGORY);
        onCategorySelected(currentCategory != null ? currentCategory : TaskCategory.NEXT);

//        Bundle arguments = new Bundle();
//        arguments.putString(TaskListFragment.ARG_CATEGORY, TaskCategory.INBOX.toString());
//        TaskListFragment fragment = new TaskListFragment();
//        fragment.setArguments(arguments);
//        getFragmentManager().beginTransaction()
//                .add(R.id.task_list_container, fragment, TAG_FRAGMENT_TASKS)
//                .commit();

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
            menu = new SlidingMenu(this, SlidingMenu.SLIDING_WINDOW);
            menu.setMode(SlidingMenu.LEFT);
            menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
            menu.setBehindWidthRes(R.dimen.slidingmenu_width);
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
     * Callback method from {@link TaskItemAdapter.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(long id) {
        Uri taskUri = Uri.withAppendedPath(TaskContract.Tasks.CONTENT_URI, Long.toString(id));

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(TaskDetailFragment.ARG_ITEM_URI, taskUri);
            TaskDetailFragment fragment = new TaskDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.task_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, TaskDetailActivity.class);
            detailIntent.putExtra(TaskDetailFragment.ARG_ITEM_URI, taskUri);
            startActivity(detailIntent);
        }
    }

    @Override
    public void onItemChecked(long id, boolean checked) {
        Uri taskUri = Uri.withAppendedPath(TaskContract.Tasks.CONTENT_URI, Long.toString(id));
        ContentValues values = new ContentValues();
        values.put(TaskContract.TasksColumns.STATE, checked ? TaskContract.Tasks.STATE_DONE : TaskContract.Tasks.STATE_NOT_DONE);

        AsyncTask task = new UpdateTask3(this, taskUri, values);
        task.execute("");

    }

    @Override
    public void onCategorySelected(TaskCategory category) {
        this.currentCategory = category;

        mViewTitle.setText(category.getTitle());
        mViewTitle.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(category.getIcon()), null, null, null);
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(category.getColor())));

        Bundle arguments = new Bundle();
        arguments.putString(TaskListFragment.ARG_CATEGORY, category.toString());
        TaskListFragment fragment = new TaskListFragment();
        fragment.setArguments(arguments);
        getFragmentManager().beginTransaction()
                .replace(R.id.task_list_container, fragment, TAG_FRAGMENT_TASKS)
                .commit();

        if(menu != null) {
            menu.showContent(true);
        }

    }

    private static final class UpdateTask3 extends AsyncTask<Object, Void, Uri> {
        Activity activity;
        Uri uri;
        ContentValues values;

        public UpdateTask3(Activity activity, Uri uri, ContentValues values) {
            this.activity = activity;
            this.uri = uri;
            this.values = values;
        }


        @Override
        protected Uri doInBackground(Object... params) {
            activity.getContentResolver().update(uri, values, null, null);
            return uri;
        }
    }
}
