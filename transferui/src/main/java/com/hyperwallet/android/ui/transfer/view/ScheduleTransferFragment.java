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
package com.hyperwallet.android.ui.transfer.view;

import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getStringFontIcon;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getStringResourceByName;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getTransferMethodDetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.model.transfer.ForeignExchange;
import com.hyperwallet.android.ui.common.view.OneClickListener;
import com.hyperwallet.android.ui.transfer.R;
import com.hyperwallet.android.ui.transfer.viewmodel.ScheduleTransferViewModel;

import java.util.List;
import java.util.Locale;

/**
 * Schedule Transfer Fragment
 */
public class ScheduleTransferFragment extends Fragment {

    private ScheduleTransferViewModel mScheduleTransferViewModel;
    private View mTransferConfirmButtonProgress;

    public ScheduleTransferFragment() {
    }

    static ScheduleTransferFragment newInstance() {
        return new ScheduleTransferFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScheduleTransferViewModel = ViewModelProviders.of(requireActivity()).get(ScheduleTransferViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule_transfer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        showTransferDestination();
        showForeignExchange();
        showSummary();
        showNotes();
        showConfirmButton();

        registerObserver();
    }

    void retry() {
        mScheduleTransferViewModel.scheduleTransfer();
    }

    private void registerObserver() {
        mScheduleTransferViewModel.isScheduleTransferLoading().observe(getViewLifecycleOwner(),
                new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean loading) {
                        if (loading) {
                            mTransferConfirmButtonProgress.setVisibility(View.VISIBLE);
                        } else {
                            mTransferConfirmButtonProgress.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void showTransferDestination() {
        TextView transferIcon = getView().findViewById(R.id.transfer_destination_icon);
        TextView transferTitle = getView().findViewById(R.id.transfer_destination_title);
        TextView transferCountry = getView().findViewById(R.id.transfer_destination_description_1);
        TextView transferIdentifier = getView().findViewById(R.id.transfer_destination_description_2);

        String type = mScheduleTransferViewModel.getTransferDestination().getField(TYPE);
        String transferMethodIdentification = getTransferMethodDetail(transferIdentifier.getContext(),
                mScheduleTransferViewModel.getTransferDestination(), type);
        Locale locale = new Locale.Builder().setRegion(
                mScheduleTransferViewModel.getTransferDestination().getField(TRANSFER_METHOD_COUNTRY)).build();

        transferIdentifier.setText(transferMethodIdentification);
        transferTitle.setText(getStringResourceByName(transferTitle.getContext(), type));
        transferIcon.setText(getStringFontIcon(transferIcon.getContext(), type));
        transferCountry.setText(locale.getDisplayName());
    }

    private void showForeignExchange() {
        RecyclerView recyclerView = getView().findViewById(R.id.list_foreign_exchange);
        if (mScheduleTransferViewModel.getTransfer().hasForeignExchange()) {
            recyclerView.setNestedScrollingEnabled(false);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(
                    new ForeignExchangeListAdapter(mScheduleTransferViewModel.getTransfer().getForeignExchanges()));
        } else {
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void showSummary() {
        TextView amount = getView().findViewById(R.id.amount_value);
        TextView fee = getView().findViewById(R.id.fee_value);
        TextView receiveAmount = getView().findViewById(R.id.transfer_value);
        View feeContainer = getView().findViewById(R.id.fee_container);
        View receiveAmountContainer = getView().findViewById(R.id.transfer_container);
        View amountHorizontalBar = getView().findViewById(R.id.amount_horizontal_bar);

        if (mScheduleTransferViewModel.getTransfer().hasFee()) {
            feeContainer.setVisibility(View.VISIBLE);
            receiveAmountContainer.setVisibility(View.VISIBLE);
            amountHorizontalBar.setVisibility(View.VISIBLE);
            fee.setText(requireContext().getString(R.string.amount_currency_format,
                    mScheduleTransferViewModel.getTransfer().getDestinationFeeAmount(),
                    mScheduleTransferViewModel.getTransfer().getDestinationCurrency()));
            amount.setText(requireContext().getString(R.string.amount_currency_format,
                    mScheduleTransferViewModel.getTransferTotalAmount(),
                    mScheduleTransferViewModel.getTransfer().getDestinationCurrency()));
            receiveAmount.setText(requireContext().getString(R.string.amount_currency_format,
                    mScheduleTransferViewModel.getTransfer().getDestinationAmount(),
                    mScheduleTransferViewModel.getTransfer().getDestinationCurrency()));
        } else {
            feeContainer.setVisibility(View.GONE);
            receiveAmountContainer.setVisibility(View.GONE);
            amountHorizontalBar.setVisibility(View.GONE);
            amount.setText(requireContext().getString(R.string.amount_currency_format,
                    mScheduleTransferViewModel.getTransfer().getDestinationAmount(),
                    mScheduleTransferViewModel.getTransfer().getDestinationCurrency()));
        }
    }

    private void showNotes() {
        View notesContainer = getView().findViewById(R.id.notes_container);
        TextView notesView = getView().findViewById(R.id.notes_value);

        if (mScheduleTransferViewModel.getTransfer().hasNotes()) {
            notesContainer.setVisibility(View.VISIBLE);
            notesView.setText(mScheduleTransferViewModel.getTransfer().getNotes());
        } else {
            notesContainer.setVisibility(View.GONE);
        }
    }

    private void showConfirmButton() {
        // transfer confirm button
        mTransferConfirmButtonProgress = getView().findViewById(R.id.transfer_confirm_button_progress_bar);
        Button button = getView().findViewById(R.id.transfer_confirm_button);
        button.setOnClickListener(new OneClickListener() {
            @Override
            public void onOneClick(View v) {
                mScheduleTransferViewModel.scheduleTransfer();
            }
        });
    }

    private static class ForeignExchangeListAdapter extends
            RecyclerView.Adapter<ForeignExchangeListAdapter.ViewHolder> {

        private static short DATA_WITH_HEADER = 0;
        private static short DATA_WITHOUT_HEADER = 1;

        private List<ForeignExchange> foreignExchanges;

        ForeignExchangeListAdapter(@NonNull final List<ForeignExchange> foreignExchanges) {
            this.foreignExchanges = foreignExchanges;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layout = LayoutInflater.from(parent.getContext());

            if (viewType == DATA_WITH_HEADER) {
                View viewDataWithHeader = layout.inflate(R.layout.item_foreign_exchange_with_header, parent, false);
                return new ViewHolder(viewDataWithHeader);
            }

            View viewDataOnly = layout.inflate(R.layout.item_foreign_exchange, parent, false);
            return new ViewHolder(viewDataOnly);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final ForeignExchange foreignExchange = foreignExchanges.get(position);
            if (foreignExchange != null) { // make sure its not view holder placeholder
                holder.bind(foreignExchange);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return DATA_WITH_HEADER;
            }
            return DATA_WITHOUT_HEADER;
        }

        @Override
        public int getItemCount() {
            return foreignExchanges.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            ViewHolder(@NonNull final View itemView) {
                super(itemView);
            }

            void bind(@NonNull final ForeignExchange fx) {
                TextView sellValue = itemView.findViewById(R.id.sell_value);
                TextView buyValue = itemView.findViewById(R.id.buy_value);
                TextView exchangeRateValue = itemView.findViewById(R.id.exchange_rate_value);

                sellValue.setText(itemView.getContext().getString(R.string.amount_currency_format,
                        fx.getSourceAmount(), fx.getSourceCurrency()));
                buyValue.setText(itemView.getContext().getString(R.string.amount_currency_format,
                        fx.getDestinationAmount(), fx.getDestinationCurrency()));
                exchangeRateValue.setText(itemView.getContext().getString(R.string.exchange_rate_format,
                        fx.getSourceCurrency(), fx.getRate(), fx.getDestinationCurrency()));
            }
        }
    }
}