package com.example.documentmanagement.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.documentmanagement.model.Document;
import com.example.documentmanagement.model.Employee;
import com.example.documentmanagement.repository.DocumentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    @Transactional
    public Mono<Document> createDocument(Document document, Employee employee) {
        log.info("Creating document: {} for employee: {}", document.getTitle(), employee.getName());
        document.setOwner(employee);
        document.setDepartment(employee.getDepartment());
        Document savedDocument = documentRepository.save(document);
        log.info("Document created successfully with ID: {}", savedDocument.getId());
        return Mono.just(savedDocument);
    }

    public Flux<Document> getAccessibleDocuments(Employee employee) {
        log.info("Getting accessible documents for employee: {}", employee.getName());
        List<Document> documents = documentRepository.findAll();
        log.info("Found {} documents in total", documents.size());
        return Flux.fromIterable(documents)
                .doOnNext(doc -> log.info("Document found: {} (ID: {})", doc.getTitle(), doc.getId()));
    }

    public Mono<Document> getDocumentById(Long id) {
        log.info("Getting document by ID: {}", id);
        return Mono.justOrEmpty(documentRepository.findById(id))
                .doOnSuccess(doc -> {
                    if (doc != null) {
                        log.info("Found document: {} (ID: {})", doc.getTitle(), doc.getId());
                    } else {
                        log.warn("No document found with ID: {}", id);
                    }
                });
    }

    public Mono<Void> deleteDocument(Long id) {
        log.info("Deleting document with ID: {}", id);
        documentRepository.deleteById(id);
        log.info("Document deleted successfully");
        return Mono.empty();
    }
} 