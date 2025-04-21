package com.example.documentmanagement.model;

public record CreateDepartmentInput(
    String name,
    String description,
    String location
) {} 