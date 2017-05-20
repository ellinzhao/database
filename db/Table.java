package db;

import db.TableItem.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;


/**
 * Created by Ellin on 2/25/17.
 */

public class Table {
    private Map<Integer, Column> tbl;
    private ArrayList<String> colNames;

    private String tblName;
    private int numCols;
    private int numRows;


    public Table(String name, List<String> cols) {
        tbl = new HashMap<>();
        colNames = new ArrayList<>();
        numCols = cols.size() / 2;
        numRows = 0;
        tblName = name;
        addEmptyCols(cols);
    }

    public Table(List<Column> cols) {
        tbl = new HashMap<>();
        colNames = new ArrayList<>();
        numCols = 0;
        tblName = "";
        for (Column col : cols) {
            numRows = col.getSize();
            this.addCol(col);
        }
    }


    public String getName() {
        return tblName;
    }

    public int getNumCols() {
        return numCols;
    }

    public int getNumRows() {
        return numRows;
    }

    public String getColumnType(int index) {
        return tbl.get(index).getType();
    }

    public String getColumnType(String colName) {
        return this.getCol(colName).getType();
    }

    public Column getCol(String name) {
        return tbl.get(colNames.indexOf(name));
    }

    public Column getCol(int index) {
        return tbl.get(index);
    }

    public List<TableItem> getRow(int idx) {
        List<TableItem> row = new ArrayList<>();
        for (int i = 0; i < numCols; i++) {
            Column col = tbl.get(i);
            row.add(col.getItem(idx));
        }
        return row;
    }

    public ArrayList<String> getColNames() {
        return colNames;
    }

    public TableItem getItem(String colName, int i) {
        return getCol(colName).getItem(i);
    }

    private void addEmptyCols(List<String> cols) {
        Integer i = 0;
        while (!cols.isEmpty()) {
            String name = cols.remove(0);
            String type = cols.remove(0);
            Column c = new Column(name, type);
            tbl.put(i, c);
            colNames.add(i, name);
            i += 1;
        }
    }

    public void addCol(Column col) {
        tbl.put(numCols, col);
        colNames.add(col.getName());
        numCols += 1;
    }

    public void addRow(List<TableItem> row) {
        numRows += 1;
        for (int i = 0; i < numCols; i++) {
            TableItem item = row.get(i);
            tbl.get(i).add(item);
        }
    }

    public void removeRow(int i) {
        for (String colName : colNames) {
            getCol(colName).removeItem(i);
        }
        numRows -= 1;
    }

    public void editItem(String colName, int i, TableItem item) {
        tbl.get(colNames.indexOf(colName)).editItem(i, item);
    }

    public String toString() {

        String tblString = "";

        for (int i = 0; i < numCols; i++) {
            String colName = colNames.get(i);
            String colType = getCol(colName).getType();
            tblString += colName + " " + colType;
            if ((i == numCols - 1) && (numRows > 0)) {
                tblString += "\n";
            } else if (i != numCols - 1) {
                tblString += ",";
            }
        }

        for (int i = 0; i < numRows; i++) {
            List<TableItem> row = getRow(i);
            for (int j = 0; j < row.size(); j++) {
                if (row.get(j) == null) {
                    return "ERROR: invalid operation";
                }
                tblString += row.get(j).toString();
                if (j < row.size() - 1) {
                    tblString += ",";
                } else if (i != numRows - 1) {
                    tblString += "\n";
                }
            }
        }

        return tblString;
    }

    public void changeColName(String oldName, String newName) {
        this.getCol(oldName).changeName(newName);
        int index = colNames.indexOf(oldName);
        colNames.remove(index);
        colNames.add(index, newName);
    }

    public Table duplicateTable() {
        List<Column> dupCols = new ArrayList<>();
        for (String colName : colNames) {
            dupCols.add(getCol(colName).duplicateCol());
        }
        return new Table(dupCols);
    }

}
