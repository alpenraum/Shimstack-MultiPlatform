package com.alpenraum.shimstack.base

import com.alpenraum.shimstack.base.network.ApiResolver
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.zip

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}

fun <T1, T2, T3, T4, R> zip(
    first: Flow<T1>,
    second: Flow<T2>,
    third: Flow<T3>,
    fourth: Flow<T4>,
    transform: suspend (T1, T2, T3, T4) -> R
): Flow<R> =
    first
        .zip(second) { a, b -> a to b }
        .zip(third) { (a, b), c ->
            Triple(a, b, c)
        }.zip(fourth) { (a, b, c), d ->
            transform(a, b, c, d)
        }

suspend inline fun <reified T> HttpResponse.resolve(apiResolver: ApiResolver): T = with(apiResolver) { this@resolve.handle<T>() }