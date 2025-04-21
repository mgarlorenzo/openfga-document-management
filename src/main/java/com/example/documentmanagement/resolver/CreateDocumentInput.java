package com.example.documentmanagement.resolver;

public record CreateDocumentInput(
    String title,
    String content,
    String classificationLevel,
    String department,
    Long ownerId,
    String status
) {} 