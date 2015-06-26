package com.pratamalabs.furqan.models;

import java.util.Date;

/**
 * Created by pratamalabs on 13/6/13.
 */
public class Translation implements Comparable<Translation> {
    private final int id;
    private final String translator;
    private final String language;
    private final Date lastModifiedDate;
    private final String tanzilId;
    protected int order;

    public Translation(int order, String tanzilId, Date lastModifiedDate, String language, String translator, int id) {
        this.order = order;
        this.tanzilId = tanzilId;
        this.lastModifiedDate = lastModifiedDate;
        this.language = language;
        this.translator = translator;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTranslator() {
        return translator;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Translation)) return false;

        Translation that = (Translation) o;

        if (id != that.id) return false;
        if (!language.equals(that.language)) return false;
        if (!lastModifiedDate.equals(that.lastModifiedDate)) return false;
        if (!translator.equals(that.translator)) return false;

        return true;
    }

    public String getTanzilId() {
        return tanzilId;
    }

    @Override
    public int hashCode() {
        int result = id;
        return result;
    }

    @Override
    public int compareTo(Translation translation) {
        int a = this.getOrder();
        int b = translation.getOrder();
        return a > b ? +1 : a < b ? -1 : 0;
    }
}
