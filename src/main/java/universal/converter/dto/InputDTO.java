package universal.converter.dto;

public class InputDTO {
    private final String from;
    private final String to;

    public InputDTO(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}
