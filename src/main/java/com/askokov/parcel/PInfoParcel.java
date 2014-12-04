package com.askokov.parcel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.askokov.common.PInfo;

public class PInfoParcel implements Serializable {
    private List<PInfo> list = new ArrayList<PInfo>();

    public PInfoParcel(final List<PInfo> infos) {
        this.list = infos;
    }

    public List<PInfo> getList() {
        return list;
    }
}
