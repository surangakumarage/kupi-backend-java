package com.kupi.cc.base.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.io.Serializable;

@MappedSuperclass
@Getter
@Setter
public abstract class Auditable implements Serializable {
    @Column(nullable = false)
    protected String createdBy;

    @Column(nullable = false)
    protected long createdDate;

    @Column(nullable = false)
    protected String lastModifiedBy;

    @Column(nullable = false)
    protected long lastModifiedDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = System.currentTimeMillis();
        this.lastModifiedDate = createdDate;
    }

    @PreUpdate
    public void preUpdate() {
        this.lastModifiedDate = System.currentTimeMillis();
    }

    public void persistAuditor(String userId) {
        this.createdBy = userId;
        this.lastModifiedBy = userId;
    }

    public void updateAuditor(String userId) {
        this.lastModifiedBy = userId;
    }
}
