package com.example.shorst_begone

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.time.LocalTime
import java.util.Calendar
import android.os.Build

class ShortsDisablerService : AccessibilityService() {

    // --- VARIABLES ---
    private var lastBackTime: Long = 0
    private val cooldown = 400L

    // Sleep Timer
    private var isTimerRunning = false
    private val timerHandler = Handler(Looper.getMainLooper())
    private var audioManager: AudioManager? = null

    // Config: 15 Minutes inactivity
    private val INACTIVITY_TIMEOUT_MS =10 * 1000L //15 * 60 * 1000L
    private val START_HOUR = 22 // 10 PM

    // DEBUG: Track when the timer started
    private var timerStartTime: Long = 0

    // DEBUG: A runnable that prints time every second
    private val logTicker = object : Runnable {
        override fun run() {
            val elapsedMillis = System.currentTimeMillis() - timerStartTime
            val elapsedSeconds = elapsedMillis / 1000
            val remainingSeconds = (INACTIVITY_TIMEOUT_MS - elapsedMillis) / 1000

            Log.d("SleepTimerDebug", "Elapsed: ${elapsedSeconds}s | Remaining: ${remainingSeconds}s")

            // Run this again in 1 second (1000ms)
            timerHandler.postDelayed(this, 1000)
        }
    }

    // Track if we are currently watching a normal video
    private var isWatchingNormalVideo = false

    // --- LIFECYCLE ---

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (Intent.ACTION_SCREEN_OFF == intent?.action) {
                stopSleepTimer()
            }
        }
    }

    private val sleepRunnable = Runnable {
        if (isAfterBedtime()) {
            performSleepSequence()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenStateReceiver, filter)

        Log.d("ShortsDisabler", "Service Ready")
    }

    // --- MAIN LOGIC ---

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // We only care about content changes or interactions in YouTube
        if (event.packageName != "com.google.android.youtube") return

        val rootNode = rootInActiveWindow ?: return

        // 1. CHECK FOR SHORTS (Priority 1)
        if (isShortsView(rootNode)) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackTime > cooldown) {
                performGlobalAction(GLOBAL_ACTION_BACK)
                Log.d("ShortsDisabler", "Shorts detected -> GOING BACK")
                lastBackTime = currentTime

                // If we are kicked out of a Short, we are likely not watching a normal video anymore
                stopSleepTimer()
            }
            return
        }

        // 2. CHECK FOR NORMAL VIDEO (Priority 2)
        // We check if the unique "Player View" exists in the current window
        if (isNormalVideoView(rootNode)) {
            isWatchingNormalVideo = true

            // If the user touched something (Pause, Scroll comments), reset the timer.
            // If they are just watching, the timer keeps ticking.
            if (isUserInteraction(event)) {
                resetSleepTimer()
            } else {
                // If the timer isn't running yet (e.g. just opened video), start it.
                ensureTimerRunning()
            }
        } else {
            // 3. BROWSING / HOME / SETTINGS
            // If we can't find the player, we assume they are browsing.
            isWatchingNormalVideo = false
            stopSleepTimer()
        }
    }

    // Handle Volume Buttons (Only if watching normal video)
    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (isWatchingNormalVideo && event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP,
                KeyEvent.KEYCODE_VOLUME_DOWN,
                KeyEvent.KEYCODE_VOLUME_MUTE -> {
                    resetSleepTimer()
                    return false
                }
            }
        }
        return super.onKeyEvent(event)
    }

    // --- VIEW DETECTION ---

    private fun isShortsView(rootNode: AccessibilityNodeInfo): Boolean {
        // "reel_recycler" is the specific ID for the Shorts infinite scroll container
        return rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_recycler").isNotEmpty()
    }

    private fun isNormalVideoView(rootNode: AccessibilityNodeInfo): Boolean {
        // We look for specific IDs that only appear in the standard video player.
        // "watch_player" is the main container. "player_view" is another common one.
        // "player_control_view" contains the pause buttons/seek bar.

        val playerNodes = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/player_view")
        val watchNodes = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/watch_player")

        return playerNodes.isNotEmpty() || watchNodes.isNotEmpty()
    }

    private fun isUserInteraction(event: AccessibilityEvent): Boolean {
        // STRICTER FILTER: We removed TYPE_VIEW_TEXT_CHANGED
        // YouTube updates the time text ("1:02" -> "1:03") constantly.
        // We must NOT reset the timer for that. Only reset on actual clicks/scrolls.
        return event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED ||
                event.eventType == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED ||
                event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED
    }

    // --- TIMER HELPERS ---

    private fun ensureTimerRunning() {
        if (!isTimerRunning && isAfterBedtime()) {
            resetSleepTimer()
        }
    }

    private fun resetSleepTimer() {
        // 1. Clear existing timers and logs
        timerHandler.removeCallbacks(sleepRunnable)
        timerHandler.removeCallbacks(logTicker)

        if (isAfterBedtime()) {
            // 2. Start the Main Sleep Timer
            timerHandler.postDelayed(sleepRunnable, INACTIVITY_TIMEOUT_MS)

            // 3. Start the Debug Logger
            timerStartTime = System.currentTimeMillis()
            timerHandler.post(logTicker)

            Log.d("SleepTimer", "Timer STARTED. Waiting ${INACTIVITY_TIMEOUT_MS / 1000}s")
        }
    }

    private fun stopSleepTimer() {
        timerHandler.removeCallbacks(sleepRunnable)
        timerHandler.removeCallbacks(logTicker) // Stop the logs too!
        Log.d("SleepTimer", "Timer CANCELLED (Not watching video or Screen Off)")
    }

    private fun isAfterBedtime(): Boolean {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return currentHour >= START_HOUR || currentHour < 5
    }

    private fun performSleepSequence() {
        Log.d("SleepTimer", "Bedtime triggered. Goodnight.")

        // 1. Pause Media (Works on all versions)
        val eventTime = System.currentTimeMillis()
        val downEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0)
        val upEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0)

        audioManager?.dispatchMediaKeyEvent(downEvent)
        audioManager?.dispatchMediaKeyEvent(upEvent)

        // 2. Lock Screen (Only works on Android 9+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        } else {
            // Optional: On older phones, you could go to the Home screen instead
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenStateReceiver)
        timerHandler.removeCallbacks(sleepRunnable)
    }

    override fun onInterrupt() {}
}