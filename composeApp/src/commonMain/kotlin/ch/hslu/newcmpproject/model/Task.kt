package ch.hslu.newcmpproject.model

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val dueDate: String,
    val dueTime: String,
    val status: String = "To Do"
)