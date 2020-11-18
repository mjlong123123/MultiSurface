package com.dragon.multisurface.background

import com.dragon.multisurface.background.BackgroundScope.Companion.CALL_EVENT
import com.dragon.multisurface.background.BackgroundScope.Companion.QUIT_EVENT

/**
 * @author dragon
 */
fun BackgroundScope.runInBackground(block: () -> Unit) = if (available) backgroundHandler.sendMessage(backgroundHandler.obtainMessage(CALL_EVENT, block)) else false
fun BackgroundScope.quit() = if (available) {
    available = false
    backgroundHandler.sendMessage(backgroundHandler.obtainMessage(QUIT_EVENT))
} else false