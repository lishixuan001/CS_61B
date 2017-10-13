// This is a SUGGESTED skeleton for a class that represents a single
// Table.  You can throw this away if you want, but it is a good
// idea to try to understand it first.  Our solution changes or adds
// about 100 lines in this skeleton.

// Comments that start with "//" are intended to be removed from your
// solutions.
package db61b;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static db61b.Utils.*;

/** A single table in a database.
 *  @author P. N. Hilfinger
 */
class Table {
    /** A new Table whose columns are given by COLUMNTITLES, which may
     *  not contain duplicate names. */
    Table(String[] columnTitles) {
        if (columnTitles.length == 0) {
            throw error("table must have at least one column");
        }
        _size = 0;
        _rowSize = columnTitles.length;
        for (int i = columnTitles.length - 1; i >= 1; i -= 1) {
            for (int j = i - 1; j >= 0; j -= 1) {
                if (columnTitles[i].equals(columnTitles[j])) {
                    throw error("duplicate column name: %s", columnTitles[i]);
                }
            }
        }

        _titles = columnTitles;
        _columns = new ValueList[_rowSize];
        for (int i = 0; i < _titles.length; i++) {
            _columns[i] = new ValueList();
        }
    }

    /** A new Table whose columns are give by COLUMNTITLES. */
    Table(List<String> columnTitles) {
        this(columnTitles.toArray(new String[columnTitles.size()]));
    }

    public String[] mytitles() {
        return _titles;
    }

    /** Return the number of columns in this table. */
    public int columns() {
        return _titles.length;
    }

    /** Return the title of the Kth column.  Requires 0 <= K < columns(). */
    public String getTitle(int k) {
        if (k > columns() || k < 0) {
            throw new IndexOutOfBoundsException("'k' index is out of boundary");
        }
        return _titles[k];
    }

    /** Return the number of the column whose title is TITLE, or -1 if
     *  there isn't one. */
    public int findColumn(String title) {
        for (int i = 0; i < _titles.length; i++) {
            if (_titles[i].equals(title)) {
                return i;
            }
        } return -1;
    }

    /** Return the number of rows in this table. */
    public int size() {
        return _size;
    }

    /** Return the value of column number COL (0 <= COL < columns())
     *  of record number ROW (0 <= ROW < size()). */
    public String get(int row, int col) {
        try {
            return _columns[col].get(row);
        } catch (IndexOutOfBoundsException excp) {
            throw error("invalid row or column");
        }
    }

    /** Add a new row whose column values are VALUES to me if no equal
     *  row already exists.  Return true if anything was added,
     *  false otherwise. */
    public boolean add(String[] values) {
        ValueList current_list;
        String new_item;
        boolean check = false;
        /** Checking. */
        for (int i = 0; i < _titles.length; i++) {
            current_list = _columns[i];
            new_item = values[i];
            if (!current_list.contains(new_item)) {
                check = true;
            }
        }
        /** Operating. */
        if (check) {
            for (int index_add = 0; index_add < _titles.length; index_add++) {
                _columns[index_add].add(values[index_add]);
            }
            _size += 1;
            return true;
        } else {
            return false;
        }
    }

    /** Add a new row whose column values are extracted by COLUMNS from
     *  the rows indexed by ROWS, if no equal row already exists.
     *  Return true if anything was added, false otherwise. See
     *  Column.getFrom(Integer...) for a description of how Columns
     *  extract values. */
    public boolean add(List<Column> columns, Integer... rows) {
        String[] new_row = new String[columns()];
        int index = 0;
        for (Column column : columns) {
            new_row[index] = column.getFrom(rows);
            index += 1;
        }
        return add(new_row);
    }

