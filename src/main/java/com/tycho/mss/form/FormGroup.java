package com.tycho.mss.form;

import java.util.ArrayList;
import java.util.List;

public abstract class FormGroup {

    protected final List<FormGroup> items = new ArrayList<>();

    public void add(final FormGroup item) {
        if (!items.contains(item)) items.add(item);
    }

    public void remove(final FormGroup item) {
        items.remove(item);
    }

    public abstract boolean isValid();
}
