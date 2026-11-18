package com.kupi.notification.controller;

import com.kupi.notification.configuration.Translator;
import com.kupi.notification.domain.response.ResponseDto;
import com.kupi.notification.enums.ErrorResponseStatusType;
import com.kupi.notification.enums.ResponseStatusType;
import com.kupi.notification.enums.SuccessResponseStatusType;
import com.kupi.notification.wrapper.ErrorResponseWrapper;
import com.kupi.notification.wrapper.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller
 */
public class Controller {

    protected static final String APPLICATION_JSON_UTF_8 = "application/json;charset=UTF-8";
    protected static final String APPLICATION_JSON = "application/json";
    protected static final String TIMEZONE_HEADER = "Time-Zone";
    private static final String AUTH_TOKEN = "Auth-Token";
    private final Translator translator;

    @Autowired
    public Controller(Translator translator) {
        this.translator = translator;
    }

    /**
     * This method creates the empty data response for bad request.
     *
     * @param errorResponseStatusType errorResponseStatusType
     * @return bad request error response
     */
    protected ResponseEntity<ResponseWrapper> getBadRequestError(ErrorResponseStatusType errorResponseStatusType) {
        ResponseWrapper responseWrapper =
                new ErrorResponseWrapper(errorResponseStatusType,
                        translator.toLocale(errorResponseStatusType.getCodeString(errorResponseStatusType.getCode())),
                        null);
        return new ResponseEntity<>(responseWrapper, HttpStatus.BAD_REQUEST);
    }


    /**
     * This method creates empty data response for success response
     *
     * @return ok
     */
    protected ResponseEntity<ResponseWrapper> getSuccessResponse(SuccessResponseStatusType successResponseStatusType,
                                                                 ResponseDto responseDto) {
        ResponseWrapper responseWrapper = new ResponseWrapper(ResponseStatusType.SUCCESS,
                successResponseStatusType.getMessage(), responseDto,
                translator.toLocale(successResponseStatusType.getCode()));
        return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
    }

    /**
     * This method creates the empty data response for internal server error.
     *
     * @return internal server error response
     */
    protected ResponseEntity<ResponseWrapper> getInternalServerError() {
        ResponseWrapper responseWrapper =
                new ErrorResponseWrapper(ErrorResponseStatusType.INTERNAL_SERVER_ERROR,
                        translator.toLocale(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                                .getCodeString(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())),
                        null);
        return new ResponseEntity<>(responseWrapper, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * This method returns the auth token.
     *
     * @param request request
     * @return auth token
     */
    protected String getToken(HttpServletRequest request) {
        return request.getHeader(AUTH_TOKEN).trim();
    }


    /**
     * This method returns the timeZoneId.
     *
     * @param request request
     * @return timeZoneId
     */
    protected String getTimeZoneId(HttpServletRequest request) {
        return request.getHeader(TIMEZONE_HEADER) == null ? "" : request.getHeader(TIMEZONE_HEADER).trim();
    }

}
