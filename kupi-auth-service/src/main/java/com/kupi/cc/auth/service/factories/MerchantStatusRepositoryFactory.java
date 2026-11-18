package com.kupi.cc.auth.service.factories;

import com.kupi.cc.auth.enums.ApprovalStatus;
import com.kupi.cc.auth.exception.InvalidActionException;
import com.kupi.cc.auth.repository.ApprovedBusinessRepository;
import com.kupi.cc.auth.repository.BlockedMerchantRepository;
import com.kupi.cc.auth.repository.RejectedProfileUpdatesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class MerchantStatusRepositoryFactory {

    private final RejectedProfileUpdatesRepository rejectedProfileUpdatesRepository;
    private final ApprovedBusinessRepository approvedBusinessRepository;
    private final BlockedMerchantRepository blockedMerchantRepository;

    @Autowired
    public MerchantStatusRepositoryFactory(RejectedProfileUpdatesRepository rejectedProfileUpdatesRepository,
                                           ApprovedBusinessRepository approvedBusinessRepository,
                                           BlockedMerchantRepository blockedMerchantRepository) {
        this.rejectedProfileUpdatesRepository = rejectedProfileUpdatesRepository;
        this.approvedBusinessRepository = approvedBusinessRepository;
        this.blockedMerchantRepository = blockedMerchantRepository;
    }

    public JpaRepository getRepository(ApprovalStatus approvalStatus) {
        switch (approvalStatus) {
            case APPROVED:
                return approvedBusinessRepository;
            case REJECTED:
                return rejectedProfileUpdatesRepository;
            case BLOCKED:
                return blockedMerchantRepository;
            default:
                throw new InvalidActionException("Unsupported approval status: " + approvalStatus);
        }
    }
}
