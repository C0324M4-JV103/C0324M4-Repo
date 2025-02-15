package com.c0324.casestudym5.repository;

import com.c0324.casestudym5.model.Phase;
import com.c0324.casestudym5.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhaseRepository extends JpaRepository<Phase, Long> {
    List<Phase> findPhaseByTopicOrderByIdAsc(Topic topic);

    List<Phase> findByTopicIdAndPhaseNumber(Long topicId, int phaseNumber);
}
