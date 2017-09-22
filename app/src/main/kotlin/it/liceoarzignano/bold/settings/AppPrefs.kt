package it.liceoarzignano.bold.settings

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

open class AppPrefs(context: Context) {
    var mPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    inline fun <reified T : Any> get(key: String, defaultValue: T? = null): T =
            mPrefs[key, defaultValue]

    inline fun <reified T : Any> set(key: String, value: T) {
        mPrefs[key] = value
    }

    fun remove(key: String) = mPrefs.remove(key)

    private fun SharedPreferences.remove(key: String) = edit { it.remove(key) }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    operator fun SharedPreferences.set(key: String, value: Any?) {
        when(value) {
            is String -> edit { it.putString(key, value) }
            is Int -> edit { it.putInt(key, value) }
            is Boolean -> edit { it.putBoolean(key, value) }
            is Float -> edit { it.putFloat(key, value) }
            is Long -> edit { it.putLong(key, value) }
        }
    }

    operator inline fun <reified T : Any> SharedPreferences.get(key: String,
                                                                defaultValue: T? = null): T =
            when (T::class.java.simpleName) {
                "String" -> getString(key, defaultValue as? String ?: "") as T
                "Integer" -> getInt(key, defaultValue as? Int ?: 0) as T
                "Boolean" -> getBoolean(key, defaultValue as? Boolean == true) as T
                "Float" -> getFloat(key, defaultValue as? Float ?: 0F) as T
                "Long" -> getLong(key, defaultValue as? Long ?: 0) as T
                else -> throw UnsupportedOperationException("This type is not supproted!")
            }

    fun migrate(context: Context) {
        val safePrefs = context.getSharedPreferences(PREFS_SAFE, 0)
        val extraPrefs = context.getSharedPreferences(PREFS_EXTRA, 0)

        if (mPrefs[KEY_MIGRATION, false]) {
            return
        }

        mPrefs.edit {
            // Migrate safe
            it.putBoolean(KEY_SAFE_DONE, safePrefs[KEY_OLD_SAFE_DONE, false])
            it.putBoolean(KEY_SAFE_SETUP, safePrefs[KEY_OLD_SAFE_SETUP, false])
            it.putBoolean(KEY_SAFE_PASSED, safePrefs[KEY_OLD_SAFE_PASSED, false])
            it.putString(KEY_SAFE_ACCESS, safePrefs[KEY_OLD_SAFE_ACCESS, ""])
            it.putString(KEY_SAFE_USERNAME, safePrefs[KEY_OLD_SAFE_USERNAME, ""])
            it.putString(KEY_SAFE_REG, safePrefs[KEY_OLD_SAFE_REG, ""])
            it.putString(KEY_SAFE_PC, safePrefs[KEY_OLD_SAFE_PC, ""])
            it.putString(KEY_SAFE_INTERNET, safePrefs[KEY_OLD_SAFE_INTERNET, ""])
            // Migrate extra
            it.putString(KEY_INTRO_DAY, extraPrefs[KEY_OLD_INITIAL_DAY, "2017-01-01"])
            it.putBoolean(KEY_INTRO_SCREEN, extraPrefs[KEY_OLD_INTRO_SCREEN, false])
            it.putBoolean(KEY_INTRO_DRAWER, extraPrefs[KEY_OLD_INTRO_DRAWER, false])
            it.putString(KEY_INTRO_VERSION, extraPrefs[KEY_OLD_INTRO_VERSION, ""])
            it.putBoolean(KEY_INTRO_SAFE, extraPrefs[KEY_OLD_INTRO_SAFE, false])
            it.putString(KEY_CURRENT_YEAR, extraPrefs[KEY_OLD_CURRENT_SCHOOL_YEAR, "2017"])
            it.putInt(KEY_QUARTER_SELECTOR, extraPrefs[KEY_OLD_QUARTER_SELECTOR, 0])
            it.putString(KEY_BACKUP_FOLDER, extraPrefs[KEY_OLD_BACKUP_FOLDER, ""])
        }
        set(KEY_MIGRATION, true)
    }

    companion object {
        val KEY_IS_TEACHER = "isTeacher_key"
        val KEY_ADDRESS = "address_key"
        val KEY_USERNAME = "username_key"
        val KEY_SUGGESTIONS = "showSuggestions_key"
        val KEY_NOTIF_NEWS = "notification_news_key"
        val KEY_NOTIF_EVENT = "notification_events_key"
        val KEY_NOTIF_EVENT_TIME = "notification_events_time_key"

        val KEY_SAFE_DONE = "safe_done_key"
        val KEY_SAFE_SETUP = "safe_setup_key"
        val KEY_SAFE_PASSED = "safe_passed_key"
        val KEY_SAFE_ACCESS = "safe_access_key"
        val KEY_SAFE_USERNAME = "safe_username_key"
        val KEY_SAFE_REG = "safe_reg_key"
        val KEY_SAFE_PC = "safe_pc_key"
        val KEY_SAFE_INTERNET = "safe_internet_key"
        val KEY_SAFE_SHARED = "safe_shared_key"
        val KEY_INTRO_VERSION = "intro_version_key"
        val KEY_INTRO_DAY = "intro_day_key"
        val KEY_INTRO_DRAWER = "intro_drawer_key"
        val KEY_INTRO_SCREEN = "intro_screen_key"
        val KEY_INTRO_SAFE = "intro_safe_key"
        val KEY_CURRENT_YEAR = "current_year_key"
        val KEY_BACKUP_FOLDER = "backup_folder_key"
        val KEY_QUARTER_SELECTOR = "quarter_selector_key"

        // Deprecated stuffs
        private val KEY_MIGRATION = "key_prefs_migration"
        private val KEY_OLD_SAFE_DONE = "doneSetup"
        private val KEY_OLD_SAFE_SETUP = "hasCompletedSetup"
        private val KEY_OLD_SAFE_PASSED = "safetyNetPassed"
        private val KEY_OLD_SAFE_ACCESS = "access_pwd"
        private val KEY_OLD_SAFE_USERNAME = "user_name"
        private val KEY_OLD_SAFE_REG = "reg_pwd"
        private val KEY_OLD_SAFE_PC = "pd_pwd"
        private val KEY_OLD_SAFE_INTERNET = "internet_pwd"
        private val KEY_OLD_INTRO_SAFE = "safeKey"
        private val KEY_OLD_INTRO_VERSION = "introVersion"
        private val KEY_OLD_INITIAL_DAY = "introDay"
        private val KEY_OLD_INTRO_DRAWER = "introDrawer"
        private val KEY_OLD_INTRO_SCREEN = "introScreen"
        private val KEY_OLD_BACKUP_FOLDER = "BACKUP_FOLDER"
        private val KEY_OLD_CURRENT_SCHOOL_YEAR = "currentSchoolYear"
        private val KEY_OLD_QUARTER_SELECTOR = "quarterSelector_key"
        private val PREFS_SAFE = "SafePrefs"
        private val PREFS_EXTRA = "extraPrefs"
    }

}