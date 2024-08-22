package com.smartmssa.pos_recsys.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "association_rules")
public class AssociationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "antecedents", columnDefinition = "TEXT")
    private String antecedents;

    @Column(name = "consequents", columnDefinition = "TEXT")
    private String consequents;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "lift")
    private Double lift;

    @Column(name = "support")
    private Double support;

    // Default constructor
    public AssociationRule() {
    }

    // Parameterized constructor
    public AssociationRule(Long id, String antecedents, String consequents, Double confidence, Double lift, Double support) {
        this.id = id;
        this.antecedents = antecedents;
        this.consequents = consequents;
        this.confidence = confidence;
        this.lift = lift;
        this.support = support;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAntecedents() {
        return antecedents;
    }

    public void setAntecedents(String antecedents) {
        this.antecedents = antecedents;
    }

    public String getConsequents() {
        return consequents;
    }

    public void setConsequents(String consequents) {
        this.consequents = consequents;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Double getLift() {
        return lift;
    }

    public void setLift(Double lift) {
        this.lift = lift;
    }

    public Double getSupport() {
        return support;
    }

    public void setSupport(Double support) {
        this.support = support;
    }
}
