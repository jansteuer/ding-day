package eu.dingday.app;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class NewTaskFragment extends Fragment implements View.OnClickListener {

    public Callbacks mCallbacks = new Callbacks() {
        @Override
        public void onTaskAdd(String title) {}
    };

    private Button buttonTaskAdd;
    private EditText viewTaskTitle;

    @Override
    public void onClick(View v) {
        String taskTitle = viewTaskTitle.getText().toString();
        mCallbacks.onTaskAdd(taskTitle);
    }

    public interface Callbacks {
        public void onTaskAdd(String title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_new, container, false);
        viewTaskTitle = (EditText) rootView.findViewById(R.id.task_title);
        buttonTaskAdd = (Button) rootView.findViewById(R.id.task_add);
        buttonTaskAdd.setOnClickListener(this);
        return rootView;
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

}
