package db;

/**
 * Created by navar_000 on 2/25/2017.
 */

public class NOVALUE implements TableItem {
    private final String val = "NOVALUE";

    public String toString() {
        return val;
    }

    public Object getValue() {
        return val;
    }

    public int compareTo(TableItem o) {
        return -1;
    }

    public TableItem operate(String operator, TableItem operand) {
        if (operand instanceof NOVALUE) {
            return this;
        } else if (operand instanceof FloatWrapper) {
            return new FloatWrapper((float) 0.0).operate(operator, (FloatWrapper) operand);
        } else if (operand instanceof IntegerWrapper) {
            return new IntegerWrapper(0).operate(operator, (IntegerWrapper) operand);
        } else {
            return new StringWrapper("").operate(operator, (StringWrapper) operand);
        }
    }

    public boolean equals(TableItem item) {
        return false;
    }
}
