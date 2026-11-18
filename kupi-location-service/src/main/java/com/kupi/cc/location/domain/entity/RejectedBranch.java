package com.kupi.cc.location.domain.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

/**
 * Rejected branch
 */
@Getter
@Setter
@Entity
@Table(name = "rejected_branch")
@NoArgsConstructor
public class RejectedBranch {

    @Id
    private String id;
    @OneToOne
    @JoinColumn
    private Branch branch;
    private String comment;
    private long rejectedOn;

    public RejectedBranch(Branch branch, String comment) {
        this.id = branch.getId();
        this.branch = branch;
        this.comment = comment;
        this.rejectedOn = branch.getUpdatedAt();
    }
}
