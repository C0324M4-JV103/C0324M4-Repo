package com.c0324.casestudym5.service;

import com.c0324.casestudym5.model.Document;
import com.c0324.casestudym5.model.MultiFile;
import com.c0324.casestudym5.repository.DocumentRepository;
import com.c0324.casestudym5.repository.FacultyRepository;
import com.c0324.casestudym5.repository.MultiFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private MultiFileRepository multiFileRepository;

    public Page<Document> getDocumentsPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return documentRepository.findAll(pageable);
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public void saveDocument(Document document, String fileUrl) {
        MultiFile file = new MultiFile();
        file.setUrl(fileUrl);
        multiFileRepository.save(file);

        document.setFileUrl(file);
        documentRepository.save(document);
    }
}

