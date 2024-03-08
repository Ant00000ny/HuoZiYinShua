import com.github.promeg.pinyinhelper.Pinyin
import java.io.File
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.function.Consumer


/**
 * 拼接音频片段
 */
private fun concatAudio(voiceParts: List<VoicePart>): ByteArray? {
    val files = voiceParts.mapNotNull { getAudio(it) }
    if (voiceParts.isEmpty()) {
        throw Exception("No syllable found")
    }

    val outputFile = tempDir.resolve("${Instant.now().toEpochMilli()}-result.wav")
    val command = run {
        val command = mutableListOf<String>()
        command.add("ffmpeg")

        files.forEach(Consumer { filePath ->
            command.add("-i")
            command.add(filePath.absolutePath)
        })


        val filterComplex = StringBuilder()
        files.forEachIndexed { i, _ ->
            filterComplex.append(String.format("[%d:0]", i))
        }
        filterComplex.append(String.format("concat=n=%d:v=0:a=1[out]", files.size))

        command.add("-filter_complex")
        command.add(filterComplex.toString())
        command.add("-map")
        command.add("[out]")
        command.add(outputFile.absolutePath)
        command
    }

    val processBuilder = ProcessBuilder(command)
    val process = processBuilder
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
    if (!process.waitFor(15, TimeUnit.SECONDS)) {
        process.destroyForcibly()
        return null
    }

    if (process.exitValue() != 0) {
        return null
    }

    return outputFile.readBytes()
}

/**
 * 在 [tempDir] 下创建临时文件
 */
private fun createTempFileIfNotExist(fileName: String, content: ByteArray?): File? {
    content ?: return null

    val file = tempDir.resolve(fileName)
    if (!file.exists()) file.writeBytes(content)

    return file
}

/**
 * 根据拼音片段获取音频
 */
private fun getAudio(voicePart: VoicePart): File {
    val filename = "${voicePart.str}.wav"
    val tempFile = tempDir.resolve(filename)
    if (tempFile.exists()) {
        return tempFile
    }

    val classLoader = object {}.javaClass.classLoader
    val inputStream = if (voicePart.isYuanShengDaDie) {
        classLoader.getResourceAsStream("ysdd/$filename")
            ?: classLoader.getResourceAsStream("syllable/_.wav")
    } else {
        classLoader.getResourceAsStream("syllable/$filename")
            ?: classLoader.getResourceAsStream("syllable/_.wav")
    }
    tempFile
        .also { it.createNewFile() }
        .writeBytes(inputStream.use { it.readAllBytes() })


    return tempFile
}

/**
 * 原声大碟替换、单字替换
 */
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

        listIterator.remove()
        pinyin.forEach {
            listIterator.add(VoicePart(it, false))
        }
    }

    return voicePartList
}

fun huoZiYinShua(s: String) {
    val voicePartList = parsePinyin(s)
    concatAudio(voicePartList) ?: return
}

data class VoicePart(val str: String, val isYuanShengDaDie: Boolean)
