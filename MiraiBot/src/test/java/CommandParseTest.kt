import com.kenvix.moecraftbot.mirai.lib.parseCommandFromMessage
import com.kenvix.moecraftbot.ng.Defines
import com.kenvix.moecraftbot.ng.lib.exception.UserInvalidUsageException
import org.junit.Test

//--------------------------------------------------
// Class CommandParseTest
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

class CommandParseTest {
    @Test
    fun testParseCommand() {
        Defines.setupSystem()

        try {
            parseCommandFromMessage("", true)
            throw AssertionError("expected UserInvalidUsageException not thrown")
        } catch (expected: UserInvalidUsageException) { }

        val b = parseCommandFromMessage(".repeat 18 fffffd sasdd s", true)
        println(b)
    }
}