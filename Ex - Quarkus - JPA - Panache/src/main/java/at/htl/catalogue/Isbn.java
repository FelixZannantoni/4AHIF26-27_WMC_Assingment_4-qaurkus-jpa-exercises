package at.htl.catalogue;

public record Isbn(String value) {

    public Isbn {
        if (value == null || !value.matches("\\d{13}")) {
            throw new IllegalArgumentException("An ISBN has 13 digits: " + value);
        }
    }
}
