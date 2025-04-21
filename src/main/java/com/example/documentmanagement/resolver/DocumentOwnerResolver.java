package com.example.documentmanagement.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import com.example.documentmanagement.model.Employee;
import com.example.documentmanagement.repository.EmployeeRepository;

@Controller
public class DocumentOwnerResolver {
    private final EmployeeRepository employeeRepository;

    @Autowired
    public DocumentOwnerResolver(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @SchemaMapping(typeName = "Employee")
    public Employee manager(Employee employee) {
        if (employee.getManager() != null) {
            return employee.getManager();
        }
        return null;
    }
} 