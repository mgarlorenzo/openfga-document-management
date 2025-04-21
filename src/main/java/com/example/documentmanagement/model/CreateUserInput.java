package com.example.documentmanagement.model;

public record CreateUserInput(
    NationalIdType nationalIdType,
    String nationalIdNumber,
    String issuingCountry,
    String name,
    String surname,
    String address,
    String username,
    String email,
    Long departmentId,
    String location
) {} 