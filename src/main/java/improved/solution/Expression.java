package improved.solution;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

class Expression {
    private String text;
    private HashMap<DataType, AtomicInteger> powers;
    private double totalRatio;

    public Expression() {
        powers = new HashMap<>();
        totalRatio = 1;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public HashMap<DataType, AtomicInteger> getPowers() {
        return powers;
    }

    public void setPowers(HashMap<DataType, AtomicInteger> powers) {
        this.powers = powers;
    }

    public double getTotalRatio() {
        return totalRatio;
    }

    public void setTotalRatio(double totalRatio) {
        this.totalRatio = totalRatio;
    }

    public void incrementPower(DataType dataType) {
        powers.putIfAbsent(dataType, new AtomicInteger(0));
        int power = powers.get(dataType).incrementAndGet();
        if(power == 0) {
            powers.remove(dataType);
        }

    }

    public void decrementPower(DataType dataType) {
        powers.putIfAbsent(dataType, new AtomicInteger(0));
        int power = powers.get(dataType).decrementAndGet();
        if(power == 0) {
            powers.remove(dataType);
        }
    }
}
