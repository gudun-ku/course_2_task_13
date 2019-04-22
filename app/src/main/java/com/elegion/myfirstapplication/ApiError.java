package com.elegion.myfirstapplication;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ApiError {

    @SerializedName("code")
    private int code;
    @SerializedName("errors")
    private ErrorBean mError;

    public ApiError() {}

    public ApiError(int statusCode) {
        this.code = statusCode;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public ErrorBean getError() {
        return mError;
    }

    public void setErrors(ErrorBean error) {
        mError = error;
    }

    public static class ErrorBean {

        @SerializedName("email")
        private List<String> emailErrors;
        @SerializedName("name")
        private List<String> nameErrors;
        @SerializedName("password")
        private List<String> passwordErrors;


        public List<String> getEmailErrors() {
            return emailErrors;
        }

        public List<String> getNameErrors() {
            return nameErrors;
        }

        public List<String> getPasswordErrors() {
            return passwordErrors;
        }

        public String getEmailFirstError() {
            if (emailErrors != null && emailErrors.size() > 0)
                return emailErrors.get(0);
            else
                return "";
        }

        public String getNameFirstError() {

            if (nameErrors!= null && nameErrors.size() > 0)
                return nameErrors.get(0);
            else
                return "";
        }

        public String getPasswordFirstError() {
            if (passwordErrors != null && passwordErrors.size() > 0)
                return passwordErrors.get(0);
            else
                return "";
        }

    }



}
