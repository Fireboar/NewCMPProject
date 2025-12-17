package ch.hslu.newcmpproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.hslu.newcmpproject.TaskSDK
import ch.hslu.newcmpproject.entity.Task
import ch.hslu.newcmpproject.entity.TokenStorage
import ch.hslu.newcmpproject.model.SyncMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel (private val sdk: TaskSDK) : ViewModel(){

    val tokenStorage = TokenStorage()
    private val _isLoggedIn = MutableStateFlow(tokenStorage.loadToken() != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _currentUserName = MutableStateFlow(
        tokenStorage.loadUsername()
    )
    val currentUserName: StateFlow<String> = _currentUserName

    private val _currentUserId = MutableStateFlow<Long?>(
        tokenStorage.loadUserId()
    )
    val currentUserId: StateFlow<Long?> = _currentUserId

    /*private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers*/

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _syncMessage = MutableStateFlow(
        SyncMessage("", isPositive = false, priority = 0)
    )

    val syncMessage: StateFlow<SyncMessage> = _syncMessage

    private var _isServerOnline: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isServerOnline: StateFlow<Boolean> = _isServerOnline


    init {
        checkServerStatus()
        /*loadAllUsers()*/
        // loadTasks nur aufrufen, wenn eingeloggt
        viewModelScope.launch {
            if (isLoggedIn.value) {
                loadTasks()
            }
        }
    }

/*
    fun loadAllUsers() {
        viewModelScope.launch {
            _allUsers.value = sdk.getAllUsers()
        }
    }

    fun addUser(username: String, password: String) {
        viewModelScope.launch {
            val success = sdk.addUser(username, password)
            if(success) loadAllUsers()
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            val success = sdk.updateUser(user)
            if(success) loadAllUsers()
        }
    }

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            val success = sdk.deleteUser(userId)
            if(success) loadAllUsers()
        }
    }
*/
    fun manualOfflineLogin(username: String) {
        viewModelScope.launch {
            _currentUserName.value = username
            _currentUserId.value = null
            _isLoggedIn.value = true
            setSyncMessage("Offline-Modus aktiv", true)
            loadTasks()
        }
    }


    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            val success = sdk.updateUsername(newUsername)
            if (success) {
                _currentUserName.value = newUsername
                setSyncMessage("Username aktualisiert", true)
            } else {
                setSyncMessage("Username konnte nicht aktualisiert werden.", false)
            }
        }
    }

    fun updatePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            val success = sdk.updatePassword(oldPassword, newPassword)
            if (success) {
                setSyncMessage("Passwort erfolgreich geändert.", true)
            } else {
                setSyncMessage("Passwort konnte nicht geändert werden.", false)
            }
        }
    }


    fun login(username: String, password: String) {
        viewModelScope.launch {
            val success = sdk.login(username, password)
            _isLoggedIn.value = success

            if(success) {
                // Werte aus Storage oder SDK verwenden
                _currentUserName.value = tokenStorage.loadUsername()
                _currentUserId.value = tokenStorage.loadUserId()
                loadTasks()
            } else {
                _currentUserName.value = ""
                _currentUserId.value = null
                _tasks.value = emptyList()
            }
        }
    }




    fun logout(){
        viewModelScope.launch {
            sdk.logout()
            _isLoggedIn.value = false;
            _currentUserName.value = "";
            _currentUserId.value = null;
        }
    }

    fun checkServerStatus(){
        viewModelScope.launch {
            while (true) {
                val online = sdk.isServerOnline()
                _isServerOnline.value = online
                if (online) {
                    isInSync()
                }
                delay(8000)
            }
        }
    }

    fun setSyncMessage(message: String, positive: Boolean, priority: Int = 2) {
        viewModelScope.launch {
            if(isServerOnline.value){
                // Neue Message nur setzen wenn priority >= aktuelle priority
                if (priority < _syncMessage.value.priority) {
                    return@launch
                }

                _syncMessage.value = SyncMessage(message, positive, priority)

            }
        }
    }

    fun isInSync(){
        viewModelScope.launch {
            if(isServerOnline.value){
                val inSync = sdk.isInSync()
                if (!inSync) {
                    setSyncMessage("Server nicht synchron oder hat keine Tasks", false, 1)
                }
            }
        }
    }

    fun addTask(title: String, description: String?, dueDate: String, dueTime: String, status: String?) {
        viewModelScope.launch {
            val task = Task(
                id = 0,
                title = title,
                description = description,
                dueDate = dueDate,
                dueTime = dueTime,
                status = status ?: "To Do"
            )
            addTask(task)
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            val success = sdk.addTask(task, isServerOnline.value)
            if (success) {
                setSyncMessage("'${task.title}' erfolgreich hinzugefügt und synchronisiert.", true)
            } else {
                setSyncMessage("'${task.title}' konnte nicht auf den Server hochgeladen werden.", false)
            }
            loadTasks()

        }
    }

    private fun loadTasks() {
        viewModelScope.launch {
            val loadedTasks = sdk.getTasks()
            _tasks.value = loadedTasks.toList()
        }
    }

    fun updateTask(task: Task){
        viewModelScope.launch {
            val success = sdk.updateTask(task,isServerOnline.value)
            if(success) {
                setSyncMessage("'${task.title}' erfolgreich aktualisiert.", true)
            } else {
                setSyncMessage("'${task.title}' konnte nicht synchronisiert werden.", false)
            }

            loadTasks()
        }
    }

    fun moveTask(task: Task, newStatus: String) {
        viewModelScope.launch {
            val updatedTask = task.copy(status = newStatus)
            val success = sdk.updateTask(updatedTask,isServerOnline.value)
            if(success) {
                setSyncMessage("'${task.title}' erfolgreich aktualisiert.", true)
            } else {
                setSyncMessage("'${task.title}' konnte nicht synchronisiert werden.", false)
            }

            loadTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            val success = sdk.deleteTask(task, isServerOnline.value)
            if(success){
                setSyncMessage("'${task.title}' erfolgreich gelöscht und synchronisiert.", true)
            } else {
                setSyncMessage("'${task.title}' konnte nicht vom Server gelöscht werden.", false)
            }

            loadTasks()
        }
    }


    fun postTasks() {
        viewModelScope.launch {
            val success = sdk.postTasks(isServerOnline.value)
            if(success){
                setSyncMessage("Tasks wurden auf den Server gepostet.", true)
            } else {
                setSyncMessage("Fehler beim Posten der Tasks.", false)
            }
            loadTasks()

        }
    }

    fun pullTasks() {
        viewModelScope.launch {
            val success = sdk.pullTasks(isServerOnline.value)
            if(success){
                setSyncMessage("Tasks vom Server geladen und lokal synchronisiert.", true)
            } else {
                setSyncMessage("Fehler beim Laden der Tasks.", false)
            }
            loadTasks()

        }
    }

    fun mergeTasks() {
        viewModelScope.launch {
            val success = sdk.mergeTasks(isServerOnline.value)
            if(success){
                setSyncMessage("Server- und lokale Tasks wurden zusammengeführt.", true)
            } else {
                setSyncMessage("Fehler beim Mergen der Tasks.", false)
            }
            loadTasks()

        }
    }




}
