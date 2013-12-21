package cz.steuer.gtdapp.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import cz.steuer.gtdapp.enums.TaskCategory;

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

        db.execSQL(
                "CREATE TABLE " + TAB_TASK + "("
                        + COL_ID + " integer PRIMARY KEY AUTOINCREMENT,"
                        + COL_TITLE + " text,"
                        + COL_CATEGORY + " string,"
                        + COL_STATE + " integer DEFAULT 0,"
                        + COL_REMOTE_ID + " string UNIQUE" +
                        ")");

        ContentValues values = new ContentValues();
        values.put(COL_TITLE, "in next category");
        values.put(COL_CATEGORY, TaskCategory.NEXT.toString());
        db.insert(TAB_TASK, null, values);

        values.put(COL_TITLE, "in inbox category");
        values.put(COL_CATEGORY, TaskCategory.INBOX.toString());
        db.insert(TAB_TASK, null, values);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try { db.execSQL("drop table " + TAB_TASK); }
        catch (SQLiteException e) { }
        onCreate(db);
    }
}
