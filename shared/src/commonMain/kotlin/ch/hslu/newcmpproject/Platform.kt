package ch.hslu.newcmpproject

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform