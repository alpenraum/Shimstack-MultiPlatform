package com.alpenraum.shimstack.base.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.module.Module

expect fun platformModule(): Module

@org.koin.core.annotation.Module
@ComponentScan("com.alpenraum.shimstack.platform")
class GeneratedPlatformModule