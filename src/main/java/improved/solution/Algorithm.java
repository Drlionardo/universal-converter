package improved.solution;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
//TODO: User Expression powers to validate if conversion is possible
//TODO: Add exception handling
//TODO: Use BigDecimal to save accuracy
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
        double ratio = fromEx.getTotalRatio() / toEx.getTotalRatio();
        StringBuilder output = new StringBuilder();
        return output.append("1 ").append(fromEx.getText()).append(" = ").append(ratio).append(" ").append(toEx.getText()).toString();
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
        input = formatInput(input);

        int dividerIndex = input.indexOf('/');
        String numerator;
        String denominator;
        if(dividerIndex == -1) {
            numerator = input;
            denominator = "";
        } else {
            numerator = input.substring(0, dividerIndex-1);
            denominator = input.substring(dividerIndex + 2);
        }

        Expression expression = new Expression();
        expression.setText(input);
        for(String unit : numerator.split(" \\* ")) {
            DataType dataType = findDataTypeWithUnit(unit);
            expression.incrementPower(dataType);
            expression.setTotalRatio(expression.getTotalRatio() / dataType.getRatioForUnit(unit));
        }
        for(String unit : denominator.split(" \\* ")) {
            DataType dataType = findDataTypeWithUnit(unit);
            expression.decrementPower(dataType);
            expression.setTotalRatio(expression.getTotalRatio() * dataType.getRatioForUnit(unit));
        }
        return expression;
    }

    private String formatInput(String input) {
        //Remove extra spaces and format input
        input = input.replaceAll("\\s","");
        input = input.replaceAll("\\*", " * ");
        input = input.replaceAll("/", " / ");
        return input;
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

}
