package com.alpenraum.shimstack.base

import androidx.core.bundle.Bundle
import com.alpenraum.shimstack.base.di.KoinViewModel
import com.alpenraum.shimstack.base.logger.ShimstackLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

abstract class BaseViewModel(
    protected val dispatchersProvider: DispatchersProvider,
    protected val logger: ShimstackLogger
) : KoinViewModel() {
    init {
        logger.setTag(this::class.simpleName)
    }

    var arguments: Bundle? = null

    protected val iOScope: CoroutineScope by lazy {
        CoroutineScope(dispatchersProvider.io + SupervisorJob(null))
    }
    protected val viewModelScope: CoroutineScope by lazy {
        CoroutineScope(dispatchersProvider.main + SupervisorJob(null))
    }

    override fun onCleared() {
        super.onCleared()
        iOScope.cancel()
    }

    open fun onStart() {}

    open fun onStop() {}

    protected fun <T> Flow<T>.inViewModelScope(initialValue: T) =
        this.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = initialValue)
}