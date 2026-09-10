package com.checkpoint.repository;

import com.checkpoint.model.Checkpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CheckpointRepository extends JpaRepository<Checkpoint,Long> {
    Optional<Checkpoint> findFirstByOrderByIdDesc();
}
