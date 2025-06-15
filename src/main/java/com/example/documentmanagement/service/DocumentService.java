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
    private final AuthorizationService authorizationService;

    @Transactional
    public Mono<Document> createDocument(Document document, Employee employee) {
        log.info("Creating document: {} for employee: {}", document.getTitle(), employee.getName());
        document.setOwner(employee);
        document.setDepartment(employee.getDepartment());
        Document savedDocument = documentRepository.save(document);
        log.info("Document created successfully with ID: {}", savedDocument.getId());
        
        // Create OpenFGA tuples for the new document
        return authorizationService.createDocumentTuples(savedDocument)
                .thenReturn(savedDocument)
                .doOnSuccess(doc -> log.info("OpenFGA tuples created for document: {}", doc.getId()))
                .doOnError(error -> log.error("Failed to create OpenFGA tuples for document: {}", savedDocument.getId(), error));
    }

    public Flux<Document> getAccessibleDocuments(Employee employee) {
        log.info("Getting accessible documents for employee: {}", employee.getName());
        List<Document> allDocuments = documentRepository.findAll();
        log.info("Found {} documents in total, filtering by permissions...", allDocuments.size());
        
        return Flux.fromIterable(allDocuments)
                .flatMap(document -> 
                    authorizationService.canReadDocument(employee, document)
                            .onErrorReturn(false) // If OpenFGA fails, deny access
                            .map(canRead -> canRead ? document : null)
                )
                .filter(doc -> doc != null) // Filter out null values
                .doOnNext(doc -> log.info("Employee {} has access to document: {} (ID: {})", 
                         employee.getName(), doc.getTitle(), doc.getId()))
                .doOnComplete(() -> log.info("Finished filtering documents for employee: {}", employee.getName()))
                .onErrorResume(error -> {
                    log.error("Error filtering documents for employee: {}", employee.getName(), error);
                    // Fallback: return documents based on simple rules
                    return getDocumentsFallback(employee, allDocuments);
                });
    }

    private Flux<Document> getDocumentsFallback(Employee employee, List<Document> allDocuments) {
        log.warn("Using fallback authorization rules for employee: {}", employee.getName());
        
        return Flux.fromIterable(allDocuments)
                .filter(document -> {
                    // Simple fallback rules when OpenFGA is not available
                    boolean isOwner = document.getOwner().getId().equals(employee.getId());
                    boolean isSameDepartment = document.getDepartment().getId().equals(employee.getDepartment().getId());
                    boolean isHR = "HR".equalsIgnoreCase(employee.getDepartment().getName());
                    
                    return isOwner || isSameDepartment || isHR;
                })
                .doOnNext(doc -> log.info("Fallback: Employee {} has access to document: {} (ID: {})", 
                         employee.getName(), doc.getTitle(), doc.getId()));
    }

    public Mono<Document> getDocumentById(Long id, Employee employee) {
        log.info("Getting document by ID: {} for employee: {}", id, employee.getName());
        return Mono.justOrEmpty(documentRepository.findById(id))
                .flatMap(document -> 
                    authorizationService.canReadDocument(employee, document)
                            .map(canRead -> canRead ? document : null)
                )
                .doOnSuccess(doc -> {
                    if (doc != null) {
                        log.info("Employee {} has access to document: {} (ID: {})", employee.getName(), doc.getTitle(), doc.getId());
                    } else {
                        log.warn("Employee {} does not have access to document with ID: {}", employee.getName(), id);
                    }
                });
    }

    public Mono<Document> getDocumentById(Long id) {
        log.info("Getting document by ID without authorization check: {}", id);
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