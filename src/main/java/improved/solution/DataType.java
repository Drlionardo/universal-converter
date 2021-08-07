package improved.solution;

import java.util.HashMap;

class DataType {
    private String mainKey; //Can be removed later, for debug info only
    private HashMap<String, Double> data;

    public DataType() {
        this.data = new HashMap<>();
    }

    public boolean contains(String unit) {
        return data.containsKey(unit);
    }

    public double getRatioForUnit(String unit) {
        return data.get(unit);
    }

    public void addRule(String from, String to, double ratio) {
        if (data.isEmpty()) {
            mainKey = from;
            data.put(from, 1.0);
            data.put(to, ratio);
        } else {
            if (data.containsKey(from)) {
                data.put(to, data.get(from) * ratio);
            } else if (data.containsKey(to)) {
                data.put(from, data.get(to) / ratio);
            } else {
                System.out.println("ERROR, CANT ADD RULE TO DATATYPE");
            }
        }
    }
}
