package com.example.documentmanagement.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.example.documentmanagement.model.Department;
import com.example.documentmanagement.model.Document;
import com.example.documentmanagement.model.Employee;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final OpenFgaClient openFgaClient;

    /**
     * Check if an employee can read a specific document
     */
    public Mono<Boolean> canReadDocument(Employee employee, Document document) {
        // First check if OpenFGA is available by doing a simple fallback
        if (!isOpenFGAAvailable()) {
            log.warn("OpenFGA is not available, using fallback authorization for employee {} and document {}", 
                     employee.getId(), document.getId());
            return Mono.just(canReadDocumentFallback(employee, document));
        }

        return Mono.fromFuture(() -> {
            try {
                var checkRequest = new ClientCheckRequest()
                        .user("employee:" + employee.getId())
                        .relation("reader")
                        ._object("document:" + document.getId());

                return openFgaClient.check(checkRequest)
                        .thenApply(response -> response.getAllowed())
                        .exceptionally(ex -> {
                            log.error("OpenFGA check failed for employee {} and document {}, using fallback", 
                                     employee.getId(), document.getId(), ex);
                            return canReadDocumentFallback(employee, document);
                        });
            } catch (Exception e) {
                log.error("Error creating OpenFGA check request for employee {} and document {}", 
                         employee.getId(), document.getId(), e);
                return CompletableFuture.completedFuture(canReadDocumentFallback(employee, document));
            }
        }).onErrorReturn(canReadDocumentFallback(employee, document));
    }

    /**
     * Fallback authorization when OpenFGA is not available
     */
    private boolean canReadDocumentFallback(Employee employee, Document document) {
        // Owner can always read their documents
        if (document.getOwner().getId().equals(employee.getId())) {
            return true;
        }
        
        // HR can read all documents
        if (isHREmployee(employee)) {
            return true;
        }
        
        // Same department members can read department documents
        if (document.getDepartment().getId().equals(employee.getDepartment().getId())) {
            return true;
        }
        
        // Manager can read subordinate documents
        if (employee.getManagedUsers() != null && 
            employee.getManagedUsers().stream().anyMatch(sub -> sub.getId().equals(document.getOwner().getId()))) {
            return true;
        }
        
        return false;
    }

    /**
     * Simple check to see if OpenFGA is available
     */
    private boolean isOpenFGAAvailable() {
        // This is a simple heuristic - in production you might want a proper health check
        return openFgaClient != null;
    }

    /**
     * Create relationship tuples when an employee is created
     */
    public Mono<Void> createEmployeeTuples(Employee employee) {
        return Mono.fromFuture(() -> {
            try {
                var tuples = new ArrayList<ClientTupleKey>();
                
                // Employee belongs to department
                tuples.add(new ClientTupleKey()
                        .user("employee:" + employee.getId())
                        .relation("department")
                        ._object("department:" + employee.getDepartment().getId()));
                
                // Department has member
                tuples.add(new ClientTupleKey()
                        .user("employee:" + employee.getId())
                        .relation("member")
                        ._object("department:" + employee.getDepartment().getId()));

                // Add manager relationship if exists
                if (employee.getManager() != null) {
                    tuples.add(new ClientTupleKey()
                            .user("employee:" + employee.getManager().getId())
                            .relation("manager")
                            ._object("employee:" + employee.getId()));
                }

                var writeRequest = new ClientWriteRequest()
                        .writes(tuples);

                return openFgaClient.write(writeRequest)
                        .thenApply(response -> {
                            log.info("Created tuples for employee: {}", employee.getId());
                            return null;
                        });
            } catch (Exception e) {
                log.error("Error creating employee tuples for employee {}", employee.getId(), e);
                return CompletableFuture.completedFuture(null);
            }
        }).then();
    }

    /**
     * Create relationship tuples when a document is created
     */
    public Mono<Void> createDocumentTuples(Document document) {
        return Mono.fromFuture(() -> {
            try {
                var tuples = new ArrayList<ClientTupleKey>();
                
                // Document owner
                tuples.add(new ClientTupleKey()
                        .user("employee:" + document.getOwner().getId())
                        .relation("owner")
                        ._object("document:" + document.getId()));
                
                // Document belongs to department
                tuples.add(new ClientTupleKey()
                        .user("department:" + document.getDepartment().getId())
                        .relation("department")
                        ._object("document:" + document.getId()));
                
                // HR department has special access to all documents
                tuples.add(new ClientTupleKey()
                        .user("department:hr")
                        .relation("reader")
                        ._object("document:" + document.getId()));

                var writeRequest = new ClientWriteRequest()
                        .writes(tuples);

                return openFgaClient.write(writeRequest)
                        .thenApply(response -> {
                            log.info("Created tuples for document: {}", document.getId());
                            return null;
                        });
            } catch (Exception e) {
                log.error("Error creating document tuples for document {}", document.getId(), e);
                return CompletableFuture.completedFuture(null);
            }
        }).then();
    }

    /**
     * Check if employee is in HR department (has special access)
     */
    public boolean isHREmployee(Employee employee) {
        return employee.getDepartment() != null && 
               "HR".equalsIgnoreCase(employee.getDepartment().getName());
    }

    /**
     * Get employee identifier for OpenFGA
     */
    public String getEmployeeIdentifier(Employee employee) {
        return "employee:" + employee.getId();
    }

    /**
     * Get document identifier for OpenFGA
     */
    public String getDocumentIdentifier(Document document) {
        return "document:" + document.getId();
    }

    /**
     * Get department identifier for OpenFGA
     */
    public String getDepartmentIdentifier(Department department) {
        return "department:" + department.getId();
    }
} 