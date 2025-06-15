package com.example.documentmanagement.resolver;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.example.documentmanagement.model.CreateUserInput;
import com.example.documentmanagement.model.Department;
import com.example.documentmanagement.model.Employee;
import com.example.documentmanagement.repository.DepartmentRepository;
import com.example.documentmanagement.repository.EmployeeRepository;
import com.example.documentmanagement.service.AuthorizationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class EmployeeResolver {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AuthorizationService authorizationService;

    public EmployeeResolver(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository, AuthorizationService authorizationService) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.authorizationService = authorizationService;
    }

    @QueryMapping
    public Employee employee(@Argument Long id) {
        return employeeRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Employee> employees() {
        return employeeRepository.findAll();
    }

    @MutationMapping
    public Employee createEmployee(@Argument CreateUserInput input) {
        log.info("Creating employee: {} {}", input.name(), input.surname());
        Employee employee = new Employee();
        employee.setNationalIdType(input.nationalIdType());
        employee.setNationalIdNumber(input.nationalIdNumber());
        employee.setIssuingCountry(input.issuingCountry());
        employee.setName(input.name());
        employee.setSurname(input.surname());
        employee.setAddress(input.address());
        employee.setUsername(input.username());
        employee.setEmail(input.email());
        employee.setLocation(input.location());
        
        if (input.departmentId() != null) {
            Department department = departmentRepository.findById(input.departmentId()).orElse(null);
            employee.setDepartment(department);
        }
        
        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employee created successfully with ID: {}", savedEmployee.getId());
        
        // Create OpenFGA tuples for the new employee asynchronously
        authorizationService.createEmployeeTuples(savedEmployee)
                .subscribe(
                    unused -> log.info("OpenFGA tuples created for employee: {}", savedEmployee.getId()),
                    error -> log.error("Failed to create OpenFGA tuples for employee: {}", savedEmployee.getId(), error)
                );
        
        return savedEmployee;
    }
} 