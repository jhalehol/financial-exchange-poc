package com.yellowpepper.challenge.financial.repository;

import com.yellowpepper.challenge.financial.model.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferRepository extends CrudRepository<Transfer, Long> {

    /**
     * Retrieves all transfers where the given account was involved
     * @param accountId Account identifier used to retrieve all movements
     * @param startDate Start date to retrieve all movements after
     * @param endDate End date to retrieve all movements before
     *
     * @return All required movements
     */
    @Query(nativeQuery = true, value = "SELECT * FROM fin_transfers " +
            "WHERE (sender_account_id=?1 OR recipient_account_id=?1) " +
            "AND transfer_date>=?2 AND transfer_date<=?3 " +
            "ORDER BY transfer_date ASC")
    Page<Transfer> getTransfersByAccount(long accountId, long startDate, long endDate, Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT COUNT(*) > 0 FROM fin_transfers " +
            "WHERE (sender_account_id=?1 OR recipient_account_id=?2) " +
            "AND transfer_date=?3")
    boolean existsByTransfersDetails(long senderAccount, long recipientAccount, long transferDate);
}
