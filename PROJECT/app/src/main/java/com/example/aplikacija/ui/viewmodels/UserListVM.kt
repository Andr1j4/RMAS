package com.example.aplikacija.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aplikacija.classes.UserClass
import com.example.aplikacija.repository.UserRepository
import com.google.firebase.firestore.getField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserListVM(private val usRep: UserRepository?) : ViewModel() {

    private val _usrUID = MutableStateFlow<String>("")
    val usrUID: StateFlow<String> = _usrUID.asStateFlow()

    private val _users = MutableStateFlow<List<UserClass>?>(null)
    val users: StateFlow<List<UserClass>?> = _users.asStateFlow()

    private val _friends = MutableStateFlow<List<UserClass>?>(null)
    val friends: StateFlow<List<UserClass>?> = _friends.asStateFlow()

    private val _filterType = MutableStateFlow<FilterType>(FilterType.POINTS_DESCENDING)
    val filterType: StateFlow<FilterType> = _filterType.asStateFlow()

    enum class FilterType {
        POINTS_ASCENDING, POINTS_DESCENDING, DATE_POSTED, FRIENDS
    }

    fun getAllUsers() {
        viewModelScope.launch {
            val users = usRep?.getAllUsers()

            val usersList = users?.documents?.map { usr ->
                val ime = usr.getString("ime") ?: ""
                val prezime = usr.getString("prezime") ?: ""
                val bodovi = usr.getField<Int>("bod") ?: 0
                val uid = usr.getString("uid") ?: ""


                UserClass(ime, prezime, uid, bodovi)
            } ?: emptyList()

            _users.value = usersList.sortedByDescending { it.bod }
        }
    }

    fun getFriends() {
        viewModelScope.launch {
            val currentUserUID = _usrUID.value
            if (currentUserUID.isNotEmpty()) {
                val friends = usRep?.getFriends(currentUserUID)
                val friendsUIDs = friends?.documents?.map { it.getString("FriendUID") }

                // Filter users who are in the friends list
                _friends.value = _users.value?.filter { it.uid in friendsUIDs.orEmpty() }
            }
        }
    }

    fun getFriends1() {
        viewModelScope.launch {
            val currentUID = _usrUID.value
            if (currentUID.isNotEmpty()) {
                val friendsSnapshot = usRep?.getFriends(currentUID)
                val friendsList = friendsSnapshot?.documents?.map { friendDoc ->
                    val uid = friendDoc.getString("FriendUID") ?: ""
                    val ime = friendDoc.getString("ime") ?: ""
                    val prezime = friendDoc.getString("prezime") ?: ""
                    val bodovi = friendDoc.getLong("bodovi")?.toInt() ?: 0

                    UserClass(ime, prezime, uid, bodovi)
                } ?: emptyList()

                _friends.value = friendsList
            }
        }
    }

    // Function to apply the selected filter
    fun filterUsers() {
        viewModelScope.launch {
            val allUsers = _users.value ?: return@launch

            val filteredUsers = when (_filterType.value) {
                FilterType.POINTS_ASCENDING -> allUsers.sortedBy { it.bod }
                FilterType.POINTS_DESCENDING -> allUsers.sortedByDescending { it.bod }
                FilterType.DATE_POSTED -> allUsers // Implement date sorting if you have the date field
                FilterType.FRIENDS -> _friends.value ?: emptyList() // Show only friends
            }

            _users.value = filteredUsers
        }
    }

    // Change the filter type
    fun changeFilter(filter: FilterType) {
        _filterType.value = filter
        filterUsers()
        if (filter == FilterType.POINTS_DESCENDING){
            getAllUsers()
            filterUsers()
        }
    }

    fun DodajPrijatelja(imePrijatelja: String, prezimePrijatelja: String) {
        viewModelScope.launch {
            val users = _users.value ?: return@launch

            val friend = users.find { user ->
                user.ime == imePrijatelja && user.prezime == prezimePrijatelja
            }

            val friendUID = friend?.uid

            if (!friendUID.isNullOrEmpty()) {
                val currentUserUID = _usrUID.value

                if (currentUserUID.isNotEmpty()) {
                    usRep?.addFriend(currentUserUID, friendUID)
                    getFriends() //Refresh after adding
                } else {
                    Log.e("AddFriend", "Current User UID is empty")
                }
            } else {
                Log.e("AddFriend", "Friend not found in user list")
            }
        }
    }

    fun getCurrentUID() {
        _usrUID.value = usRep?.getUserTF()?.uid.toString()
    }
}

class UserListVMFactory(private val usRep: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserListVM::class.java)) {
            return UserListVM(usRep) as T
        }
        throw IllegalArgumentException("Unknown viewModel class")
    }
}
