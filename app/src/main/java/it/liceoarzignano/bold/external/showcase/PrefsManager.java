/**
 * Copyright 2015 Dean Wild
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.liceoarzignano.bold.external.showcase;

import android.content.Context;
import android.content.SharedPreferences;


class PrefsManager {

    private static final int SEQUENCE_NEVER_STARTED = 0;
    private static final int SEQUENCE_FINISHED = -1;


    private static final String PREFS_NAME = "HomePrefs";
    private static final String STATUS = "status_";
    private String showcaseID = null;
    private Context context;

    PrefsManager(Context context, String showcaseID) {
        this.context = context;
        this.showcaseID = showcaseID;
    }


    /***
     * METHODS FOR INDIVIDUAL SHOWCASE VIEWS
     */
    boolean hasFired() {
        int status = getSequenceStatus();
        return (status == SEQUENCE_FINISHED);
    }

    void setFired() {
        setSequenceStatus();
    }

    /***
     * METHODS FOR SHOWCASE SEQUENCES
     */
    private int getSequenceStatus() {
        return context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(STATUS + showcaseID, SEQUENCE_NEVER_STARTED);

    }

    private void setSequenceStatus() {
        SharedPreferences internal = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        internal.edit().putInt(STATUS + showcaseID, PrefsManager.SEQUENCE_FINISHED).apply();
    }

    void resetShowcase() {
        SharedPreferences internal = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        internal.edit().putInt(STATUS + showcaseID, SEQUENCE_NEVER_STARTED).apply();
    }

    void close() {
        context = null;
    }
}
