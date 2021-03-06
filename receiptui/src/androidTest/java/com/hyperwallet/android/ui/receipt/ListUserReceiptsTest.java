package com.hyperwallet.android.ui.receipt;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;

import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.atPosition;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Pair;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;
import com.hyperwallet.android.ui.receipt.view.ListUserReceiptActivity;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletSdkRule;
import com.hyperwallet.android.ui.testutils.util.RecyclerViewCountAssertion;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;

@RunWith(AndroidJUnit4.class)
public class ListUserReceiptsTest {

    @ClassRule
    public static HyperwalletExternalResourceManager sResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public HyperwalletSdkRule mHyperwalletSdkRule = new HyperwalletSdkRule();
    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ActivityTestRule<ListUserReceiptActivity> mActivityTestRule =
            new ActivityTestRule<>(ListUserReceiptActivity.class, true, false);
    private TimeZone mDefaultTimeZone;

    private String usdCurrencySymbol = "$";
    private String cadCurrencySymbol = "CA$";
    private String krwCurrencySymbol = "₩";
    private String debitSymbol = "-";
    private String monthLabel1 = "June 2019";
    private String monthLabel2 = "December 2018";

    Pair CAD = new Pair("CAD","CA$");
    Pair USD = new Pair("USD","$");
    Pair EURO = new Pair("EUR", "€");
    Pair KRW = new Pair("KRW", "₩");
    Pair JOD = new Pair("JOD", "JOD");

