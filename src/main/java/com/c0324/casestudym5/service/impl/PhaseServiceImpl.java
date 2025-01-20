package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.model.Phase;
import com.c0324.casestudym5.model.Topic;
import com.c0324.casestudym5.repository.PhaseRepository;
import com.c0324.casestudym5.service.PhaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhaseServiceImpl implements PhaseService {

    private final PhaseRepository phaseRepository;

    @Autowired
    public PhaseServiceImpl(PhaseRepository phaseRepository) {
        this.phaseRepository = phaseRepository;
    }

    @Override
    public List<Phase> findPhasesByTopic(Topic topic) {
        return phaseRepository.findPhaseByTopicOrderByIdAsc(topic);
    }
}
