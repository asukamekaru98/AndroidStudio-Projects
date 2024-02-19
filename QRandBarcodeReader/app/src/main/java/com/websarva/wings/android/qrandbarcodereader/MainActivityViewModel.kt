package com.websarva.wings.android.qrandbarcodereader

import androidx.lifecycle.ViewModel
import com.websarva.wings.android.qrandbarcodereader.result.ScanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update


class MainActivityViewModel : ViewModel() {
    val resultFlow: MutableStateFlow<List<ScanResult>> = MutableStateFlow(emptyList())

    fun add(result: ScanResult) {
        resultFlow.update {
            if (it.contains(result)) {
                it
            } else {
                it + result
            }
        }
    }
}