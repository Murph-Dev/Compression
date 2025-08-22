//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {

    val compressionEngine: CompressionEngine = HuffmanCompression()

    val fileAbsPath = "/path/to/file.txt"

    compressionEngine.compress(fileAbsPath)
}

