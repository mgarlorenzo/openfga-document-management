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

@Controller
public class EmployeeResolver {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeResolver(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
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
        
        return employeeRepository.save(employee);
    }
} 