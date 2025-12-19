package com.example.ridertipstracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application class for RiderTipsTracker.
 * Required for Hilt dependency injection.
 */
@HiltAndroidApp
class RiderTipsTrackerApplication : Application()

