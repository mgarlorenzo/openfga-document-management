package com.example.documentmanagement.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import com.example.documentmanagement.model.Department;
import com.example.documentmanagement.model.Employee;
import com.example.documentmanagement.model.NationalIdType;
import com.example.documentmanagement.repository.DepartmentRepository;
import com.example.documentmanagement.repository.EmployeeRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Base64Utils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureGraphQlTester
@ActiveProfiles("test")
class DocumentResolverIT {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createDocumentMutation_createsDocument() {
        Department department = new Department();
        department.setName("Finance");
        department = departmentRepository.save(department);

        Employee employee = new Employee();
        employee.setNationalIdType(NationalIdType.DNI);
        employee.setNationalIdNumber("0001");
        employee.setIssuingCountry("ES");
        employee.setName("Ana");
        employee.setSurname("Gomez");
        employee.setUsername("agomez");
        employee.setPassword(passwordEncoder.encode("password"));
        employee.setEmail("ana.gomez@example.com");
        employee.setDepartment(department);
        employee = employeeRepository.save(employee);

        String mutation = """
            mutation($input: CreateDocumentInput!, $employeeId: ID!) {
                createDocument(input: $input, employeeId: $employeeId) {
                    id
                    title
                    content
                    classificationLevel
                    owner { id }
                }
            }
            """;

        Map<String, Object> input = new HashMap<>();
        input.put("title", "Test GraphQL Document");
        input.put("content", "Some text");
        input.put("departmentId", department.getId());
        input.put("classificationLevel", "personal");

        Map<String, Object> variables = new HashMap<>();
        variables.put("input", input);
        variables.put("employeeId", employee.getId());

        String authHeader = "Basic " + Base64Utils.encodeToString("agomez:password".getBytes());
        GraphQlTester authTester = graphQlTester.mutate()
            .header(HttpHeaders.AUTHORIZATION, authHeader)
            .build();

        authTester.document(mutation)
            .variables(variables)
            .execute()
            .path("createDocument.title").entity(String.class)
            .satisfies(t -> assertThat(t).isEqualTo("Test GraphQL Document"))
            .path("createDocument.owner.id").entity(String.class)
            .satisfies(id -> assertThat(id).isEqualTo(employee.getId().toString()))
            .path("createDocument.classificationLevel").entity(String.class)
            .isEqualTo("personal");
    }

    @Test
    void createDocumentMutation_failsOnEmployeeMismatch() {
        Department department = new Department();
        department.setName("IT");
        department = departmentRepository.save(department);

        Employee authEmployee = new Employee();
        authEmployee.setNationalIdType(NationalIdType.DNI);
        authEmployee.setNationalIdNumber("0002");
        authEmployee.setIssuingCountry("ES");
        authEmployee.setName("Luis");
        authEmployee.setSurname("Lopez");
        authEmployee.setUsername("llopez");
        authEmployee.setPassword(passwordEncoder.encode("password"));
        authEmployee.setEmail("luis.lopez@example.com");
        authEmployee.setDepartment(department);
        authEmployee = employeeRepository.save(authEmployee);

        Employee targetEmployee = new Employee();
        targetEmployee.setNationalIdType(NationalIdType.DNI);
        targetEmployee.setNationalIdNumber("0003");
        targetEmployee.setIssuingCountry("ES");
        targetEmployee.setName("Maria");
        targetEmployee.setSurname("Perez");
        targetEmployee.setUsername("mperez");
        targetEmployee.setPassword(passwordEncoder.encode("password"));
        targetEmployee.setEmail("maria.perez@example.com");
        targetEmployee.setDepartment(department);
        targetEmployee = employeeRepository.save(targetEmployee);

        String mutation = """
            mutation($input: CreateDocumentInput!, $employeeId: ID!) {
                createDocument(input: $input, employeeId: $employeeId) {
                    id
                }
            }
            """;

        Map<String, Object> input = new HashMap<>();
        input.put("title", "Unauthorized Document");
        input.put("content", "Hidden");
        input.put("departmentId", department.getId());
        input.put("classificationLevel", "personal");

        Map<String, Object> variables = new HashMap<>();
        variables.put("input", input);
        variables.put("employeeId", targetEmployee.getId());

        String authHeader = "Basic " + Base64Utils.encodeToString("llopez:password".getBytes());
        GraphQlTester authTester = graphQlTester.mutate()
            .header(HttpHeaders.AUTHORIZATION, authHeader)
            .build();

        authTester.document(mutation)
            .variables(variables)
            .execute()
            .path("createDocument").valueIsNull();
    }
}
