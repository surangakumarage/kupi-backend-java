package com.kupi.cc.auth.service;

import com.kupi.cc.auth.domain.entity.DemoVideo;
import com.kupi.cc.auth.domain.request.DemoVideoRequestDto;
import com.kupi.cc.auth.domain.response.DemoVideoResponseDto;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.repository.DemoVideoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Demo Video Service
 */
@Slf4j
@Service
public class DemoVideoService {

    private final DemoVideoRepository demoVideoRepository;

    public DemoVideoService(DemoVideoRepository demoVideoRepository) {
        this.demoVideoRepository = demoVideoRepository;
    }

    /**
     * This method creates a demo video
     * If the new video is set to active, all other videos will be deactivated
     *
     * @param demoVideoRequestDto demoVideoRequestDto
     * @return created demo video
     */
    @Transactional
    public DemoVideoResponseDto createDemoVideo(DemoVideoRequestDto demoVideoRequestDto) {
        try {
            if (demoVideoRequestDto.isActive()) {
                demoVideoRepository.deactivateAll();
            }
            DemoVideo demoVideo = new DemoVideo(demoVideoRequestDto);
            DemoVideo saved = demoVideoRepository.save(demoVideo);
            log.debug("Successfully created demo video: {}", saved.getId());
            return new DemoVideoResponseDto(saved);
        } catch (DataAccessException e) {
            log.error("Creating demo video failed", e);
            throw new AuthServiceException("Failed to create demo video", e);
        }
    }

    /**
     * This method returns all demo videos
     *
     * @return list of demo videos
     */
    public List<DemoVideoResponseDto> getAllDemoVideos() {
        try {
            List<DemoVideo> demoVideos = demoVideoRepository.findAllByOrderByCreatedAtDesc();
            return demoVideos.stream()
                    .map(DemoVideoResponseDto::new)
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            log.error("Getting demo videos failed", e);
            throw new AuthServiceException("Failed to get demo videos", e);
        }
    }

    /**
     * This method returns the active demo video
     *
     * @return active demo video
     */
    public Optional<DemoVideoResponseDto> getActiveDemoVideo() {
        try {
            Optional<DemoVideo> activeVideo = demoVideoRepository.findByActiveTrue();
            return activeVideo.map(DemoVideoResponseDto::new);
        } catch (DataAccessException e) {
            log.error("Getting active demo video failed", e);
            throw new AuthServiceException("Failed to get active demo video", e);
        }
    }
}

