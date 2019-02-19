package com.amo.app.ui.mvp;

public interface OurPresenter<V extends OurView> {
    void attach(V view); //registers observer of view register
    void detach(V view); //unregisters observer of view
    void load(int mode); //invokes presenter to access model and trigger the callback of view to update UI
    void fetch(int mode); //invokes presenter to access model only
    void remove(int position); //invokes presenter to remove model at specific position and trigger the callback of view to update UI
    void recover(int position); //invokes presenter to recover the model just removed at specific position and trigger the callback of view to update UI
    void cancel(boolean cancel);
    interface MVPCallback { //callback of V called by P
        void onNotice(int result, String message);
        void onFetch(int result, Object pojo);
        void onLoad(int result, Object pojo); //pojo is specified object holding information used to update UI
        void onRemove(int result, int position, Object pojo);
        void onRecover(int result, int position, Object pojo);
    }
}
