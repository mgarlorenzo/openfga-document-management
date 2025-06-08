package com.example.documentmanagement.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.documentmanagement.model.Department;
import com.example.documentmanagement.model.Document;
import com.example.documentmanagement.model.Employee;
import com.example.documentmanagement.model.NationalIdType;
import com.example.documentmanagement.repository.DepartmentRepository;
import com.example.documentmanagement.repository.EmployeeRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DocumentServiceTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void createDocument_setsOwnerAndDepartment() {
        Department department = new Department();
        department.setName("HR");
        department = departmentRepository.save(department);

        Employee employee = new Employee();
        employee.setNationalIdType(NationalIdType.PASSPORT);
        employee.setNationalIdNumber("A1234567");
        employee.setIssuingCountry("US");
        employee.setName("John");
        employee.setSurname("Doe");
        employee.setUsername("jdoe");
        employee.setPassword("password");
        employee.setEmail("john.doe@example.com");
        employee.setDepartment(department);
        employee = employeeRepository.save(employee);

        Document document = new Document();
        document.setTitle("Test Document");
        document.setContent("Test content");

        Document saved = documentService.createDocument(document, employee).block();

        assertThat(saved).isNotNull();
        assertThat(saved.getOwner()).isEqualTo(employee);
        assertThat(saved.getDepartment()).isEqualTo(employee.getDepartment());
    }
}
