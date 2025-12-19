package ch.hslu.newcmpproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.hslu.newcmpproject.TaskSDK
import ch.hslu.newcmpproject.model.SyncMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SyncViewModel (private val sdk: TaskSDK) : ViewModel(){

    init {
        checkServerStatus()
    }

    private var _isServerOnline: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isServerOnline: StateFlow<Boolean> = _isServerOnline

    private val _syncMessage = MutableStateFlow(
        SyncMessage("", isPositive = false, priority = 0)
    )
    val syncMessage: StateFlow<SyncMessage> = _syncMessage

    private var clearMessageJob: kotlinx.coroutines.Job? = null

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


    fun checkServerStatus(){
        viewModelScope.launch {
            while (true) {
                val online = sdk.isServerOnline()
                _isServerOnline.value = online
                if (online) {
                    isInSync()
                }
                delay(8000)
            }
        }
    }

    fun isInSync(){
        viewModelScope.launch {
            if(isServerOnline.value){
                val inSync = sdk.isInSync()
                if (!inSync) {
                    setSyncMessage("Server nicht synchron oder hat keine Tasks", false, 1)
                }
            }
        }
    }


}