package codes.laurence.warden.atts

import kotlin.math.absoluteValue
import kotlin.random.Random

private val random = Random.Default

fun <T> List<T>.random(): T {
    return get(random.nextInt((this.size)))
}

fun <T> Array<T>.random(): T {
    return get(random.nextInt((this.size)))
}

fun <T> MutableList<T>.randomPop(): T {
    return removeAt(random.nextInt(this.size))
}

fun randRange(min: Double, max: Double): Double {
    return min + (max - min) * random.nextDouble()
}

fun randRange(min: Int, max: Int): Int {
    return random.nextInt(max - min) + min
}

fun randRange(min: Long, max: Long): Long {
    return (min + (max - min) * random.nextDouble()).toLong()
}

fun randString(): String {
    return "GENERATED${random.nextInt().absoluteValue}"
}

fun randLong(): Long {
    return random.nextLong()
}

fun randDouble(): Double {
    return randRange(0.0, 100.0)
}

fun randInt(): Int {
    return randRange(0, 100)
}

fun randBool(): Boolean {
    return listOf(true, false).random()
}

inline fun <reified E : Enum<E>> randEnum(): E {
    return enumValues<E>().random()
}
