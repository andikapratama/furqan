package com.pratamalabs.furqan.models;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Created by pratamalabs on 30/7/13.
 */
public class Source implements Comparable<Source>, FilterableItem {
    public final int id;
    public final String type;
    public final String name;
    public final String author;
    public final String downloadLink;
    public final String updateLink;
    public final Date lastModifiedDate;
    public final String language;
    public final String providerName;
    public String status;
    protected int order;

    public Source(int id, String type, String name, String author, String downloadLink, String updateLink, Date lastModifiedDate, String language, String providerName, String status, int order) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.author = author;
        this.downloadLink = downloadLink;
        this.updateLink = updateLink;
        this.lastModifiedDate = lastModifiedDate;
        this.language = language;
        this.providerName = providerName;
        this.status = status;
        this.order = order;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
        if (!(o instanceof Source)) return false;

        Source source = (Source) o;

        return id == source.id;

    }

    @Override
    public int compareTo(Source source) {
        int a = this.getOrder();
        int b = source.getOrder();
        return a > b ? +1 : a < b ? -1 : 0;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean isFound(CharSequence keyword) {
        return StringUtils.containsIgnoreCase(name, keyword) ||
                StringUtils.containsIgnoreCase(language, keyword) ||
                StringUtils.containsIgnoreCase(author, keyword);
    }
}
