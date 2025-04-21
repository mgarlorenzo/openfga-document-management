package com.example.documentmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.documentmanagement.model.Department;
import com.example.documentmanagement.model.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByUsername(String username);
    List<Employee> findByDepartmentId(Long departmentId);
    List<Employee> findByManagerId(Long managerId);
    List<Employee> findByDepartment(Department department);
    List<Employee> findByManager(Employee manager);
} 