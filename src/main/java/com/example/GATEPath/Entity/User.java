
package com.example.GATEPath.Entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String role = "ROLE_USER";

    // Additional profile details
    @Column(length = 15)
    private String phone;

    @Column(length = 150)
    private String college;

    @Column(length = 50)
    private String degree;

    @Column(length = 50)
    private String branch;

    private Integer graduationYear;

    private Integer targetYear;

    // A student can select multiple GATE papers
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_gate_papers",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "gate_paper", length = 10)
    private Set<String> gatePapers = new HashSet<>();

    public User() {
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(Integer graduationYear) {
        this.graduationYear = graduationYear;
    }

    public Integer getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(Integer targetYear) {
        this.targetYear = targetYear;
    }

    public Set<String> getGatePapers() {
        return gatePapers;
    }

    public void setGatePapers(Set<String> gatePapers) {
        this.gatePapers = gatePapers == null
                ? new HashSet<>()
                : new HashSet<>(gatePapers);
    }
}
