package db;

/**
 * Created by navar_000 on 2/25/2017.
 */
public class NaN implements TableItem {
    private final String val = "NaN";

    public String toString() {
        return val;
    }

    public Object getValue() {
        return val;
    }

    public int compareTo(Object o) {
        if (o instanceof NaN) {
            return 0;
        }
        return 1;
    }

    public TableItem operate(String operator, TableItem operand) {
        return this;
    }

    public boolean equals(TableItem item) {
        return true;
    }

}