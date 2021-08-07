package improved.solution;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
//TODO: User Expression powers to validate if conversion is possible
//TODO: Add exception handling
//TODO: Return formatted string instead of ratio value
public class Algorithm {
    private ArrayList<DataType> rules;
    private String filePath;

    public Algorithm(String filePath) {
        rules = new ArrayList<>();
        this.filePath = filePath;
        readDataFromFile();
    }

    public String convert(String from, String to) {
        Expression fromEx = parseString(from);
        Expression toEx = parseString(to);
        System.out.println(fromEx.totalRatio / toEx.totalRatio);
        return null;
    }
    private void readDataFromFile() {
        File file = new File(filePath);
        try(Scanner scan = new Scanner(file)) {
            while (scan.hasNextLine()) {
                String currentRule = scan.nextLine();
                String[] rule = currentRule.split(",");
                String from = rule[0];
                String to = rule[1];
                double ratio = Double.parseDouble(rule[2]);

                addNewRule(from, to, ratio);
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private Expression parseString(String input) {
        //Remove extra spaces
        input = input.replaceAll("\\s","");
        input = input.replaceAll("\\*", " * ");


        int dividerIndex = input.indexOf('/');
        String numerator;
        String denominator;
        if(dividerIndex == -1) {
            numerator = input;
            denominator = "";
        }
        else {
            numerator = input.substring(0, dividerIndex);
            denominator = input.substring(dividerIndex + 1);
        }


        Expression expression = new Expression();
        for(String unit : numerator.split(" \\* ")) {
            DataType dataType = findDataTypeWithUnit(unit);
            expression.powers.putIfAbsent(dataType, new AtomicInteger(0));
            expression.powers.get(dataType).incrementAndGet();
            expression.totalRatio = expression.totalRatio / dataType.data.get(unit);
        }
        for(String unit : denominator.split(" \\* ")) {
            DataType dataType = findDataTypeWithUnit(unit);
            expression.powers.putIfAbsent(dataType, new AtomicInteger(0));
            expression.powers.get(dataType).decrementAndGet();
            expression.totalRatio = expression.totalRatio * dataType.data.get(unit);
        }
        return expression;
    }

    private void addNewRule(String from, String to, double ratio) {
        DataType dataType = findDataTypeWithUnit(from, to);
        if(dataType == null) {
            dataType = new DataType();
            rules.add(dataType);
        }
        dataType.addRule(from, to, ratio);
    }

    private DataType findDataTypeWithUnit(String from, String to) {
        for(DataType type : rules) {
            if(type.contains(from) || type.contains(to)) {
                return type;
            }
        }
        return null;
    }
    private DataType findDataTypeWithUnit(String unit) {
        for(DataType type : rules) {
            if(type.contains(unit)) {
                return type;
            }
        }
        return null;
    }

    class Expression {
        private HashMap<DataType, AtomicInteger> powers;
        double totalRatio;

        public Expression() {
            powers = new HashMap<>();
            totalRatio = 1;
        }

    }
    class DataType {
        private String mainKey; //Can be removed later
        private HashMap<String,Double> data;

        public DataType() {
            this.data = new HashMap<>();
        }
        public boolean contains(String unit) {
            return data.containsKey(unit);
        }
        public void addRule(String from, String to, double ratio) {
            if(data.isEmpty()) {
                mainKey = from;
                data.put(from, 1.0);
                data.put(to, ratio);
            }
            else {
                if(data.containsKey(from)) {
                    data.put(to, data.get(from) * ratio);
                }
                else if(data.containsKey(to)) {
                    data.put(from, data.get(to) / ratio);
                }
                else {
                    System.out.println("ERROR, CANT ADD RULE TO DATATYPE");
                }
            }
        }
    }
}
