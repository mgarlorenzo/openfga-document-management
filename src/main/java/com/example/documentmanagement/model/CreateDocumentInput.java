package com.example.documentmanagement.model;

public record CreateDocumentInput(
    String title,
    String content,
    Long departmentId,
    String classificationLevel
) {} 