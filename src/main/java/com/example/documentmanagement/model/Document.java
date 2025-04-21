package com.example.documentmanagement.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "documents")
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "classification_level")
    private String classificationLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Employee owner;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "status")
    private String status;

    public Document(String title, String content, Employee owner, Department department) {
        this.title = title;
        this.content = content;
        this.owner = owner;
        this.department = department;
        this.classificationLevel = "department"; // Default classification level
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getClassificationLevel() {
        return classificationLevel;
    }

    public String getStatus() {
        return status;
    }

    public Employee getOwner() {
        return owner;
    }

    public Department getDepartment() {
        return department;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setClassificationLevel(String classificationLevel) {
        this.classificationLevel = classificationLevel;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setOwner(Employee owner) {
        this.owner = owner;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
} 