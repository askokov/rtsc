package com.askokov.rtsc.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatHandler {
    private Date current;
    private boolean flush;
    private List<PInfo> apps = new ArrayList<PInfo>();

    public StatHandler(final List<PInfo> apps) {
        this.apps = apps;
        setCurrent(new Date());

        for(PInfo info : apps) {
            info.setDate(current);
        }
    }

    public List<PInfo> getApps() {
        return apps;
    }

    public boolean setCurrent(final Date date) {
        boolean result = false;
        Date tmp = Func.truncateDate(date);

        if (current == null) {
            current = tmp;
        } else {
            if (!current.equals(tmp)) {
                current = tmp;
                result = true;
            }
        }

        return result;
    }

    public Date getCurrent() {
        return current;
    }

    public boolean isFlush() {
        return flush;
    }

    public void setFlush(final boolean flush) {
        this.flush = flush;
    }
}
