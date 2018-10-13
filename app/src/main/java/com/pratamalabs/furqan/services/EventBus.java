package com.pratamalabs.furqan.services;

import com.pratamalabs.furqan.FurqanApp;
import com.squareup.otto.Bus;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

/**
 * Created by KatulSomin on 10/08/2014.
 */
@EBean(scope = EBean.Scope.Singleton)
public class EventBus extends Bus {


    public static EventBus get() {
        return EventBus_.getInstance_(FurqanApp.instance);
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void post(Object event) {
        super.post(event);
    }
}
