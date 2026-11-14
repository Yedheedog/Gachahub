package com.yedheedog.gachahub.data

data class Game(
    val id: String,
    val name: String,
    val regionLabel: String = "Japan"
)

val supportedGames = listOf(
    Game("hololive_dreams", "hololive Dreams"),
    Game("学園アイドルマスター", "Gakuen Idolmaster"),
    Game("umamusume", "Uma Musume"),
    Game("七つの大罪", "The Seven Deadly Sins: Grand Cross")
)
