package com.c0324.casestudym5.service;

import com.c0324.casestudym5.model.MultiFile;
import com.c0324.casestudym5.repository.MultiFileRepository;
import org.springframework.stereotype.Service;

@Service
public class MultiFileService {
    private final MultiFileRepository multiFileRepository;

    public MultiFileService(MultiFileRepository multiFileRepository) {
        this.multiFileRepository = multiFileRepository;
    }

    public MultiFile save(MultiFile multiFile) {
        return multiFileRepository.save(multiFile);
    }

    public MultiFile findById(Long id) {
        return multiFileRepository.findById(id).orElse(null);
    }
}
