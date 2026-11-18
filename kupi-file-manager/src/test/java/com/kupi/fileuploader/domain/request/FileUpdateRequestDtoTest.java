package com.kupi.fileuploader.domain.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link FileUpdateRequestDto} class
 */
class FileUpdateRequestDtoTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_RequiredFieldsAreAvailable() {
        FileUpdateRequestDto fileUpdateRequestDto = getSampleFileUpdateRequestDto();
        assertTrue(fileUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_FileIdIsNull() {
        FileUpdateRequestDto fileUpdateRequestDto = getSampleFileUpdateRequestDto();
        fileUpdateRequestDto.setFileId(null);
        assertFalse(fileUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_FileIdIsEmpty() {
        FileUpdateRequestDto fileUpdateRequestDto = getSampleFileUpdateRequestDto();
        fileUpdateRequestDto.setFileId("");
        assertFalse(fileUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NameIsNull() {
        FileUpdateRequestDto fileUpdateRequestDto = getSampleFileUpdateRequestDto();
        fileUpdateRequestDto.setName(null);
        assertFalse(fileUpdateRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_NameIsEmpty() {
        FileUpdateRequestDto fileUpdateRequestDto = getSampleFileUpdateRequestDto();
        fileUpdateRequestDto.setName("");
        assertFalse(fileUpdateRequestDto.isRequiredAvailable());
    }

    /**
     * This method creates sample FileUpdateRequestDto
     *
     * @return FileUpdateRequestDto
     */
    private FileUpdateRequestDto getSampleFileUpdateRequestDto() {
        FileUpdateRequestDto fileUpdateRequestDto = new FileUpdateRequestDto();
        fileUpdateRequestDto.setFileId("ID");
        fileUpdateRequestDto.setDescription("Description");
        fileUpdateRequestDto.setName("Name");
        return fileUpdateRequestDto;
    }
}