package com.hyperwallet.android.ui.transfermethod.view.widget;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.graphql.field.HyperwalletField;

import org.junit.Test;

public class AbstractMaskedInputWidgetTest {

    private TestInputWidget mTestInputWidget;

    public AbstractMaskedInputWidgetTest() {
        mTestInputWidget = new TestInputWidget(null, null, null, null);
    }

    @Test
    public void testFormatToDisplay_usingExcelData() {
        // TODO
    }

    @Test
    public void testFormatToDisplay_edgeCases() {
        // TODO temporary tests for most scenarios and edge cases, use values from excel sheet instead
        String test1 = mTestInputWidget.format("v23", "@#@-#@#");
        assertThat(test1, is("v2"));

        String test2 = mTestInputWidget.format("v5l", "@#@ #@#");
        assertThat(test2, is("v5l"));

        String test3 = mTestInputWidget.format("v5l3", "@#@-#@#");
        assertThat(test3, is("v5l-3"));

        String test4 = mTestInputWidget.format("v5l3c2", "@#@-#@#");
        assertThat(test4, is("v5l-3c2"));

        String num1 = mTestInputWidget.format("123456", "### ###");
        assertThat(num1, is("123 456"));

        String num2 = mTestInputWidget.format("123", "### ###");
        assertThat(num2, is("123"));

        String num3 = mTestInputWidget.format("123abc", "### ###");
        assertThat(num3, is("123"));

        String num4 = mTestInputWidget.format("123abc456", "### ###");
        assertThat(num4, is("123 456"));

        String word1 = mTestInputWidget.format("b2", "@@@");
        assertThat(word1, is("b"));

        String phone1 = mTestInputWidget.format("16046332234", "+# (###) ###-####");
        assertThat(phone1, is("+1 (604) 633-2234"));

        String star1 = mTestInputWidget.format("v5l", "***-***");
        assertThat(star1, is("v5l"));

        String star2 = mTestInputWidget.format("v5l3", "***-***");
        assertThat(star2, is("v5l-3"));

        String star3 = mTestInputWidget.format("v5!@#$l-)(*&^%$#@!3$^c&%&%2", "***-***");
        assertThat(star3, is("v5l-3c2"));

        String empty = mTestInputWidget.format("", "***-***");
        assertThat(empty, is(""));
    }

    class TestInputWidget extends AbstractMaskedInputWidget {
        public TestInputWidget(@Nullable HyperwalletField field, @NonNull WidgetEventListener listener,
                @Nullable String defaultValue, @NonNull View defaultFocusView) {
            super(field, listener, defaultValue, defaultFocusView);
        }

        public View getView(@NonNull final ViewGroup viewGroup) {
            return null;
        }

        public String getValue() {
            return null;
        }

        public void showValidationError(String errorMessage) {
        }
    }
}