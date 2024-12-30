package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.model.Clazz;
import com.c0324.casestudym5.repository.ClazzRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClazzService {

    @Autowired
    private ClazzRepository clazzRepository;

    public List<Clazz> getAllClazzes() {
        return clazzRepository.findAll();
    }

    public Clazz saveClazz(Clazz clazz) {
        return clazzRepository.save(clazz);
    }
}
