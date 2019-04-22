package com.elegion.myfirstapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.elegion.myfirstapplication.albums.AlbumsActivity;
import com.elegion.myfirstapplication.model.User;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import retrofit2.Response;


public class AuthFragment extends Fragment {
    private AutoCompleteTextView mEmail;
    private EditText mPassword;
    private Button mEnter;
    private Button mRegister;
    private SharedPreferencesHelper mSharedPreferencesHelper;
    private Disposable mSubscriptionDisposable;

    private ArrayAdapter<String> mEmailedUsersAdapter;

    public static AuthFragment newInstance() {
        Bundle args = new Bundle();

        AuthFragment fragment = new AuthFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private View.OnClickListener mOnEnterClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isInputValid()) {
                final AuthActivity activity = (AuthActivity) getActivity();

                mSubscriptionDisposable =  ApiUtils.getApiService(mEmail.getText().toString(),mPassword.getText().toString(),true)
                    .authentication()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        new Consumer<User>() {
                            @Override
                            public void accept(User user) throws Exception {
                                if (user != null && user instanceof User) {
                                    showMessage("Hi again, " + user.getName() + "!");
                                } else {
                                    showMessage(R.string.msg_no_internet);
                                }
                                startActivity(new Intent(activity, AlbumsActivity.class));
                                getActivity().finish();
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {

                                //изменим обработку ошибок
                                if(throwable instanceof  HttpException) {
                                    Response response = ((HttpException) throwable)
                                            .response();
                                    ApiError error = ApiUtils.parseError(response, response.code());
                                    highlightErrors(error, activity);
                                } else {
                                    showMessage(R.string.msg_no_internet);
                                }

                            }
                        }
                    );

            } else {
                showMessage(R.string.input_error);
            }
        }
    };

    private void highlightErrors(ApiError error, SingleFragmentActivity activity) {
        /*
        ApiError.ErrorBean errorBean = error.getError();
        int code = error.getCode();
        String commonMessage = activity.getResponseErrorMessage(code);
        */
        // По требованиям к заданию
        String commonMessage = getString(R.string.response_code_400);

        mEmail.setError(commonMessage);
        mPassword.setError(commonMessage);

        showMessage(commonMessage);
    }


    private View.OnClickListener mOnRegisterClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, RegistrationFragment.newInstance())
                    .addToBackStack(RegistrationFragment.class.getName())
                    .commit();
        }
    };

    private View.OnFocusChangeListener mOnEmailFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (hasFocus) {

                //Oreo 8.0 show popup window error - need to delay a bit
                new Handler(getActivity().getMainLooper()).postDelayed(new Runnable(){
                    public void run() {
                        mEmail.showDropDown();
                    }
                }, 200L);
            }
        }
    };

    private boolean isInputValid() {
        return isEmailValid()
                & isPasswordValid();
    }

    private boolean isEmailValid() {
        boolean result =
        !TextUtils.isEmpty(mEmail.getText())
                && Patterns.EMAIL_ADDRESS.matcher(mEmail.getText()).matches();

        if (!result)
            mEmail.setError(getString(R.string.email_validation_error));

        return result;
    }

    private boolean isPasswordValid() {
        String password = mPassword.getText().toString();
        boolean result =
                !TextUtils.isEmpty(password)
                        && (password.length() >= 8);
        if (!result)
            mPassword.setError(getString(R.string.password_validation_error));

        return result;
    }

    private void showMessage(@StringRes int string) {
        Toast.makeText(getActivity(), string, Toast.LENGTH_LONG).show();
    }

    private void showMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fr_auth, container, false);

        mSharedPreferencesHelper = new SharedPreferencesHelper(getActivity());

        mEmail = v.findViewById(R.id.etEmail);
        mPassword = v.findViewById(R.id.etPassword);
        mEnter = v.findViewById(R.id.buttonEnter);
        mRegister = v.findViewById(R.id.buttonRegister);

        mEnter.setOnClickListener(mOnEnterClickListener);
        mRegister.setOnClickListener(mOnRegisterClickListener);
        mEmail.setOnFocusChangeListener(mOnEmailFocusChangeListener);

        mEmailedUsersAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_dropdown_item_1line,
                mSharedPreferencesHelper.getSuccessEmails()
        );
        mEmail.setAdapter(mEmailedUsersAdapter);

        return v;
    }

    @Override
    public void onDestroy() {
        mSubscriptionDisposable.dispose();
        super.onDestroy();
    }
}
