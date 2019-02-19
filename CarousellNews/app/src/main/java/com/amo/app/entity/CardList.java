package com.amo.app.entity;

import java.util.List;

public class CardList {
    public static final long DEFAULT_LAST_DATE = 0;
    private long mLastDate = DEFAULT_LAST_DATE;
    private List<Card> mCards;

    /**
     * getter
     */
    public long getLastDate() {
        return mLastDate;
    }

    public List<Card> getCards() {
        return mCards;
    }

    /**
     * setter
     */
    public void setLastDate(long mLastItemDate) {
        this.mLastDate = mLastItemDate; //shallow copy
    }

    public void setCards(List<Card> mCards) {
        this.mCards = mCards;
    }
}
