package com.dragon.multisurface.background

import android.os.Handler


/**
 * @author dragon
 */
interface BackgroundScope {
    val backgroundHandler: Handler
    var available : Boolean

    companion object {
        const val CALL_EVENT = 1
        const val QUIT_EVENT = CALL_EVENT + 1
    }
}