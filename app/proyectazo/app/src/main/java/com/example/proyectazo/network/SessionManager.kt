package com.example.proyectazo.network

import android.content.Context

/**
* Manages the user session by persisting the JWT token and basic user data
 * * in SharedPreferences. Acts as the single source of truth for authentication state.
 *
 * Stored in "smartfit_session" — separate from "smartfit_config" so that
 * closing the session does not erase the user's app preferences.
*/
class SessionManager(context: Context) {

    // MODE_PRIVATE ensures this file is only accessible by this app
    private val prefs = context.getSharedPreferences("smartfit_session", Context.MODE_PRIVATE)

    /**
     * Persists the JWT token and user identity after a successful login.
     * Called once on login — all subsequent API calls read from here.
     */
    fun guardarSesion(token: String, userId: Int, nombre: String) {
        prefs.edit()
            .putString("token", token)
            .putInt("user_id", userId)
            .putString("user_nombre", nombre)
            .apply() // apply() is asynchronous — use commit() only if the result needs to be confirmed immediately
    }

    fun getUserNombre(): String = prefs.getString("user_nombre", "Usuario") ?: "Usuario"

    // Returns null if no session exists — used by isLoggedIn() to check auth state
    fun getToken(): String? = prefs.getString("token", null)

    fun getUserId(): Int = prefs.getInt("user_id", -1)   // -1 indicates no active session

    // Used by the NavGraph to decide whether to show Login or Home on app launch
    fun isLoggedIn(): Boolean = getToken() != null

    /**
     * Clears all session data on logout.
     * Does NOT affect "smartfit_config" where workout time and setup preferences are stored.
     */
    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}