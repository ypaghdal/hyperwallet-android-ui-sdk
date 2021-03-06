/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Hyperwallet Systems Inc.
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
package com.hyperwallet.android.ui.transfermethod.view.widget;

import android.app.Activity;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.hyperwallet.android.model.graphql.field.Field;
import com.hyperwallet.android.ui.R;

import java.text.ParseException;

public class DateWidget extends AbstractWidget implements DateChangedListener {

    private final DateUtils mDateUtils;
    private ViewGroup mContainer;
    private String mValue;
    private TextInputLayout mTextInputLayout;
    private EditText mEditText;

    public DateWidget(@NonNull Field field, @NonNull WidgetEventListener listener,
            @Nullable String defaultValue, @NonNull View defaultFocusView) {
        super(field, listener, defaultValue, defaultFocusView);
        mDateUtils = new DateUtils();
        mValue = defaultValue;
    }

    @Override
    public View getView(@NonNull final ViewGroup viewGroup) {
        if (mContainer == null) {
            mContainer = (ViewGroup) LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_widget_layout, viewGroup, false);
            setIdFromFieldLabel(mContainer);
            mContainer.setFocusable(true);
            mContainer.setFocusableInTouchMode(false);

            // input control
            mTextInputLayout = new TextInputLayout(new ContextThemeWrapper(viewGroup.getContext(),
                    mField.isEditable() ? R.style.Widget_Hyperwallet_TextInputLayout
                            : R.style.Widget_Hyperwallet_TextInputLayout_Disabled));

            mEditText = new EditText(
                    new ContextThemeWrapper(viewGroup.getContext(), R.style.Widget_Hyperwallet_TextInputEditText));
            mEditText.setTextColor(viewGroup.getContext().getResources().getColor(R.color.regularColorSecondary));
            try {
                mEditText.setText(mDateUtils.convertDateFromServerToWidgetFormat(mDefaultValue));
            } catch (ParseException e) {
                mEditText.setText("");
            }
            setIdFromFieldLabel(mTextInputLayout);

            mEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            mEditText.setKeyListener(null);
            mEditText.setFocusableInTouchMode(false);
            mEditText.setFocusable(false);
            setIdFromFieldName(mEditText);
            mEditText.setCustomSelectionActionModeCallback(new ActionModeCallbackInterceptor());
            mTextInputLayout.setHint(mField.getLabel());
            mTextInputLayout.addView(mEditText);

            mEditText.setEnabled(mField.isEditable());
            if (mField.isEditable()) {
                mEditText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideSoftKey(v);
                        mListener.openWidgetDateDialog(mValue, mField.getName());
                    }
                });
            }

            appendLayout(mTextInputLayout, true);
            mContainer.addView(mTextInputLayout);
        }
        return mContainer;
    }

    @Override
    public String getValue() {
        return mValue;
    }

    @Override
    public void showValidationError(String errorMessage) {
        mTextInputLayout.setError(errorMessage);
    }

    @Override
    public void onUpdate(@Nullable final String selectedDate) {
        if (!TextUtils.isEmpty(selectedDate)) {
            mValue = selectedDate;
            try {
                mEditText.setText(mDateUtils.convertDateFromServerToWidgetFormat(selectedDate));
                mListener.saveTextChanged(getName(), getValue());
                mListener.valueChanged(DateWidget.this);
            } catch (ParseException e) {
                mEditText.setText(selectedDate);
            }
        }
        if (isValid()) {
            mTextInputLayout.setError(null);
        }
    }

    private void hideSoftKey(@NonNull View focusedView) {
        InputMethodManager inputMethodManager = (InputMethodManager) focusedView.getContext().getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
    }

}