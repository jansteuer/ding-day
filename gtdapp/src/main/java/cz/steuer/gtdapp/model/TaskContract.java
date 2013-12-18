package cz.steuer.gtdapp.model;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by honza on 12/18/13.
 */
public class TaskContract {

    /** The authority for the contacts provider */
    public static final String AUTHORITY = "cz.steuer.gtdapp";
    /** A content:// style uri to the authority for the contacts provider */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * An optional URI parameter for insert, update, or delete queries
     * that allows the caller
     * to specify that it is a sync adapter. The default value is false. If true
     * {@link RawContacts#DIRTY} is not automatically set and the
     * "syncToNetwork" parameter is set to false when calling
     * {@link
     * ContentResolver#notifyChange(android.net.Uri, android.database.ContentObserver, boolean)}.
     * This prevents an unnecessary extra synchronization, see the discussion of
     * the delete operation in {@link RawContacts}.
     */
    public static final String CALLER_IS_SYNCADAPTER = "caller_is_syncadapter";

    /**
     * Query parameter that should be used by the client to access a specific
     * {@link Directory}. The parameter value should be the _ID of the corresponding
     * directory, e.g.
     * {@code content://com.android.contacts/data/emails/filter/acme?directory=3}
     */
    public static final String DIRECTORY_PARAM_KEY = "directory";

    /**
     * A query parameter that limits the number of results returned. The
     * parameter value should be an integer.
     */
    public static final String LIMIT_PARAM_KEY = "limit";

    /**
     * A query parameter specifing a primary account. This parameter should be used with
     * {@link #PRIMARY_ACCOUNT_TYPE}. The contacts provider handling a query may rely on
     * this information to optimize its query results.
     *
     * For example, in an email composition screen, its implementation can specify an account when
     * obtaining possible recipients, letting the provider know which account is selected during
     * the composition. The provider may use the "primary account" information to optimize
     * the search result.
     */
    public static final String PRIMARY_ACCOUNT_NAME = "name_for_primary_account";

    /**
     * A query parameter specifing a primary account. This parameter should be used with
     * {@link #PRIMARY_ACCOUNT_NAME}. See the doc in {@link #PRIMARY_ACCOUNT_NAME}.
     */
    public static final String PRIMARY_ACCOUNT_TYPE = "type_for_primary_account";

    public static class Tasks {
        private Tasks() {}

        public static final String TABLE = "tasks";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE);

        public static final String MIME_TYPE = "/cz.steuer.gtdapp.contacts";

        /** Contacts DIR type */
        public static final String CONTENT_TYPE_DIR
                = ContentResolver.CURSOR_DIR_BASE_TYPE + MIME_TYPE;

        /** Contacts ITEM type */
        public static final String CONTENT_TYPE_ITEM
                = ContentResolver.CURSOR_ITEM_BASE_TYPE + MIME_TYPE;

        public static final int STATE_DONE = 1;
        public static final int STATE_NOT_DONE = 0;

    }


    public static interface TasksColumns extends BaseColumns {

        public static final String TITLE = "title"; //string
        public static final String STATE = "state"; //int
        public static final String CATEGORY = "category"; //string

    }

}
