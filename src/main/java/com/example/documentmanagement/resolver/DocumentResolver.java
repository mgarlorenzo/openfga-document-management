package com.example.documentmanagement.resolver;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import com.example.documentmanagement.model.Document;
import com.example.documentmanagement.model.Employee;
import com.example.documentmanagement.model.CreateDocumentInput;
import com.example.documentmanagement.repository.EmployeeRepository;
import com.example.documentmanagement.service.DocumentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DocumentResolver {

    private final DocumentService documentService;
    private final EmployeeRepository employeeRepository;

    @QueryMapping
    public Flux<Document> documents() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Authentication: {}", authentication);
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authentication found or user not authenticated");
            return Flux.empty();
        }

        String username = authentication.getName();
        log.info("Username: {}", username);
        
        Employee employee = employeeRepository.findByUsername(username)
                .orElse(null);
                
        if (employee == null) {
            log.warn("No employee found for username: {}", username);
            return Flux.empty();
        }

        log.info("Current employee: {} {}", employee.getName(), employee.getSurname());
        return documentService.getAccessibleDocuments(employee)
                .doOnNext(doc -> log.info("Found document: {}", doc.getTitle()))
                .doOnComplete(() -> log.info("Finished fetching documents"));
    }

    @QueryMapping
    public Mono<Document> document(@Argument Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Fetching document with ID: {}", id);
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authentication found or user not authenticated");
            return Mono.empty();
        }

        String username = authentication.getName();
        Employee employee = employeeRepository.findByUsername(username).orElse(null);
        
        if (employee == null) {
            log.warn("No employee found for username: {}", username);
            return Mono.empty();
        }

        return documentService.getDocumentById(id, employee)
                .doOnSuccess(doc -> {
                    if (doc != null) {
                        log.info("Employee {} accessed document: {}", employee.getName(), doc.getTitle());
                    } else {
                        log.warn("Employee {} denied access to document with ID: {}", employee.getName(), id);
                    }
                });
    }

    @MutationMapping
    public Mono<Document> createDocument(@Argument CreateDocumentInput input, @Argument Long employeeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Creating document {} for employee {}", input.title(), employeeId);

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authentication found or user not authenticated for createDocument");
            return Mono.empty();
        }

        String username = authentication.getName();
        return Mono.fromSupplier(() -> employeeRepository.findByUsername(username).orElse(null))
            .flatMap(employee -> {
                if (employee == null) {
                    log.warn("No employee found for username: {}", username);
                    return Mono.empty();
                }

                if (!employee.getId().equals(employeeId)) {
                    log.warn("Authenticated employee ID {} does not match provided employeeId {}", employee.getId(), employeeId);
                    return Mono.empty();
                }

                Document document = new Document();
                document.setTitle(input.title());
                document.setContent(input.content());
                document.setClassificationLevel(input.classificationLevel());
                return documentService.createDocument(document, employee);
            })
            .doOnSuccess(doc -> {
                if (doc != null) {
                    log.info("Created document: {}", doc.getTitle());
                }
            });
    }

    @MutationMapping
    public Mono<Document> updateDocument(@Argument Long id, @Argument String title, @Argument String content) {
        log.info("Updating document with ID: {}", id);
        return documentService.getDocumentById(id)
                .flatMap(document -> {
                    document.setTitle(title);
                    document.setContent(content);
                    return documentService.createDocument(document, document.getOwner());
                })
                .doOnSuccess(doc -> {
                    if (doc != null) {
                        log.info("Updated document: {}", doc.getTitle());
                    } else {
                        log.warn("No document found with ID: {}", id);
                    }
                });
    }

    @MutationMapping
    public Mono<Boolean> deleteDocument(@Argument Long id) {
        log.info("Deleting document with ID: {}", id);
        return documentService.deleteDocument(id)
                .thenReturn(true)
                .onErrorReturn(false)
                .doOnSuccess(result -> log.info("Delete operation result: {}", result));
    }
} 