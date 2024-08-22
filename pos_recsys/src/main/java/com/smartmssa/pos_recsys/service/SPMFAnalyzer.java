package com.smartmssa.pos_recsys.service;

import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
import com.smartmssa.pos_recsys.entity.AssociationRule;
import com.smartmssa.pos_recsys.repository.AssociationRuleRepository;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SPMFAnalyzer {

    @Value("${app.min-support}")
    private double minSupport;

    @Value("${app.confidence}")
    private double confidence;

    @Value("${app.login-url}")
    private String loginUrl;

    @Value("${app.orders-url}")
    private String ordersUrl;

    @Value("${app.username}")
    private String username;

    @Value("${app.password}")
    private String password;

    @Autowired
    private Authenticator authenticator;

    @Autowired
    private OrderFetcher orderFetcher;

    @Autowired
    private OrderProcessor orderProcessor;

    @Autowired
    private AssociationRuleRepository associationRuleRepository;

    private List<List<String>> transactions;
    private Map<String, Integer> itemToIdMap = new HashMap<>();
    private Map<Integer, String> idToItemMap = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            String token = authenticator.login(loginUrl, username, password);
            JSONArray orders = orderFetcher.getOrders(ordersUrl, token);
            transactions = orderProcessor.processOrders(orders);

            // Log the first 10 transactions for inspection
            transactions.stream().limit(10).forEach(System.out::println);

            // Get unique items and frequent itemsets
            calculateUniqueItems();

            analyzeAndStoreRules(minSupport, confidence);

            // Test if the algorithm works well with a sample of transactions
            testTransactions(transactions);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<int[]> convertTransactionsToIntegerArray(List<List<String>> transactions) {
        int itemCounter = 1;

        List<int[]> transactionList = new ArrayList<>();
        for (List<String> transaction : transactions) {
            int[] intTransaction = new int[transaction.size()];
            int index = 0;
            for (String item : transaction) {
                if (!itemToIdMap.containsKey(item)) {
                    itemToIdMap.put(item, itemCounter);
                    idToItemMap.put(itemCounter, item);
                    itemCounter++;
                }
                intTransaction[index++] = itemToIdMap.get(item);
            }
            transactionList.add(intTransaction);
        }

        return transactionList;
    }

    private void calculateUniqueItems() {
        Set<String> uniqueItems = transactions.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        System.out.println("Nombre d'items uniques: " + uniqueItems.size());
        uniqueItems.forEach(System.out::println);
    }

    public void analyzeAndStoreRules(double minSupport, double minConfidence) throws Exception {
        List<int[]> intTransactions = convertTransactionsToIntegerArray(transactions);

        // Create temporary file for transactions
        File tempFile = File.createTempFile("transactions", ".txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
            for (int[] transaction : intTransactions) {
                writer.println(Arrays.stream(transaction)
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining(" ")));
            }
        }

        double relativeMinSupport = minSupport * transactions.size();

        AlgoFPGrowth algo = new AlgoFPGrowth();
        Itemsets itemsets = algo.runAlgorithm(tempFile.getAbsolutePath(), null, relativeMinSupport);
        algo.printStats();

        // You can skip these lines or comment them. They show the names of the Frequent Itemsets.
        System.out.println("Itemsets fréquents:");
        for (List<Itemset> level : itemsets.getLevels()) {
            for (Itemset itemset : level) {
                System.out.println(Arrays.stream(itemset.getItems())
                        .mapToObj(idToItemMap::get)
                        .collect(Collectors.joining(" ")));
            }
        }
        // these previous lines finish here.

        // Extracting rules
        List<AssociationRule> rules = extractRules(itemsets, minConfidence);

        // Update the database with new rules
        updateDatabaseWithRules(rules);

        // Printing rules to a CSV file "associations.csv"
        String filePath = "associations.csv";
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("id,antecedents,consequents,confidence,lift,support\n");

            int id = 1;
            for (AssociationRule rule : rules) {
                writer.append(String.valueOf(id++))
                        .append(",")
                        .append(rule.getAntecedents())
                        .append(",")
                        .append(rule.getConsequents())
                        .append(",")
                        .append(String.valueOf(rule.getConfidence()))
                        .append(",")
                        .append(String.valueOf(rule.getLift()))
                        .append(",")
                        .append(String.valueOf(rule.getSupport()))
                        .append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<AssociationRule> extractRules(Itemsets itemsets, double minConfidence) {
        List<AssociationRule> rules = new ArrayList<>();
        Set<String> uniqueRules = new HashSet<>();
        int id = 1;

        for (int k = 2; k <= itemsets.getLevels().size(); k++) {
            for (Itemset itemset : itemsets.getLevels().get(k - 1)) {
                List<Integer> items = Arrays.stream(itemset.getItems()).boxed().collect(Collectors.toList());
                for (int i = 0; i < items.size(); i++) {
                    List<Integer> antecedent = new ArrayList<>(items);
                    Integer consequent = antecedent.remove(i);

                    if (antecedent.isEmpty()) {
                        continue;
                    }

                    double supportAntecedent = calculateSupport(antecedent);
                    double supportConsequent = calculateSupport(consequent, transactions);
                    double confidence = supportAntecedent == 0 ? 0 : supportAntecedent / supportConsequent;

                    if (confidence >= minConfidence && confidence <= 1) {
                        double lift = confidence / supportConsequent;
                        String antecedentStr = antecedent.stream()
                                .map(idToItemMap::get)
                                .collect(Collectors.joining(" "));
                        String consequentStr = idToItemMap.get(consequent);
                        String ruleStr = antecedentStr + "->" + consequentStr;

                        if (!uniqueRules.contains(ruleStr)) {
                            uniqueRules.add(ruleStr);
                            rules.add(new AssociationRule((long) id++, antecedentStr, consequentStr, confidence, lift, supportAntecedent));
                        }
                    }
                }
            }
        }

        return rules;
    }

    private double calculateSupport(List<Integer> antecedent) {
        long count = transactions.stream()
                .filter(transaction -> antecedent.stream()
                        .allMatch(item -> transaction.contains(idToItemMap.get(item))))
                .count();
        return (double) count / transactions.size();
    }

    private double calculateSupport(Integer item, List<List<String>> transactions) {
        String itemName = idToItemMap.get(item);
        long count = transactions.stream()
                .filter(transaction -> transaction.contains(itemName))
                .count();
        return (double) count / transactions.size();
    }

    private void updateDatabaseWithRules(List<AssociationRule> rules) {
        // First, clear the previous rules from the database
        associationRuleRepository.deleteAll();

        // Then, store the new rules
        associationRuleRepository.saveAll(rules);
    }

    private void testTransactions(List<List<String>> transactions) {
        if (transactions.isEmpty()) {
            System.out.println("No transactions available for testing.");
            return;
        }

        List<int[]> intTransactions = convertTransactionsToIntegerArray(transactions);

        System.out.println("Testing the conversion of transactions:");
        intTransactions.forEach(transaction -> System.out.println(Arrays.toString(transaction)));

        if (intTransactions.size() > 0) {
            System.out.println("Sample converted transaction: " + Arrays.toString(intTransactions.get(0)));
        }

        System.out.println("Testing complete.");
    }

    public double getMinSupport() {
        return minSupport;
    }

    public void setMinSupport(double minSupport) {
        this.minSupport = minSupport;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
