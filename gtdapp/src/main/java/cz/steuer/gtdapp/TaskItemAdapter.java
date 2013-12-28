package cz.steuer.gtdapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CheckBox;

import cz.steuer.gtdapp.model.TaskContract;

/**
 * Created by honza on 12/23/13.
 */
public class TaskItemAdapter extends CursorAdapter {

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(long id);

        public void onItemChecked(long id, boolean checked);

        public void onItemDragStarted(long id, View view);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(long id) {
        }

        @Override
        public void onItemChecked(long id, boolean checked) {
        }

        @Override
        public void onItemDragStarted(long id, View view) {

        }

    };

    public void setCallbacksListener(TaskItemAdapter.Callbacks listener) {

        mCallbacks = listener;
    }

    public TaskItemAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.activity_task_list_item,null,true);

        ViewHolder holder = new ViewHolder();
        holder.taskTitle = (TextView) rowView.findViewById(R.id.task_title);
        holder.tastState = (CheckBox) rowView.findViewById(R.id.task_state);

        long id = cursor.getLong(cursor.getColumnIndex(TaskContract.TasksColumns._ID));
        holder.id = id;

        View rootLayout = rowView.findViewById(R.id.root_layout);
        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder holder = (ViewHolder) v.getTag();
                mCallbacks.onItemSelected(holder.id);
            }
        });

        rootLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ViewHolder holder = (ViewHolder) v.getTag();
                mCallbacks.onItemDragStarted(holder.id, v);
                return true;
            }
        });

        holder.tastState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder holder = (ViewHolder) ((View)v.getParent()).getTag();
                mCallbacks.onItemChecked(holder.id, holder.tastState.isChecked());
            }
        });

        rowView.setTag(holder);

        return rowView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String taskTitle = cursor.getString(cursor.getColumnIndex(TaskContract.TasksColumns.TITLE));
        int taskState = cursor.getInt(cursor.getColumnIndex(TaskContract.TasksColumns.STATE));

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.taskTitle.setText(taskTitle);
        holder.tastState.setChecked(taskState == TaskContract.Tasks.STATE_DONE);

        long id = cursor.getLong(cursor.getColumnIndex(TaskContract.TasksColumns._ID));
        holder.id = id;
    }

    static class ViewHolder {
        long id;
        TextView taskTitle;
        CheckBox tastState;
    }
}
