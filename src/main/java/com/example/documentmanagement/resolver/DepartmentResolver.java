package com.example.documentmanagement.resolver;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.example.documentmanagement.model.CreateDepartmentInput;
import com.example.documentmanagement.model.Department;
import com.example.documentmanagement.repository.DepartmentRepository;

@Controller
public class DepartmentResolver {
    private final DepartmentRepository departmentRepository;

    public DepartmentResolver(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @QueryMapping
    public Department department(@Argument Long id) {
        return departmentRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Department> departments() {
        return departmentRepository.findAll();
    }

    @MutationMapping
    public Department createDepartment(@Argument CreateDepartmentInput input) {
        Department department = new Department();
        department.setName(input.name());
        department.setDescription(input.description());
        department.setLocation(input.location());
        return departmentRepository.save(department);
    }
} 