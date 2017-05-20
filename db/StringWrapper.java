package db;

/**
 * Created by navar_000 on 2/25/2017.
 */
public class StringWrapper implements TableItem {
    private String str;

    public StringWrapper(String s) {
        this.str = s;
    }

    public String toString() {
        return this.str;
    }

    public String getValue() {
        return str;
    }


    public int compareTo(StringWrapper s) {

        int value = str.compareTo(s.getValue());
        if (value == 0) {
            return 0;
        } else if (value > 0) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * Strings can only be concatenated.
     */
    public TableItem operate(String operator, TableItem operand) {
        //if (operand instanceof NOVALUE) {
        //    return this.operate(operator, new StringWrapper(""));
        //}
        if (!operator.equals("+")) {
            return null;
        }
        String op2 = ((StringWrapper) operand).getValue();
        return new StringWrapper(str.substring(0, str.length() - 1) + op2.substring(1));
    }

    public boolean equals(StringWrapper o) {
        return str.substring(1, str.length() - 1).equals(o.getValue().substring(1, o.getValue().length() - 1));
    }
}