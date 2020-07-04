import com.kenvix.complexbot.ComplexBotDriver
import com.kenvix.complexbot.callBridge
import com.kenvix.moecraftbot.ng.Bootstrapper
import com.kenvix.moecraftbot.ng.Defines
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.CommandLine
import org.junit.Test

//--------------------------------------------------
// Class BackendAPITest
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

class BackendAPITest {
    @Test
    fun testClassificateTextMessage() = runBlocking {
        Bootstrapper.startCoreOnly(arrayOf("-d complexbot"))
        val driver = ComplexBotDriver()
        driver.loadBackend()
        println("Backend loaded")
        driver.backendClient!!.classificateTextMessage("这个是咱们学校的学习墙开学准备（基本包含各学科）技能提升（计算机二级" +
                "  word  ppt等）各种考证资料（考研 四六级  二级  教资 单招 会计 专升本等）基本都有的，抗疫时期闲着也是闲着 需要的加墙墙就好啦")
        Unit
    }
}