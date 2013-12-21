package cz.steuer.gtdapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import java.util.concurrent.ExecutionException;

import cz.steuer.gtdapp.enums.TaskCategory;
import cz.steuer.gtdapp.model.TaskContract;


public class NewTaskActivity extends FragmentActivity implements NewTaskFragment.Callbacks {

    public static final String ARG_TASK_CATEGORY = "task_category";

    private TaskCategory taskCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_new);

        taskCategory = (TaskCategory) getIntent().getSerializableExtra(ARG_TASK_CATEGORY);
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(taskCategory.getColor())));

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            NewTaskFragment fragment = new NewTaskFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.task_new_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent upIntent = new Intent(this, TaskListActivity.class);
            upIntent.putExtra(TaskListActivity.ARG_TASK_CATEGORY, taskCategory);
            NavUtils.navigateUpTo(this, upIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTaskAdd(String title) {



        ContentValues values = new ContentValues();
        values.put(TaskContract.TasksColumns.TITLE, title);
        values.put(TaskContract.TasksColumns.CATEGORY, taskCategory.toString());
        values.put(TaskContract.TasksColumns.STATE, TaskContract.Tasks.STATE_NOT_DONE);

        AsyncInsertTask insertTask = new AsyncInsertTask(this, values);
        insertTask.execute();
        Uri uri;
        try {
            uri = insertTask.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Intent detailIntent = new Intent(this, TaskDetailActivity.class);
        detailIntent.putExtra(TaskDetailFragment.ARG_ITEM_URI, uri);
        startActivity(detailIntent);
    }

    private static final class AsyncInsertTask extends AsyncTask<Void, Void, Uri> {

        private final ContentValues values;
        private Context context;

        public AsyncInsertTask(Context context, ContentValues values) {
            this.context = context;
            this.values = values;
        }

        @Override
        protected Uri doInBackground(Void... params) {
            Uri result = context.getContentResolver().insert(TaskContract.Tasks.CONTENT_URI, values);
            return result;
        }
    }
}
