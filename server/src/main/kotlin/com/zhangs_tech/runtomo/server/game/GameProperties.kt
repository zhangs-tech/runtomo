package com.zhangs_tech.runtomo.server.game

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("runtomo.game")
data class GameProperties(
    val species: List<Species>,
    val tunables: Tunables,
    val quests: List<Quest>,
    val banners: List<Banner>,
    val foods: List<Food>,
)
