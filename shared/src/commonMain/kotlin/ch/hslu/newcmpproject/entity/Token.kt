package ch.hslu.newcmpproject.entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Serializable
data class Token(val value: String)

fun decodeBase64Url(input: String): ByteArray {
    // Base64 URL safe: '-' -> '+', '_' -> '/'
    val fixed = input.replace('-', '+')
        .replace('_', '/')
        .padEnd((input.length + 3) / 4 * 4, '=')

    val base64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    val clean = fixed.filter { it != '=' }

    val output = mutableListOf<Byte>()
    var buffer = 0
    var bitsLeft = 0

    for (c in clean) {
        val value = base64Chars.indexOf(c)
        if (value == -1) throw IllegalArgumentException("Invalid Base64 char: $c")

        buffer = (buffer shl 6) or value
        bitsLeft += 6
        if (bitsLeft >= 8) {
            bitsLeft -= 8
            output.add(((buffer shr bitsLeft) and 0xFF).toByte())
        }
    }
    return output.toByteArray()
}

fun ByteArray.toUtf8String(): String {
    return this.decodeToString()
}

fun decodeBase64UrlToString(input: String): String {
    return decodeBase64Url(input).toUtf8String()
}