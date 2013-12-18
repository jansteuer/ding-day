package cz.steuer.gtdapp.model;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import net.callmeike.android.data.util.ColumnMap;
import net.callmeike.android.data.util.ProjectionMap;

import java.util.UUID;

/**
 * Created by honza on 12/18/13.
 */
public class TasksContentProvider extends ContentProvider {

    private static final String PK_CONSTRAINT = TasksDbHelper.COL_ID + "=";

    private static final int URI_TASKS_DIR = 1;
    private static final int URI_TASKS_ITEM = 2;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(
                TaskContract.AUTHORITY,
                TaskContract.Tasks.TABLE,
                URI_TASKS_DIR);
        uriMatcher.addURI(
                TaskContract.AUTHORITY,
                TaskContract.Tasks.TABLE + "/#",
                URI_TASKS_ITEM);
    }

    private static final ProjectionMap TASKS_PROJ_MAP = new ProjectionMap.Builder()
            .addColumn(TaskContract.TasksColumns._ID, TasksDbHelper.COL_ID)
            .addColumn(TaskContract.TasksColumns.TITLE, TasksDbHelper.COL_TITLE)
            .addColumn(TaskContract.TasksColumns.CATEGORY, TasksDbHelper.COL_CATEGORY)
            .addColumn(TaskContract.TasksColumns.STATE, TasksDbHelper.COL_STATE)
            .build();

    private static final ColumnMap TASKS_COL_MAP = new ColumnMap.Builder()
            .addColumn(
                    TaskContract.TasksColumns._ID,
                    TasksDbHelper.COL_ID,
                    ColumnMap.Type.LONG)
            .addColumn(
                    TaskContract.TasksColumns.TITLE,
                    TasksDbHelper.COL_TITLE,
                    ColumnMap.Type.STRING)
            .addColumn(
                    TaskContract.TasksColumns.CATEGORY,
                    TasksDbHelper.COL_CATEGORY,
                    ColumnMap.Type.STRING)
            .addColumn(
                    TaskContract.TasksColumns.STATE,
                    TasksDbHelper.COL_STATE,
                    ColumnMap.Type.STRING)
            .build();

    private volatile TasksDbHelper helper;

    @Override
    public boolean onCreate() {
        this.helper = new TasksDbHelper(getContext());
        return null != helper;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case URI_TASKS_DIR:
                return TaskContract.Tasks.CONTENT_TYPE_DIR;
            case URI_TASKS_ITEM:
                return TaskContract.Tasks.CONTENT_TYPE_ITEM;
            default:
                return null;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        long pk = -1;
        String table;
        ProjectionMap projectionMap;
        switch (uriMatcher.match(uri)) {
            case URI_TASKS_ITEM:
                pk = ContentUris.parseId(uri);
            case URI_TASKS_DIR:
                table = TasksDbHelper.TAB_TASK;
                projectionMap = TASKS_PROJ_MAP;
                break;
            default:
                throw new IllegalArgumentException("Unrecognized URI: " + uri);
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            qb.setStrict(true);
        }

        qb.setProjectionMap(projectionMap.getProjectionMap());

        qb.setTables(table);

        if (0 <= pk) {
            qb.appendWhere("(" + PK_CONSTRAINT + pk + ")");
        }

        Cursor cur = qb.query(getDb(), projection, selection, selectionArgs, null, null, sortOrder);

        cur.setNotificationUri(getContext().getContentResolver(), uri);

        return cur;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (uriMatcher.match(uri)) {
            case URI_TASKS_DIR:
                break;
            default:
                throw new UnsupportedOperationException(
                        "Unrecognized URI: " + uri);
        }

        if (0 >= values.size() ) { return null; }

        // create a remote id if there isn't one
        if (null == values.getAsString(TasksDbHelper.COL_REMOTE_ID)) {
            values.put(TasksDbHelper.COL_REMOTE_ID, UUID.randomUUID().toString());
        }

        long pk = getDb().insert(
                TasksDbHelper.TAB_TASK,
                null,
                values);

        if (pk < 0) {
            uri = null;
        } else {
            uri = uri.buildUpon().appendPath(String.valueOf(pk)).build();
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return uri;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String table;

        switch (uriMatcher.match(uri)) {
            case URI_TASKS_ITEM:
                selection = addPkConstraint(uri, selection);
            case URI_TASKS_DIR:
                table = TasksDbHelper.TAB_TASK;
            break;
            default:
                throw new IllegalArgumentException("Unrecognized URI: " + uri);
        }

        int deleted = getDb().delete(
                table,
                selection,
                selectionArgs);

        // not clear this is necessary.
        // The record was already marked deleted (so invisible to queries)
        if (deleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String table;
        ColumnMap colMap;
        switch (uriMatcher.match(uri)) {
            case URI_TASKS_ITEM:
                selection = addPkConstraint(uri, selection);
            case URI_TASKS_DIR:
                table = TasksDbHelper.TAB_TASK;
                colMap = TASKS_COL_MAP;
                break;
            default:
                throw new IllegalArgumentException("Unrecognized URI: " + uri);
        }

        values = colMap.translateCols(values);

        int updated = getDb().update(
                table,
                values,
                selection,
                selectionArgs);

        if (updated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updated;
    }

    private String addPkConstraint(Uri uri, String sel) {
        String pkConstraint = PK_CONSTRAINT + ContentUris.parseId(uri);
        sel = (null == sel)
                ? pkConstraint
                : "(" + pkConstraint + ") AND (" + sel + ")";
        return sel;
    }

    private SQLiteDatabase getDb() {
        return helper.getWritableDatabase();
    }
}
