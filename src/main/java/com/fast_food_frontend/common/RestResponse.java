package com.fast_food_frontend.common;

import org.springframework.http.HttpStatus;

public record RestResponse<T>(int status, String message, T data) {
    public static <T> RestResponse<T> ok(T data) {
        return new RestResponse<T>(HttpStatus.OK.value(), "OK", data);
    }

    public static <T> RestResponse<T> created(T data) {
        return new RestResponse<T>(HttpStatus.CREATED.value(), "Created", data);
    }

    public static <T> RestResponse<T> accepted(T data) {
        return new RestResponse<T>(HttpStatus.ACCEPTED.value(), "Accepted", data);
    }

    public static <T> RestResponse<T> badRequest(String message, T data) {
        return new RestResponse<T>(HttpStatus.BAD_REQUEST.value(), message, data);
    }

    public static <T> RestResponse<T> badGateway(String message, T data) {
        return new RestResponse<T>(HttpStatus.BAD_GATEWAY.value(), message, data);
    }

    public static <T> RestResponse<T> unauthorized(String message, T data) {
        return new RestResponse<T>(HttpStatus.UNAUTHORIZED.value(), message, data);
    }

    public static <T> RestResponse<T> multiStatus(T data) {
        return new RestResponse<T>(HttpStatus.MULTI_STATUS.value(), "", data);
    }

    public static <T> RestResponse<T> notFound(String message, T data) {
        return new RestResponse<T>(HttpStatus.NOT_FOUND.value(), message, data);
    }

    public static <T> RestResponse<T> success() {
        return new RestResponse<T>(HttpStatus.OK.value(), "Success", (T) null);
    }
}
