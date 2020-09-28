//--------------------------------------------------
// Class AccessTest
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

object AccessTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val securityManager = SecurityManager()


        System.setSecurityManager(securityManager)
    }
}