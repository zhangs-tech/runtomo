package com.zhangs_tech.runtomo.tomo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhangs_tech.runtomo.network.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UC-11 1a. */
enum class SortOrder { RARITY, BOND, DISTANCE }

enum class TomosError { NoConnection, Failed, InvalidNickname }

data class TomosUiState(
    val isLoading: Boolean = true,
    val collection: TomoCollection? = null,
    val sortOrder: SortOrder = SortOrder.RARITY,
    /** The species whose card is open. */
    val selectedSpeciesCode: String? = null,
    val isRenaming: Boolean = false,
    val isSaving: Boolean = false,
    val error: TomosError? = null,
) {
    val sortedEntries: List<CollectionEntry>
        get() = collection?.entries.orEmpty().sortedWith(sortOrder.comparator)

    val selectedEntry: CollectionEntry?
        get() = collection?.entries?.find { it.species.code == selectedSpeciesCode }
}

private val SortOrder.comparator: Comparator<CollectionEntry>
    get() = when (this) {
        SortOrder.RARITY -> compareByDescending<CollectionEntry> { it.species.rarity }
        SortOrder.BOND -> compareByDescending<CollectionEntry> { it.tomo?.bondPoints ?: -1 }
        SortOrder.DISTANCE -> compareByDescending<CollectionEntry> { it.tomo?.distanceTogetherM ?: -1L }
    }.thenBy { it.species.name }

@HiltViewModel
class TomosViewModel @Inject constructor(
    private val tomoApi: TomoApi,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TomosUiState())
    val uiState: StateFlow<TomosUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun load() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            try {
                val collection = tomoApi.getCollection()
                _uiState.update { it.copy(isLoading = false, collection = collection, error = null) }
            } catch (e: ApiException) {
                _uiState.update { it.copy(isLoading = false, error = e.toError()) }
            }
        }
    }

    fun setSortOrder(sortOrder: SortOrder) {
        _uiState.update { it.copy(sortOrder = sortOrder) }
    }

    fun select(speciesCode: String) {
        _uiState.update { it.copy(selectedSpeciesCode = speciesCode, isRenaming = false, error = null) }
    }

    fun dismiss() {
        _uiState.update { it.copy(selectedSpeciesCode = null, isRenaming = false, error = null) }
    }

    fun startRename() {
        _uiState.update { it.copy(isRenaming = true, error = null) }
    }

    fun cancelRename() {
        _uiState.update { it.copy(isRenaming = false, error = null) }
    }

    /** UC-11 step 5. */
    fun equip(tomoId: String) = save {
        tomoApi.equip(tomoId)
        _uiState.update { state ->
            state.copy(collection = state.collection?.copy(equippedTomoId = tomoId))
        }
    }

    /** UC-11 5a. */
    fun rename(tomoId: String, nickname: String) {
        val trimmed = nickname.trim()
        if (!Nickname.isValid(trimmed)) {
            _uiState.update { it.copy(error = TomosError.InvalidNickname) }
            return
        }
        save {
            val renamed = tomoApi.rename(tomoId, trimmed)
            _uiState.update { state ->
                state.copy(
                    isRenaming = false,
                    collection = state.collection?.let { collection ->
                        collection.copy(entries = collection.entries.map { if (it.tomo?.id == tomoId) it.copy(tomo = renamed) else it })
                    },
                )
            }
        }
    }

    private fun save(block: suspend () -> Unit) {
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                block()
                _uiState.update { it.copy(isSaving = false) }
            } catch (e: ApiException) {
                _uiState.update { it.copy(isSaving = false, error = e.toError()) }
            }
        }
    }

    private fun ApiException.toError() = if (this is ApiException.Network) TomosError.NoConnection else TomosError.Failed
}
