package com.amo.app.adapter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

public abstract class BaseModelAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static int SPECIFIC_POSITION_BASE = -10;
    public final static int ADD_POSITION_AT_FIRST       = SPECIFIC_POSITION_BASE-1;
    public final static int ADD_POSITION_AT_LAST        = SPECIFIC_POSITION_BASE-2;
    public final static int REMOVE_POSITION_AT_FIRST    = SPECIFIC_POSITION_BASE-3;
    public final static int REMOVE_POSITION_AT_LAST     = SPECIFIC_POSITION_BASE-4;
    public final static int POSITION_AT_FIRST = 0;

    private final static int RESULT_BASE    = 20;
    public final static int RESULT_SUCCESS = RESULT_BASE + 1;
    public final static int RESULT_FAILURE = RESULT_BASE + 2;

    private Context mContext;
    private List<T> mModels = new LinkedList<>();
    private int mFocusedPosition = -1;

    private static boolean is_debug = false;

    public BaseModelAdapter(Context context) {
        this.mContext = context;
    }

    /**
     * getter
     */
    public int getFocusedPosition() {
        return mFocusedPosition;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * setter
     */
    public void setFocusedPosition(int position) {
        mFocusedPosition = position;
    }

    /**
     * models access
     */
    private void addModel(int position, T model) {
        if (mModels == null) return;
        mModels.add(position, model);
    }

    private void addModels(List<T> models) {
        if (mModels == null) return;
        mModels.addAll(models);
    }

    private void setModels(List<T> models) {
        if (mModels == null) return;
        clearModels();
        mModels.addAll(models);
    }

    /**
     * removes the model at the specified position in the list of model
     * and returns the model which is previously at the specified position
     */
    private T removeModel(int position) {
        if (mModels == null) return null;
        return mModels.remove(position);
    }

    /**
     * removes models at the specified range in the list of model
     * and returns the count of the un-null removed
     *
     * with linked list, this shifts subsequent elements to the left (subtracts one from their indices) if success of removing one
     */
    private int removeModel(int start, int end) {
        int count_of_un_null_removed = 0;
        int count_of_remove = end-start+1;
        if (mModels == null) return 0;
        for (int i=1; i<=count_of_remove; i++) {
            Logger.d("... i:" + i);
            Logger.d("... count_of_un_null_removed:" + count_of_un_null_removed);
            if (mModels.remove(start) != null) {
                count_of_un_null_removed++;
            } else {
                Logger.e("!!! null model at index " + i);
            }
        }
        return count_of_un_null_removed;
    }

    private void clearModels() {
        if (mModels == null) return;
        mModels.clear();
    }

    private T getModel(int position) {
        if (position > mModels.size()-1) return null;
        return mModels.get(position);
    }

    private List<T> getModels() {
        if (mModels == null) return null;
        return mModels;
    }

    private int getCountOfModels() {
        if (mModels == null) return 0;
        return mModels.size();
    }


    /**
     * returns a model stored in data set at specified position
     */
    public T getItemOfDataSet(int position) {
        return getModel(position);
    }

    /**
     * returns all models stored in data set
     */
    public List<T> getItemsOfDataSet() {
        return getModels();
    }

    public int getItemCount() {
        return getCountOfModels();
    }

    /**
     * sets (clear and add) the data set of adapter and notifies the RecyclerView of changes
     *
     * this method clears data previously stored even if failed to set data
     */
    public void setItemsAsDataSet(List<T> data) {
        if (getModels() != null) {
            int past_data_len = getCountOfModels();
            Logger.d("... before count of items:" + getCountOfModels());
            Logger.d("... going to clear and add all items");
            setModels(data);
            if (data.size() == getCountOfModels()) {
                Logger.d("... succeeded to set items");
                notifyItemRangeRemoved(0, past_data_len);
                notifyItemRangeInserted(0, getCountOfModels());
            } else {
                Logger.e("!!! failed to set items");
                clearModels();
                notifyItemRangeRemoved(0, past_data_len);
            }
            Logger.d("... after count of items:" + getCountOfModels());
        } else {
            Logger.e("!!! data is null");
        }
    }

    /**
     * appends models to end of the data set of adapter and notifies the RecyclerView of changes if success
     */
    public void addItemsToDataSet(List<T> data) {
        if (getModels() != null) {
            //represent the first element that got added
            int past_data_len = getCountOfModels();
            Logger.d("... before count of items:" + past_data_len);
            Logger.d("... going to add all items");
            addModels(data);
            //makes changes to the data set directly and notifies the adapter of changes
            notifyItemRangeInserted(past_data_len, data.size());
            Logger.d("... after count of items:" + getCountOfModels());
        } else {
            Logger.e("!!! data is null");
        }
    }

    /**
     * inserts a model at specified position in the data set of adapter and notifies the RecyclerView of changes if success
     *
     * at tail if position is BaseModelAdapter#ADD_POSITION_AT_LAST
     */
    public void addItemToDataSet(int position, T data) {
        if (getModels() != null) {
            int past_data_len = getCountOfModels();
            Logger.d("... before count of item:" + past_data_len);
            if (position == ADD_POSITION_AT_FIRST) {
                Logger.d("... going to add item at index " + past_data_len);
                addModel(0, data);
                notifyItemRangeInserted(0, 1);
            }
            else if (position == ADD_POSITION_AT_LAST) {
                Logger.d("... going to add item at index " + past_data_len);
                addModel(past_data_len, data);
                notifyItemRangeInserted(past_data_len, 1);
            } else {
                Logger.d("... going to add item at index " + String.valueOf(position));
                addModel(position, data);
                notifyItemRangeInserted(position, 1);
                //notifyItemRangeInserted(position, getCountOfModels()-position); //out of index error while position is 0
            }
            Logger.d("... after count of item:" + getCountOfModels());
        } else {
            Logger.e("!!! data is null");
        }

    }

    /**
     * removes a model at specified position in the data set of adapter and notifies the RecyclerView of changes if success
     *
     * at head if position is BaseModelAdapter#REMOVE_POSITION_AT_FIRST
     * at tail if position is BaseModelAdapter#REMOVE_POSITION_AT_LAST
     */
    public T removeItemFromDataSet(int position) {
        T item_removed = null;
        if (getCountOfModels() == 0) {
            Logger.d("... count of item is 0, no need to remove items");
            return null;
        }
        if (getModels() != null) {
            int index_of_remove;
            Logger.d("... before count of item:" + getCountOfModels());
            if (position == REMOVE_POSITION_AT_LAST) {
                index_of_remove = getCountOfModels()-1;
                Logger.d("... going to remove item at index " + String.valueOf(index_of_remove));
                item_removed = removeModel(index_of_remove);
                notifyItemRangeRemoved(index_of_remove, 1);
            } else {
                index_of_remove = position;
                Logger.d("... going to remove item at index " + String.valueOf(index_of_remove));
                item_removed = removeModel(index_of_remove);
                notifyItemRangeRemoved(index_of_remove, 1);
                //notifyItemRangeRemoved(index_of_remove, getCountOfModels()-index_of_remove); //out of index error while position is 0
            }
            Logger.d("... after count of item:" + getCountOfModels());
        } else {
            Logger.e("!!! data is null");
        }
        return item_removed;
    }

    /**
     * removes models from the data set in specified range of adapter and notifies to update the RecyclerView no matter if success to remove
     *
     * for all if start is BaseModelAdapter#REMOVE_POSITION_AT_FIRST and end is BaseModelAdapter#REMOVE_POSITION_AT_LAST
     * 
     * returns the count of the un-null removed
     */
    public int removeItemsFromDataSet(int start, int end) {
        int count_of_un_null_removed = 0;
        if (getCountOfModels() == 0) {
            Logger.d("... count of item is 0, no need to remove items");
            return 0;
        }
        if (getModels() != null) {
            int past_data_len = getCountOfModels();
            Logger.d("... before count of item:" + getCountOfModels());
            if (start == REMOVE_POSITION_AT_FIRST && end == REMOVE_POSITION_AT_LAST) {
                Logger.d("... going to remove item from index " + "0" + " to " + String.valueOf(past_data_len-1));
                count_of_un_null_removed = removeModel(0, past_data_len-1);
                notifyItemRangeRemoved(0, past_data_len);
            } else {
                Logger.d("... going to remove item from index " + String.valueOf(start) + " to " + String.valueOf(end));
                count_of_un_null_removed = removeModel(start, end);
                notifyItemRangeRemoved(start, (end-start)+1);
            }
            Logger.d("... after count of item:" + getCountOfModels());
        } else {
            Logger.e("!!! data is null");
        }
        return count_of_un_null_removed;
    }

    public boolean isInternetConnectedOrConnecting() {
        if (mContext == null) return false;
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cm.getActiveNetworkInfo();
        return network != null && network.isConnectedOrConnecting();
    }

    private static class Logger {
        static void d(@NonNull String message, @Nullable Object... args) {
            if (is_debug) com.orhanobut.logger.Logger.d(message, args);
        }

        static void d(@Nullable Object object) {
            if (is_debug) com.orhanobut.logger.Logger.d(object);
        }

        static void e(@NonNull String message, @Nullable Object... args) {
            if (is_debug) com.orhanobut.logger.Logger.e(message, args);
        }
    }
}
