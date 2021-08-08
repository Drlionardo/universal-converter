package universal.converter.serivce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;
import universal.converter.entity.DataType;
import universal.converter.entity.Expression;
import universal.converter.exceptions.UnableToConvertException;
import universal.converter.exceptions.UnknownUnitException;

import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

@Service
public class ConverterService {
    private static final MathContext MATH_CONTEXT = new MathContext(15, RoundingMode.HALF_UP);
    private ArrayList<DataType> rules;
    private String filePath;

    @Autowired
    public ConverterService(ApplicationArguments applicationArguments) {
        this.filePath = applicationArguments.getSourceArgs()[0];
        this.rules = new ArrayList<>();
        readDataFromFile();
    }

    public ConverterService(String filePath) {
        this.filePath = filePath;
        this.rules = new ArrayList<>();
        readDataFromFile();
    }

    public String convert(String from, String to) {
        BigDecimal ratio = getConvertRatio(from, to);
        String fromText = formatInput(from).isEmpty() ? "" : formatInput(from);
        String toText = formatInput(to).isEmpty() ? "" : formatInput(to);
        //Check if string has coefficient in prefix {}

        return ("1 " + fromText + " = " + ratio.toPlainString() + " " + toText).replaceAll("\\s\\s"," ").trim();
    }

    public BigDecimal getConvertRatio(String from, String to) {
        Expression fromEx = readExpression(from);
        Expression toEx = readExpression(to);
        if(fromEx.hasEqualsPowers(toEx)) {
            return fromEx.getTotalRatio().divide(toEx.getTotalRatio(), MATH_CONTEXT)
                    .stripTrailingZeros();
        } else {
            throw new UnableToConvertException();
        }
    }

    private void readDataFromFile() {
        File file = new File(filePath);
        try(Scanner scan = new Scanner(file)) {
            while (scan.hasNextLine()) {
                String[] rule = scan.nextLine().split(",");
                String from = rule[0];
                String to = rule[1];
                BigDecimal ratio = new BigDecimal(rule[2]);
                addNewRule(from, to, ratio);
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private Expression readExpression(String input) {
        input = formatInput(input);
        Expression expression = new Expression(MATH_CONTEXT);
        expression.setText(input);

        String numerator = getNumerator(input);
        Arrays.stream(numerator.split(" \\* ")).parallel().forEach(unit -> {
            if(!unit.isEmpty() && !unit.equals("1")) {
                DataType dataType = findDataTypeWithUnit(unit);
                if(dataType == null) {
                    throw new UnknownUnitException();
                }
                expression.incrementPower(dataType);
                expression.addNumeratorRatio(dataType.getRatioForUnit(unit));
            }
        });

        String denominator = getDenominator(input);
        Arrays.stream(denominator.split(" \\* ")).parallel().forEach(unit ->{
            if(!unit.isEmpty() && !unit.equals("1")) {
                DataType dataType = findDataTypeWithUnit(unit);
                if(dataType == null) {
                    throw new UnknownUnitException();
                }
                expression.decrementPower(dataType);
                expression.addDenominatorRatio(dataType.getRatioForUnit(unit));
            }
        });
        return expression;
    }
    private String formatInput(String input) {
        //Remove extra spaces and format input
        input = input.replaceAll("\\s","");
        input = input.replaceAll("\\*", " * ");
        input = input.replaceAll("/", " / ");
        return input;
    }
    private String getNumerator(String input) {
        int dividerIndex = input.indexOf('/');
        if(dividerIndex == -1) {
            return input;
        } else {
            return input.substring(0, dividerIndex-1);
        }
    }
    private String getDenominator(String input) {
        int dividerIndex = input.indexOf('/');
        if(dividerIndex == -1) {
            return "";
        } else {
            return input.substring(dividerIndex + 2);
        }
    }

    private void addNewRule(String from, String to, BigDecimal ratio) {
        DataType dataType = findDataTypeWithUnit(from, to);
        if(dataType == null) {
            dataType = new DataType(MATH_CONTEXT);
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