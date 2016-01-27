package com.pratamalabs.furqan.services;

import com.squareup.otto.Bus;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

/**
 * Created by KatulSomin on 10/08/2014.
 */
@EBean(scope = EBean.Scope.Singleton)
public class EventBus extends Bus {

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void post(Object event) {
        super.post(event);
    }
}
