package com.c0324.casestudym5.service;

import com.c0324.casestudym5.model.Document;
import com.c0324.casestudym5.model.MultiFile;
import com.c0324.casestudym5.model.Teacher;
import com.c0324.casestudym5.repository.DocumentRepository;
import com.c0324.casestudym5.repository.MultiFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    private final MultiFileRepository multiFileRepository;

    @Autowired
    public DocumentService(DocumentRepository documentRepository, MultiFileRepository multiFileRepository) {
        this.documentRepository = documentRepository;
        this.multiFileRepository = multiFileRepository;
    }

    public Page<Document> getDocumentsPage(int page, int size, Teacher teacher) {
        Pageable pageable = PageRequest.of(page, size);
        return documentRepository.findByTeacherId(pageable, teacher.getId());
    }


    public void saveDocument(Document document, String fileUrl) {
        MultiFile file = new MultiFile();
        file.setUrl(fileUrl);
        multiFileRepository.save(file);

        document.setFileUrl(file);
        documentRepository.save(document);
    }

}

