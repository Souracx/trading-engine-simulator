package model;

import java.util.Objects;

public class Company {
    private final String code;   // e.g., "AAPL"
    private final String name;   // e.g., "Apple"
    private int price;           // current price (>= 1)

    public Company(String code, String name, int startingPrice) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Company code cannot be null/blank.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Company name cannot be null/blank.");
        }
        if (startingPrice < 1) {
            throw new IllegalArgumentException("Starting price must be >= 1.");
        }

        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.price = startingPrice;
    }

    //Getters 
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    /**
     * Adjusts price by delta. 
     */
    public void adjustPrice(int delta) {
        price = Math.max(1, price + delta);
    }

    @Override
    public String toString() {
        return code + " (" + name + ") $" + price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Company)) return false;
        Company company = (Company) o;
        return code.equals(company.code); // code uniquely identifies a company
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}