    /** Read the contents of the file NAME.db, and return as a Table.
     *  Format errors in the .db file cause a DBException. */
    static Table readTable(String name) {
        BufferedReader input;
        Table table;
        input = null;
        table = null;
        try {
            input = new BufferedReader(new FileReader(name + ".db"));
            String header = input.readLine();
            if (header == null) {
                throw error("missing header in DB file");
            }
            String[] columnNames = header.split(",");
            /** Create a new table. */
            table = new Table(columnNames);
            String valueLine = input.readLine();
            while (valueLine != null) {
                String[] extracted_row = valueLine.split(",");
                table.add(extracted_row);
                valueLine = input.readLine();
            }

        } catch (FileNotFoundException e) {
            throw error("could not find %s.db", name);
        } catch (IOException e) {
            throw error("problem reading from %s.db", name);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    /* Ignore IOException */
                }
            }
        }
        return table;
    }

    /** Write the contents of TABLE into the file NAME.db. Any I/O errors
     *  cause a DBException. */
    void writeTable(String name) {
        PrintStream output;
        output = null;
        try {
            String sep;
            sep = "";
            output = new PrintStream(name + ".db");
            /** Write in the titles and the commas */
            for (int index = 0; index < _titles.length; index++) {
                if (index != _titles.length - 1) {
                    output.print(getTitle(index) + ",");
                } else {
                    output.print(getTitle(index));
                }
            } output.println();

            /** Write in the values and the commas */
            for (int row = 0; row < size(); row++) {
                for (int col = 0; col < columns(); col++) {
                    if (col != columns() - 1) {
                        output.print(_columns[col].get(row) + ",");
                    } else {
                        output.print(_columns[col].get(row));
                    }
                }
            }
        } catch (IOException e) {
            throw error("trouble writing to %s.db", name);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    /** Print my contents on the standard output, separated by spaces
     *  and indented by two spaces. */
    void print() {
        for (int row = 0; row < size(); row++) {
            for (int col = 0; col < columns(); col++) {
                if (col == 0) {
                    System.out.print("  ");
                } else {
                    System.out.print(" " + _columns[col].get(row));
                }
            }
            System.out.println("");
        }
    }

    /** Return a new Table whose columns are COLUMNNAMES, selected from
     *  rows of this table that satisfy CONDITIONS. */
    Table select(List<String> columnNames, List<Condition> conditions) {
        Table result;

        /** First select the columns */
        List<Column> newColumns = new ArrayList<Column>();
        for (String wantedCol : columnNames) {
            if (findColumn(wantedCol) < 0) {
                throw error("Invalid column name");
            }
            Column col = new Column(wantedCol, this);
            newColumns.add(col);
        }
        result = new Table(columnNames);

        /** The select based on conditions */
        for (int row = 0; row < size(); row++) {
            List<String> newRow = new ArrayList<String>(newColumns.size());
            List<Integer> rowIndex = new ArrayList<Integer>();
            for (int col = 0; col < columns(); col++) {
                String item = _columns[col].get(row);
                newRow.add(item);
            }
            rowIndex.add(row);
            String[] theNewRow = newRow.toArray(new String[newRow.size()]);

            /** Go over the conditions */
            for (Integer num : rowIndex) {
                if (Condition.test(conditions, num)) {
                    result.add(theNewRow);
                }
            }
        }

        return result;
    }

    /** Return a new Table whose columns are COLUMNNAMES, selected
     *  from pairs of rows from this table and from TABLE2 that match
     *  on all columns with identical names and satisfy CONDITIONS. */
    Table select(Table table2, List<String> columnNames,
                 List<Condition> conditions) {
        Table result;

        boolean canTable1 = true;
        boolean canTable2 = true;

        /** See if tables meet requirements */
        for (String wantedCol : columnNames) {
            if (findColumn(wantedCol) < 0) {
                canTable1 = false;
            } else if (table2.findColumn(wantedCol) < 0) {
                canTable2 = false;
            }
        }

        /** Find results based on the availability */
        if (!canTable1 && !canTable2) {
            throw error("Both tables don't have the selected columns");
        } else if (canTable1 && !canTable2) {
            Table initTable1 = this.select(columnNames, conditions);
            return initTable1;
        } else if (!canTable1 && canTable2) {
            Table initTable2 = table2.select(columnNames, conditions);
            return initTable2;
        }

        Table initTable1 = this.select(columnNames, conditions);
        Table initTable2 = table2.select(columnNames, conditions);

        int tableTwoRowNum = initTable2._columns[0].size();
        String[] oneRow;

        for (int i = 0; i < tableTwoRowNum; i++) {
             oneRow = table2.getrow(i);
             initTable1.add(oneRow);
        }

        result = initTable1;
        return result;

//        /** Filter the same rows from tables */
//        ValueList[] resultList = initTable1._columns;
//        ValueList[] tableTwoList = initTable1._columns;
//        ArrayList[] rowsResult = new ArrayList[];
//        ArrayList[] eachRow = new ArrayList[];
//
//        int listLength = resultList.length;
//        int resultRows = resultList[0].size();
//        for (int i = 0; i < resultRows; i++) {
//
//        }
//
//
//        for (int i = 0; i < listLength; i++) {
//            for (int j = 0; j < listLength; j++) {
//                resultList[i].add(tableTwoList[j])
//            }
//        }






//        Table result = new Table(columnNames);
//        List<Column> newColumns = new ArrayList<Column>();
//        List<Column> recordSame1 = new ArrayList<Column>();
//        List<Column> recordSame2 = new ArrayList<Column>();
//
//        /** First select the columns */
//        for (String wantedCol : columnNames) {
//            Column col = new Column(wantedCol, this, table2);
//            newColumns.add(col);
//        }
//
//        /** Record the same cols in the tables */
//        int findSame;
//        Column sameColumn1, sameColumn2;
//        for (String title : mytitles()) {
//            findSame = table2.findColumn(title);
//            if (findSame >= 0) {
//                sameColumn1 = new Column(title, this);
//                sameColumn2 = new Column(title, table2);
//                recordSame1.add(sameColumn1);
//                recordSame2.add(sameColumn2);
//            }
//        }
//
//        /** Apply conditions on the results and ignore the same */
    }

    /** Return the row that wanted */
    public String[] getrow(int row) {
        List<String> resultList = new ArrayList<String>();
        int limit = _columns.length;
        for (int i = 0; i < limit; i++) {
            resultList.add(_columns[i].get(row));
        }
        String[] result = resultList.toArray(new String[resultList.size()]);
        return result;
    }

    /** Return <0, 0, or >0 depending on whether the row formed from
     *  the elements _columns[0].get(K0), _columns[1].get(K0), ...
     *  is less than, equal to, or greater than that formed from elememts
     *  _columns[0].get(K1), _columns[1].get(K1), ....  This method ignores
     *  the _index. */
    private int compareRows(int k0, int k1) {
        for (int i = 0; i < _columns.length; i += 1) {
            int c = _columns[i].get(k0).compareTo(_columns[i].get(k1));
            if (c != 0) {
                return c;
            }
        }
        return 0;
    }

    /** Return true if the columns COMMON1 from ROW1 and COMMON2 from
     *  ROW2 all have identical values.  Assumes that COMMON1 and
     *  COMMON2 have the same number of elements and the same names,
     *  that the columns in COMMON1 apply to this table, those in
     *  COMMON2 to another, and that ROW1 and ROW2 are indices, respectively,
     *  into those tables. */
    private static boolean equijoin(List<Column> common1, List<Column> common2,
                                    int row1, int row2) {
        int limit = common1.size();
        Column col1, col2;
        String item1, item2;
        for (int i = 0; i < limit; i++) {
            col1 = common1.get(i);
            col2 = common2.get(i);
            item1 = col1.getFrom(row1);
            item2 = col2.getFrom(row2);
            if (!item1.equals(item2)) {
                return false;
            }
        }
        return true;
    }

    /** A class that is essentially ArrayList<String>.  For technical reasons,
     *  we need to encapsulate ArrayList<String> like this because the
     *  underlying design of Java does not properly distinguish between
     *  different kinds of ArrayList at runtime (e.g., if you have a
     *  variable of type Object that was created from an ArrayList, there is
     *  no way to determine in general whether it is an ArrayList<String>,
     *  ArrayList<Integer>, or ArrayList<Object>).  This leads to annoying
     *  compiler warnings.  The trick of defining a new type avoids this
     *  issue. */
    private static class ValueList extends ArrayList<String> {
    }

    /** My column titles. */
    private final String[] _titles;
    /** My columns. Row i consists of _columns[k].get(i) for all k. */
    private final ValueList[] _columns;

    /** Rows in the database are supposed to be sorted. To do so, we
     *  have a list whose kth element is the index in each column
     *  of the value of that column for the kth row in lexicographic order.
     *  That is, the first row (smallest in lexicographic order)
     *  is at position _index.get(0) in _columns[0], _columns[1], ...
     *  and the kth row in lexicographic order in at position _index.get(k).
     *  When a new row is inserted, insert its index at the appropriate
     *  place in this list.
     *  (Alternatively, we could simply keep each column in the proper order
     *  so that we would not need _index.  But that would mean that inserting
     *  a new row would require rearranging _rowSize lists (each list in
     *  _columns) rather than just one. */
    private final ArrayList<Integer> _index = new ArrayList<>();

    /** My number of rows (redundant, but convenient). */
    private int _size;
    /** My number of columns (redundant, but convenient). */
    private final int _rowSize;
}
