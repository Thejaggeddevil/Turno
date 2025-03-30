package com.mansi.focusway.features.groups.viewmodel

import androidx.lifecycle.ViewModel
import com.mansi.focusway.features.groups.model.Group
import com.mansi.focusway.features.groups.model.GroupMember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GroupsViewModel : ViewModel() {
    
    private val _groupsState = MutableStateFlow(GroupsState())
    val groupsState: StateFlow<GroupsState> = _groupsState.asStateFlow()
    
    fun loadGroups() {
        // In a real app, this would load groups from a repository
        val mockGroups = listOf(
            Group(
                id = "1",
                name = "Study Group A",
                description = "For CS students",
                members = listOf(
                    GroupMember(id = "user1", name = "John", isActive = true),
                    GroupMember(id = "user2", name = "Jane", isActive = false)
                )
            ),
            Group(
                id = "2",
                name = "Work Team",
                description = "For project collaboration",
                members = listOf(
                    GroupMember(id = "user1", name = "John", isActive = true),
                    GroupMember(id = "user3", name = "Bob", isActive = true)
                )
            )
        )
        _groupsState.value = _groupsState.value.copy(groups = mockGroups)
    }
    
    fun joinGroup(groupId: String) {
        // In a real app, this would call an API to join a group
    }
    
    fun leaveGroup(groupId: String) {
        // In a real app, this would call an API to leave a group
    }
}

data class GroupsState(
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 