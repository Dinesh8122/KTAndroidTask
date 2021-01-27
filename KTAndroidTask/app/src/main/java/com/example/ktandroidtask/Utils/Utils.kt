package com.example.ktandroidtask.Utils

import android.content.Context
import java.util.regex.Pattern

class Utils {

    fun isEmailValid(email: String): Boolean {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    /* phone number validation */
    fun isValidMobile(phone: String): Boolean {
        var check = false
        if (!Pattern.matches("[a-zA-Z]+", phone)) {
            check = phone.length >= 10
        } else {
            check = false
        }
        return check
    }

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The [Context].
     */
    fun requestingLocationUpdates(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false)
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    fun setRequestingLocationUpdates(context: Context, requestingLocationUpdates: Boolean) {

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
            .apply()
    }

    val KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates"
    val PREFS_NAME = "com.example.ktandroidtask"
    val KEY_FIRST_LAUNCH = "first_launch"
}