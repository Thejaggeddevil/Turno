package com.mansi.focusway.features.groups.model

data class GroupMember(
    val id: String = "",
    val name: String = "",
    val isActive: Boolean = false,
    val isAdmin: Boolean = false,
    val joinedAt: Long = System.currentTimeMillis()
) 