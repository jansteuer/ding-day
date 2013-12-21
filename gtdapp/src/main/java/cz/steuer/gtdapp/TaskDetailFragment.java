package cz.steuer.gtdapp;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.steuer.gtdapp.dummy.DummyContent;
import cz.steuer.gtdapp.model.TaskContract;

/**
 * A fragment representing a single Task detail screen.
 * This fragment is either contained in a {@link TaskListActivity}
 * in two-pane mode (on tablets) or a {@link TaskDetailActivity}
 * on handsets.
 */
public class TaskDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_URI = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.DummyItem mItem;

    private static final int LOADER_ID = 58;

    private static final String[] PROJ = new String[] {
            TaskContract.TasksColumns._ID,
            TaskContract.TasksColumns.TITLE,
            TaskContract.TasksColumns.CATEGORY,
            TaskContract.TasksColumns.STATE
    };

    private Uri taskUri;
    TextView taskTitleView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TaskDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        taskUri = getArguments().getParcelable(ARG_ITEM_URI);
        if (taskUri != null) {
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), taskUri, PROJ, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        populateView(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_detail, container, false);

        taskTitleView = (TextView) rootView.findViewById(R.id.task_detail);

        return rootView;
    }

    private void populateView(Cursor c) {
        if (!c.moveToNext()) { return; }
        String taskTitle = c.getString(c.getColumnIndex(TaskContract.TasksColumns.TITLE));
        taskTitleView.setText(taskTitle);
    }
}
