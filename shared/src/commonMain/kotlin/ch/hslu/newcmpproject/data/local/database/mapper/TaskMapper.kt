package ch.hslu.newcmpproject.data.local.database.mapper

import ch.hslu.newcmpproject.domain.entity.Task

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