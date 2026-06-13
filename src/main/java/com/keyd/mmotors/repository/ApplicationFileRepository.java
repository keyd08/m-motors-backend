package com.keyd.mmotors.repository;

import com.keyd.mmotors.entity.ApplicationFile;
import com.keyd.mmotors.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationFileRepository extends JpaRepository<ApplicationFile, Long> {

    List<ApplicationFile> findByClientId(Long clientId);

    List<ApplicationFile> findByStatus(ApplicationStatus status);

    Optional<ApplicationFile> findByIdAndClientId(Long id, Long clientId);
}
