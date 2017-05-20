package db;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

public class Database {
    private List<Table> list;
    private List<String> tblNames;

    public Database() {
        list = new ArrayList<>();
        tblNames = new ArrayList<>();
    }

    public String transact(String query) {
        String result = "";
        Parser p = new Parser();
        String[] input = p.eval(query);
        String first = input[0];
        switch (first) {
            case "ERROR: ":
                result = printError(input);
                break;
            case "createNewTable":
                result = this.createNewTable(Arrays.copyOfRange(input, 1, input.length));
                break;
            case "createSelectedTable":
                result = this.createSelectedTable(Arrays.copyOfRange(input, 1, input.length));
                break;
            case "loadTable":
                result = this.loadTable(Arrays.copyOfRange(input, 1, input.length));
                break;
            case "dropTable":
                result = this.dropTable(Arrays.copyOfRange(input, 1, input.length));
                break;
            case "insertRow":
                result = this.insertRow(Arrays.copyOfRange(input, 1, input.length));
                break;
            case "storeTable":
                result = this.storeTable(Arrays.copyOfRange(input, 1, input.length));
                break;
            case "printTable":
                result = this.printTable(Arrays.copyOfRange(input, 1, input.length));
                break;
            default:
                result = this.select(Arrays.copyOfRange(input, 1, input.length));
        }
        return result;
    }

    private String printError(String[] str) {
        return printArray(str);
    }

    private String createNewTable(String[] str) {
        if (tblNames.contains(str[0])) {
            return "ERROR: Table already exists: " + str[0];
        }
        String name = str[0];
        String[] withoutName = Arrays.copyOfRange(str, 1, str.length - 1);
        ArrayList<String> colList = new ArrayList<>();
        String types = "intfloatstring";
        for (int i = 1; i < str.length; i++) {
            if (!types.contains(str[i]) && i % 2 == 0) {
                return "ERROR: Invalid type " + str[i];
            }
            colList.add(str[i]);
        }
        Table table = new Table(name, colList);
        this.list.add(table);
        this.tblNames.add(name);
        return "";
    }

    //this is going to take a string array, call select, which returns a table in "toString" form
    //then createTable from toString can turn that into a real table
    private String createSelectedTable(String[] str) {
        String name = str[0];
        String toString = select(Arrays.copyOfRange(str, 1, str.length));
        ArrayList<String> listForm = new ArrayList<>();
        String[] textStr = toString.split("\\r\\n|\\n|\\r");
        listForm.add(name);
        for (int i = 0; i < textStr.length; i++) {
            listForm.add(textStr[i]);
        }
        return createTableFromToString(listForm);

    }

    private String loadTable(String[] str) {
        try {
            String filename = str[0] + ".tbl";
            FileReader reader = new FileReader("./" + filename);
            BufferedReader buff = new BufferedReader(reader);
            ArrayList<String> fileInfo = new ArrayList<>();
            fileInfo.add(str[0]);
            try {
                while (buff.ready()) {
                    String line = buff.readLine();
                    fileInfo.add(line);
                }
            } catch (IOException f) {
                //Just means file is done being read.
            }
            return createTableFromToString(fileInfo);
        } catch (FileNotFoundException e) {
            return "ERROR: File not found";
        }
    }

    private String createTableFromToString(ArrayList<String> toString) {
        String result = "";
        try {
            Parser p = new Parser();
            ArrayList<String[]> innerCalls = p.loadTableParser(toString);
            String[] dropArray = {innerCalls.get(0)[1]};
            this.dropTable(dropArray);
            while (result.equals("") && !innerCalls.isEmpty()) {
                String[] input = innerCalls.remove(0);
                String first = input[0];
                if (first.equals("createNewTable")) {
                    result = createNewTable(Arrays.copyOfRange(input, 1, input.length));
                } else if (first.equals("ERROR: ")) {
                    return first + input[1];
                } else {
                    result = insertRow(Arrays.copyOfRange(input, 1, input.length));
                }
            }
        } catch (IndexOutOfBoundsException e) {
            return "ERROR: Malformed load";
        }
        return result;
    }

