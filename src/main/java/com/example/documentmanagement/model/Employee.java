package com.example.documentmanagement.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "employees")
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "national_id_type", nullable = false)
    private NationalIdType nationalIdType;

    @Column(name = "national_id_number", nullable = false, unique = true)
    private String nationalIdNumber;

    @Column(name = "issuing_country", nullable = false)
    private String issuingCountry;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "address")
    private String address;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "location")
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL)
    private List<Employee> managedUsers;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<Document> documents;

    public Employee(String name, String email, Department department) {
        this.name = name;
        this.email = email;
        this.department = department;
    }

    public Employee(NationalIdType nationalIdType, String nationalIdNumber, String issuingCountry, 
                String name, String surname, String address, String username, 
                String password, String email, Department department, String location) {
        this.nationalIdType = nationalIdType;
        this.nationalIdNumber = nationalIdNumber;
        this.issuingCountry = issuingCountry;
        this.name = name;
        this.surname = surname;
        this.address = address;
        this.username = username;
        this.password = password;
        this.email = email;
        this.department = department;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getLocation() {
        return location;
    }

    public NationalIdType getNationalIdType() {
        return nationalIdType;
    }

    public String getNationalIdNumber() {
        return nationalIdNumber;
    }

    public String getIssuingCountry() {
        return issuingCountry;
    }

    public Department getDepartment() {
        return department;
    }

    public Employee getManager() {
        return manager;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setNationalIdType(NationalIdType nationalIdType) {
        this.nationalIdType = nationalIdType;
    }

    public void setNationalIdNumber(String nationalIdNumber) {
        this.nationalIdNumber = nationalIdNumber;
    }

    public void setIssuingCountry(String issuingCountry) {
        this.issuingCountry = issuingCountry;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public void setManager(Employee manager) {
        this.manager = manager;
    }
} 