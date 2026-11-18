package com.kupi.cc.auth.service;

import com.kupi.cc.auth.domain.entity.DemoVideo;
import com.kupi.cc.auth.domain.request.DemoVideoRequestDto;
import com.kupi.cc.auth.domain.response.DemoVideoResponseDto;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.repository.DemoVideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests the {@link DemoVideoService} class.
 */
class DemoVideoServiceTest {

    private static final String DEMO_VIDEO_ID = "dvid-1234567890";
    private static final String DEMO_VIDEO_NAME = "Introduction Video";
    private static final String DEMO_VIDEO_URL = "https://example.com/video.mp4";
    private static final String FAILED = "failed";

    private DemoVideoService demoVideoService;

    @Mock
    private DemoVideoRepository demoVideoRepository;

    @BeforeEach
    void setUp() {
        initMocks(this);
        demoVideoService = new DemoVideoService(demoVideoRepository);
    }

    /**
     * Create demo video
     */
    @Test
    void Should_ReturnDemoVideoResponseDto_When_CreatingDemoVideoIsSuccessful() {
        DemoVideoRequestDto requestDto = getSampleDemoVideoRequestDto(false);
        DemoVideo savedVideo = getSampleDemoVideo();

        when(demoVideoRepository.save(any(DemoVideo.class))).thenReturn(savedVideo);

        DemoVideoResponseDto response = demoVideoService.createDemoVideo(requestDto);

        verify(demoVideoRepository, never()).deactivateAll();
        verify(demoVideoRepository).save(any(DemoVideo.class));
        assertNotNull(response);
        assertEquals(DEMO_VIDEO_ID, response.getId());
        assertEquals(DEMO_VIDEO_NAME, response.getName());
        assertEquals(DEMO_VIDEO_URL, response.getVideoUrl());
        assertFalse(response.isActive());
    }

    @Test
    void Should_DeactivateAllVideos_When_CreatingActiveDemoVideo() {
        DemoVideoRequestDto requestDto = getSampleDemoVideoRequestDto(true);
        DemoVideo savedVideo = getSampleDemoVideo();
        savedVideo.setActive(true);

        doNothing().when(demoVideoRepository).deactivateAll();
        when(demoVideoRepository.save(any(DemoVideo.class))).thenReturn(savedVideo);

        DemoVideoResponseDto response = demoVideoService.createDemoVideo(requestDto);

        verify(demoVideoRepository).deactivateAll();
        verify(demoVideoRepository).save(any(DemoVideo.class));
        assertNotNull(response);
        assertTrue(response.isActive());
    }

    @Test
    void Should_ThrowAuthServiceException_When_SavingDemoVideoIsFailed() {
        DemoVideoRequestDto requestDto = getSampleDemoVideoRequestDto(false);
        when(demoVideoRepository.save(any(DemoVideo.class)))
                .thenThrow(new DataAccessException(FAILED) {
                });

        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                demoVideoService.createDemoVideo(requestDto));

