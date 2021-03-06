package com.hyperwallet.android.ui.transfermethod;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

import static com.hyperwallet.android.ui.common.view.error.DefaultErrorDialogFragment.RESULT_ERROR;
import static com.hyperwallet.android.ui.testutils.util.EspressoUtils.nestedScrollTo;

import android.app.Instrumentation;
import android.content.Intent;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;
import com.hyperwallet.android.ui.common.view.error.DefaultErrorDialogFragment;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.ui.testutils.util.RecyclerViewCountAssertion;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;
import com.hyperwallet.android.ui.transfermethod.rule.HyperwalletInsightMockRule;
import com.hyperwallet.android.ui.transfermethod.view.AddTransferMethodActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class AddTransferMethodTest {

    private static final String ACCOUNT_NUMBER = "8017110254";
    private static final String ROUTING_NUMBER = "211179539";

    @ClassRule
    public static HyperwalletExternalResourceManager sResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public HyperwalletInsightMockRule mHyperwalletInsightMockRule = new HyperwalletInsightMockRule();
    @Rule
    public HyperwalletMockWebServer mMockWebServer = new HyperwalletMockWebServer(8080);
    @Rule
    public ActivityTestRule<AddTransferMethodActivity> mActivityTestRule =
            new ActivityTestRule<AddTransferMethodActivity>(AddTransferMethodActivity.class, true, false) {
                @Override
                protected Intent getActivityIntent() {
                    Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                            AddTransferMethodActivity.class);
                    intent.putExtra("TRANSFER_METHOD_TYPE", "BANK_ACCOUNT");
                    intent.putExtra("TRANSFER_METHOD_COUNTRY", "US");
                    intent.putExtra("TRANSFER_METHOD_CURRENCY", "USD");
                    return intent;
                }
            };

    @Before
    public void setup() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("authentication_token_response.json")).mock();
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
    }

    @After
    public void cleanup() {
        TransferMethodRepositoryFactory.clearInstance();
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    @Test
    public void testAddTransferMethod_accountDetailsHiddenOnEmptyFeeAndProcessingResponse() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_fields_empty_details_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.add_transfer_method_static_container)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.add_transfer_method_information)).check(
                matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void testAddTransferMethod_displaysErrorDialogOnDuplicateAccountFailure() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_fields_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_BAD_REQUEST).withBody(sResourceManager
                .getResourceContent("bank_account_duplicate_routing_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.branchId)).perform(nestedScrollTo(), replaceText(ROUTING_NUMBER));
        onView(withId(R.id.bankAccountId)).perform(nestedScrollTo(), replaceText(ACCOUNT_NUMBER));
        onView(withId(R.id.bankAccountPurpose)).perform(nestedScrollTo(), click());
        onView(allOf(withId(R.id.select_name), withText("Savings"))).perform(click());
        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        // check dialog content
        onView(withText(R.string.error_dialog_title)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(containsString(
                "The account information you provided is already registered. Based on the external account "
                        + "configuration duplications are not allowed.")))
                .inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.ok)));
        onView(withId(android.R.id.button1)).perform(click());

        // should display the add tm form
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar)))).check(
                matches(withText(R.string.title_add_bank_account)));

        // connectivity dialog should be dismissed and does not exist in ui
        onView(withText(R.string.error_dialog_title)).check(doesNotExist());
    }

    @Test
    public void testAddTransferMethod_displaysUnexpectedErrorDialogOnException() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_fields_bank_account_response.json")).mock();
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_BAD_REQUEST).withBody(sResourceManager
                .getResourceContent("invalid_json_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.branchId)).perform(nestedScrollTo(), replaceText(ROUTING_NUMBER));
        onView(withId(R.id.bankAccountId)).perform(nestedScrollTo(), replaceText(ACCOUNT_NUMBER));
        onView(withId(R.id.bankAccountPurpose)).perform(nestedScrollTo(), click());
        onView(allOf(withId(R.id.select_name), withText("Savings"))).perform(click());

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());

        // check dialog content
        onView(withText(R.string.error_dialog_unexpected_title)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(R.string.unexpected_exception)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.ok)));
        onView(withId(android.R.id.button1)).perform(click());

        // verify activity is finished
        assertThat("Result code is incorrect",
                mActivityTestRule.getActivityResult().getResultCode(), is(RESULT_ERROR));
    }

    @Test
    public void testAddTransferMethod_displaysNetworkErrorDialogOnConnectionTimeout() throws IOException {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_fields_bank_account_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.branchId)).perform(nestedScrollTo(), replaceText(ROUTING_NUMBER));
        onView(withId(R.id.bankAccountId)).perform(nestedScrollTo(), replaceText(ACCOUNT_NUMBER));
        onView(withId(R.id.bankAccountPurpose)).perform(nestedScrollTo(), click());
        onView(allOf(withId(R.id.select_name), withText("Savings"))).perform(click());

        // initiate test
        mMockWebServer.getServer().shutdown();

        onView(withId(R.id.add_transfer_method_button)).perform(nestedScrollTo(), click());
        // by default screen is in portrait mode

        // assert error dialog information exist in portrait mode
        onView(withText(R.string.error_dialog_connectivity_title)).check(matches(isDisplayed()));
        onView(withText(R.string.io_exception)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.try_again_button_label)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.cancelButtonLabel)));

        // retry button clicked
        onView(withId(android.R.id.button1)).perform(click());

        // should still display connectivity issue
        onView(withText(R.string.error_dialog_connectivity_title)).check(matches(isDisplayed()));
        onView(withText(R.string.io_exception)).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.try_again_button_label)));
        onView(withId(android.R.id.button2)).check(matches(withText(R.string.cancelButtonLabel)));

        // cancel button clicked
        onView(withId(android.R.id.button2)).perform(click());

        Instrumentation.ActivityResult result = mActivityTestRule.getActivityResult();
        assertThat(result.getResultCode(), is(DefaultErrorDialogFragment.RESULT_ERROR));
        assertThat(mActivityTestRule.getActivity().isFinishing(), is(true));
    }

    @Test
    public void testAddTransferMethod_verifySelectionWidgetSearch() {
        mMockWebServer.mockResponse().withHttpResponseCode(HTTP_OK).withBody(sResourceManager
                .getResourceContent("successful_tmc_fields_bank_account_selection_response.json")).mock();

        mActivityTestRule.launchActivity(null);

        onView(withId(R.id.bankId)).perform(nestedScrollTo(), click());
        onView(withId(R.id.search_button)).perform(click());
        onView(withId(R.id.search_src_text)).perform(typeText("HSBC Bank Argentina"));
        onView(withId(R.id.input_selection_list)).check(new RecyclerViewCountAssertion(1));
        onView(allOf(withId(R.id.select_name), withText("HSBC Bank Argentina"))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.select_name), withText("HSBC Bank Argentina"))).perform(click());

        onView(withId(R.id.bankId)).check(matches(withText("HSBC Bank Argentina")));
        onView(withId(R.id.input_selection_list)).check(doesNotExist());
    }
}
