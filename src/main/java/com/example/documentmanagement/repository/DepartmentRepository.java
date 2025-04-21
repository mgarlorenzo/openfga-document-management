package com.example.documentmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.documentmanagement.model.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Department findByName(String name);
} 