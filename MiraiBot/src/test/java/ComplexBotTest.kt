import com.kenvix.complexbot.ComplexBotDriver
import com.kenvix.moecraftbot.ng.lib.asFlow
import com.kenvix.utils.tools.CommonTools
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.flow.asFlow
import org.junit.Test
import java.io.IOException

//--------------------------------------------------
// Class BackendTest
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

@Deprecated("")
class ComplexBotTest {
    @Test
    fun initTest() {
        mapOf<Any, Any>().asSequence()
        mapOf<Any, Any>().asFlow()
        listOf<Any>().asFlow()
    }

    @Test
    fun getSysEnv() {
        System.getenv().forEach {
            println("${it.key} -> ${it.value}")
        }
    }

    @Test
    fun getSysProps() {
        System.getProperties().forEach {
            println("${it.key} -> ${it.value}")
        }
    }

    @Test
    fun loadBackend() = runBlocking {
        ComplexBotDriver().loadBackend()
    }

    @Test
    fun coroutineTest() {
        println("fin  ${Thread.currentThread().name} ")

        runBlocking {
            withContext(IO) {
                launch { sleepNoException(1000) }
                launch { sleepWithThrowsAnnotation(1000) }
                launch { Thread.sleep(1000); println("fin4-1 ${Thread.currentThread().name} : " + this.coroutineContext) }
                launch { Thread.sleep(1000); println("fin5-1 ${Thread.currentThread().name} : " + this.coroutineContext) }
                launch { Thread.sleep(1000); println("fin6-1 ${Thread.currentThread().name} : " + this.coroutineContext) }
                launch { Thread.sleep(1000); println("fin7-1 ${Thread.currentThread().name} : " + this.coroutineContext) }
            }

            withContext(Unconfined) {
                launch { Thread.sleep(1000); println("fin4-2 ${Thread.currentThread().name} : " + this.coroutineContext) }
                launch { Thread.sleep(1000); println("fin5-2 ${Thread.currentThread().name} : " + this.coroutineContext) }
                launch { Thread.sleep(1000); println("fin6-2 ${Thread.currentThread().name} : " + this.coroutineContext) }
                launch { Thread.sleep(1000); println("fin7-2 ${Thread.currentThread().name} : " + this.coroutineContext) }
            }

            launch { Thread.sleep(1000); println("fin1 ${Thread.currentThread().name} : " + this.coroutineContext) }
            launch { Thread.sleep(1000); println("fin2 ${Thread.currentThread().name} : " + this.coroutineContext) }
            launch { Thread.sleep(1000); println("fin3 ${Thread.currentThread().name} : " + this.coroutineContext) }
        }

        Thread.sleep(1000); println("fin8  ${Thread.currentThread().name} ")
        Thread.sleep(1000); println("fin9  ${Thread.currentThread().name} ")
        Thread.sleep(1000); println("fin10  ${Thread.currentThread().name} ")
    }

    private suspend fun sleepOnIO() = withContext(IO) {
        Thread.sleep(1000); println("fin4-0 ${Thread.currentThread().name} : " + this.coroutineContext)

        launch(IO) { Thread.sleep(1000); println("fin4-0 ${Thread.currentThread().name} : " + this.coroutineContext) }
        launch { Thread.sleep(1000); println("fin5-0 ${Thread.currentThread().name} : " + this.coroutineContext) }
        launch { Thread.sleep(1000); println("fin6-0 ${Thread.currentThread().name} : " + this.coroutineContext) }
        launch { Thread.sleep(1000); println("fin7-0 ${Thread.currentThread().name} : " + this.coroutineContext) }
    }

    @Test
    fun contextTest() = runBlocking {
        println(this.coroutineContext)
        withContext(IO) {
            println(this.coroutineContext)
        }
        withContext(Default) {
            println(this.coroutineContext)
        }
    }

    @Test
    fun unconfinedTest() {
        runBlocking {
            launch(Unconfined) {
                suspended()
            }
        }
    }

    @Test
    fun ioTest() {
        runBlocking {
            launch(IO) {
                suspended()
            }
        }
    }

    suspend fun suspended() {
        for (i in 0..4) {
            println("Before $i ${Thread.currentThread().name}")
            delay(2000)
            println("After $i ${Thread.currentThread().name}")
        }
    }

    fun sleepNoException(time: Long) = kotlin.runCatching { Thread.sleep(time) }.getOrNull()

    @Throws(InterruptedException::class)
    fun sleepWithThrowsAnnotation(time: Long) = kotlin.runCatching {
        Thread.sleep(time)
    }.getOrNull()
}