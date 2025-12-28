package ch.hslu.newcmpproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.hslu.newcmpproject.domain.repository.TaskRepository
import ch.hslu.newcmpproject.domain.entity.Task
import ch.hslu.newcmpproject.network.SyncService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(
    private val taskRepository: TaskRepository,
    private val syncService: SyncService,
    private val syncViewModel: SyncViewModel,
    private val userViewModel: UserViewModel
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    init {
        loadTasks()
        viewModelScope.launch {
            userViewModel.currentUser.collect { user ->
                loadTasks()
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
            val success = taskRepository.addTask(task)
            if (success) {
                syncViewModel.setSyncMessage("'${task.title}' erfolgreich hinzugefügt und synchronisiert.", true)
            } else {
                syncViewModel.setSyncMessage("'${task.title}' konnte nicht auf den Server hochgeladen werden.", false)
            }
            loadTasks()
        }
    }

    fun loadTasks() {
        viewModelScope.launch {
            val loadedTasks = taskRepository.getLocalTasks()
            _tasks.value = loadedTasks.toList()
        }
    }

    fun updateTask(task: Task){
        viewModelScope.launch {
            val success = taskRepository.updateTask(task)
            if(success) {
                syncViewModel.setSyncMessage("'${task.title}' erfolgreich aktualisiert.", true)
            } else {
                syncViewModel.setSyncMessage("'${task.title}' konnte nicht synchronisiert werden.", false)
            }

            loadTasks()
        }
    }

    fun moveTask(task: Task, newStatus: String) {
        viewModelScope.launch {
            val updatedTask = task.copy(status = newStatus)
            val success = taskRepository.updateTask(updatedTask)
            if(success) {
                syncViewModel.setSyncMessage("'${task.title}' erfolgreich aktualisiert.", true)
            } else {
                syncViewModel.setSyncMessage("'${task.title}' konnte nicht synchronisiert werden.", false)
            }

            loadTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            val success = taskRepository.deleteTask(task)
            if(success){
                syncViewModel.setSyncMessage("'${task.title}' erfolgreich gelöscht und synchronisiert.", true)
            } else {
                syncViewModel.setSyncMessage("'${task.title}' konnte nicht vom Server gelöscht werden.", false)
            }

            loadTasks()
        }
    }


    fun postTasks() {
        viewModelScope.launch {
            val success = syncService.push()
            if(success){
                syncViewModel.setSyncMessage("Tasks wurden auf den Server gepostet.", true)
            } else {
                syncViewModel.setSyncMessage("Fehler beim Posten der Tasks.", false)
            }
            loadTasks()

        }
    }

    fun pullTasks() {
        viewModelScope.launch {
            val success = syncService.pull()
            if(success){
                syncViewModel.setSyncMessage("Tasks vom Server geladen und lokal synchronisiert.", true)
            } else {
                syncViewModel.setSyncMessage("Fehler beim Laden der Tasks.", false)
            }
            loadTasks()

        }
    }

    fun mergeTasks() {
        viewModelScope.launch {
            val success = syncService.merge()
            if(success){
                syncViewModel.setSyncMessage("Server- und lokale Tasks wurden zusammengeführt.", true)
            } else {
                syncViewModel.setSyncMessage("Fehler beim Mergen der Tasks.", false)
            }
            loadTasks()

        }
    }

}
