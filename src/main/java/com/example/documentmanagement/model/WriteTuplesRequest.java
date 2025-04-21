package com.example.documentmanagement.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WriteTuplesRequest {
    private List<TupleKey> writes;
} 