package com.c0324.casestudym5.repository;

import com.c0324.casestudym5.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
