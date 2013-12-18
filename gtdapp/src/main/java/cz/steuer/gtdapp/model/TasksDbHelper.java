package cz.steuer.gtdapp.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by honza on 12/18/13.
 */
public class TasksDbHelper extends SQLiteOpenHelper {

    public static final String TAB_TASK = "task";

    public static final String COL_ID = "id";
    public static final String COL_REMOTE_ID = "remote_id";

    public static final String COL_TITLE = "title";
    public static final String COL_CATEGORY = "category";
    public static final String COL_STATE = "state";

    private static final int VERSION = 1;

    private static final String DB_FILE = "tasks.db";

    public TasksDbHelper(Context context) {
        super(context, DB_FILE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
