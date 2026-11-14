package com.yedheedog.gachahub.data

data class Game(
    val id: String,
    val name: String,
    val imageUrl: String,
    val regionLabel: String = "Japan"
)

val supportedGames = listOf(
    Game(
        "hololive_dreams",
        "hololive Dreams",
        "https://cimg.kgl-systems.io/camion/files/dengeki/67952/a2e095047ae6dc362e7e121b228d9ec42.jpeg?x=1280",
        "Worldwide"
    ),
    Game(
        "学園アイドルマスター",
        "Gakuen Idolmaster",
        "https://www.jigsaw.jp/img/goods/L/epo7738912708.jpg"
    ),
    Game(
        "umamusume",
        "Uma Musume",
        "https://cdn2.fptshop.com.vn/unsafe/Uploads/images/tin-tuc/186497/Originals/uma-musume-pretty-derby-1.jpeg"
    ),
    Game(
        "七つの大罪",
        "The Seven Deadly Sins: Grand Cross",
        "https://p2.bahamut.com.tw/B/2KU/28/bde0b1473e1c139bcad84ad9b110yqc5.JPG"
    )
)
