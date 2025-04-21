package com.example.documentmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.documentmanagement.model.Department;
import com.example.documentmanagement.model.Document;
import com.example.documentmanagement.model.Employee;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByOwner(Employee owner);
    List<Document> findByDepartment(Department department);
    List<Document> findByDepartmentId(Long departmentId);
} 