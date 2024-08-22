package com.smartmssa.pos_recsys.analysis;

import com.smartmssa.pos_recsys.service.SPMFAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class AnalysisService {

    private final SPMFAnalyzer spmfAnalyzer;

    @Autowired
    public AnalysisService(SPMFAnalyzer spmfAnalyzer) {
        this.spmfAnalyzer = spmfAnalyzer;
    }

    @PostConstruct
    public void init() {
        // Perform the analysis as part of application initialization
        performAnalysis();
    }

    public void performAnalysis() {
        try {
            spmfAnalyzer.analyzeAndStoreRules(spmfAnalyzer.getMinSupport(), spmfAnalyzer.getConfidence());
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception as needed
        }
    }
}