package com.studio.movierama.enums;

public enum Rating {
    LIKE(true),
    DISLIKE(false);

    private final boolean booleanValue;

    public boolean getBooleanValue() {
        return booleanValue;
    }

    Rating(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }
}
