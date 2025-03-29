package com.alpenraum.shimstack.base.logger

import co.touchlab.kermit.BaseLogger
import co.touchlab.kermit.LoggerConfig
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import co.touchlab.kermit.platformLogWriter
import com.alpenraum.shimstack.base.BuildInfo
import org.koin.core.annotation.Factory
import kotlin.jvm.JvmOverloads

// TODO: create interface and second "prod"-logger that handles uploading to e.g crashlytics
@Factory
class ShimstackLogger(
    config: LoggerConfig =
        loggerConfigInit(
            platformLogWriter(),
            minSeverity = loggerMinSeverity()
        ),
    private var tag: String? = null
) : BaseLogger(config) {
    private fun generateTag() = "___"

    @JvmOverloads
    fun v(
        throwable: Throwable? = null,
        tag: String = generateTag(),
        message: () -> String
    ) {
        logBlock(Severity.Verbose, tag, throwable, message)
    }

    @JvmOverloads
    fun d(
        throwable: Throwable? = null,
        tag: String = generateTag(),
        message: () -> String
    ) {
        logBlock(Severity.Debug, tag, throwable, message)
    }

    @JvmOverloads
    fun i(
        throwable: Throwable? = null,
        tag: String = generateTag(),
        message: () -> String
    ) {
        logBlock(Severity.Info, tag, throwable, message)
    }

    @JvmOverloads
    fun w(
        throwable: Throwable? = null,
        tag: String = generateTag(),
        message: () -> String
    ) {
        logBlock(Severity.Warn, tag, throwable, message)
    }

    @JvmOverloads
    fun e(
        throwable: Throwable? = null,
        tag: String = generateTag(),
        message: () -> String
    ) {
        logBlock(Severity.Error, tag, throwable, message)
    }

    @JvmOverloads
    fun a(
        throwable: Throwable? = null,
        tag: String = generateTag(),
        message: () -> String
    ) {
        logBlock(Severity.Assert, tag, throwable, message)
    }

    @JvmOverloads
    fun v(
        messageString: String,
        throwable: Throwable? = null,
        tag: String = generateTag()
    ) {
        log(Severity.Verbose, tag, throwable, messageString)
    }

    @JvmOverloads
    fun d(
        messageString: String,
        throwable: Throwable? = null,
        tag: String = generateTag()
    ) {
        log(Severity.Debug, tag, throwable, messageString)
    }

    @JvmOverloads
    fun i(
        messageString: String,
        throwable: Throwable? = null,
        tag: String = generateTag()
    ) {
        log(Severity.Info, tag, throwable, Exception().stackTraceToString())
    }

    @JvmOverloads
    fun w(
        messageString: String,
        throwable: Throwable? = null,
        tag: String = generateTag()
    ) {
        log(Severity.Warn, tag, throwable, messageString)
    }

    @JvmOverloads
    fun e(
        messageString: String,
        throwable: Throwable? = null,
        tag: String = generateTag()
    ) {
        log(Severity.Error, tag, throwable, messageString)
    }

    @JvmOverloads
    fun a(
        messageString: String,
        throwable: Throwable? = null,
        tag: String = generateTag()
    ) {
        log(Severity.Assert, tag, throwable, messageString)
    }

    private fun createStackElementTag(element: String): String? {
        var tag = "" // element.className.substringAfterLast('.')
        tag = tag.replace(ANONYMOUS_CLASS, "")
        return tag
    }

    fun setTag(simpleName: String?) {
        tag = simpleName
    }

    companion object {
        private val ANONYMOUS_CLASS = "(\\$\\d+)+$".toRegex()

        private fun loggerMinSeverity() = if (BuildInfo.isDebug()) Severity.Verbose else Severity.Error
    }
}