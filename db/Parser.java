package db;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;


public class Parser {
    // Various common constructs, simplifies parsing.
    private static final String REST = "\\s*(.*)\\s*",
            COMMA = "\\s*,\\s*",
            AND = "\\s+and\\s+";

    // Stage 1 syntax, contains the command name.
    private static final Pattern CREATE_CMD = Pattern.compile("create table " + REST),
            LOAD_CMD = Pattern.compile("load " + REST),
            STORE_CMD = Pattern.compile("store " + REST),
            DROP_CMD = Pattern.compile("drop table " + REST),
            INSERT_CMD = Pattern.compile("insert into " + REST),
            PRINT_CMD = Pattern.compile("print " + REST),
            SELECT_CMD = Pattern.compile("select " + REST);

    // Stage 2 syntax, contains the clauses of commands.
    private static final Pattern CREATE_NEW = Pattern.compile("(\\S+)\\s+\\((\\S+\\s+\\S+\\s*"
            + "(?:,\\s*\\S+\\s+\\S+\\s*)*)\\)"),
            SELECT_CLS = Pattern.compile("([^,]+?(?:,[^,]+?)*)\\s+from\\s+"
                    + "(\\S+\\s*(?:,\\s*\\S+\\s*)*)(?:\\s+where\\s+"
                    + "([\\w\\s+\\-*/'<>=!]+?(?:\\s+and\\s+"
                    + "[\\w\\s+\\-*/'<>=!]+?)*))?"),
            CREATE_SEL = Pattern.compile("(\\S+)\\s+as select\\s+"
                    + SELECT_CLS.pattern()),
            INSERT_CLS = Pattern.compile("(\\S+)\\s+values\\s+(.+?"
                    + "\\s*(?:,\\s*.+?\\s*)*)");

    public String[] eval(String query) {
        Matcher m;
        if (query.substring(query.length() - 1).equals(",")) {
            return new String[]{"ERROR: ", "Malformed query: ", query};
        } else if ((m = CREATE_CMD.matcher(query)).matches()) {
            return createTable(m.group(1));
        } else if ((m = LOAD_CMD.matcher(query)).matches()) {
            return loadTableID(m.group(1));
        } else if ((m = STORE_CMD.matcher(query)).matches()) {
            return storeTableID(m.group(1));
        } else if ((m = DROP_CMD.matcher(query)).matches()) {
            return dropTableID(m.group(1));
        } else if ((m = INSERT_CMD.matcher(query)).matches()) {
            return insertRowID(m.group(1));
        } else if ((m = PRINT_CMD.matcher(query)).matches()) {
            return printTableID(m.group(1));
        } else if ((m = SELECT_CMD.matcher(query)).matches()) {
            return selectID(m.group(1));
        } else {
            return new String[]{"ERROR: ", "Malformed query: ", query};
        }
    }

    private static String[] createTable(String expr) {
        Matcher m;
        if ((m = CREATE_NEW.matcher(expr)).matches()) {
            return createNewTableID(m.group(1), m.group(2).split(COMMA));
        } else if ((m = CREATE_SEL.matcher(expr)).matches()) {
            return createSelectedTableID(m.group(1), m.group(2), m.group(3), m.group(4));
        } else {
            return new String[]{"ERROR: ", "Malformed create: ", expr};
        }
    }

    /**
     * returns array in form: [method, tableName, colName1, colType 1, colName2, colType2... ]
     */
    private static String[] createNewTableID(String name, String[] cols) {
        List<String> cmdsList = new ArrayList<>();
        cmdsList.add("createNewTable");
        cmdsList.add(name);

        for (int i = 0; i < cols.length; i++) {
            String[] temp = cols[i].split("\\s+");

            for (int j = 0; j < temp.length; j++) {
                cmdsList.add(temp[j].replaceAll(" ", ""));
            }
        }

        String[] cmds = new String[cmdsList.size()];
        cmds = cmdsList.toArray(cmds);
        return cmds;
    }

    /**
     * "create table hello as select a/2 as half, b from r, q where a <=2 and b==2"
     * ["createSelectedTable", "hello", "a", "/", "2", "half", ",", "b", ",,", "r", "q", ",,", "a", "<=", "2", ",", "b", "==", "2"]
     */
    private static String[] createSelectedTableID(String name, String exprs, String tables, String conds) {
        String[] createSelect = selectHelper(exprs, tables, conds);
        String[] rv = new String[createSelect.length + 2];
        rv[0] = "createSelectedTable";
        rv[1] = name;
        System.arraycopy(createSelect, 0, rv, 2, createSelect.length);
        if (rv[2].equals("ERROR: ")) {
            return Arrays.copyOfRange(rv, 2, rv.length);
        }
        return rv;
    }

    /**
     * returns array in form: [method, tableName]
     */
    private static String[] loadTableID(String name) {
        return new String[]{"loadTable", name};
    }

