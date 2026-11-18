package com.kupi.cc.auth.domain.response;

import com.kupi.cc.auth.domain.entity.DemoVideo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Demo Video Response DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class DemoVideoResponseDto implements ResponseDto {

    private String id;
    private String name;
    private String videoUrl;
    private boolean active;

    public DemoVideoResponseDto(DemoVideo demoVideo) {
        if (demoVideo != null) {
            this.id = demoVideo.getId();
            this.name = demoVideo.getName();
            this.videoUrl = demoVideo.getVideoUrl();
            this.active = demoVideo.isActive();
        }
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}

