package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.model.Phase;
import com.c0324.casestudym5.model.Topic;
import com.c0324.casestudym5.repository.PhaseRepository;
import com.c0324.casestudym5.service.PhaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PhaseServiceImpl implements PhaseService {

    private final PhaseRepository phaseRepository;

    @Autowired
    public PhaseServiceImpl(PhaseRepository phaseRepository) {
        this.phaseRepository = phaseRepository;
    }

    @Override
    public void createPhasesForTopic(Topic topic) {
        LocalDate approvedDate = LocalDate.now();
        for (int i = 1; i <= 4; i++) {
            int status = (i == 1) ? 1 : 0;
            Phase phase = Phase.builder()
                    .topic(topic)
                    .phaseNumber(i)
                    .status(status)
                    .phaseProgressPercent(0)
                    .build();
            phase.setPhaseDates(approvedDate);
            phaseRepository.save(phase);
        }
    }

    @Override
    public List<Phase> findPhasesByTopic(Topic topic) {
        return phaseRepository.findPhaseByTopic(topic);
    }
}
