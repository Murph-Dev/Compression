import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.util.PriorityQueue
import java.util.Queue
import kotlin.math.round

class HuffmanCompression: CompressionEngine {
    override fun compress(path: String) {
        // Get File
        println("Fetching File...")

        val file = getFile(path)
        if (file == null) {
            println("File found null. Cannot compress...")
            return
        }
        val startingSize = file.length()
        println("Starting File Size: ${startingSize} bytes")
        println("")

        // Get Freq Map
        val usageMap = buildMap(file)
        if (usageMap.isEmpty()) {
            println("Usage map was empty. Cannot compress...")
            return
        }


        // Get Freq Queue
        val pq = buildFreqQueue(usageMap)

        println("Building Huffman Tree...")
        println("")
        // Build Huffman Tree
        val treeRoot = buildHuffTree(pq)

        println("Tree build success...")
        println("")

        // Build mapping file
        val mapping = printTree(treeRoot)

        println("Saving mapping file...")
        println("")
        // Save mapping file
        saveMappingFile(mapping)
        val mappingFile = File("mapping.huff")

        println("Compressing File...")
        println("")
        compressFile(file, treeRoot, mapping)

        println("Compression Finished")
        println("")
        val compressedFile = File("compressed.huff")
        val endSize = compressedFile.length()
        println("Compressed File Size: ${endSize} bytes")

        println("")
        println("================================================================")
        println("File: ${file.name}")
        println("Starting Size: ${startingSize} bytes")
        println("Compressed Size: ${endSize} bytes")
        println("File size change of ${percentageChange(startingSize, endSize)}%")
        println("================================================================")
        println("Starting File Path: ${file.absolutePath}")
        println("Compressed File Path: ${compressedFile.absolutePath}")
        println("Mapping File Path: ${mappingFile.absolutePath}")
        println("================================================================")
    }

    fun percentageChange(oldSize: Long, newSize: Long): Double {
        if (oldSize == 0L) {
            throw IllegalArgumentException("Old size cannot be zero when calculating percentage change.")
        }
        val rawChange = ((newSize - oldSize).toDouble() / oldSize.toDouble()) * 100
        return (round(rawChange * 100) / 100)  // rounds to 2 decimal places
    }

    private fun compressFile(file: File, root: Node, mapping: Map<Char, String>)  {
        val reader = file.bufferedReader()
        val output = DataOutputStream(BufferedOutputStream(FileOutputStream("compressed.huff")))
        val bitBuffer = StringBuilder()

        reader.forEachLine { line ->
            line.forEach { bitBuffer.append(mapping[it]) }
        }

        val encodedBinary = bitBuffer.toString()
//        output.writeInt(encodedBinary.length)
        output.write(binaryStringToByteArray(encodedBinary))

        reader.close()
        output.flush()
        output.close()
    }

    private fun binaryStringToByteArray(binary: String): ByteArray {
        val padded = binary.padEnd((binary.length + 7) / 8 * 8, '0')
        return padded.chunked(8)
            .map { it.toUByte(2).toByte() }
            .toByteArray()
    }

    private fun saveMappingFile(mapping: Map<Char,String>) {
        val writer = File("mapping.huff").bufferedWriter()


        for ((char, code) in mapping) {
            writer.write("'${char.code}' -> $code")
            writer.newLine()
        }

        writer.close()
    }

    private fun printTree(root: Node, prefix: String = "", codes: MutableMap<Char, String> = mutableMapOf()): Map<Char, String> {
        if(root.character == null) {
            printTree(root.left!!, prefix + "0", codes)
            printTree(root.right!!, prefix + "1", codes)
        } else {
            codes[root.character!!] = prefix
        }
        return codes
    }

    private fun buildHuffTree(pq: PriorityQueue<Node>): Node {
        while(pq.size > 1) {
            val left = pq.poll()
            val right = pq.poll()
            val parentNode = Node(null, left.freq + right.freq, left, right)
            pq.add(parentNode)
        }
        return pq.poll()
    }

    private fun buildFreqQueue(map: Map<Char, Int>): PriorityQueue<Node> {

        val pq = PriorityQueue<Node>(compareBy { it.freq })

        val m = map.toList().sortedBy { (_,value) -> value }.toMap()

        m.forEach { k,v ->
            pq.add(Node(k,v, null, null))
        }

        return pq
    }

    private fun buildMap(file: File): Map<Char, Int> {
        val m = mutableMapOf<Char, Int>()

        try {
            val reader = file.bufferedReader()
            var charCode: Int

            while(reader.read().also { charCode = it} != -1) {
                m.merge(charCode.toChar(), 1, Int::plus)
            }

            reader.close()
            return m.toMap()

        } catch (e: Exception) {
            println("Exception thrown while building usage map.")
            return mapOf<Char,Int>()
        }

    }

    override fun decompress(path: String, map: Any?) {
        // TODO
    }

    private fun getFile(path: String): File? {
        try {
            val file = File(path)

            println("File Found! - ${file.name}")
            println("")
            return file
        }
        catch (e: Exception) {
            println("Exception occurred when fetching file. $e")
        }
        return null
    }
}