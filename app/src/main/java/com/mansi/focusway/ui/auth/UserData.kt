package com.mansi.focusway.ui.auth

/**
 * Data class to store user information
 */
data class UserData(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis()
) 