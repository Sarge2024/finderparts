package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class PartRepository(private val partDao: PartDao) {
    val allVehicles: Flow<List<Vehicle>> = partDao.getAllVehicles()
    val allScannedParts: Flow<List<ScannedPart>> = partDao.getAllScannedParts()
    val allAssociations: Flow<List<PartAssociation>> = partDao.getAllAssociations()

    suspend fun getScannedPartByBarcode(barcode: String): ScannedPart? {
        return partDao.getScannedPartByBarcode(barcode)
    }

    suspend fun insertAssociation(association: PartAssociation) {
        partDao.insertAssociation(association)
    }

    suspend fun deleteAssociation(id: Int) {
        partDao.deleteAssociationById(id)
    }

    suspend fun initDefaultDataIfNeeded() {
        val currentVehicles = partDao.getAllVehicles().firstOrNull() ?: emptyList()
        if (currentVehicles.isEmpty()) {
            val defaultVehicles = listOf(
                Vehicle(brand = "VOLKSWAGEN", model = "VOYAGE", modification = "5U2*** / HIGHLINE", year = 2011),
                Vehicle(brand = "VOLKSWAGEN", model = "GOL", modification = "G5 / TREND", year = 2012),
                Vehicle(brand = "VOLKSWAGEN", model = "FOX", modification = "1.6 PRIME", year = 2010),
                Vehicle(brand = "VOLKSWAGEN", model = "SAVEIRO", modification = "CROSS", year = 2013),
                Vehicle(brand = "CHEVROLET", model = "ONIX", modification = "1.4 LTZ", year = 2018),
                Vehicle(brand = "FIAT", model = "UNO", modification = "1.0 WAY", year = 2015),
                Vehicle(brand = "FORD", model = "KA", modification = "1.5 SEL", year = 2017),
                Vehicle(brand = "HYUNDAI", model = "HB20", modification = "1.6 COMFORT", year = 2016)
            )
            partDao.insertVehicles(defaultVehicles)
        }

        val currentParts = partDao.getAllScannedParts().firstOrNull() ?: emptyList()
        if (currentParts.isEmpty()) {
            val defaultParts = listOf(
                ScannedPart(
                    barcode = "5U0903025B-2024",
                    name = "ALTERNADOR BR SPEC",
                    originalReference = "5U0 903 025 B",
                    specification = "14V / 90A",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAheJix2HfNLjYvRF3bN6uy8Fl6sjkfONsjqVZigQZlNab4r-Wm4fjUojQUz10bP0EEy4p19imlJ2ZVIDSFbxVNWAL-Ib4u1UOStebdISBSrE0Ivo7JJ7D4YXpxLlmYbNtHb8v7ZLAqM5TUPCt73GVxRQeW8rqbqGcIw9tXwkGzhz_x_2e0GdDZzY5tjfIeitVj74D3H3EOT74w9Lr18OBSpzQBz9LTNLjxRvYnAy1u51zA3e_Byeg-_w"
                ),
                ScannedPart(
                    barcode = "02M911023X-2024",
                    name = "MOTOR DE PARTIDA VWB",
                    originalReference = "02M 911 023 X",
                    specification = "12V / 1.1kW",
                    imageUrl = "https://images.unsplash.com/photo-1619642751034-765dfdf7c58e?auto=format&fit=crop&q=80&w=400"
                ),
                ScannedPart(
                    barcode = "702550410-2024",
                    name = "BOMBA DE COMBUSTÍVEL AUX",
                    originalReference = "7.02550.41.0",
                    specification = "3.0 Bar / 95 L/h",
                    imageUrl = "https://images.unsplash.com/photo-1486006920555-c77dce18193b?auto=format&fit=crop&q=80&w=400"
                ),
                ScannedPart(
                    barcode = "036905715F-2024",
                    name = "BOBINA DE IGNIÇÃO BOSCH",
                    originalReference = "036 905 715 F",
                    specification = "4 PINOS",
                    imageUrl = "https://images.unsplash.com/photo-1517524206127-48bbd363f3d7?auto=format&fit=crop&q=80&w=400"
                )
            )
            partDao.insertScannedParts(defaultParts)
        }
    }
}
