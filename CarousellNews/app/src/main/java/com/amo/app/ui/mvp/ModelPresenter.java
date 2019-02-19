package com.amo.app.ui.mvp;

public interface ModelPresenter extends OurPresenter {
    void notifyItemRangeInserted(int positionStart, int itemCount);
    void notifyItemRangeRemoved(int positionStart, int itemCount);
    void notifyChanged();
}
