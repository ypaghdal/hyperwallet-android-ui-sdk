package com.hyperwallet.android.transaction_history.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.common.repository.*;
import com.hyperwallet.android.common.view.error.DefaultErrorDialogFragment;
import com.hyperwallet.android.common.view.error.OnNetworkErrorCallback;
import com.hyperwallet.android.hyperwallet_transactionhistory.R;
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.transaction_history.viewmodel.Event;
import com.hyperwallet.android.transaction_history.viewmodel.ListDetailNavigator;
import com.hyperwallet.android.transaction_history.viewmodel.ListReceiptViewModel;

import java.util.List;


public class ListTransactionHistoryActivity extends AppCompatActivity implements
        ListDetailNavigator<Event<HyperwalletTransferMethod>>,
        OnNetworkErrorCallback {

    public static final String EXTRA_RECEIPT_SOURCE_TOKEN = "EXTRA_RECEIPT_SOURCE_TOKEN";

    private ListReceiptViewModel mListReceiptViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_transaction_history);
        ReceiptRepositoryFactory repositoryFactory = ReceiptRepositoryFactory.getInstance();

        String receiptSourceToken = getIntent().getStringExtra(EXTRA_RECEIPT_SOURCE_TOKEN);

        /*
            the idea is to provide the token as key of the view model map so we don't need to set the token in the
            view model using setters, we keep it as part of the factory constructor
         */
        mListReceiptViewModel = ViewModelProviders.of(this,
                new ListReceiptViewModel.ListReceiptViewModelFactory(receiptSourceToken,
                        repositoryFactory.getReceiptRepository(receiptSourceToken)))
                .get(receiptSourceToken, ListReceiptViewModel.class);

        if (savedInstanceState == null) {
            initFragment(ListTransactionHistoryFragment.newInstance(receiptSourceToken));
        }

        mListReceiptViewModel.getDetailNavigation().observe(this, new Observer<Event<HyperwalletTransferMethod>>() {
            @Override
            public void onChanged(Event<HyperwalletTransferMethod> hyperwalletTransferMethodEvent) {
                navigate(hyperwalletTransferMethodEvent);
            }
        });
        mListReceiptViewModel.getErrors().observe(this, new Observer<Event<List<HyperwalletError>>>() {
            @Override
            public void onChanged(Event<List<HyperwalletError>> listEvent) {
                if (!listEvent.hasBeenHandled()) {
                    showErrorsLoadTransactionHistory(listEvent.getContentIfNotHandled());
                }

            }
        });
    }


    private void initFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.list_transfer_method_fragment, fragment);
        fragmentTransaction.commit();
    }



    private void showErrorsLoadTransactionHistory(@NonNull List<HyperwalletError> errors) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DefaultErrorDialogFragment fragment = (DefaultErrorDialogFragment)
                fragmentManager.findFragmentByTag(DefaultErrorDialogFragment.TAG);

        if (fragment == null) {
            fragment = DefaultErrorDialogFragment.newInstance(errors);
        }

        if (!fragment.isAdded()) {
            fragment.show(fragmentManager);
        }
    }

    @Override
    public void retry() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ListTransactionHistoryFragment fragment = (ListTransactionHistoryFragment)
                fragmentManager.findFragmentById(R.id.list_transfer_method_fragment);

        if (fragment == null) {
            fragment = ListTransactionHistoryFragment.newInstance(mListReceiptViewModel.getToken());
        }
        fragment.retry();
    }


    @Override
    public void navigate(Event<HyperwalletTransferMethod> event) {
        if (!event.hasBeenHandled()) {
            Intent it = new Intent(this, ReceiptDetailActivity.class);
            it.putExtra(ReceiptDetailActivity.EXTRA_RECEIPT, event.peekContent());
            startActivity(it);
        }
    }

}
