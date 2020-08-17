package com.tycho.mss.property;

public class StringProperty extends Property<String> {

    public StringProperty(String key, String value) {
        super(key, value);
    }

    @Override
    public boolean isValidValue(String value) {
        for (String string : getValidValues()) {
            if (string.equals(value)) return true;
        }
        return false;
    }

    protected String[] getValidValues() {
        return new String[1];
    }
}