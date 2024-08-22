package com.smartmssa.pos_recsys;

import com.smartmssa.pos_recsys.service.SPMFAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    @Autowired
    private SPMFAnalyzer analyzer;

    @Scheduled(cron = "0 * * * * ?") // Executing every minute.
    public void performScheduledTask() {
        analyzer.setMinSupport(0.0001);  // Adjust the Minimum Support
        analyzer.setConfidence(0.1);   // Adjust the confidence
        analyzer.init();  // Executing the analyzer
        System.out.println("Scheduled task executed successfully.");
    }
}
