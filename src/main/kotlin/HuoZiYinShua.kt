import com.github.promeg.pinyinhelper.Pinyin
import java.io.File
import java.io.InputStream
import java.io.SequenceInputStream
import java.util.*
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

fun huoZiYinShua(voiceParts: List<VoicePart>): File {
    val syllableFiles = voiceParts.mapNotNull { createTempFileIfNotExist("${it.str}.wav", getAudio(it)) }
    if (voiceParts.isEmpty()) {
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

private fun createTempFileIfNotExist(fileName: String, content: ByteArray?): File? {
    content ?: return null

    val file = tempDir.resolve(fileName)
    if (!file.exists()) file.writeBytes(content)

    return file
}

private fun getAudio(voicePart: VoicePart): ByteArray? {
    val inputStream = if (voicePart.isYuanShengDaDie) {
        object {}.javaClass.classLoader
            .getResourceAsStream("ysdd/${voicePart.str}.wav")
            ?: return null
    } else {
        object {}.javaClass.classLoader
            .getResourceAsStream("syllable/${voicePart.str}.wav")
            ?: return null
    }

    return inputStream.readBytes()
}

private fun parsePinyin(input: String): List<VoicePart> {
    val voicePartList = mutableListOf(VoicePart(input, false))

    yuanShengDaDie.forEach { (str, ysddFileName) ->
        val listIterator = voicePartList.listIterator()
        while (listIterator.hasNext()) {
            val part = listIterator.next()
            if (part.isYuanShengDaDie) {
                continue
            }

            val range = part.str.indexOf(str).let { it..<it + str.length }
            if (range.first == -1) {
                continue
            }

            listIterator.remove()

            part.str.substring(range.last + 1)
                .takeIf { it.isNotEmpty() }
                ?.let {
                    listIterator.add(VoicePart(it, false))
                    listIterator.previous()
                }

            listIterator.add(VoicePart(ysddFileName, true))
            listIterator.previous()

            part.str.substring(0, range.first)
                .takeIf { it.isNotEmpty() }
                ?.let {
                    listIterator.add(VoicePart(it, false))
                    listIterator.previous()
                }
        }
    }

    // ---

    val listIterator = voicePartList.listIterator()
    while (listIterator.hasNext()) {
        val part = listIterator.next()
        if (part.isYuanShengDaDie) {
            continue
        }

        val pinyin = Pinyin.toPinyin(part.str, "|")
            .split("|")
            .map { it.lowercase() }
            .flatMap {
                if (it in alterSyllables) {
                    alterSyllables[it]!!
                } else {
                    listOf(it)
                }
            }

        pinyin.forEach {
            listIterator.add(VoicePart(it, false))
        }
    }

    return voicePartList
}

fun main() {
    val s = "大家好，我是说的道理，今天来点大家想看的东西"
    val voicePartList = parsePinyin(s)
    val file = huoZiYinShua(voicePartList)
    println(file.absolutePath)
}

data class VoicePart(val str: String, val isYuanShengDaDie: Boolean)
