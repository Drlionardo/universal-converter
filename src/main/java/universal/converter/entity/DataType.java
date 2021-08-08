package universal.converter.entity;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;

public class DataType {
    private final MathContext mathContext;
    private String mainKey; //Can be removed later, for debug info only
    private HashMap<String, BigDecimal> data;

    public DataType(MathContext mathContext) {
        this.mathContext = mathContext;
        this.data = new HashMap<>();
    }

    public boolean contains(String unit) {
        return data.containsKey(unit);
    }

    public BigDecimal getRatioForUnit(String unit) {
        return data.get(unit);
    }

    public void addRule(String from, String to, BigDecimal ratio) {
        if (data.isEmpty()) {
            mainKey = from;
            data.put(from, BigDecimal.ONE);
            data.put(to, ratio);
        } else {
            if (data.containsKey(from)) {
                data.put(to, data.get(from).multiply(ratio));
            } else if (data.containsKey(to)) {
                data.put(from, data.get(to).divide(ratio, mathContext));
            } else {
                //TODO: add proper exception message
                System.out.println("ERROR, CANT ADD RULE TO DATATYPE");
            }
        }
    }
}
