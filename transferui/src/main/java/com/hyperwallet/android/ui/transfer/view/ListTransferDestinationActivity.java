/*
 * Copyright 2019 Hyperwallet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package com.hyperwallet.android.ui.transfer.view;

import static com.hyperwallet.android.ui.common.intent.HyperwalletIntent.ADD_TRANSFER_METHOD_REQUEST_CODE;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.common.intent.HyperwalletIntent;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.common.view.OneClickListener;
import com.hyperwallet.android.ui.common.view.error.DefaultErrorDialogFragment;
import com.hyperwallet.android.ui.common.view.error.OnNetworkErrorCallback;
import com.hyperwallet.android.ui.transfer.R;
import com.hyperwallet.android.ui.transfer.viewmodel.ListTransferDestinationViewModel;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;

import java.util.List;

public class ListTransferDestinationActivity extends AppCompatActivity implements OnNetworkErrorCallback {

    public static final String EXTRA_SELECTED_DESTINATION_TOKEN = "SELECTED_DESTINATION_TOKEN";

    private ListTransferDestinationViewModel mListTransferDestinationViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_transfer_destination);

        String transferToken = getIntent().getStringExtra(EXTRA_SELECTED_DESTINATION_TOKEN);
        if (TextUtils.isEmpty(transferToken)) {
            throw new IllegalArgumentException(
                    "EXTRA_SELECTED_DESTINATION_TOKEN intent data is needed to start this activity");
        }

        mListTransferDestinationViewModel = ViewModelProviders.of(this,
                new ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory(
                        TransferMethodRepositoryFactory.getInstance().getTransferMethodRepository()))
                .get(ListTransferDestinationViewModel.class);

        if (savedInstanceState == null) {
            initFragment(ListTransferDestinationFragment.newInstance(transferToken));
        }

        FloatingActionButton button = findViewById(R.id.create_transfer_method_fab);
        final Intent intent = new Intent();
        intent.setAction(HyperwalletIntent.ACTION_SELECT_TRANSFER_METHOD);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        if (intent.resolveActivity(getPackageManager()) != null) {
            button.setOnClickListener(new OneClickListener() {
                @Override
                public void onOneClick(View v) {
                    startActivityForResult(intent, ADD_TRANSFER_METHOD_REQUEST_CODE);
                }
            });
        } else {
            button.hide();
        }

        registerObservers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_TRANSFER_METHOD_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mListTransferDestinationViewModel.loadNewlyAddedTransferDestination();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        getWindow().getDecorView().setSystemUiVisibility(0);
        super.onBackPressed();
    }

    @Override
    public void retry() {
        mListTransferDestinationViewModel.loadTransferDestinationList();
    }

    private void initFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.list_destination_fragment, fragment);
        transaction.commit();
    }

    private void registerObservers() {
        mListTransferDestinationViewModel.getSelectedTransferDestination().observe(this,
                new Observer<Event<HyperwalletTransferMethod>>() {
                    @Override
                    public void onChanged(Event<HyperwalletTransferMethod> destination) {
                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_SELECTED_DESTINATION_TOKEN, destination.getContent());
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                });

        mListTransferDestinationViewModel.getTransferDestinationError().observe(this,
                new Observer<Event<HyperwalletErrors>>() {
                    @Override
                    public void onChanged(Event<HyperwalletErrors> errorsEvent) {
                        if (!errorsEvent.isContentConsumed()) {
                            showError(errorsEvent.getContent().getErrors());
                        }
                    }
                });
    }

    private void showError(@NonNull List<HyperwalletError> errors) {
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
}