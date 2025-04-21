package com.example.documentmanagement.resolver;

import com.example.documentmanagement.model.NationalIdType;

public record CreateEmployeeInput(
    String username,
    String password,
    String name,
    String surname,
    String email,
    String address,
    String location,
    NationalIdType nationalIdType,
    String nationalIdNumber,
    String issuingCountry,
    Long departmentId
) {} 