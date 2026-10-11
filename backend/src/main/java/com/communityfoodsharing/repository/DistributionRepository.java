package com.communityfoodsharing.repository;

import com.communityfoodsharing.entity.Distribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface DistributionRepository extends JpaRepository<Distribution, Long> {
    Optional<Distribution> findByDonationId(Long donationId);
    @Query("select coalesce(sum(d.peopleServed), 0) from Distribution d")
    long totalPeopleServed();
    @Query("select coalesce(sum(d.peopleServed), 0) from Distribution d where d.donation.volunteer.id = :volunteerId")
    long peopleServedByVolunteer(@Param("volunteerId") Long volunteerId);
}