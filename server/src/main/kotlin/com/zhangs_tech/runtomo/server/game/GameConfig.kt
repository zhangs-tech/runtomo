package com.zhangs_tech.runtomo.server.game

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
@EnableConfigurationProperties(GameProperties::class)
class GameConfig {
    @Bean
    fun gameData(properties: GameProperties) =
        GameData(properties.species, properties.tunables, properties.quests, properties.banners)

    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
