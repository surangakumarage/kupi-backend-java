package com.kupi.cc.location.controller;

import com.kupi.cc.location.configuration.Translator;
import com.kupi.cc.location.domain.response.ResponseDto;
import com.kupi.cc.location.enums.ErrorResponseStatusType;
import com.kupi.cc.location.enums.ResponseStatusType;
import com.kupi.cc.location.enums.SuccessResponseStatusType;
import com.kupi.cc.location.wrapper.ErrorResponseWrapper;
import com.kupi.cc.location.wrapper.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Controller
 */
public class Controller {
    protected static final String USER_ID_HEADER = "User-Id";
    protected static final int DEFAULT_PAGE = 0;
    protected static final int PAGE_MAX_SIZE = 250;
    protected static final String UNSUPPORTED_USER_TYPE_ACTION_LOG =
            "Unsupported userType: {} to perform the action: {}";
    protected static final String DEFAULT_SORT = "updatedAt";
    protected static final String AUTH_TOKEN_HEADER = "Auth-Token";
    protected static final String ALL = "ALL";

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
                successResponseStatusType.getMessage(), translator.toLocale(successResponseStatusType.getCode()),
                responseDto);
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
}
