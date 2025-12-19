package ch.hslu.newcmpproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.hslu.newcmpproject.TaskSDK
import ch.hslu.newcmpproject.entity.TokenStorage
import ch.hslu.newcmpproject.entity.UserSimple
import ch.hslu.newcmpproject.entity.UserStorage
import ch.hslu.newcmpproject.view.user.addUser.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel (private val sdk: TaskSDK, private val syncViewModel: SyncViewModel) : ViewModel(){
    val userStorage = UserStorage()
    val tokenStorage = TokenStorage()

    private val _isLoggedIn = MutableStateFlow(tokenStorage.loadToken() != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _currentUser= MutableStateFlow(
        userStorage.loadUser()
    )
    val currentUser: StateFlow<UserSimple?> = _currentUser

    private val _allUsers = MutableStateFlow<List<UserSimple>>(emptyList())
    val allUsers: StateFlow<List<UserSimple>> = _allUsers

    private val _selectedUser= MutableStateFlow(
        userStorage.loadUser()
    )
    val selectedUser: StateFlow<UserSimple?> = _selectedUser


    init {
        loadAllUsers()
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            val usersFromServer: List<UserSimple> = sdk.getAllUsers()
            _allUsers.value = usersFromServer
        }
    }

    fun loadUser(userId:Long){
        viewModelScope.launch {
            val userFromServer: UserSimple? = sdk.getUserWithId(userId)
            _selectedUser.value = userFromServer
        }
    }

    fun addUser(username: String, password: String, role: String) {
        viewModelScope.launch {
            val success = sdk.addUser(username, password, role)
            if(success){
                loadAllUsers()
                setSyncMessage("User $username erfolgreich hinzugefügt.", true)
            }
            else setSyncMessage("User konnte nicht erstellt werden.", false)
        }
    }

    fun updateUsername(userId: Long, newUsername: String) {
        viewModelScope.launch {
            val success = sdk.updateUsername(userId, newUsername)
            if (success) {
                loadAllUsers()
                loadUser(userId)

                // 🔹 Wenn der eigene User geändert wurde, currentUser aktualisieren
                if (userId == _currentUser.value?.userId) {
                    _currentUser.value = sdk.currentUser
                }
                setSyncMessage("Username erfolgreich geändert.", true)

            } else {
                setSyncMessage("Username konnte nicht geändert werden.", false)
            }
        }
    }


    fun updatePassword(userId: Long, oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            val success = sdk.updatePassword(userId, oldPassword, newPassword)
            if(success){
                loadAllUsers()
                setSyncMessage("Passwort erfolgreich geändert.", true)
            }
            else setSyncMessage("Passwort konnte nicht geändert werden.", false)
        }
    }

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            val success = sdk.deleteUser(userId)
            if(success){
                loadAllUsers()
                setSyncMessage("User erfolgreich gelöscht", true)
            }
            else setSyncMessage("User konnte nicht gelöscht werden.", false)
        }
    }

    fun manualOfflineLogin(username: String) {
        viewModelScope.launch {
            val offlineUser = UserSimple(
                userId = -1L,
                userName = username,
                role = "OFFLINE"
            )

            userStorage.saveUser(offlineUser)
            _currentUser.value = offlineUser
            _isLoggedIn.value = true

            setSyncMessage("Offline-Modus aktiv", true)
        }
    }


    fun login(username: String, password: String) {
        viewModelScope.launch {
            val success = sdk.login(username, password)
            _isLoggedIn.value = success

            if (success) {
                val user = userStorage.loadUser()
                _currentUser.value = user
            } else {
                _currentUser.value = null
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sdk.logout()
            userStorage.clearUser()
            tokenStorage.clearToken()

            _isLoggedIn.value = false
            _currentUser.value = null
        }
    }

    fun setSyncMessage(message: String, positive: Boolean, priority: Int = 2) {
        viewModelScope.launch {
            syncViewModel.setSyncMessage(message, positive, priority)
        }
    }

}