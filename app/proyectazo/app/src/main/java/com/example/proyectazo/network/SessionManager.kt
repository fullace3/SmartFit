package com.example.proyectazo.network

import android.content.Context

/**
 * Manages the local user session via SharedPreferences.
 * Without a backend there is no JWT token — the session is just
 * the userId and display name persisted after a successful local login.
 *
 * The key names are kept identical to the original so no other file
 * needs to change how it reads session data.
 */
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("smartfit_session", Context.MODE_PRIVATE)

    /** Called after a successful login() or registrar() from the Repository. */
    fun guardarSesion(userId: Int, nombre: String) {
        prefs.edit()
            .putInt("user_id", userId)
            .putString("user_nombre", nombre)
            .putBoolean("logged_in", true)
            .apply()
    }

    fun getUserNombre(): String = prefs.getString("user_nombre", "Usuario") ?: "Usuario"

    fun getUserId(): Int = prefs.getInt("user_id", -1)

    fun isLoggedIn(): Boolean = prefs.getBoolean("logged_in", false)

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}