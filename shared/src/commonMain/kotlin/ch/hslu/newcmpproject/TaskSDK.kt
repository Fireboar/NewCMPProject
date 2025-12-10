package ch.hslu.newcmpproject

import ch.hslu.newcmpproject.cache.Database
import ch.hslu.newcmpproject.entity.Task
import ch.hslu.newcmpproject.network.TaskApi

class TaskSDK(val database: Database, val api: TaskApi) {

    suspend fun getTasks(): List<Task> {
        return database.getTasks()
    }

    suspend fun addTask(task: Task, isServerOnline: Boolean) : Boolean{
        database.insertTask(task)
        return true;
    }

    suspend fun deleteTask(task: Task, isServerOnline: Boolean) : Boolean{
        database.deleteTask(task)
        return true;
    }

    suspend fun updateTask(task: Task, isServerOnline: Boolean) : Boolean{
        database.updateTask(task)
        return true;
    }

    suspend fun isServerOnline():Boolean{
        return false;
    }

    suspend fun isInSync(): Boolean {
        return false;
    }

    suspend fun mergeTasks(isServerOnline: Boolean): Boolean {
        return true;
    }

    suspend fun pullTasks(isServerOnline: Boolean): Boolean {
        return true;
    }

    suspend fun postTasks(isServerOnline: Boolean): Boolean {
        return true;
    }

}
