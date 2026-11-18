package com.kupi.fileuploader.controller;

import com.kupi.fileuploader.configurations.Translator;
import com.kupi.fileuploader.domain.response.CsvResponseDto;
import com.kupi.fileuploader.enums.ErrorResponseStatusType;
import com.kupi.fileuploader.enums.SuccessResponseStatusType;
import com.kupi.fileuploader.exception.FileManagerException;
import com.kupi.fileuploader.service.CsvService;
import com.kupi.fileuploader.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

/**
 * CsvFile Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/csv")
public class CsvFileController extends Controller {

    private static final String CSV_FILE_EXTENSION = "CSV";
    private final long maxCsvByteSize;
    private final CsvService csvService;

    @Autowired
    public CsvFileController(@Value("${csv.maxByteSize}") long maxCsvByteSize,
                             Translator translator,
                             CsvService csvService) {
        super(translator);
        this.maxCsvByteSize = maxCsvByteSize;
        this.csvService = csvService;
    }

    /**
     * This method generates the csv url for the given csv file.
     *
     * @param file MultipartFile
     * @return csv file url
     */
    @PostMapping(path = {"/upload"},
            consumes = MULTIPART_FORM_DATA, produces = APPLICATION_JSON)
    public ResponseEntity<ResponseWrapper> uploadCsvFile(@RequestParam String fileName,
                                                         @RequestParam("file") MultipartFile file) {
        if (file.getOriginalFilename() == null ||
                file.getSize() == 0) {
            return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
        }

        if (!validateFileType(file)) {
            return getBadRequestError(ErrorResponseStatusType.UNSUPPORTED_FILE_FORMAT);
        }
        String csvName = file.getOriginalFilename();
        log.debug("Csv uploading request for csv name:{}.", csvName);
        try {
            String csvContentType = file.getContentType();
            long csvByteSize = file.getSize();
            CsvResponseDto csvResponseDto = new CsvResponseDto(file, null);
            if (csvContentType != null) {
                if (csvByteSize <= maxCsvByteSize) {
                    return uploadCsvIfValid(file, fileName);
                } else {
                    return getBadRequestError(ErrorResponseStatusType.EXCEEDED_CVS_SIZE, csvResponseDto);
                }
            } else {
                return getBadRequestError(ErrorResponseStatusType.INVALID_CSV_TYPE, csvResponseDto);
            }
        } catch (FileManagerException e) {
            log.error("Uploading csv was failed for the csv name: {}", csvName, e);
            return getInternalServerError();
        }
    }

    /**
     * This method uploads valid csv.
     *
     * @param csv csv
     * @return csv url
     */
    private ResponseEntity<ResponseWrapper> uploadCsvIfValid(MultipartFile csv, String fileName) {
        var csvS3UploadResponseDto = csvService.uploadCsv(csv, fileName);
        CsvResponseDto csvResponseDto = new CsvResponseDto(csv, csvS3UploadResponseDto);
        log.debug("Successfully uploaded csv: {}, csvUrl: {}", csv.getOriginalFilename(),
                csvResponseDto.toLogJson());
        return getSuccessResponse(SuccessResponseStatusType.CSV_UPLOAD, csvResponseDto);
    }

    /**
     * This method checks the validity of file type.
     *
     * @param file multipartFile
     * @return true/ false
     */
    private boolean validateFileType(MultipartFile file) {
        if (!file.isEmpty()) {
            String filenameExtension = Objects
                    .requireNonNull(StringUtils.getFilenameExtension(file.getOriginalFilename())).toUpperCase();
            return filenameExtension.endsWith(CSV_FILE_EXTENSION);
        }
        return false;
    }
}
