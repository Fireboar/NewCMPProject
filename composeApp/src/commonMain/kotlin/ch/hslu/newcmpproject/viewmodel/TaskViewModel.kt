package ch.hslu.newcmpproject.viewmodel

import androidx.lifecycle.ViewModel
import ch.hslu.newcmpproject.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TaskViewModel () : ViewModel(){
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private var nextId = 0

    fun addTask(
        title: String,
        description: String,
        dueDate: String,
        dueTime: String,
        status: String = "To Do"
    ) {
        val task = Task(
            title = title,
            description = description,
            dueDate = dueDate,
            dueTime = dueTime,
            status = status,
            id = nextId++
        )
        _tasks.value = _tasks.value + task
    }

    fun deleteTask(task: Task) {
        _tasks.value = _tasks.value - task
    }

    fun moveTask(task: Task, targetStatus: String) {
        val updatedTask = task.copy(status = targetStatus)
        _tasks.value = _tasks.value.map {
            if (it.id == task.id) updatedTask else it
        }
    }

    fun updateTask(updatedTask: Task) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == updatedTask.id) updatedTask else task
        }
    }

}
