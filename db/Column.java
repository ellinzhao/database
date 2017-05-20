package db;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by navar_000 on 2/25/2017.
 */

public class Column {
    private String type;
    private String name;
    private int size;
    private List<TableItem> list;

    public Column(String name, String type) {
        this.type = type;
        this.name = name;
        this.list = new ArrayList();
        size = 0;
    }

    public List<TableItem> getItems() {
        return list;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public TableItem getItem(int i) {
        return list.get(i);
    }

    public void add(TableItem item) {
        size += 1;
        list.add(item);
    }

    public void removeItem(int i) {
        list.remove(i);
        size -= 1;
    }

    public void editItem(int i, TableItem item) {
        list.remove(i);
        list.add(i, item);
    }

    public void changeName(String name) {
        this.name = name;
    }

    public String toString() {
        String ans = "";
        for (TableItem s : list) {
            ans += s.toString() + " ";
        }
        return ans;
    }

    public Column duplicateCol() {
        Column result = new Column(this.name, this.type);
        for (int i = 0; i < list.size(); i++) {
            result.add(list.get(i));
        }
        return result;
    }


}
