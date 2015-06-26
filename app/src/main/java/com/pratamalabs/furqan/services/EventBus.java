package com.pratamalabs.furqan.services;

import com.squareup.otto.Bus;

import org.androidannotations.annotations.EBean;

/**
 * Created by KatulSomin on 10/08/2014.
 */
@EBean(scope = EBean.Scope.Singleton)
public class EventBus extends Bus {
}
