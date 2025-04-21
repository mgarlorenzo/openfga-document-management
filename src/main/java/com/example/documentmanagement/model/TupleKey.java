package com.example.documentmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TupleKey {
    private String user;
    private String relation;
    private String object;
} 