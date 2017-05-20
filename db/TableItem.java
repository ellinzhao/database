package db;

/**
 * Created by navar_000 on 2/25/2017.
 */
public interface TableItem {
    String toString();

    TableItem operate(String operator, TableItem operand);
}
