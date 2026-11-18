package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.response.ResponseDto;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.wrapper.ErrorResponseWrapper;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import com.kupi.cc.base.wrapper.SuccessResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Controller
 */
@Slf4j
public class Controller {

    protected static final String APPLICATION_JSON_UTF_8 = "application/json;charset=UTF-8";
    protected static final String HEADER_USER_ID = "User-Id";
    protected static final String TIME_ZONE_HEADER = "Time-Zone";
    protected static final String AUTH_TOKEN_HEADER = "Auth-Token";
    protected static final String USER_ID_HEADER = "User-Id";
    protected static final int PAGE_MAX_SIZE = 250;
    protected static final int DEFAULT_PAGE = 0;
    protected static final String DEFAULT_SORT = "updatedAt";
    protected static final String TITLE_SORT = "title";

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
                        translator.toLocale(ErrorResponseStatusType.getCodeString(errorResponseStatusType.getCode())),
                        null);
        return new ResponseEntity<>(responseWrapper, HttpStatus.BAD_REQUEST);
    }


    /**
     * This method creates empty data response for success response
     *
     * @return ok
     */
    protected ResponseEntity<ResponseWrapper> getSuccessResponse(ResponseDto responseDto,
                                                                 SuccessResponseStatusType successResponseStatusType) {
        ResponseWrapper responseWrapper = new SuccessResponseWrapper(successResponseStatusType,
                translator.toLocale(SuccessResponseStatusType.getCodeString(successResponseStatusType.getCode())),
                responseDto);
        return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
    }

    /**
     * This method creates the internal server error response.
     *
     * @return response entity
     */
    protected ResponseEntity<ResponseWrapper> getInternalServerError() {
        var errorResponseWrapper = new ErrorResponseWrapper(
                ErrorResponseStatusType.INTERNAL_SERVER_ERROR,
                ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage(),
                null);
        return new ResponseEntity<>(errorResponseWrapper, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    protected boolean isEmpty(String field) {
        return field == null || field.trim().isEmpty();
    }
}
