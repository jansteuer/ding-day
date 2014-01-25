package eu.dingday.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import eu.dingday.app.enums.TaskCategory;
import eu.dingday.app.model.TaskContract;

public class MenuFragment  extends Fragment implements View.OnClickListener, View.OnDragListener {

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    private Button mButtonInbox;
    private Button mButtonNext;
    private Button mButtonCalendar;
    private Button mButtonWaiting;
    private Button mButtonSuspended;


    public MenuFragment() {
    }

    @Override
    public void onClick(View v) {
        if(v == mButtonInbox) {
            mCallbacks.onCategorySelected(TaskCategory.INBOX);
        } else if(v == mButtonNext) {
            mCallbacks.onCategorySelected(TaskCategory.NEXT);
        } else if(v == mButtonCalendar) {
            mCallbacks.onCategorySelected(TaskCategory.SCHEDULED);
        } else if(v == mButtonWaiting) {
            mCallbacks.onCategorySelected(TaskCategory.WAITING);
        } else if(v == mButtonSuspended) {
            mCallbacks.onCategorySelected(TaskCategory.SUSPENDED);
        }
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch(event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                if(!"Task".equals(event.getClipDescription().getLabel())) {
                    return false;
                }
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                System.err.println("button entered");
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                System.err.println("button exited");
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                break;
            case DragEvent.ACTION_DROP:
                TaskCategory category;

                if(v == mButtonNext) {
                    category = TaskCategory.NEXT;
                } else if(v == mButtonCalendar) {
                    category = TaskCategory.SCHEDULED;
                } else if(v == mButtonWaiting) {
                    category = TaskCategory.WAITING;
                } else if(v == mButtonSuspended) {
                    category = TaskCategory.SUSPENDED;
                } else {
                    category = TaskCategory.INBOX;
                }



                ContentValues values = new ContentValues();
                values.put(TaskContract.TasksColumns.CATEGORY, category.toString());

                Uri taskUri = Uri.withAppendedPath(TaskContract.Tasks.CONTENT_URI, event.getClipData().getItemAt(0).getText().toString());
                AsyncTask task = new UpdateTask3(getActivity(), taskUri, values);
                task.execute("");
                break;
        }
        return true;
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


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        public void onCategorySelected(TaskCategory category);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onCategorySelected(TaskCategory category) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_menu, container, false);

        mButtonInbox = (Button) rootView.findViewById(R.id.menu_item_inbox);
        mButtonNext = (Button) rootView.findViewById(R.id.menu_item_next);
        mButtonCalendar = (Button) rootView.findViewById(R.id.menu_item_calendar);
        mButtonWaiting = (Button) rootView.findViewById(R.id.menu_item_waiting);
        mButtonSuspended = (Button) rootView.findViewById(R.id.menu_item_suspended);

        mButtonInbox.setOnClickListener(this);
        mButtonNext.setOnClickListener(this);
        mButtonCalendar.setOnClickListener(this);
        mButtonWaiting.setOnClickListener(this);
        mButtonSuspended.setOnClickListener(this);

        mButtonInbox.setOnDragListener(this);
        mButtonNext.setOnDragListener(this);
        mButtonCalendar.setOnDragListener(this);
        mButtonWaiting.setOnDragListener(this);
        mButtonSuspended.setOnDragListener(this);
        return rootView;
    }
}
