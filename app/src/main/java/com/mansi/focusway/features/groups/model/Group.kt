package com.mansi.focusway.features.groups.model

data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val members: List<GroupMember> = emptyList(),
    val createdBy: String = "",
    val isPublic: Boolean = true
) 