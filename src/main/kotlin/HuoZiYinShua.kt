import com.github.promeg.pinyinhelper.Pinyin
import java.io.File
import java.io.InputStream
import java.io.SequenceInputStream
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.time.Duration

fun huoZiYinShua(syllables: List<String>): File {
    val syllableFiles = syllables.mapNotNull { createTempFileIfNotExist("$it.wav", getAudio("$it.wav")) }
    if (syllables.isEmpty()) {
        throw Exception("No syllable found")
    }
    val audioInputStreams = syllableFiles.map { AudioSystem.getAudioInputStream(it) }
    val format = audioInputStreams.first().format
    val totalFrameLength = audioInputStreams.map { it.frameLength }
        .reduce { x, y -> x + y }

    try {
        val sequenceInputStream = SequenceInputStream(object : Enumeration<InputStream> {
            private val iterator = audioInputStreams.iterator()

            override fun hasMoreElements(): Boolean = iterator.hasNext()

            override fun nextElement(): InputStream = iterator.next()
        })


        val newFile = tempDir.resolve("output.wav")

        AudioSystem.write(
            AudioInputStream(
                sequenceInputStream,
                format,
                totalFrameLength
            ),
            AudioSystem.getAudioFileFormat(syllableFiles.first()).type,
            newFile
        )

        return newFile
    } finally {
        audioInputStreams.forEach { it.close() }
        syllableFiles.forEach { it.delete() }
    }
}

fun createTempFileIfNotExist(fileName: String, content: ByteArray?): File? {
    content ?: return null

    val file = tempDir.resolve(fileName)
    if (!file.exists()) file.writeBytes(content)

    return file
}

fun getAudio(name: String): ByteArray? {
    val inputStream = object {}.javaClass.classLoader
        .getResourceAsStream("yuanshengdadie/$name")
        ?: return null
    return inputStream.readBytes()
}

fun parsePinyin(input: String): List<String> = Pinyin.toPinyin(input, "|")
    .split("|")
    .map { it.lowercase() }


fun execBlock(command: String, timeout: Duration) {
    val process = ProcessBuilder(command.split(" "))
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()

    if (!process.waitFor(timeout.inWholeSeconds.coerceAtLeast(1), TimeUnit.SECONDS)) {
        process.destroy()
        throw Exception("Command $command timeout")
    }

    if (process.exitValue() != 0) {
        throw Exception("Command $command failed")
    }

    process.destroy()
}

val tempDir: File = Paths.get(System.getProperty("user.dir")).toFile()

fun main() {
    val s = "测试s你好"
    val pinyin = parsePinyin(s)
    val file = huoZiYinShua(pinyin)
    println(file.absolutePath)
}
