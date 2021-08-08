package universal.converter.entity;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Expression {
    private final MathContext mathContext;
    private String text;
    private HashMap<DataType, AtomicInteger> powers;
    private BigDecimal totalRatio;

    public Expression(MathContext mathContext) {
        this.mathContext = mathContext;
        this.powers = new HashMap<>();
        this.totalRatio = BigDecimal.ONE;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public BigDecimal getTotalRatio() {
        return totalRatio;
    }

    public void setTotalRatio(BigDecimal totalRatio) {
        this.totalRatio = totalRatio;
    }

    public synchronized void incrementPower(DataType dataType) {
        powers.putIfAbsent(dataType, new AtomicInteger(0));
        int power = powers.get(dataType).incrementAndGet();
        if(power == 0) {
            powers.remove(dataType);
        }
    }

    public synchronized void decrementPower(DataType dataType) {
        powers.putIfAbsent(dataType, new AtomicInteger(0));
        int power = powers.get(dataType).decrementAndGet();
        if(power == 0) {
            powers.remove(dataType);
        }
    }

    //Custom equals method for comparing hashmaps because default method equals for AtomicInteger always returns false.
    public synchronized boolean hasEqualsPowers(Expression exp) {
        if (this.powers == exp.powers) {
            return true;
        }
        if (exp.powers.size() != this.powers.size()) {
            return false;
        }
        try {
            for (Map.Entry<DataType, AtomicInteger> e : this.powers.entrySet()) {
                DataType key = e.getKey();
                AtomicInteger value = e.getValue();
                if (value == null) {
                    if (!(exp.powers.get(key) == null && exp.powers.containsKey(key)))
                        return false;
                } else {
                    if (!(value.get() == (exp.powers.get(key).get())))
                        return false;
                }
            }
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }
        return true;
    }

    public synchronized void addNumeratorRatio(BigDecimal ratioForUnit) {
        this.totalRatio = this.totalRatio.divide(ratioForUnit, mathContext);
    }

    public synchronized void addDenominatorRatio(BigDecimal ratioForUnit) {
        this.totalRatio = this.totalRatio.multiply(ratioForUnit, mathContext);
    }
}