    private String storeTable(String[] str) {
        int index = tblNames.indexOf(str[0]);
        if (index < 0) {
            return "ERROR: No such table";
        }
        String filename = "./" + str[0] + ".tbl";
        File file = new File(filename);
        try {
            if (file.createNewFile()) {
                FileWriter printer = new FileWriter(file);
                Table table = list.get(index);
                printer.write(table.toString());
                printer.close();
            } else {
                file.delete();
                file.createNewFile();
                FileWriter printer = new FileWriter(file);
                Table table = list.get(index);
                printer.write(table.toString());
                printer.close();
            }
        } catch (IOException e) {
            return "ERROR: Error storing file";
        }
        return "";
    }


    private String dropTable(String[] str) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (list.get(i).getName().equals(str[0])) {
                list.remove(i);
                tblNames.remove(i);
                return "";
            }
        }
        return "ERROR: No such table" + str[0];
    }

    private String insertRow(String[] str) {
        int index = tblNames.indexOf(str[0]);
        if (index < 0) {
            return "ERROR: No such table";
        }
        String rowError = "ERROR: Row does not match table";
        String result = "";
        boolean naN = str[1].equals("NaN");
        Table table = list.get(index);
        ArrayList<TableItem> row = new ArrayList<>();
        for (int i = 0; i < str.length - 1; i++) {
            if (i >= table.getNumCols()) {
                return rowError;
            }
            if (naN) {
                row.add(new NaN());
            } else if (str[i + 1].equals("NOVALUE")) {
                row.add(new NOVALUE());
            } else if (isInt(str[i + 1]) != null) {
                if (!table.getColumnType(i).equals("int")) {
                    result = rowError;
                }
                row.add(isInt(str[i + 1]));
            } else if (isFloat(str[i + 1]) != null) {
                if (!table.getColumnType(i).equals("float")) {
                    result = rowError;
                }
                row.add(isFloat(str[i + 1]));
            } else {
                if (!table.getColumnType(i).equals("string")) {
                    result = rowError;
                }
                String firstChar = str[i + 1].substring(0, 1);
                int len = str[i + 1].length();
                String lastChar = str[i + 1].substring(len - 1, len);
                if (firstChar.equals("\"") || !((firstChar.equals("'")) && lastChar.equals("'"))) {
                    result = "ERROR: Malformed data entry: " + str[i + 1];
                }
                row.add(new StringWrapper(str[i + 1]));
            }
        }
        if (str.length - 1 != table.getNumCols()) {
            result = rowError;
        }
        if (result.equals("")) {
            table.addRow(row);
        }
        return result;
    }

    private static IntegerWrapper isInt(String str) {
        try {
            IntegerWrapper i = new IntegerWrapper(Integer.parseInt(str));
            return i;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static FloatWrapper isFloat(String str) {
        try {
            //I dont know about this....
            if (str.substring(0, 1).equals("'") || str.equals("NaN")) {
                return null;
            }
            FloatWrapper f = new FloatWrapper(Float.parseFloat(str));
            return f;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String printTable(String[] str) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().equals(str[0])) {
                return list.get(i).toString();
            }
        }
        return "ERROR: No such table: " + printArray(str);
    }

    private String select(String[] str) {
        if (str[str.length - 1].equals(",")) {
            return "ERROR: Malformed query " + printArray(str);
        }
        ArrayList<List> cols = new ArrayList<>();
        ArrayList<String> colNames = new ArrayList<>();
        int i, index;
        ArrayList<String> colParts = new ArrayList<>();
        for (i = 0; !str[i].equals(",,"); i++) {
            if (str[i].equals("*")) {
                colParts.add(str[i]);
                cols.add(colParts);
            } else if (str[i].equals(",")) {
                cols.add(colParts);
                colParts = new ArrayList<>();
            } else if (str[i + 1].equals(",") || str[i + 1].equals(",,")) {
                colNames.add(str[i]);
                colParts.add(str[i]);
                colNames.add(str[i]);
                cols.add(colParts);
            } else if (!str[i].equals(",")) {
                colParts.add(str[i]);
                colNames.add(str[i]);
                colParts.add(str[i + 1]);
                colParts.add(str[i + 2]);
                colNames.add(str[i + 3]);
                cols.add(colParts);
                colParts = new ArrayList<>();
                i += 3;
                i = indexChecker(str, i);
            }
        }
        i++;
        ArrayList<Table> tables = new ArrayList<>();
        while (i < str.length && !str[i].equals(",,")) {
            index = tblNames.indexOf(str[i]);
            if (index < 0) {
                return "ERROR: Table not found";
            }
            tables.add(list.get(index));
            i++;
        }
        i++;
        if (!colChecker(cols, tables).equals("")) {
            return colChecker(cols, tables);
        }
        ArrayList<List> conditionCls = conditionMaker(str, i, tables);
        if (tables.size() == 0) {
            return "ERROR: Malformed query";
        }
        Stack<Table> unmerged = new Stack<>();
        for (i = tables.size() - 1; i >= 0; i--) {
            unmerged.push((tables.get(i)));
        }
        Table newTable = unmerged.peek();
        while (unmerged.size() > 1) {
            Table t1 = unmerged.pop();
            Table t2 = unmerged.pop();
            newTable = merge(t1, t2);
            if (newTable == null) {
                return "ERROR: Row does not match table";
            }
            unmerged.push(newTable);
        }
        newTable = filterOutRows(newTable, conditionCls);
        if (newTable == null) {
            return "ERROR: Invalid where clause";
        }
        newTable = columnFilter(newTable, cols, colNames);
        if (newTable == null) {
            return "ERROR: Invalid select clause";
        }
        return newTable.toString();
    }

    private static int indexChecker(String[] str, int i) {
        if (str[i + 1].equals(",")) {
            i += 1;
        }
        return i;
    }
    private static String colChecker(ArrayList<List> list, ArrayList<Table> tables) {
        for (List<String> s : list) {
            if (s.size() == 0) {
                break;
            }
            if (!Boolean.parseBoolean(isColName(s.get(0), tables)) && !s.get(0).equals("*")) {
                return "ERROR: Column does not exist";
            }
            boolean b = s.size() > 1 && !s.get(2).substring(0, 1).equals("'");
            if (b && !Boolean.parseBoolean(isColName(s.get(2), tables))) {
                return "ERROR: Column does not exist";
            }
        }
        return "";
    }

    private static ArrayList<List> conditionMaker(String[] str, int i, ArrayList<Table> t) {
        ArrayList<List> conditionCls = new ArrayList<>();
        ArrayList<String> conditions = new ArrayList<>();
        while (i < str.length) {
            if (!str[i].equals(",,")) {
                if (str[i].equals(",") || i + 1 == str.length) {
                    conditions = new ArrayList<>();
                } else {
                    String first = str[i];
                    conditions.add(first);
                    String second = str[i + 1];
                    conditions.add(second);
                    String third = str[i + 2];
                    String binary = isColName(third, t);
                    conditions.add(third);
                    conditions.add(binary);
                    conditionCls.add(conditions);
                    i += 2;
                }
            }
            i++;
        }
        return conditionCls;
    }

    private static String isColName(String s, ArrayList<Table> tables) {
        for (int i = 0; i < tables.size(); i++) {
            if (isColOfTable(s, tables.get(i))) {
                return "true";
            }
        }
        return "false";
    }

    private static boolean isColOfTable(String s, Table table) {
        return table.getColNames().contains(s);
    }

    private static Table merge(Table t1, Table t2) {
        List<Integer> matchIndices = new ArrayList<>();
        List<String> matchNames = new ArrayList<>();
        List<String> matchCols = new ArrayList<>();
        List<String> leftoverCols = new ArrayList<>();
        List<String> allCols = new ArrayList<>();
        for (int i = 0; i < t1.getColNames().size(); i++) {
            String colName = t1.getColNames().get(i);
            String type = t1.getCol(colName).getType();
            allCols.add(colName);
            allCols.add(type);
            if (t2.getColNames().contains(colName)) {
                if (!type.equals(t2.getCol(colName).getType())) {
                    return null;
                }
                matchNames.add(colName);
                matchCols.add(colName);
                matchCols.add(type);
                matchIndices.add(i);
                matchIndices.add(t2.getColNames().indexOf(colName) + t1.getNumCols());
            } else {
                leftoverCols.add(colName);
                leftoverCols.add(type);
            }
        }
        for (String colName : t2.getColNames()) {
            allCols.add(colName);
            allCols.add(t2.getCol(colName).getType());
            if (!matchNames.contains(colName)) {
                leftoverCols.add(colName);
                leftoverCols.add(t2.getCol(colName).getType());
            }
        }
        Table fullMerge = new Table("fullMerge", allCols);

        for (int i = 0; i < t1.getNumRows(); i++) {
            List<TableItem> t1Row = t1.getRow(i);
            for (int j = 0; j < t2.getNumRows(); j++) {
                List<TableItem> t2Row = t2.getRow(j);
                List<TableItem> newRow = new ArrayList<>();
                newRow.addAll(t1Row);
                newRow.addAll(t2Row);
                fullMerge.addRow(newRow);
            }
        }
        List<String> orderedCols = new ArrayList<>();
        orderedCols.addAll(matchCols);
        orderedCols.addAll(leftoverCols);
        return matchColumns(fullMerge, matchIndices, orderedCols);
    }

    private static ArrayList<Integer> indexCopier(List<Integer> list) {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            result.add(list.get(i));
        }
        return result;
    }

    private static ArrayList<String> stringCopier(List<String> list) {
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            result.add(list.get(i));
        }
        return result;
    }

    private static Table matchColumns(Table t, List<Integer> mI, List<String> oC) {
        ArrayList<String> columns = stringCopier(oC);
        Table reorderedTbl = new Table("", columns);
        List<Integer> tempMatchIndices;
        for (int row = 0; row < t.getNumRows(); row++) {
            tempMatchIndices = indexCopier(mI);
            boolean itemsMatch = true;
            while (!tempMatchIndices.isEmpty()) {
                int i1 = tempMatchIndices.remove(0);
                int i2 = tempMatchIndices.remove(0);
                TableItem item1 = t.getCol(i1).getItem(row);
                TableItem item2 = t.getCol(i2).getItem(row);
                String type = t.getCol(i1).getType();
                if (type.equals("int")) {
                    if (!((IntegerWrapper) item1).equals((IntegerWrapper) item2)) {
                        itemsMatch = false;
                        break;
                    }
                } else if (type.equals("float")) {
                    if (!((FloatWrapper) item1).equals((FloatWrapper) item2)) {
                        itemsMatch = false;
                        break;
                    }
                } else if (type.equals("string")) {
                    if (!((StringWrapper) item1).equals((StringWrapper) item2)) {
                        itemsMatch = false;
                        break;
                    }
                }
            }
            if (itemsMatch) {
                List<TableItem> filteredRow = new ArrayList<>();
                for (int i = 0; i < oC.size(); i += 2) {
                    String col = oC.get(i);
                    filteredRow.add(t.getItem(col, row));
                }
                reorderedTbl.addRow(filteredRow);
            }
        }
        return reorderedTbl;
    }

    private static Table filterOutRows(Table table, ArrayList<List> conditions) {
        Table newTable = table.duplicateTable();
        ArrayList<Integer> index = new ArrayList<>();
        int i = 0;
        while (i < conditions.size()) {
            int test = newTable.getNumRows();
            ArrayList<String> cls = (ArrayList<String>) conditions.get(i);
            String columnName = cls.get(0);
            String cVal = cls.get(1);
            boolean binaryComparison = Boolean.parseBoolean(cls.get(3));
            int k = 0;
            if (binaryComparison) {
                int j = 0;
                while (j < table.getNumRows()) {
                    if (!inside(index, j)) {
                        TableItem third = table.getItem(cls.get(2), j);
                        TableItem val = table.getItem(columnName, j);
                        Integer compare;
                        if (third instanceof FloatWrapper) {
                            compare = ((FloatWrapper) val).compareTo((FloatWrapper) third);
                        } else if (third instanceof IntegerWrapper) {
                            compare = ((IntegerWrapper) val).compareTo((IntegerWrapper) third);
                        } else {
                            compare = ((StringWrapper) val).compareTo((StringWrapper) third);
                        }
                        if (!checkCompare(compare, cVal)) {
                            newTable.removeRow(k);
                            index.add(j);
                        } else {
                            k += 1;
                        }
                    }
                    j++;
                }
            } else {
                String third = cls.get(2);
                int j = 0;
                while (j < table.getNumRows()) {
                    if (!inside(index, j)) {
                        TableItem val = table.getItem(columnName, j);
                        Integer compare;
                        if (isFloat(third) != null && val instanceof FloatWrapper) {
                            FloatWrapper fox = new FloatWrapper(Float.parseFloat(third));
                            compare = ((FloatWrapper) val).compareTo(fox);
                        } else if (isInt(third) != null) {
                            IntegerWrapper in = new IntegerWrapper(Integer.parseInt(third));
                            compare = ((IntegerWrapper) val).compareTo(in);
                        } else {
                            compare = ((StringWrapper) val).compareTo(new StringWrapper(third));
                        }
                        if (!checkCompare(compare, cVal)) {
                            newTable.removeRow(k);
                            index.add(j);
                        } else {
                            k += 1;
                        }
                    }
                    j++;
                }
            }
            i++;
        }
        return newTable;
    }

    private static boolean inside(ArrayList<Integer> list, int j) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).intValue() == j) {
                return true;
            }
        }
        return false;
    }

    private static Table columnFilter(Table tbl, ArrayList<List> cols, ArrayList<String> cN) {
        if (cols.get(0).get(0).equals("*")) {
            return tbl;
        }
        Table newTable = tbl.duplicateTable();
        for (int i = 0; i < cols.size(); i++) {
            ArrayList<String> parts = (ArrayList<String>) cols.get(i);
            String fromColName = parts.get(0);
            if (parts.size() != 1) {
                for (int j = 0; j < newTable.getNumRows(); j++) {
                    TableItem item = tbl.getItem(fromColName, j);
                    if (parts.get(2).substring(0, 1).equals("'")) {
                        String operand = parts.get(2);
                        if (!parts.get(1).equals("+")) {
                            return null;
                        } else {
                            TableItem sr = item.operate("+", new StringWrapper(operand));
                            newTable.editItem(fromColName, j, sr);
                        }
                    } else {
                        try {
                            float operand = Float.parseFloat(parts.get(2));
                            FloatWrapper fox = new FloatWrapper(operand);
                            TableItem f = item.operate(parts.get(1), fox);
                            if (f == null) {
                                return null;
                            }
                            newTable.editItem(fromColName, j, f);
                        } catch (NumberFormatException e) {
                            String otherColName = parts.get(2);
                            String type = newTable.getColumnType(otherColName);
                            if (!newTable.getColumnType(fromColName).equals(type)) {
                                return null;
                            } else {
                                TableItem operand = tbl.getItem(otherColName, j);
                                TableItem it;
                                try {
                                    it = item.operate(parts.get(1), operand);

                                } catch (ArithmeticException d) {
                                    it = new NaN();
                                }
                                newTable.editItem(fromColName, j, it);
                            }
                        }
                    }
                }
            }
        }
        List<Column> colList = new ArrayList<>();
        int a = 0;
        for (int i = 0; i < cN.size(); i += 2) {
            colList.add(newTable.getCol(cN.get(i)));
            colList.get(a).changeName(cN.get(i + 1));
            a++;
        }
        return new Table(colList);
    }

    private String printArray(String[] str) {
        String result = "";
        for (int i = 0; i < str.length - 1; i++) {
            result += str[i] + " ";
        }
        result += str[str.length - 1];
        return result;
    }

    private static boolean checkCompare(int c, String oper) {
        switch (oper) {
            case "==":
                return c == 0;
            case "!=":
                return c != 0;
            case "<=":
                return c <= 0;
            case ">=":
                return c >= 0;
            case "<":
                return c < 0;
            default:
                return c > 0;
        }

    }

}
