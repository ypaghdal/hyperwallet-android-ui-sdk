/*
 * The MIT License (MIT)
 * Copyright (c) 2019 Hyperwallet Systems Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.hyperwallet.android.ui.receipt.repository;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.paging.PageList;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.model.receipt.ReceiptQueryParam;
import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.util.DateUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * PrepaidCardReceiptDataSource mediates communication to HW API Platform particularly on
 * Receipts PrepaidCard V3 API
 */
public class PrepaidCardReceiptDataSource extends PageKeyedDataSource<Date, Receipt> {

    private static final int YEAR_BEFORE_NOW = -1;

    private final Calendar mCalendarYearBeforeNow;
    private final String mToken;
    private final MutableLiveData<Event<Errors>> mErrors = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsFetchingData = new MutableLiveData<>();
    private PageKeyedDataSource.LoadCallback<Date, Receipt> mLoadAfterCallback;
    private PageKeyedDataSource.LoadParams<Date> mLoadAfterParams;
    private PageKeyedDataSource.LoadInitialCallback<Date, Receipt> mLoadInitialCallback;
    private PageKeyedDataSource.LoadInitialParams<Date> mLoadInitialParams;

    /**
     * Initialize Prepaid card data source
     *
     * @param token Prepaid card token identifier, please get this data from HW Platform
     */
    PrepaidCardReceiptDataSource(@NonNull final String token) {
        mToken = token;
        mCalendarYearBeforeNow = Calendar.getInstance();
        mCalendarYearBeforeNow.add(Calendar.YEAR, YEAR_BEFORE_NOW);
    }

    /**
     * @see PageKeyedDataSource#loadInitial(LoadInitialParams, LoadInitialCallback)
     */
    @Override
    public void loadInitial(@NonNull final LoadInitialParams<Date> params,
            @NonNull final LoadInitialCallback<Date, Receipt> callback) {
        mLoadInitialCallback = callback;
        mLoadInitialParams = params;
        mIsFetchingData.postValue(Boolean.TRUE);

        ReceiptQueryParam queryParam = new ReceiptQueryParam.Builder()
                .createdAfter(mCalendarYearBeforeNow.getTime())
                .sortByCreatedOnDesc().build();

        EspressoIdlingResource.increment();
        getHyperwallet().listPrepaidCardReceipts(mToken, queryParam,
                new HyperwalletListener<PageList<Receipt>>() {
                    @Override
                    public void onSuccess(@Nullable PageList<Receipt> result) {
                        mErrors.postValue(null);

                        if (result != null) {
                            callback.onResult(sortPrepaidCardReceiptsByDateAndDesc(result),
                                    mCalendarYearBeforeNow.getTime(), null);
                        }

                        // reset
                        mIsFetchingData.postValue(Boolean.FALSE);
                        mLoadInitialCallback = null;
                        mLoadInitialParams = null;
                        EspressoIdlingResource.decrement();
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        mIsFetchingData.postValue(Boolean.FALSE);
                        mErrors.postValue(new Event<>(exception.getErrors()));
                        EspressoIdlingResource.decrement();
                    }

                    @Override
                    public Handler getHandler() {
                        return null;
                    }
                });
    }

    @VisibleForTesting
    List<Receipt> sortPrepaidCardReceiptsByDateAndDesc(@Nullable PageList<Receipt> result) {
        List<Receipt> resultDataList = result != null ? result.getDataList() : new ArrayList<Receipt>();
        Collections.sort(resultDataList, new Comparator<Receipt>() {

            @Override
            public int compare(Receipt firstReceipt, Receipt secondReceipt) {
                Date firstDate = parseDate(firstReceipt.getCreatedOn());
                Date secondDate = parseDate(secondReceipt.getCreatedOn());

                return (int) (secondDate.compareTo(firstDate));
            }
        });

        return resultDataList;
    }

    @VisibleForTesting
    Date parseDate(String receipt) {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(receipt);
        } catch (ParseException pe) {
            throw new IllegalArgumentException("An exception occurred when attempting to parse " +
                    "the date " + receipt, pe);
        }
        return date;
    }

    /**
     * @see PageKeyedDataSource#loadBefore(LoadParams, LoadCallback)
     */
    @Override
    public void loadBefore(@NonNull final LoadParams<Date> params,
            @NonNull final LoadCallback<Date, Receipt> callback) {
    }

    /**
     * @see PageKeyedDataSource#loadAfter(LoadParams, LoadCallback)
     */
    @Override
    public void loadAfter(@NonNull final LoadParams<Date> params, @NonNull final LoadCallback<Date, Receipt> callback) {
        mLoadAfterCallback = callback;
        mLoadAfterParams = params;
        mIsFetchingData.postValue(Boolean.TRUE);

        ReceiptQueryParam queryParam = new ReceiptQueryParam.Builder()
                .createdAfter(params.key)
                .limit(params.requestedLoadSize)
                .sortByCreatedOnDesc().build();

        EspressoIdlingResource.increment();
        getHyperwallet().listPrepaidCardReceipts(mToken, queryParam,
                new HyperwalletListener<PageList<Receipt>>() {
                    @Override
                    public void onSuccess(@Nullable PageList<Receipt> result) {
                        mErrors.postValue(null);

                        if (result != null) {
                            callback.onResult(result.getDataList(), getNextDate(result));
                        }

                        // reset
                        mIsFetchingData.postValue(Boolean.FALSE);
                        mLoadAfterCallback = null;
                        mLoadAfterParams = null;
                        EspressoIdlingResource.decrement();
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        mIsFetchingData.postValue(Boolean.FALSE);
                        mErrors.postValue(new Event<>(exception.getErrors()));
                        EspressoIdlingResource.decrement();
                    }

                    @Override
                    public Handler getHandler() {
                        return null;
                    }
                });
    }

    /**
     * Facilitates retry when network is down; any error that we can have a retry operation
     */
    void retry() {
        if (mLoadInitialCallback != null) {
            loadInitial(mLoadInitialParams, mLoadInitialCallback);
        } else if (mLoadAfterCallback != null) {
            loadAfter(mLoadAfterParams, mLoadAfterCallback);
        }
    }

    /**
     * Retrieve reference of Hyperwallet errors inorder for consumers to observe on data changes
     *
     * @return Live event data of {@link Errors}
     */
    public LiveData<Event<Errors>> getErrors() {
        return mErrors;
    }

    LiveData<Boolean> isFetchingData() {
        return mIsFetchingData;
    }

    Hyperwallet getHyperwallet() {
        return Hyperwallet.getDefault();
    }

    @VisibleForTesting
    Date getNextDate(@NonNull final PageList<Receipt> result) {
        if (result.getDataList() != null && !result.getDataList().isEmpty()) {
            // get last receipt date
            return DateUtil.fromDateTimeString(
                    result.getDataList().get(result.getDataList().size() - 1).getCreatedOn());
        }
        return Calendar.getInstance().getTime();
    }
}
