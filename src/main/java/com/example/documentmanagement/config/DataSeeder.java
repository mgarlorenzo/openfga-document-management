package com.example.documentmanagement.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.example.documentmanagement.model.Department;
import com.example.documentmanagement.model.Document;
import com.example.documentmanagement.model.Employee;
import com.example.documentmanagement.model.NationalIdType;
import com.example.documentmanagement.repository.DepartmentRepository;
import com.example.documentmanagement.repository.DocumentRepository;
import com.example.documentmanagement.repository.EmployeeRepository;
import com.example.documentmanagement.service.AuthorizationService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    private static final List<String> DEPARTMENTS = Arrays.asList("HR", "Sales", "Marketing");
    private static final String DEFAULT_PASSWORD = "password123";
    private static final String HR_BOSS_USERNAME = "hrBoss";
    private static final String SALES_BOSS_USERNAME = "salesBoss";
    private static final String MARKETING_BOSS_USERNAME = "marketingBoss";
    private static final String PRODUCT_BOSS_USERNAME = "productBoss";
    private static final String HR_MANAGER1_USERNAME = "hrManager1";
    private static final String HR_MANAGER2_USERNAME = "hrManager2";
    private static final String HR_EMPLOYEE1_USERNAME = "hrEmployee1";
    private static final String HR_EMPLOYEE2_USERNAME = "hrEmployee2";
    private static final String HR_EMPLOYEE3_USERNAME = "hrEmployee3";
    private static final String HR_EMPLOYEE4_USERNAME = "hrEmployee4";

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DocumentRepository documentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Starting data seeding...");

        // Create departments
        logger.info("Creating departments...");
        Department hrDepartment = createDepartment("HR");
        Department salesDepartment = createDepartment("Sales");
        Department marketingDepartment = createDepartment("Marketing");
        Department productDepartment = createDepartment("Product");
        List<Department> departments = List.of(hrDepartment, salesDepartment, marketingDepartment, productDepartment);

        // Create department bosses
        logger.info("Creating department bosses...");
        Employee hrBoss = createDepartmentBoss(hrDepartment, HR_BOSS_USERNAME);
        Employee salesBoss = createDepartmentBoss(salesDepartment, SALES_BOSS_USERNAME);
        Employee marketingBoss = createDepartmentBoss(marketingDepartment, MARKETING_BOSS_USERNAME);
        Employee productBoss = createDepartmentBoss(productDepartment, PRODUCT_BOSS_USERNAME);

        // Create HR staff
        logger.info("Creating HR staff...");
        Employee hrManager1 = createHRManager(hrDepartment, hrBoss, HR_MANAGER1_USERNAME);
        Employee hrManager2 = createHRManager(hrDepartment, hrBoss, HR_MANAGER2_USERNAME);
        Employee hrEmployee1 = createHREmployee(hrDepartment, hrManager1, HR_EMPLOYEE1_USERNAME);
        Employee hrEmployee2 = createHREmployee(hrDepartment, hrManager1, HR_EMPLOYEE2_USERNAME);
        Employee hrEmployee3 = createHREmployee(hrDepartment, hrManager2, HR_EMPLOYEE3_USERNAME);
        Employee hrEmployee4 = createHREmployee(hrDepartment, hrManager2, HR_EMPLOYEE4_USERNAME);

        // Create random users for other departments
        logger.info("Creating random users for other departments...");
        List<Employee> salesEmployees = createRandomUsers(salesDepartment, salesBoss, 5);
        List<Employee> marketingEmployees = createRandomUsers(marketingDepartment, marketingBoss, 5);
        List<Employee> productEmployees = createRandomUsers(productDepartment, productBoss, 5);

        // Combine all employees
        List<Employee> allEmployees = new ArrayList<>();
        allEmployees.add(hrBoss);
        allEmployees.add(salesBoss);
        allEmployees.add(marketingBoss);
        allEmployees.add(productBoss);
        allEmployees.add(hrManager1);
        allEmployees.add(hrManager2);
        allEmployees.add(hrEmployee1);
        allEmployees.add(hrEmployee2);
        allEmployees.add(hrEmployee3);
        allEmployees.add(hrEmployee4);
        allEmployees.addAll(salesEmployees);
        allEmployees.addAll(marketingEmployees);
        allEmployees.addAll(productEmployees);

        // Create documents for all users
        logger.info("Creating documents for all users...");
        for (Employee employee : allEmployees) {
            createDocumentsForUser(employee);
        }

        // Verify document creation
        long documentCount = documentRepository.count();
        logger.info("Total documents created: {}", documentCount);
        if (documentCount == 0) {
            logger.error("No documents were created during seeding!");
        } else {
            logger.info("Documents in database:");
            documentRepository.findAll().forEach(doc -> 
                logger.info("Document: {} (ID: {}) - Owner: {} - Department: {}", 
                    doc.getTitle(), doc.getId(), doc.getOwner().getName(), doc.getDepartment().getName()));
        }

        // Create OpenFGA tuples for all employees and documents
        logger.info("Creating OpenFGA tuples for all entities...");
        createOpenFGATuples(allEmployees, documentRepository.findAll());

        logger.info("Data seeding completed successfully");
    }

    private Department createDepartment(String name) {
        Department department = new Department(name);
        return departmentRepository.save(department);
    }

    private Employee createEmployee(String username, String name, String surname, Department department, Employee manager) {
        Employee employee = new Employee(
            NationalIdType.PASSPORT,
            "P" + username.hashCode(),
            "Spain",
            name,
            surname,
            "123 Main St",
            username,
            passwordEncoder.encode(DEFAULT_PASSWORD),
            username + "@company.com",
            department,
            "Office"
        );
        employee.setManager(manager);
        return employeeRepository.save(employee);
    }

    private Employee createDepartmentBoss(Department department, String username) {
        Employee boss = createEmployee(username, "Boss", "Department", department, null);
        boss.setEmail(username + "@company.com");
        boss.setNationalIdType(NationalIdType.PASSPORT);
        boss.setNationalIdNumber("P" + username.hashCode());
        boss.setIssuingCountry("Spain");
        boss.setAddress("123 Main St");
        boss.setLocation("Office");
        return employeeRepository.save(boss);
    }

    private Employee createHRManager(Department department, Employee boss, String username) {
        Employee manager = createEmployee(username, "Manager", "Department", department, boss);
        manager.setEmail(username + "@company.com");
        manager.setNationalIdType(NationalIdType.PASSPORT);
        manager.setNationalIdNumber("P" + username.hashCode());
        manager.setIssuingCountry("Spain");
        manager.setAddress("123 Main St");
        manager.setLocation("Office");
        return employeeRepository.save(manager);
    }

    private Employee createHREmployee(Department department, Employee manager, String username) {
        Employee employee = createEmployee(username, "Employee", "Department", department, manager);
        employee.setEmail(username + "@company.com");
        employee.setNationalIdType(NationalIdType.PASSPORT);
        employee.setNationalIdNumber("P" + username.hashCode());
        employee.setIssuingCountry("Spain");
        employee.setAddress("123 Main St");
        employee.setLocation("Office");
        return employeeRepository.save(employee);
    }

    private List<Employee> createRandomUsers(Department department, Employee boss, int count) {
        List<Employee> employees = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String username = "random" + i + "_" + department.getName().toLowerCase();
            Employee employee = createEmployee(
                username,
                "Random",
                "User",
                department,
                boss
            );
            employee.setEmail(username + "@company.com");
            employees.add(employeeRepository.save(employee));
        }
        return employees;
    }

    private void createDocumentsForUser(Employee employee) {
        // Get the department name while we're still in the transaction
        String departmentName = employee.getDepartment().getName();
        
        // Create personal document
        Document personalDoc = new Document();
        personalDoc.setTitle("Personal Document - " + employee.getName());
        personalDoc.setContent("A personal document for " + employee.getName());
        personalDoc.setOwner(employee);
        personalDoc.setDepartment(employee.getDepartment());
        personalDoc.setClassificationLevel("personal");
        personalDoc.setStatus("active");
        documentRepository.save(personalDoc);
        logger.info("Created personal document for employee {}: {}", employee.getName(), personalDoc.getTitle());

        // Create department document
        Document deptDoc = new Document();
        deptDoc.setTitle("Department Document - " + departmentName);
        deptDoc.setContent("A department document for " + departmentName);
        deptDoc.setOwner(employee);
        deptDoc.setDepartment(employee.getDepartment());
        deptDoc.setClassificationLevel("department");
        deptDoc.setStatus("active");
        documentRepository.save(deptDoc);
        logger.info("Created department document for employee {}: {}", employee.getName(), deptDoc.getTitle());
    }

    private void createOpenFGATuples(List<Employee> employees, List<Document> documents) {
        logger.info("Creating OpenFGA tuples for {} employees and {} documents", employees.size(), documents.size());
        
        // Create employee tuples
        for (Employee employee : employees) {
            authorizationService.createEmployeeTuples(employee)
                    .subscribe(
                        unused -> logger.debug("Created tuples for employee: {}", employee.getId()),
                        error -> logger.error("Failed to create tuples for employee: {}", employee.getId(), error)
                    );
        }
        
        // Create document tuples
        for (Document document : documents) {
            authorizationService.createDocumentTuples(document)
                    .subscribe(
                        unused -> logger.debug("Created tuples for document: {}", document.getId()),
                        error -> logger.error("Failed to create tuples for document: {}", document.getId(), error)
                    );
        }
        
        logger.info("OpenFGA tuple creation initiated for all entities");
    }
} 