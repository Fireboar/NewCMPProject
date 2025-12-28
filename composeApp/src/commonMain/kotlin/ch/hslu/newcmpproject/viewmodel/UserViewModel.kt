package ch.hslu.newcmpproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.hslu.newcmpproject.cache.UserRepository
import ch.hslu.newcmpproject.entity.TokenStorage
import ch.hslu.newcmpproject.entity.UserSimple
import ch.hslu.newcmpproject.entity.UserStorage
import ch.hslu.newcmpproject.network.auth.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val syncViewModel: SyncViewModel,
    private val userStorage: UserStorage,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(tokenStorage.loadToken() != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _currentUser= MutableStateFlow(
        userStorage.loadUser()
    )
    val currentUser: StateFlow<UserSimple?> = _currentUser

    private val _allUsers = MutableStateFlow<List<UserSimple>>(emptyList())
    val allUsers: StateFlow<List<UserSimple>> = _allUsers

    private val _selectedUser= MutableStateFlow(userStorage.loadUser())
    val selectedUser: StateFlow<UserSimple?> = _selectedUser

    // Folgender Code


    init {
        loadAllUsers()
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            _allUsers.value = userRepository.getAllUsers()
        }
    }

    fun loadUser(userId: Long) {
        viewModelScope.launch {
            _selectedUser.value = userRepository.getUserWithId(userId)
        }
    }

    // Folgender Code

    fun addUser(username: String, password: String, role: String) {
        viewModelScope.launch {
            val success = userRepository.addUser(username, password, role)
            if (success) {
                loadAllUsers()
                setSyncMessage(
                    "User $username erfolgreich hinzugefügt.",
                    true)
            } else {
                setSyncMessage(
                    "User konnte nicht erstellt werden.",
                    false)
            }
        }
    }

    // Folgender Code

    fun updateUsername(userId: Long, newUsername: String) {
        viewModelScope.launch {
            val response = userRepository.updateUsername(
                userId, newUsername
            )
            if (response.isSuccessful) {
                loadAllUsers()
                loadUser(userId)

                // Wenn der eigene User geändert wurde
                if (userId == authService.currentUser?.userId) {
                    _currentUser.value = authService.currentUser
                }
                setSyncMessage(
                    "Username erfolgreich geändert.",
                    true)
            } else {
                setSyncMessage(
                    "Username konnte nicht geändert werden.",
                    false)
            }
        }
    }

    // Folgender Code


    fun updatePassword(userId: Long, oldPassword: String?, newPassword: String) {
        viewModelScope.launch {
            val success = userRepository.updatePassword(userId, oldPassword, newPassword)
            if (success) {
                loadAllUsers()
                setSyncMessage("Passwort erfolgreich geändert.", true)
            } else {
                setSyncMessage("Passwort konnte nicht geändert werden.", false)
            }
        }
    }

    // Folgender Code

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            val success = userRepository.deleteUser(userId)
            if (success) {
                loadAllUsers()
                setSyncMessage("User erfolgreich gelöscht.", true)
            } else {
                setSyncMessage("User konnte nicht gelöscht werden.", false)
            }
        }
    }

    // Folgender Code

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val success = authService.login(username, password)
            _isLoggedIn.value = success
            _currentUser.value = if (success) authService.currentUser else null
        }
    }

    fun logout() {
        viewModelScope.launch {
            authService.logout()
            _isLoggedIn.value = false
            _currentUser.value = null
        }
    }

    // Folgender Code

    fun setSyncMessage(message: String, positive: Boolean, priority: Int = 2) {
        viewModelScope.launch {
            syncViewModel.setSyncMessage(message, positive, priority)
        }
    }

    fun manualOfflineLogin(username: String) {
        viewModelScope.launch {
            // Offline-User erstellen
            val offlineUser = UserSimple(
                userId = -1L,       // Dummy-ID für Offline
                userName = username,
                role = "OFFLINE"
            )

            // User lokal speichern
            userStorage.saveUser(offlineUser)

            // LiveData / StateFlow aktualisieren
            _currentUser.value = offlineUser
            _isLoggedIn.value = true

            // Optional Sync-Message setzen
            setSyncMessage("Offline-Modus aktiv", true)
        }
    }

}