package ch.hslu.newcmpproject.model

import kotlinx.datetime.LocalDateTime

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val dueDate: String,
    val dueTime: String,
    val status: String = "To Do"
)


fun Task.toLocalDateTimeOrNull(): LocalDateTime? {
    return try {
        val dateParts = dueDate.split(".").map { it.toInt() }
        val timeParts = dueTime.split(":").map { it.toInt() }

        LocalDateTime(year = dateParts[2],
            month = dateParts[1],
            day = dateParts[0],
            hour = timeParts[0],
            minute = timeParts[1]
        )
    } catch (e: Exception) {
        null
    }
}
