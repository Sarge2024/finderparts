package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.PartAssociation
import com.example.data.PartRepository
import com.example.data.ScannedPart
import com.example.data.Vehicle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PartAssociationViewModel(private val repository: PartRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.initDefaultDataIfNeeded()
            // Set initial scanned part if none exists
            val parts = repository.allScannedParts.stateIn(viewModelScope).value
            if (parts.isNotEmpty()) {
                _scannedPart.value = parts.first()
            } else {
                // If it is still reading, we can load it once we fetch
                launch {
                    repository.allScannedParts.collect { list ->
                        if (list.isNotEmpty() && _scannedPart.value == null) {
                            _scannedPart.value = list.first()
                        }
                    }
                }
            }

            // Set initial primary vehicle (Voyage 2011)
            launch {
                repository.allVehicles.collect { list ->
                    if (list.isNotEmpty()) {
                        if (_primaryVehicle.value == null) {
                            _primaryVehicle.value = list.find { it.model == "VOYAGE" } ?: list.first()
                        }
                        if (_compatibleVehicles.value.isEmpty()) {
                            // Prepopulate with a few compatible vehicles (VW Gol 2012, VW Fox 2010, Saveiro 2013)
                            val gol = list.find { it.model == "GOL" }
                            val fox = list.find { it.model == "FOX" }
                            val saveiro = list.find { it.model == "SAVEIRO" }
                            val prefilled = listOfNotNull(gol, fox, saveiro)
                            _compatibleVehicles.value = prefilled
                        }
                    }
                }
            }
        }
    }

    val allVehicles: StateFlow<List<Vehicle>> = repository.allVehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allScannedParts: StateFlow<List<ScannedPart>> = repository.allScannedParts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAssociations: StateFlow<List<PartAssociation>> = repository.allAssociations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeTab = MutableStateFlow("ESCANEAR")
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    private val _scannedPart = MutableStateFlow<ScannedPart?>(null)
    val scannedPart: StateFlow<ScannedPart?> = _scannedPart.asStateFlow()

    private val _primaryVehicle = MutableStateFlow<Vehicle?>(null)
    val primaryVehicle: StateFlow<Vehicle?> = _primaryVehicle.asStateFlow()

    private val _compatibleVehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val compatibleVehicles: StateFlow<List<Vehicle>> = _compatibleVehicles.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _showToastMessage = MutableStateFlow<String?>(null)
    val showToastMessage: StateFlow<String?> = _showToastMessage.asStateFlow()

    fun selectTab(tab: String) {
        _activeTab.value = tab
    }

    fun clearToast() {
        _showToastMessage.value = null
    }

    fun toggleSyncMode() {
        _isOnline.value = !_isOnline.value
        _showToastMessage.value = if (_isOnline.value) {
            "API CONECTADA - SINCRONIZANDO DADOS..."
        } else {
            "TRABALHANDO OFFLINE - SEGURANÇA LOCAL ATIVA"
        }
    }

    fun simulateScan(barcode: String? = null) {
        viewModelScope.launch {
            val parts = allScannedParts.value
            if (parts.isEmpty()) return@launch

            val nextPart = if (barcode != null) {
                parts.find { it.barcode == barcode }
            } else {
                val current = _scannedPart.value
                val currentIndex = parts.indexOf(current)
                val nextIndex = (currentIndex + 1) % parts.size
                parts[nextIndex]
            }

            _scannedPart.value = nextPart
            _showToastMessage.value = "PEÇA ESCANEADA: ${nextPart?.name}"
        }
    }

    fun selectPrimaryVehicle(vehicle: Vehicle) {
        _primaryVehicle.value = vehicle
        // Remove it from compatibility if it was there to prevent duplicates
        _compatibleVehicles.value = _compatibleVehicles.value.filter { it.id != vehicle.id }
        _showToastMessage.value = "VEÍCULO ALVO SELECIONADO: ${vehicle.brand} ${vehicle.model}"
    }

    fun addCompatibleVehicle(vehicle: Vehicle) {
        val currentPrimary = _primaryVehicle.value
        if (currentPrimary?.id == vehicle.id) {
            _showToastMessage.value = "Veículo já é o vínculo primário"
            return
        }
        val currentList = _compatibleVehicles.value
        if (currentList.any { it.id == vehicle.id }) {
            _showToastMessage.value = "Veículo já adicionado à compatibilidade"
            return
        }
        _compatibleVehicles.value = currentList + vehicle
        _showToastMessage.value = "Adicionado: ${vehicle.brand} ${vehicle.model}"
    }

    fun removeCompatibleVehicle(vehicle: Vehicle) {
        _compatibleVehicles.value = _compatibleVehicles.value.filter { it.id != vehicle.id }
        _showToastMessage.value = "Removido: ${vehicle.brand} ${vehicle.model}"
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveAssociation() {
        val part = _scannedPart.value
        val primary = _primaryVehicle.value
        if (part == null || primary == null) {
            _showToastMessage.value = "Erro: Escaneie uma peça e escolha o veículo principal"
            return
        }

        viewModelScope.launch {
            val association = PartAssociation(
                barcode = part.barcode,
                primaryVehicleId = primary.id,
                compatibleVehicleIds = _compatibleVehicles.value.map { it.id }
            )
            repository.insertAssociation(association)
            _showToastMessage.value = "Associação salva com sucesso!"
        }
    }

    fun deleteAssociation(id: Int) {
        viewModelScope.launch {
            repository.deleteAssociation(id)
            _showToastMessage.value = "Associação excluída"
        }
    }
}

class PartAssociationViewModelFactory(private val repository: PartRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PartAssociationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PartAssociationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
