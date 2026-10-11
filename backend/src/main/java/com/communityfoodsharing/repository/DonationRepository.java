package com.communityfoodsharing.repository;

import com.communityfoodsharing.entity.FoodDonation;
import com.communityfoodsharing.enums.DonationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<FoodDonation, Long> {
    List<FoodDonation> findByDonorIdOrderByCreatedAtDesc(Long donorId);
    Optional<FoodDonation> findByIdAndDonorId(Long id, Long donorId);
    List<FoodDonation> findByStatusOrderByCreatedAtDesc(DonationStatus status);
    List<FoodDonation> findByVolunteerIdOrderByUpdatedAtDesc(Long volunteerId);
    long countByDonorId(Long donorId);
    long countByDonorIdAndStatus(Long donorId, DonationStatus status);
    long countByVolunteerIdAndStatus(Long volunteerId, DonationStatus status);
    long countByStatus(DonationStatus status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from FoodDonation d where d.id = :id")
    Optional<FoodDonation> findLockedById(@Param("id") Long id);
}