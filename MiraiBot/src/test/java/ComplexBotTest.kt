import com.kenvix.complexbot.ComplexBotDriver
import com.kenvix.utils.tools.CommonTools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Test
import java.io.IOException

//--------------------------------------------------
// Class BackendTest
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

class ComplexBotTest {
    @Test
    fun initTest() {

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
        withContext(IO) { println(this.coroutineContext) }
        withContext(Default) { println(this.coroutineContext) }
    }

    fun sleepNoException(time: Long) = kotlin.runCatching { Thread.sleep(time) }.getOrNull()

    @Throws(InterruptedException::class)
    fun sleepWithThrowsAnnotation(time: Long) = kotlin.runCatching {
        Thread.sleep(time)
    }.getOrNull()
}