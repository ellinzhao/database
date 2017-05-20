package db;

/**
 * Created by navar_000 on 2/25/2017.
 */
public class IntegerWrapper implements TableItem {

    private Integer i;

    public IntegerWrapper(int i) {
        this.i = i;
    }

    public String toString() {
        return this.i.toString();
    }

    public Integer getValue() {
        return i;
    }

    public int compareTo(IntegerWrapper otherInt) {
        int value = i.compareTo(otherInt.getValue());
        if (value == 0) {
            return 0;
        } else if (value > 0) {
            return 1;
        } else {
            return -1;
        }
    }

    public TableItem operate(String operator, TableItem operand) {
        if (operand instanceof NOVALUE) {
            return this.operate(operator, new IntegerWrapper(0));
        }
        Integer op2 = ((IntegerWrapper) operand).getValue();
        switch (operator) {
            case "/":
                return new IntegerWrapper(i / op2);
            case "*":
                return new IntegerWrapper(i * op2);
            case "+":
                return new IntegerWrapper(i + op2);
            case "-":
                return new IntegerWrapper(i - op2);
            default:
                return null;
        }
    }

    public boolean equals(IntegerWrapper o) {
        return i == (o.getValue());
    }
}
