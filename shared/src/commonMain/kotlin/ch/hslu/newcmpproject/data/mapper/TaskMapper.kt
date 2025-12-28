package ch.hslu.newcmpproject.data.mapper

import ch.hslu.newcmpproject.entity.Task

object TaskMapper {

    fun map(
        id: Long,
        userId: Long,
        title: String,
        description: String?,
        dueDate: String,
        dueTime: String,
        status: String?
    ): Task = Task(
        id = id,
        userId = userId,
        title = title,
        description = description ?: "",
        dueDate = dueDate,
        dueTime = dueTime,
        status = status ?: "To Do"
    )
}