    /**
     * returns array in form: [method, tableName]
     */
    private static String[] storeTableID(String name) {
        return new String[]{"storeTable", name};
    }

    /**
     * returns array in form: [method, tableName]
     */
    private static String[] dropTableID(String name) {
        return new String[]{"dropTable", name};
    }

    /**
     * returns array in form: [method, tableName, rowVal1, rowVal2...]
     */
    private static String[] insertRowID(String expr) {
        Matcher m = INSERT_CLS.matcher(expr);
        String[] rv;
        if (!m.matches()) {
            rv = new String[]{"ERROR: ", "Malformed insert: ", expr};
        } else {
            String[] rowVals = m.group(2).split(",");
            rv = new String[rowVals.length + 2];
            rv[0] = "insertRow";
            rv[1] = m.group(1);
            for (int i = 0; i < rowVals.length; i++) {
                rv[i + 2] = rowVals[i].trim();
            }
        }
        return rv;
    }

    /**
     * returns array in form: [method, tableName]
     */
    private static String[] printTableID(String name) {
        return new String[]{"printTable", name};
    }

    private static String[] selectID(String expr) {
        Matcher m = SELECT_CLS.matcher(expr);
        String[] rv;
        if (!m.matches()) {
            rv = new String[]{"ERROR: ", "Malformed select: ", expr};
        } else {
            rv = selectID(m.group(1), m.group(2), m.group(3));
        }
        return rv;
    }

    /**
     * "select a/2 as half, b from r, q where a <=2 and b==2"
     * ["select", "a", "/", "2", "half", ",", "b", ",,", "r", "q", ",,", "a", "<=", "2", ",", "b", "==", "2"]
     */
    private static String[] selectID(String exprs, String tables, String conds) {
        String[] select = selectHelper(exprs, tables, conds);
        String[] rv = new String[select.length + 1];
        rv[0] = "select";
        if (select[0].equals("ERROR: ")) {
            return select;
        }
        System.arraycopy(select, 0, rv, 1, select.length);
        return rv;
    }

    public static String[] selectHelper(String exprs, String tables, String conds) {
        Matcher m;
        List<String> cmdsList = new ArrayList<>();
        String[] exprsArr = exprs.split(",");

        Pattern EXPR_CLS1 = Pattern.compile("\\s*?(\\S+\\s*\\S+\\s*\\" +
                "S+)\\s+as\\s+(\\S+)\\s*?");
        Pattern EXPR_CLS2 = Pattern.compile("\\s*?(\\S+)\\s*?");

        for (int i = 0; i < exprsArr.length; i++) {
            if ((m = EXPR_CLS1.matcher(exprsArr[i])).matches()) {
                String[] op = m.group(1).split("(?=[-+/*])|(?<=[-+/*])");
                for (int j = 0; j < op.length; j++) {
                    cmdsList.add(op[j].replaceAll(" ", ""));
                }
                cmdsList.add(m.group(2));
            } else if ((m = EXPR_CLS2.matcher(exprsArr[i])).matches()) {
                cmdsList.add(m.group(1));
            } else {
                return new String[]{"ERROR: ", "Malformed column expression: ", exprsArr[i]};
            }
            if (i == exprsArr.length - 1) {
                cmdsList.add(",,");
            } else {
                cmdsList.add(",");
            }
        }

        String[] tablesArr = tables.split(",");
        for (String tbl : tablesArr) {
            cmdsList.add(tbl.replaceAll(" ", ""));
        }
        cmdsList.add(",,");

        Pattern COND_CLS = Pattern.compile("\\s*?(\\S+)\\s*(==|!=|<=|>=|>|<)\\s*(\\S+)\\s*?");
        if (conds != null) {
            String[] condsArr = conds.split("and");
            for (int i = 0; i < condsArr.length; i++) {
                if ((m = COND_CLS.matcher(condsArr[i])).matches()) {
                    for (int j = 1; j < 4; j++) {
                        cmdsList.add(m.group(j).replaceAll(" ", ""));
                    }
                } else {
                    return new String[]{"ERROR: ", "Malformed condition: ", condsArr[i]};
                }
                if (i < condsArr.length - 1) {
                    cmdsList.add(",");
                }
            }
        }

        String[] cmds = new String[cmdsList.size()];
        cmds = cmdsList.toArray(cmds);
        return cmds;

    }

    public static ArrayList<String[]> loadTableParser(ArrayList<String> file) {
        Parser p = new Parser();
        ArrayList<String[]> parsed = new ArrayList<>();
        String name = file.get(0);

        parsed.add(p.eval("create table " + name + " (" + file.get(1) + ")"));

        for (int i = 2; i < file.size(); i++) {
            parsed.add(p.eval("insert into " + name + " values " + file.get(i)));
        }

        return parsed;
    }


}
