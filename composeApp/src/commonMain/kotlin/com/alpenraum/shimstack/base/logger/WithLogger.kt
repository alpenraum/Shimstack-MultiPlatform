package com.alpenraum.shimstack.base.logger

abstract class WithLogger(
    protected val logger: ShimstackLogger
) {
    init {
        logger.setTag(this::class.simpleName)
    }
}