package com.tycho.mss.form;

public class AndGroup extends FormGroup {

    @Override
    public boolean isValid() {
        for (FormGroup item : items) {
            if (!item.isValid()) {
                return false;
            }
        }
        return true;
    }
}
