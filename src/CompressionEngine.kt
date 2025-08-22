import java.nio.file.Path

interface CompressionEngine {
    fun compress(path: String)
    fun decompress(path: String, map: Any?)
}