package uz.ttpu.coroutines

import kotlinx.coroutines.*
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis

// Question 1
fun q1() = runBlocking {
    launch {
        delay(1000)
        println("Coffee ready")
    }
    println("Order accepted")
}

// Question 2
fun q2WithDelay() = runBlocking {
    launch {
        println("Espresso start")
        delay(300)
        println("Espresso end")
    }
    launch {
        println("Tea start")
        delay(100)
        println("Tea end")
    }
    launch {
        println("Latte start")
        delay(200)
        println("Latte end")
    }
}

fun q2WithSleep() = runBlocking {
    launch {
        println("Espresso start")
        Thread.sleep(300)
        println("Espresso end")
    }
    launch {
        println("Tea start")
        Thread.sleep(100)
        println("Tea end")
    }
    launch {
        println("Latte start")
        Thread.sleep(200)
        println("Latte end")
    }
}

// Question 3
suspend fun loadUser() {
    delay(100)
    println("Step 1: User loaded")
    checkPassword()
}

suspend fun checkPassword() {
    delay(100)
    println("Step 2: Password checked")
    openProfile()
}

suspend fun openProfile() {
    delay(100)
    println("Step 3: Profile opened")
}

fun q3() = runBlocking {
    loadUser()
}

// Question 4
// Normal function calling suspend function fails to compile:
// fun login() { loadUser() }

suspend fun loginFix1() {
    loadUser()
}

fun loginFix2() = runBlocking {
    loadUser()
}

// Question 5
fun currentThreadName(): String = Thread.currentThread().name

fun q5() = runBlocking {
    val t1 = Thread.currentThread().name
    println("Before call thread: $t1")
    val t2 = currentThreadName()
    println("Inside suspend func thread: $t2")
}

// Question 6
fun q6() = runBlocking {
    for (i in 1..10) {
        launch {
            println("download $i started on ${Thread.currentThread().name}")
            delay(100)
            println("download $i finished")
        }
    }
}

// Question 7
fun q7() = runBlocking {
    println("1")
    launch {
        println("2")
        delay(100)
        println("3")
    }
    println("4")
    delay(50)
    println("5")
}

// Question 8
fun q8() = runBlocking {
    val parent = launch {
        launch {
            delay(200)
            println("child done")
        }
        println("parent body done")
    }
    parent.join()
    println("report completed")
}

// Question 9
fun q9() = runBlocking {
    val parent = launch {
        launch {
            try {
                delay(1000)
            } finally {
                println("Child 1 cleaned up")
            }
        }
        launch {
            try {
                delay(1000)
            } finally {
                println("Child 2 cleaned up")
            }
        }
    }
    delay(100)
    parent.cancelAndJoin()
    println("end")
}

// Question 10
fun q10() = runBlocking {
    coroutineScope {
        launch {
            delay(200)
            println("Avatar loaded")
        }
        launch {
            delay(100)
            println("Settings loaded")
        }
    }
    println("Profile ready")

    launch {
        delay(100)
        println("Late task finished")
    }
    println("end of main")
}

// Question 11
fun CoroutineScope.startCounter(name: String, limit: Int): Job = launch {
    for (i in 1..limit) {
        delay(100)
        println("$name: $i")
    }
}

fun q11() = runBlocking {
    coroutineScope {
        startCounter("Counter A", 2)
        startCounter("Counter B", 3)
        println("launched")
    }
}

// Question 12
fun CoroutineScope.greetBackground(name: String) = launch {
    delay(100)
    println("Hello background, $name!")
}

suspend fun greetAllSequential(names: List<String>) = coroutineScope {
    for (n in names) {
        launch {
            delay(100)
            println("Hello sequential, $n!")
        }
    }
}

fun q12() = runBlocking {
    println("Calling background helper...")
    greetBackground("Alice")
    println("Returned immediately from background helper")

    println("Calling suspend helper...")
    greetAllSequential(listOf("Bob", "Charlie"))
    println("Returned after suspend helper finished")
}

// Question 13
fun q13() = runBlocking {
    val r1 = runBlocking { "runBlocking result" }
    println("runBlocking returned: $r1")

    val r2 = launch { delay(10) }
    println("launch returned: $r2")

    val r3 = async { "async result" }
    println("async returned: $r3")

    val r4 = coroutineScope { "scope result" }
    println("coroutineScope returned: $r4")

    r2.join()
    r3.await()
}

// Question 14
fun q14() = runBlocking {
    val job: Job = launch {
        6 * 7
    }
    println("Job cannot return value directly: $job")

    val deferred: Deferred<Int> = async {
        6 * 7
    }
    val res = deferred.await()
    println("Async result: $res")
}

// Question 15
fun q15() = runBlocking {
    println("Start")
    coroutineScope {
        launch {
            delay(300)
            println("Export X")
        }
        launch {
            delay(100)
            println("Export Y")
        }
        launch {
            delay(200)
            println("Export Z")
        }
    }
    println("All done")
}

// Extra 1
fun extra1() = runBlocking {
    val tDelay = measureTimeMillis {
        coroutineScope {
            launch { delay(300) }
            launch { delay(300) }
            launch { delay(300) }
        }
    }
    println("Delay total time: ${tDelay}ms (~${(tDelay / 100.0).roundToInt() * 100}ms)")

    val tSleep = measureTimeMillis {
        coroutineScope {
            launch { Thread.sleep(300) }
            launch { Thread.sleep(300) }
            launch { Thread.sleep(300) }
        }
    }
    println("Sleep total time: ${tSleep}ms (~${(tSleep / 100.0).roundToInt() * 100}ms)")
}

fun main() {
    q1()
    q2WithDelay()
    q2WithSleep()
    q3()
    q5()
    q6()
    q7()
    q8()
    q9()
    q10()
    q11()
    q12()
    q13()
    q14()
    q15()
    extra1()
}
