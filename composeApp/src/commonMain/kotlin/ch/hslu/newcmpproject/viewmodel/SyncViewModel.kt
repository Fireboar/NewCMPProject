package ch.hslu.newcmpproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.hslu.newcmpproject.model.SyncMessage
import ch.hslu.newcmpproject.network.SyncService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SyncViewModel (private val syncService: SyncService) : ViewModel(){

    val isServerOnline: StateFlow<Boolean> = syncService.isServerOnline
    val isInSync: StateFlow<Boolean> = syncService.isInSync

    private val _syncMessage = MutableStateFlow(
        SyncMessage("", isPositive = false, priority = 0)
    )
    val syncMessage: StateFlow<SyncMessage> = _syncMessage

    private var clearMessageJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            while(true) {
                syncService.checkServerStatus()
                if (!isInSync.value) {
                    setSyncMessage("Server nicht synchron oder hat keine Tasks", false, priority = 2)
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

                clearMessageJob?.cancel()

                _syncMessage.value = SyncMessage(message, positive, priority)

                clearMessageJob = viewModelScope.launch {
                    delay(8000)
                    _syncMessage.value = SyncMessage("", true, priority = 0)
                }
            }
        }
    }


}