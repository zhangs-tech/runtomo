package com.zhangs_tech.runtomo.server.tomo

import com.zhangs_tech.runtomo.server.game.Species
import java.util.UUID

data class CollectionEntry(val species: Species, val tomo: Tomo?)

data class Collection(val entries: List<CollectionEntry>, val equippedTomoId: UUID?)
