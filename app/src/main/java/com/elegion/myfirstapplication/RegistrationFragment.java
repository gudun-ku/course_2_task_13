package com.elegion.myfirstapplication;

import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.elegion.myfirstapplication.model.User;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import retrofit2.HttpException;
import retrofit2.Response;


public class RegistrationFragment extends Fragment {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private EditText mEmail;
    private EditText mName;
    private EditText mPassword;
    private EditText mPasswordAgain;
    private Button mRegistration;
    private Disposable mSubscriptionDisposable;


    public static RegistrationFragment newInstance() {
        return new RegistrationFragment();
    }

    private View.OnClickListener mOnRegistrationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isInputValid()) {
                final AuthActivity activity = (AuthActivity) getActivity();
                User user = new User(
                        mEmail.getText().toString(),
                        mName.getText().toString(),
                        mPassword.getText().toString());

                mSubscriptionDisposable = ApiUtils.getApiService().registration(user)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            new Action() {
                                @Override
                                public void run() throws Exception {
                                    showMessage(R.string.registration_success);
                                    getFragmentManager().popBackStack();
                                }
                            },
                            new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {

                                    //изменим обработку ошибок
                                    if(throwable instanceof  HttpException) {
                                        //изменим обработку ошибок
                                        Response response = ((HttpException) throwable)
                                                .response();
                                        ApiError error = ApiUtils.parseError(response,response.code());
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
        ApiError.ErrorBean errorBean = error.getError();
        int code = error.getCode();
        String commonMessage = activity.getResponseErrorMessage(code);

        if (errorBean != null) {
            String currentError = errorBean.getNameFirstError();
            if (!currentError.isEmpty())
                mName.setError(currentError);
            else
                mName.setError(null);

            currentError = errorBean.getEmailFirstError();

            //Подсвечиваем все поля - имя, еmai
            if (!currentError.isEmpty()) {
                mEmail.setError(commonMessage);
                mName.setError(commonMessage);
            } else {
                mEmail.setError(null);
                mName.setError(null);
            }


            currentError = errorBean.getPasswordFirstError();
            if (!currentError.isEmpty()) {
                mPassword.setError(currentError);
                mPasswordAgain.setError(currentError);
            }
            else{
                mPassword.setError(null);
                mPasswordAgain.setError(currentError);
            }
        } else {
            mEmail.setError(commonMessage);
            mName.setError(commonMessage);
            mPassword.setError(commonMessage);
            mPasswordAgain.setError(commonMessage);
        }

        showMessage(commonMessage);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fr_registration, container, false);

        mEmail = view.findViewById(R.id.etEmail);
        mName = view.findViewById(R.id.etName);
        mPassword = view.findViewById(R.id.etPassword);
        mPasswordAgain = view.findViewById(R.id.tvPasswordAgain);
        mRegistration = view.findViewById(R.id.btnRegistration);

        mRegistration.setOnClickListener(mOnRegistrationClickListener);

        return view;
    }


    private boolean isInputValid() {
        return isEmailValid()
                & isNameValid()
                & isPasswordValid()
                & isRetypedPasswordsValid();
    }


    private boolean isEmailValid() {
        boolean result =
                !TextUtils.isEmpty(mEmail.getText())
                        && Patterns.EMAIL_ADDRESS.matcher(mEmail.getText()).matches();
        if (!result)
            mEmail.setError(getString(R.string.email_validation_error));

        return result;
    }

    private boolean isNameValid() {
        boolean result =!TextUtils.isEmpty(mName.getText());

        if (!result)
            mName.setError(getString(R.string.username_validation_error));

        return result;
    }

    private boolean isPasswordValid() {
        String password = mPassword.getText().toString();

        boolean result =
                !TextUtils.isEmpty(password)
                        && password.length() >= 8;
        if (!result)
            mPassword.setError(getString(R.string.password_validation_error));

        return result;
    }

    private boolean isRetypedPasswordsValid() {
        String password = mPassword.getText().toString();
        String retypedPassword = mPasswordAgain.getText().toString();

        boolean result = password.equals(retypedPassword);
        if (!result)
            mPasswordAgain.setError(getString(R.string.retyped_password_validation_error));

        return result;
    }


    private void showMessage(@StringRes int string) {
        Toast.makeText(getActivity(), string, Toast.LENGTH_LONG).show();
    }

    private void showMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        mSubscriptionDisposable.dispose();
        super.onDestroy();
    }
}
