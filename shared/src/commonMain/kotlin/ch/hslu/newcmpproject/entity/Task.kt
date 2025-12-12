package ch.hslu.newcmpproject.entity

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String? = "",
    @SerialName("dueDate")
    val dueDate: String,
    @SerialName("dueTime")
    val dueTime: String,
    @SerialName("status")
    val status: String? = "To Do"
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
