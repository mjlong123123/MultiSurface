package com.dragon.multisurface.background

import android.os.Handler
import android.os.HandlerThread
import android.os.Message

/**
 * @author dragon
 */

class HandlerThreadScope : BackgroundScope {
    val handlerThread = HandlerThread("HandlerThreadScope")
    override val backgroundHandler: Handler
    override var available = true

    init {
        handlerThread.start()
        backgroundHandler = object : Handler(handlerThread.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    BackgroundScope.QUIT_EVENT -> {
                        handlerThread.quitSafely()
                    }
                    BackgroundScope.CALL_EVENT -> {
                        (msg.obj as? () -> Unit)?.invoke()
                    }
                }
            }
        }
    }
}