    @Before
    public void setup() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("authentication_token_response.json")).mock();

        mDefaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("US/Pacific"));
        setLocale(Locale.US);
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
    }

    @After
    public void cleanup() {
        TimeZone.setDefault(mDefaultTimeZone);
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    @Test
    public void testListReceipt_userHasMultipleTransactions() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        //onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText(monthLabel1)))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0,
                hasDescendant(withText(R.string.credit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.payment)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(usdCurrencySymbol + "20.00")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("June 7, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts)).check(matches(atPosition(1,
                hasDescendant(withText(R.string.credit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(1, hasDescendant(withText(R.string.payment)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(1, hasDescendant(withText(cadCurrencySymbol + "25.00")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(1, hasDescendant(withText("June 2, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(1, hasDescendant(withText("CAD")))));

        onView(withId(R.id.list_receipts)).check(matches(atPosition(2,
                hasDescendant(withText(R.string.debit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(2, hasDescendant(withText(R.string.card_activation_fee)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(2, hasDescendant(withText(debitSymbol + usdCurrencySymbol + "1.95")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(2, hasDescendant(withText("June 1, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(2, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(3, hasDescendant(withText(monthLabel2)))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(3,
                hasDescendant(withText(R.string.debit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(3, hasDescendant(withText(R.string.transfer_to_prepaid_card)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(3, hasDescendant(withText(debitSymbol + krwCurrencySymbol + "40,000")))));

        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(3, hasDescendant(withText("December 1, 2018")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(3, hasDescendant(withText("KRW")))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(4));
    }

    @Test
    public void testListReceipt_userHasMultipleTransactionCurrencyFormat() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_currency_format_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        //onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText("June 2019")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.payment)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(KRW.second.toString() + "5,000,000")))));

        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("June 7, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText(KRW.first.toString())))));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(1, hasDescendant(withText("June 2, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(1,
                hasDescendant(withText(R.string.credit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(1, hasDescendant(withText(R.string.payment)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(1, hasDescendant(withText(USD.second.toString() + "9,000,000.00")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(1, hasDescendant(withText("June 2, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(1, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts)).check(matches(atPosition(2,
                hasDescendant(withText(R.string.debit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(2, hasDescendant(withText(R.string.card_activation_fee)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(2, hasDescendant(withText(debitSymbol + cadCurrencySymbol + "1,000,000.00")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(2, hasDescendant(withText("June 1, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(2, hasDescendant(withText(CAD.first.toString())))));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(3, hasDescendant(withText("December 2018")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(3,
                hasDescendant(withText(R.string.debit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(3, hasDescendant(withText(R.string.transfer_to_prepaid_card)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(3, hasDescendant(withText(debitSymbol + EURO.second.toString() + "10,000,000.00")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(3, hasDescendant(withText("December 1, 2018")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(3, hasDescendant(withText(EURO.first.toString())))));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(4, hasDescendant(withText("January 2018")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(4,
                hasDescendant(withText(R.string.debit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(4, hasDescendant(withText(R.string.transfer_to_bank_account)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(4, hasDescendant(withText(debitSymbol + JOD.second.toString() + "100,000,000.00")))));

        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(4, hasDescendant(withText("January 22, 2018")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(4, hasDescendant(withText(JOD.first.toString())))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(5));
    }

    @Test
    public void testListReceipt_clickTransactionDisplaysDetailsCurrencyFormatKRW() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_currency_format_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        onView(withId(R.id.transaction_header_text)).check(matches(withText(R.string.mobileTransactionTypeLabel)));
        onView(withId(R.id.transaction_type_icon)).check(matches(withText(R.string.credit)));
        onView(withId(R.id.transaction_title)).check(matches(withText(R.string.payment)));
        onView(withId(R.id.transaction_amount)).check(matches(withText(KRW.second.toString() + "5,000,000")));
        onView(withId(R.id.transaction_currency)).check(matches(withText(KRW.first.toString())));
        onView(withId(R.id.transaction_date)).check(matches(withText("June 7, 2019")));

    }

    @Test
    public void testListReceipt_clickTransactionDisplaysDetailsCurrencyFormatUSD() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_currency_format_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

        onView(withId(R.id.transaction_header_text)).check(matches(withText(R.string.mobileTransactionTypeLabel)));
        onView(withId(R.id.transaction_type_icon)).check(matches(withText(R.string.credit)));
        onView(withId(R.id.transaction_title)).check(matches(withText(R.string.payment)));
        onView(withId(R.id.transaction_amount)).check(matches(withText(USD.second.toString() + "9,000,000.00")));
        onView(withId(R.id.transaction_currency)).check(matches(withText(USD.first.toString())));
        onView(withId(R.id.transaction_date)).check(matches(withText("June 2, 2019")));

    }

    @Test
    public void testListReceipt_clickTransactionDisplaysDetailsCurrencyFormatJOD() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_currency_format_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);
        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.actionOnItemAtPosition(4, click()));

        onView(withId(R.id.transaction_header_text)).check(matches(withText(R.string.mobileTransactionTypeLabel)));
        onView(withId(R.id.transaction_type_icon)).check(matches(withText(R.string.debit)));
        onView(withId(R.id.transaction_title)).check(matches(withText(R.string.transfer_to_bank_account)));
        onView(withId(R.id.transaction_amount)).check(matches(withText(debitSymbol + JOD.second.toString() + "100,000,000.00")));
        onView(withId(R.id.transaction_currency)).check(matches(withText(JOD.first.toString())));
        onView(withId(R.id.transaction_date)).check(matches(withText("January 22, 2018")));

        onView(withId(R.id.details_header_text)).check(matches(withText(R.string.mobileFeeInfoLabel)));
        onView(withId(R.id.details_amount_label)).check(matches(withText(R.string.amount)));
        onView(withId(R.id.details_amount_value)).check(matches(withText(JOD.second.toString() + "100,000,000.00 JOD")));
        onView(withId(R.id.details_fee_label)).check(matches(withText(R.string.mobileFeeLabel)));
        onView(withId(R.id.details_fee_value)).check(matches(withText(JOD.second.toString() + "2.00 JOD")));
        onView(withId(R.id.details_transfer_amount_label)).check(
                matches(withText(R.string.mobileTransactionDetailsTotal)));
        onView(withId(R.id.details_transfer_amount_value)).check(matches(withText(JOD.second.toString() + "99,999,998.00 JOD")));
    }

    @Test
    public void testListReceipt_userHasCreditTransaction() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_credit_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText(monthLabel1)))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.credit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.payment)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(cadCurrencySymbol + "25.00")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("June 2, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("CAD")))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(1));
    }

    @Test
    public void testListReceipt_userHasDebitTransaction() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_debit_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText("May 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.debit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.transfer_to_prepaid_card)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(debitSymbol + usdCurrencySymbol +"18.05")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("May 2, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(1));
    }

    @Test
    public void testListReceipt_userHasUnknownTransactionType() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_unknown_type_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText(monthLabel1)))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.credit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.unknown_type)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(cadCurrencySymbol + "25.00")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("June 2, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("CAD")))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(1));
    }

    @Test
    public void testListReceipt_userHasNoTransactions() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        //todo: check empty view when it will be ready
        onView(withText(R.string.mobileNoTransactionsUser)).check(matches(isDisplayed()));
    }

    @Test
    public void testListReceipt_clickTransactionDisplaysDetails() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        onView(withId(R.id.transaction_header_text)).check(matches(withText(R.string.mobileTransactionTypeLabel)));
        onView(withId(R.id.transaction_type_icon)).check(matches(withText(R.string.credit)));
        onView(withId(R.id.transaction_title)).check(matches(withText(R.string.payment)));
        onView(withId(R.id.transaction_amount)).check(matches(withText(usdCurrencySymbol + "20.00")));
        onView(withId(R.id.transaction_currency)).check(matches(withText("USD")));
        onView(withId(R.id.transaction_date)).check(matches(withText("June 7, 2019")));

        onView(withId(R.id.receipt_details_header_label)).check(
                matches(withText(R.string.mobileTransactionDetailsLabel)));
        onView(withId(R.id.receipt_id_label)).check(matches(withText(R.string.mobileJournalNumberLabel)));
        onView(withId(R.id.receipt_id_value)).check(matches(withText("3051579")));
        onView(withId(R.id.date_label)).check(matches(withText(R.string.date)));
        onView(withId(R.id.date_value)).check(matches(withText("Jun 7, 2019, 10:08 AM PDT")));

        onView(withId(R.id.client_id_label)).check(matches(withText(R.string.mobileMerchantTxnLabel)));
        onView(withId(R.id.client_id_value)).check(matches(withText("8OxXefx5")));
        onView(withId(R.id.charity_label)).check(matches(withText(R.string.mobileCharityName)));
        onView(withId(R.id.charity_value)).check(matches(withText("Sample Charity")));
        onView(withId(R.id.check_number_label)).check(matches(withText(R.string.mobileCheckNumber)));
        onView(withId(R.id.check_number_value)).check(matches(withText("Sample Check Number")));
        onView(withId(R.id.website_label)).check(matches(withText(R.string.mobilePromoWebsite)));
        onView(withId(R.id.website_value)).check(matches(withText("https://localhost:8181")));
        onView(withText("A Person")).check(doesNotExist());

        onView(withId(R.id.receipt_notes_header_label)).check(matches(withText(R.string.mobileConfirmNotesLabel)));
        onView(withId(R.id.notes_value)).check(
                matches(withText("Sample payment for the period of June 15th, 2019 to July 23, 2019")));

        onView(withId(R.id.details_header_text)).check(matches(withText(R.string.mobileFeeInfoLabel)));
        onView(withId(R.id.details_amount_label)).check(matches(withText(R.string.amount)));
        onView(withId(R.id.details_amount_value)).check(matches(withText(usdCurrencySymbol + "20.00 USD")));
        onView(withId(R.id.details_fee_label)).check(matches(withText(R.string.mobileFeeLabel)));
        onView(withId(R.id.details_fee_value)).check(matches(withText(usdCurrencySymbol + "2.25 USD")));
        onView(withId(R.id.details_transfer_amount_label)).check(
                matches(withText(R.string.mobileTransactionDetailsTotal)));
        onView(withId(R.id.details_transfer_amount_value)).check(matches(withText(usdCurrencySymbol + "17.75 USD")));
    }

    @Test
    public void testListReceipt_clickTransactionDisplaysDetailsWithoutFees() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        onView(withId(R.id.list_receipts)).check(matches(isDisplayed()));

        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.actionOnItemAtPosition(3, click()));

        onView(withId(R.id.transaction_header_text)).check(matches(withText(R.string.mobileTransactionTypeLabel)));
        onView(withId(R.id.transaction_type_icon)).check(matches(withText(R.string.debit)));
        onView(withId(R.id.transaction_title)).check(matches(withText(R.string.transfer_to_prepaid_card)));
        onView(withId(R.id.transaction_amount)).check(matches(withText(debitSymbol + krwCurrencySymbol + "40,000")));
        onView(withId(R.id.transaction_currency)).check(matches(withText("KRW")));
        onView(withId(R.id.transaction_date)).check(matches(withText("December 1, 2018")));

        onView(withId(R.id.receipt_details_header_label)).check(
                matches(withText(R.string.mobileTransactionDetailsLabel)));
        onView(withId(R.id.receipt_id_label)).check(matches(withText(R.string.mobileJournalNumberLabel)));
        onView(withId(R.id.receipt_id_value)).check(matches(withText("3051590")));
        onView(withId(R.id.date_label)).check(matches(withText(R.string.date)));
        onView(withId(R.id.date_value)).check(matches(withText("Dec 1, 2018, 9:12 AM PST")));

        onView(withId(R.id.client_id_label)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.client_id_value)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.charity_label)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.charity_value)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.check_number_label)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.check_number_value)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.website_label)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.website_value)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        onView(withId(R.id.receipt_notes_information)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.receipt_notes_header_label)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.notes_value)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void testListReceipt_verifyTransactionsLoadedUponScrolling() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_paged_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_paged_second_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_paged_third_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_list_paged_last_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        // run test
        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(20));
        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.scrollToPosition(10));
        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(30));
        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.scrollToPosition(20));
        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(40));
        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.scrollToPosition(30));
        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(50));

        // verify that when the list reaches the end no additional data is loaded
        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.scrollToPosition(40));
        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(50));
        onView(withId(R.id.list_receipts)).perform(RecyclerViewActions.scrollToPosition(50));
        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(50));
    }

    @Test
    public void testListReceipt_checkDateTextOnLocaleChange() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_debit_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        setLocale(Locale.ITALY);
        // run test
        mActivityTestRule.launchActivity(null);
        // assert
        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText("maggio 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("2 maggio 2019")))));
    }

    @Test
    public void testListReceipt_displaysNetworkErrorDialogOnConnectionTimeout() {
        mMockWebServer.getServer().enqueue(new MockResponse().setResponseCode(HTTP_OK).setBody(sResourceManager
                .getResourceContent("receipt_debit_response.json")).setBodyDelay(10500, TimeUnit.MILLISECONDS));
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("receipt_debit_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_NO_CONTENT).withBody("").mock();

        mActivityTestRule.launchActivity(null);

        onView(withText(R.string.error_dialog_connectivity_title)).check(matches(isDisplayed()));
        onView(withText(R.string.io_exception)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.try_again_button_label)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.cancelButtonLabel)));

        // retry button clicked
        onView(withId(android.R.id.button1)).perform(click());
        onView(withText(R.string.error_dialog_connectivity_title)).check(doesNotExist());

        // assert
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.title_activity_receipt_list)));
        /*
        onView(withId(R.id.list_receipts))
                .check(matches(atPosition(0, hasDescendant(withText("May 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0,
                hasDescendant(withText(com.hyperwallet.android.ui.receipt.R.string.debit)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(R.string.transfer_to_prepaid_card)))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText(debitSymbol + usdCurrencySymbol + "18.05")))));
        onView(withId(R.id.list_receipts)).check(
                matches(atPosition(0, hasDescendant(withText("May 2, 2019")))));
        onView(withId(R.id.list_receipts)).check(matches(atPosition(0, hasDescendant(withText("USD")))));

        onView(withId(R.id.list_receipts)).check(new RecyclerViewCountAssertion(1));

         */
    }

    private void setLocale(Locale locale) {
        Context context = ApplicationProvider.getApplicationContext();
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.JELLY_BEAN) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }
}
