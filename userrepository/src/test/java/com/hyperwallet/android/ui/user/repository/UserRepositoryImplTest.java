package com.hyperwallet.android.ui.user.repository;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static com.hyperwallet.android.model.user.User.ProfileTypes.INDIVIDUAL;
import static com.hyperwallet.android.model.user.User.UserStatuses.PRE_ACTIVATED;
import static com.hyperwallet.android.model.user.User.VerificationStatuses.NOT_REQUIRED;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.user.User;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class UserRepositoryImplTest {
    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();
    @Mock
    UserRepository.LoadUserCallback mMockCallback;
    @Spy
    UserRepositoryImpl mUserRepository;
    @Mock
    private Hyperwallet mHyperwallet;
    @Captor
    private ArgumentCaptor<Errors> mErrorCaptor;
    @Captor
    private ArgumentCaptor<User> mUserCaptor;

    @Before
    public void setup() {
        doReturn(mHyperwallet).when(mUserRepository).getHyperwallet();
    }

    @Test
    public void testLoadUser_returnsUser() {
        User.Builder builder = new User.Builder();
        final User user = builder
                .token("test-user-token")
                .status(PRE_ACTIVATED)
                .verificationStatus(NOT_REQUIRED)
                .createdOn("2017-10-30T22:15:45")
                .clientUserId("123456")
                .profileType(INDIVIDUAL)
                .firstName("Some")
                .lastName("Guy")
                .dateOfBirth("1991-01-01")
                .email("testUser@hyperwallet.com")
                .addressLine1("575 Market Street")
                .city("San Francisco")
                .stateProvince("CA")
                .country("US")
                .postalCode("94105")
                .language("en")
                .programToken("test-program-token")
                .build();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[0];
                listener.onSuccess(user);
                return listener;
            }
        }).when(mHyperwallet).getUser(ArgumentMatchers.<HyperwalletListener<User>>any());

        mUserRepository.loadUser(mMockCallback);

        verify(mMockCallback).onUserLoaded(mUserCaptor.capture());
        verify(mMockCallback, never()).onError(any(Errors.class));

        User resultUser = mUserCaptor.getValue();
        assertThat(resultUser.getToken(), is("test-user-token"));
        assertThat(resultUser.getStatus(), is(PRE_ACTIVATED));
        assertThat(resultUser.getVerificationStatus(), is(NOT_REQUIRED));
        assertThat(resultUser.getCreatedOn(), is("2017-10-30T22:15:45"));
        assertThat(resultUser.getClientUserId(), is("123456"));
        assertThat(resultUser.getProfileType(), is(INDIVIDUAL));
        assertThat(resultUser.getFirstName(), is("Some"));
        assertThat(resultUser.getLastName(), is("Guy"));
        assertThat(resultUser.getDateOfBirth(), is("1991-01-01"));
        assertThat(resultUser.getEmail(), is("testUser@hyperwallet.com"));
        assertThat(resultUser.getAddressLine1(), is("575 Market Street"));
        assertThat(resultUser.getCity(), is("San Francisco"));
        assertThat(resultUser.getStateProvince(), is("CA"));
        assertThat(resultUser.getCountry(), is("US"));
        assertThat(resultUser.getPostalCode(), is("94105"));
        assertThat(resultUser.getLanguage(), is("en"));
        assertThat(resultUser.getProgramToken(), is("test-program-token"));
    }

    @Test
    public void testLoadUser_returnsNoUser() {

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[0];
                listener.onSuccess(null);
                return listener;
            }
        }).when(mHyperwallet).getUser(ArgumentMatchers.<HyperwalletListener<User>>any());

        mUserRepository.loadUser(mMockCallback);

        verify(mMockCallback).onUserLoaded(mUserCaptor.capture());
        verify(mMockCallback, never()).onError(any(Errors.class));

        User user = mUserCaptor.getValue();
        assertThat(user, is(Matchers.nullValue()));
    }


    @Test
    public void testLoadUser_withError() {

        final Error error = new Error("test message", "TEST_CODE");

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[0];
                List<Error> errorList = new ArrayList<>();
                errorList.add(error);
                Errors errors = new Errors(errorList);
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).getUser(ArgumentMatchers.<HyperwalletListener<User>>any());

        mUserRepository.loadUser(mMockCallback);

        verify(mMockCallback, never()).onUserLoaded(ArgumentMatchers.<User>any());
        verify(mMockCallback).onError(mErrorCaptor.capture());

        assertThat(mErrorCaptor.getValue().getErrors(), hasItem(error));
    }

    @Test
    public void testRefreshUser_verifyHyperwalletCallGetUser() {
        User.Builder builder = new User.Builder();
        final User user = builder
                .token("test-user-token")
                .profileType(INDIVIDUAL)
                .build();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[0];
                listener.onSuccess(user);
                return listener;
            }
        }).when(mHyperwallet).getUser(ArgumentMatchers.<HyperwalletListener<User>>any());

        mUserRepository.loadUser(mMockCallback);

        verify(mHyperwallet).getUser(ArgumentMatchers.<HyperwalletListener<User>>any());

        mUserRepository.loadUser(mMockCallback);
        verify(mHyperwallet).getUser(ArgumentMatchers.<HyperwalletListener<User>>any());

        mUserRepository.refreshUser();
        mUserRepository.loadUser(mMockCallback);
        verify(mHyperwallet, times(2)).getUser(ArgumentMatchers.<HyperwalletListener<User>>any());

    }
}
