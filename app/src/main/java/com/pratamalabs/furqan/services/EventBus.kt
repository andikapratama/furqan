package com.pratamalabs.furqan.services

import com.squareup.otto.Bus
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async

/**
 * Created by KatulSomin on 10/08/2014.
 */
object EventBus : Bus() {

    override fun post(event: Any) {
        async(UI) {
            super.post(event)
        }
    }
}
