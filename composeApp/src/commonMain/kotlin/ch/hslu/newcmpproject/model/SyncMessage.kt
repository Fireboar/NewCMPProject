package ch.hslu.newcmpproject.model

data class SyncMessage(
    val text: String = "",
    val isPositive: Boolean = true,
    val priority: Int
)