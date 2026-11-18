package com.kupi.fileuploader.configurations;

import com.kupi.fileuploader.enums.ErrorResponseStatusType;
import com.kupi.fileuploader.wrapper.ErrorResponseWrapper;
import com.kupi.fileuploader.wrapper.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.context.request.WebRequest;

/**
 * Handles the FileUploadBase$FileSizeLimitExceededException
 */
@ControllerAdvice
public class FileUploaderControllerAdvice extends ResponseEntityExceptionHandler {

    private final Translator translator;

    @Autowired
    public FileUploaderControllerAdvice(Translator translator) {
        this.translator = translator;
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ResponseWrapper responseWrapper =
                new ErrorResponseWrapper(ErrorResponseStatusType.EXCEEDED_FILE_SIZE,
                        translator.toLocale(ErrorResponseStatusType.EXCEEDED_FILE_SIZE
                                .getCodeString(ErrorResponseStatusType.EXCEEDED_FILE_SIZE.getCode())),
                        null);
        return new ResponseEntity<>(responseWrapper, HttpStatus.BAD_REQUEST);
    }
}
