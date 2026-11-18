package com.kupi.cc.auth.domain.entity;

import com.kupi.cc.auth.domain.request.DemoVideoRequestDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "demo_video")
public class DemoVideo implements Serializable {

    @Transient
    private static final String DEMO_VIDEO_ID_PREFIX = "dvid-";

    @Id
    private String id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "video_url", nullable = false)
    private String videoUrl;
    
    @Column(name = "active", nullable = false)
    private boolean active;
    
    @Column(name = "createdAt")
    private Date createdAt;
    
    @Column(name = "updatedAt")
    private Date updatedAt;

    public DemoVideo(DemoVideoRequestDto demoVideoRequestDto) {
        this.id = DEMO_VIDEO_ID_PREFIX + UUID.randomUUID();
        this.name = demoVideoRequestDto.getName();
        this.videoUrl = demoVideoRequestDto.getVideoUrl();
        this.active = demoVideoRequestDto.isActive();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
}

