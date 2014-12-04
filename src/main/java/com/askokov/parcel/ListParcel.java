package com.askokov.parcel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListParcel implements Serializable {
    private List<String> list = new ArrayList<String>();

    public ListParcel(final List<String> list) {
        this.list = list;
    }

    public List<String> getList() {
        return list;
    }
}
