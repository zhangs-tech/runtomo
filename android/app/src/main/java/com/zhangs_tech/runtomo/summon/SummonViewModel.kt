package com.zhangs_tech.runtomo.summon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhangs_tech.runtomo.network.ApiException
import com.zhangs_tech.runtomo.tomo.Nickname
import com.zhangs_tech.runtomo.tomo.TomoApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SummonPhase {
    data object Idle : SummonPhase
    data object Confirming : SummonPhase
    data object Summoning : SummonPhase

    /** The connection dropped during a summon. The app reads the newest summon (UC-12 6a). */
    data object Checking : SummonPhase
    data object CheckFailed : SummonPhase
    data class Hatching(val result: SummonResult) : SummonPhase
    data class Revealed(val result: SummonResult) : SummonPhase
}

enum class SummonError { NoConnection, LostConnection, Failed, InvalidNickname }

data class SummonUiState(
    val isLoading: Boolean = true,
    val banner: Banner? = null,
    val phase: SummonPhase = SummonPhase.Idle,
    /** UC-12 4a: about how far the Player must walk to afford a summon. */
    val distanceNeededM: Long? = null,
    val isEquipped: Boolean = false,
    val isRenaming: Boolean = false,
    val isSaving: Boolean = false,
    val error: SummonError? = null,
)

@HiltViewModel
class SummonViewModel @Inject constructor(
    private val summonApi: SummonApi,
    private val tomoApi: TomoApi,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SummonUiState())
    val uiState: StateFlow<SummonUiState> = _uiState.asStateFlow()

    /** The newest summon before this screen summons. A newer one after a lost response is the real result. */
    private var latestKnownId: String? = null

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // coroutineScope rethrows a failed request here, so the catch below sees it.
                val banner = coroutineScope {
                    val latest = async { summonApi.latest() }
                    val banner = summonApi.getBanner(BANNER_CODE)
                    latestKnownId = latest.await()?.id
                    banner
                }
                _uiState.update { it.copy(isLoading = false, banner = banner) }
            } catch (e: ApiException) {
                _uiState.update { it.copy(isLoading = false, error = e.toError()) }
            }
        }
    }

    /** UC-12 steps 2 and 3. Without enough TomoCoins the server answers at once with the distance needed. */
    fun requestSummon() {
        val banner = _uiState.value.banner ?: return
        if (_uiState.value.phase != SummonPhase.Idle) return
        if (banner.coinBalance < banner.cost) {
            confirmSummon()
        } else {
            _uiState.update { it.copy(phase = SummonPhase.Confirming, distanceNeededM = null, error = null) }
        }
    }

    fun cancelSummon() {
        _uiState.update { it.copy(phase = SummonPhase.Idle) }
    }

    fun confirmSummon() {
        _uiState.update { it.copy(phase = SummonPhase.Summoning, distanceNeededM = null, error = null) }
        viewModelScope.launch {
            try {
                when (val attempt = summonApi.summon(BANNER_CODE)) {
                    is SummonAttempt.Success -> hatch(attempt.result)
                    is SummonAttempt.NotEnoughCoins ->
                        _uiState.update { it.copy(phase = SummonPhase.Idle, distanceNeededM = attempt.distanceNeededM) }
                }
            } catch (e: ApiException.Network) {
                checkResult()
            } catch (e: ApiException) {
                _uiState.update { it.copy(phase = SummonPhase.Idle, error = SummonError.Failed) }
            }
        }
    }

    /** UC-12 6a: the Player never loses TomoCoins without seeing the result. */
    fun checkResult() {
        _uiState.update { it.copy(phase = SummonPhase.Checking, error = null) }
        viewModelScope.launch {
            try {
                val latest = summonApi.latest()
                if (latest != null && latest.id != latestKnownId) {
                    hatch(latest)
                } else {
                    _uiState.update { it.copy(phase = SummonPhase.Idle, error = SummonError.LostConnection) }
                }
            } catch (e: ApiException) {
                _uiState.update { it.copy(phase = SummonPhase.CheckFailed) }
            }
        }
    }

    private fun hatch(result: SummonResult) {
        latestKnownId = result.id
        _uiState.update {
            it.copy(
                phase = SummonPhase.Hatching(result),
                banner = it.banner?.copy(coinBalance = result.coinBalance),
                isEquipped = false,
                isRenaming = false,
            )
        }
    }

    /** The egg animation finished. */
    fun onHatched() {
        val phase = _uiState.value.phase as? SummonPhase.Hatching ?: return
        _uiState.update { it.copy(phase = SummonPhase.Revealed(phase.result)) }
    }

    fun equip() {
        val tomo = (_uiState.value.phase as? SummonPhase.Revealed)?.result?.tomo ?: return
        save {
            tomoApi.equip(tomo.id)
            _uiState.update { it.copy(isEquipped = true) }
        }
    }

    fun startRename() {
        _uiState.update { it.copy(isRenaming = true, error = null) }
    }

    fun cancelRename() {
        _uiState.update { it.copy(isRenaming = false, error = null) }
    }

    fun rename(nickname: String) {
        val result = (_uiState.value.phase as? SummonPhase.Revealed)?.result ?: return
        val tomo = result.tomo ?: return
        val trimmed = nickname.trim()
        if (!Nickname.isValid(trimmed)) {
            _uiState.update { it.copy(error = SummonError.InvalidNickname) }
            return
        }
        save {
            val renamed = tomoApi.rename(tomo.id, trimmed)
            _uiState.update {
                it.copy(phase = SummonPhase.Revealed(result.copy(tomo = renamed)), isRenaming = false)
            }
        }
    }

    fun done() {
        _uiState.update { it.copy(phase = SummonPhase.Idle, isRenaming = false, error = null) }
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

    private fun ApiException.toError() = if (this is ApiException.Network) SummonError.NoConnection else SummonError.Failed

    private companion object {
        const val BANNER_CODE = "standard"
    }
}
