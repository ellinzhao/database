package db;

/**
 * Created by navar_000 on 2/25/2017.
 */
public class FloatWrapper implements TableItem {
    private Float flo;
    private int zeros;

    public FloatWrapper(float f) {
        this.flo = (Float) (float) (Math.round(f * 1000.0) / 1000.0);
        this.flo = (Float) (float) (Math.round(f * 1000.0) / 1000.0);
    }

    public String toString() {
        String f = flo.toString();
        int index = f.indexOf(".");
        String result = f;
        for (int i = f.length() - index - 1; i < 3; i++) {
            result += "0";
        }
        return result;
    }

    public Float getValue() {
        return flo;
    }

    public int compareTo(FloatWrapper f) {
        int x = flo.compareTo(f.getValue());
        if (x > 0) {
            return 1;
        } else if (x < 0) {
            return -1;
        }
        return 0;
    }

    public TableItem operate(String operator, TableItem operand) {
        //if (operand instanceof NOVALUE) {
        //    return this.operate(operator, new FloatWrapper((float) 0.0));
        //}
        Float op2 = ((FloatWrapper) operand).getValue();
        switch (operator) {
            case "/":
                return new FloatWrapper(flo / op2);
            case "*":
                return new FloatWrapper(flo * op2);
            case "+":
                return new FloatWrapper(flo + op2);
            case "-":
                return new FloatWrapper(flo - op2);
            default:
                return null;
        }
    }

    public boolean equals(FloatWrapper o) {
        return flo.equals(o.getValue());
    }

}
