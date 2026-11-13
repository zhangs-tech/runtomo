package com.zhangs_tech.runtomo.server.tomo.dto

import com.zhangs_tech.runtomo.server.tomo.Collection
import java.util.UUID

data class CollectionEntryResponse(val species: SpeciesResponse, val tomo: TomoResponse?)

data class CollectionResponse(
    val entries: List<CollectionEntryResponse>,
    val collectedCount: Int,
    val totalCount: Int,
    val equippedTomoId: UUID?,
) {
    companion object {
        fun from(collection: Collection) = CollectionResponse(
            entries = collection.entries.map { entry ->
                CollectionEntryResponse(
                    species = SpeciesResponse.from(entry.species),
                    tomo = entry.tomo?.let { TomoResponse.from(it, entry.species) },
                )
            },
            collectedCount = collection.entries.count { it.tomo != null },
            totalCount = collection.entries.size,
            equippedTomoId = collection.equippedTomoId,
        )
    }
}
