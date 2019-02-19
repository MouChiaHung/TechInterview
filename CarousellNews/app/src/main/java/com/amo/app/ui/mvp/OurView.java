package com.amo.app.ui.mvp;

public interface OurView extends OurPresenter.MVPCallback {
    void feed(OurModel model, OurPresenter presenter);
}
