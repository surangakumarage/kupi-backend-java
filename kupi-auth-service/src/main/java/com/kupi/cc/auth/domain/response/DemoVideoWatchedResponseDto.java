package com.kupi.cc.auth.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Demo Video Watched Response DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DemoVideoWatchedResponseDto implements ResponseDto {

    private boolean demoVideoWatched;

    @Override
    public String toLogJson() {
        return toJson();
    }
}

