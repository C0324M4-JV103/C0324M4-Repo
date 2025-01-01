package com.c0324.casestudym5.service;

import com.c0324.casestudym5.model.MultiFile;
import com.c0324.casestudym5.repository.MultiFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MultiFileService {
    @Autowired
    private MultiFileRepository multiFileRepository;

    public MultiFile save(MultiFile multiFile) {
        return multiFileRepository.save(multiFile);
    }

    public MultiFile findById(Long id) {
        return multiFileRepository.findById(id).orElse(null);
    }
}
