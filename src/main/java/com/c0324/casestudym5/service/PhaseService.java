package com.c0324.casestudym5.service;

import com.c0324.casestudym5.model.Phase;
import com.c0324.casestudym5.model.Topic;

import java.util.List;

public interface PhaseService {
    void createPhasesForTopic(Topic topic);
    List<Phase> findPhasesByTopic(Topic topic);
}
