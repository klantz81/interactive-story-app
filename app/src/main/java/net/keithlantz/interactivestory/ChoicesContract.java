package net.keithlantz.interactivestory;

import android.provider.BaseColumns;

/**
 * Created by keith on 12/9/17.
 */

public final class ChoicesContract {
    private ChoicesContract() {

    }

    public static class ChoicesEntry implements BaseColumns {
        public static final String TABLE_NAME = "choices";
        public static final String COLUMN_NAME_CHOICE_ID = "choice_id";
        public static final String COLUMN_NAME_CHOICE_TEXT = "choice_text";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    }
}
