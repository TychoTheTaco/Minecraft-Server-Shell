package com.tycho.mss.form;

public class OrGroup extends FormGroup {

    @Override
    public boolean isValid() {
        for (FormGroup item : items) {
            if (item.isValid()) {
                return true;
            }
        }
        return false;
    }
}
