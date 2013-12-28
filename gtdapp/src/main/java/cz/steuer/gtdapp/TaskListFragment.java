package cz.steuer.gtdapp;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ClipData;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import cz.steuer.gtdapp.dummy.DummyContent;
import cz.steuer.gtdapp.enums.TaskCategory;
import cz.steuer.gtdapp.model.TaskContract;

/**
 * A list fragment representing a list of Tasks. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link TaskDetailFragment}.
 * <p>
 * interface.
 */
public class TaskListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    private static final int LOADER_ID = 42;

    public static final String ARG_CATEGORY = "category";



    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private TaskItemAdapter listAdapter;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String category = args.getString(ARG_CATEGORY);

        return new CursorLoader(
                this.getActivity(),
                TaskContract.Tasks.CONTENT_URI,
                new String[] {
                        TaskContract.TasksColumns._ID,
                        TaskContract.TasksColumns.TITLE,
                        TaskContract.TasksColumns.CATEGORY,
                        TaskContract.TasksColumns.STATE
                },
                TaskContract.TasksColumns.CATEGORY + " = ?",
                new String[] {category},
                TaskContract.TasksColumns.STATE + " ASC, " + TaskContract.TasksColumns.FINISHED_TS + " DESC, " + TaskContract.TasksColumns.TITLE + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        listAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        listAdapter.swapCursor(null);
    }


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TaskListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listAdapter = new TaskItemAdapter(getActivity(), null, false);
        listAdapter.setCallbacksListener((TaskItemAdapter.Callbacks) getActivity());

        setListAdapter(listAdapter);

        Bundle args = getArguments();
        if(args == null || !args.containsKey(ARG_CATEGORY)) {
            args = new Bundle();
            args.putString(ARG_CATEGORY, TaskCategory.NEXT.toString());
        }
        getLoaderManager().initLoader(LOADER_ID, args, this);


    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ClipData clipData = ClipData.newPlainText("Task", Long.toString(id));
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

                parent.startDrag(clipData, shadowBuilder, parent, 0);
                return true;
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof TaskItemAdapter.Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }


    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        listAdapter.setCallbacksListener(null);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }



}