        assertEquals("Failed to create demo video", exception.getMessage());
        verify(demoVideoRepository).save(any(DemoVideo.class));
    }

    @Test
    void Should_ThrowAuthServiceException_When_DeactivatingAllVideosIsFailed() {
        DemoVideoRequestDto requestDto = getSampleDemoVideoRequestDto(true);
        doThrow(new DataAccessException(FAILED) {
        }).when(demoVideoRepository).deactivateAll();

        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                demoVideoService.createDemoVideo(requestDto));

        assertEquals("Failed to create demo video", exception.getMessage());
        verify(demoVideoRepository).deactivateAll();
        verify(demoVideoRepository, never()).save(any(DemoVideo.class));
    }

    /**
     * Get all demo videos
     */
    @Test
    void Should_ReturnListOfDemoVideos_When_GettingAllDemoVideosIsSuccessful() {
        List<DemoVideo> demoVideos = getSampleDemoVideoList();
        when(demoVideoRepository.findAllByOrderByCreatedAtDesc()).thenReturn(demoVideos);

        List<DemoVideoResponseDto> response = demoVideoService.getAllDemoVideos();

        verify(demoVideoRepository).findAllByOrderByCreatedAtDesc();
        assertNotNull(response);
        assertEquals(demoVideos.size(), response.size());
        assertEquals(DEMO_VIDEO_ID, response.get(0).getId());
    }

    @Test
    void Should_ReturnEmptyList_When_NoDemoVideosFound() {
        List<DemoVideo> emptyList = new ArrayList<>();
        when(demoVideoRepository.findAllByOrderByCreatedAtDesc()).thenReturn(emptyList);

        List<DemoVideoResponseDto> response = demoVideoService.getAllDemoVideos();

        verify(demoVideoRepository).findAllByOrderByCreatedAtDesc();
        assertNotNull(response);
        assertEquals(0, response.size());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GettingAllDemoVideosIsFailed() {
        when(demoVideoRepository.findAllByOrderByCreatedAtDesc())
                .thenThrow(new DataAccessException(FAILED) {
                });

        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                demoVideoService.getAllDemoVideos());

        assertEquals("Failed to get demo videos", exception.getMessage());
        verify(demoVideoRepository).findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get active demo video
     */
    @Test
    void Should_ReturnActiveDemoVideo_When_GettingActiveDemoVideoIsSuccessful() {
        DemoVideo activeVideo = getSampleDemoVideo();
        activeVideo.setActive(true);
        when(demoVideoRepository.findByActiveTrue()).thenReturn(Optional.of(activeVideo));

        Optional<DemoVideoResponseDto> response = demoVideoService.getActiveDemoVideo();

        verify(demoVideoRepository).findByActiveTrue();
        assertTrue(response.isPresent());
        assertEquals(DEMO_VIDEO_ID, response.get().getId());
        assertTrue(response.get().isActive());
    }

    @Test
    void Should_ReturnEmptyOptional_When_NoActiveDemoVideoFound() {
        when(demoVideoRepository.findByActiveTrue()).thenReturn(Optional.empty());

        Optional<DemoVideoResponseDto> response = demoVideoService.getActiveDemoVideo();

        verify(demoVideoRepository).findByActiveTrue();
        assertFalse(response.isPresent());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GettingActiveDemoVideoIsFailed() {
        when(demoVideoRepository.findByActiveTrue())
                .thenThrow(new DataAccessException(FAILED) {
                });

        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                demoVideoService.getActiveDemoVideo());

        assertEquals("Failed to get active demo video", exception.getMessage());
        verify(demoVideoRepository).findByActiveTrue();
    }

    /**
     * Helper methods
     */
    private DemoVideoRequestDto getSampleDemoVideoRequestDto(boolean active) {
        DemoVideoRequestDto requestDto = new DemoVideoRequestDto();
        requestDto.setName(DEMO_VIDEO_NAME);
        requestDto.setVideoUrl(DEMO_VIDEO_URL);
        requestDto.setActive(active);
        return requestDto;
    }

    private DemoVideo getSampleDemoVideo() {
        DemoVideoRequestDto requestDto = getSampleDemoVideoRequestDto(false);
        DemoVideo demoVideo = new DemoVideo(requestDto);
        demoVideo.setId(DEMO_VIDEO_ID);
        return demoVideo;
    }

    private List<DemoVideo> getSampleDemoVideoList() {
        List<DemoVideo> videos = new ArrayList<>();
        DemoVideo video1 = getSampleDemoVideo();
        videos.add(video1);
        DemoVideoRequestDto requestDto2 = getSampleDemoVideoRequestDto(true);
        DemoVideo video2 = new DemoVideo(requestDto2);
        video2.setId("dvid-0987654321");
        videos.add(video2);
        return videos;
    }
}

