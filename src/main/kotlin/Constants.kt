import java.io.File
import java.nio.file.Paths

val tempDir: File = Paths.get(System.getProperty("user.dir")).toFile()


val alterSyllables = mapOf(
    "a" to "ei",
    "b" to "bi",
    "c" to "xi",
    "d" to "di",
    "e" to "yi",
    "f" to "ai fu",
    "g" to "ji",
    "h" to "ai chi",
    "i" to "ai",
    "j" to "zhei",
    "k" to "kei",
    "l" to "ai lu",
    "m" to "ai mu",
    "n" to "en",
    "o" to "ou",
    "p" to "pi",
    "q" to "kiu",
    "r" to "a",
    "s" to "ai si",
    "t" to "ti",
    "u" to "you",
    "v" to "wei",
    "w" to "da bu liu",
    "x" to "ai ke si",
    "y" to "wai",
    "z" to "zei",
    "0" to "ling",
    "1" to "yi",
    "2" to "er",
    "3" to "san",
    "4" to "si",
    "5" to "wu",
    "6" to "liu",
    "7" to "qi",
    "8" to "ba",
    "9" to "jiu"
)
    .map { (k, v) -> k to v.split(" ") }
    .toMap()

val yuanShengDaDie = mapOf(
    "bobi" to listOf("波比是我爹", "阿玛波比是我爹", "阿巴波比是我爹", "波比是我妈爹"),
    "djha" to listOf("大家好啊", "大家好"),
    "jtld" to listOf("今天来点大家想看的东西", "今天来点儿大家想看的东西"),
    "miyu" to listOf("啊米浴说的道理", "米浴说的道理"),
    "wssddl" to listOf("我是说的道理"),
    "sddl" to listOf("说的道理"),
    "ydglm" to listOf("一德格拉米"),
    "by" to listOf("白银"),
    "ds" to listOf("大司"),
    "d" to listOf("爹"),
    "ga" to listOf("滚啊", "滚"),
    "snr" to listOf("山泥若"),
    "kzzll" to listOf("卡在这里了"),
    "jyxa" to listOf("救一下啊", "救一下"),
    "gwysmgx" to listOf("跟我有什么关系"),
    "nzmzmca" to listOf("你怎么这么菜啊", "你怎么这么菜"),
    "wao" to listOf("哇袄", "哇奥"),
    "omns" to listOf("阿米诺斯", "阿弥诺斯"),
    "wcsndm" to listOf("我阐述你的梦"),
)
    .flatMap { (ysddFileName, aliasList) ->
        aliasList.map { alias ->
            alias to ysddFileName
        }
    }
    .toMap()
