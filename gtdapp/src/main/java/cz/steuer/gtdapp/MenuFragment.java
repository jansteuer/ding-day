package cz.steuer.gtdapp;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import cz.steuer.gtdapp.enums.TaskCategory;

public class MenuFragment  extends Fragment implements View.OnClickListener {

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

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MenuFragment() {
    }

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
        return rootView;
    }
}
