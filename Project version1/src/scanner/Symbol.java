package scanner;

public class Symbol{
    private final String tokenName;
    private final Object value;

    public Symbol(String tokenName, Object value) {
        this.tokenName = tokenName;
        this.value = value;
    }

    public String getTokenName() {
        return tokenName;
    }

    public Object getValue() {
        return value;
    }
}
