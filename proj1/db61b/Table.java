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

        if (values.length != _rowSize) {
            throw error("added length doesn't match the table");
        }

        /** Checking. */
        if (size() != 0) {
            for (int j = 0; j < _size; j++) {
                ArrayList<String> record = new ArrayList<>();
                for (int i = 0; i < _rowSize; i++) {
                    if (values[i].equals(this.get(j, i))) {
                        record.add(values[i]);
                    }
                }
                if (record.size() == _rowSize) {
                    return false;
                }
            }
        }
//        for (int i = 0; i < _titles.length; i++) {
//            current_list = _columns[i];
//            new_item = values[i];
//            if (!current_list.contains(new_item)) {
//                check = true;
//            }
//        }

        /** Operating. */
        _size += 1;
        for (int index_add = 0; index_add < _rowSize; index_add += 1) {
            _columns[index_add].add(values[index_add]);
        }

        /** For Index */
        int index = _size - 1;
        for (int row = 0; row < _size - 1; row += 1) {
            if (compareRows(_size - 1, row) < 0) {
                int record = _index.get(row);
                _index.remove(row);
                _index.add(row, record + 1);
                index -= 1;
            }
        }
        _index.add(index);

        return true;

    }

    /** Add a new row whose column values are extracted by COLUMNS from
     *  the rows indexed by ROWS, if no equal row already exists.
     *  Return true if anything was added, false otherwise. See
     *  Column.getFrom(Integer...) for a description of how Columns
     *  extract values. */
    public boolean add(List<Column> columns, Integer... rows) {
        String[] new_row = new String[_rowSize];
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
                String[] extractedRow = valueLine.split(",");
                table.add(extractedRow);
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
            for (int index = 0; index < _rowSize; index++) {
                if (index != _rowSize - 1) {
                    sep = sep + _titles[index] + ",";
                } else {
                    sep = sep + _titles[index];
                }
            }
            output.println(sep);


            /** Write in the values and the commas */
            for (int row = 0; row < size(); row++) {
                String result = "";
                for (int col = 0; col < columns(); col++) {
                    if (col != columns() - 1) {
                        result = result + get(row, col) + ",";
                    } else {
                        result = result + get(row, col);
                    }
                }
                output.println(result);
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
        String printMaterial = new String();
        for (int row = 0; row < _size; row++) {
            for (int col = 0; col < _rowSize; col++) {
                if (col == 0) {
                    printMaterial = "  " + get(_index.indexOf(row), col);
                } else {
                    printMaterial = printMaterial + " " + get(_index.indexOf(row), col);
                }
            }
            System.out.println(printMaterial);
        }
    }

//    /** Return a new Table whose columns are COLUMNNAMES, selected from
//     *  rows of this table that satisfy CONDITIONS. */
//    Table select(List<String> columnNames, List<Condition> conditions) {
//        for (String name : columnNames) {
//            if (findColumn(name) < 0) {
//                throw error("Invalid column name");
//            }
//        }
//        Table result = new Table(columnNames);
//        ArrayList<Integer> index = new ArrayList<>();
//        for (String name : columnNames) {
//            index.add(findColumn(name));
//        }
//
//        ArrayList<Integer> record = new ArrayList<>();
//        for (int row = 0; row < _size; row += 1) {
//            if (conditions == null) {
//                record.add(row);
//            } else {
//                if (Condition.test(conditions, row)) {
//                    record.add(row);
//                }
//            }
//        }
//        for (int i: record) {
//            int k = 0;
//            String[] rowValue = new String[columnNames.size()];
//            for (int j: index) {
//                rowValue[k] = get(i, j);
//                k += 1;
//            }
//            result.add(rowValue);
//        }
//
//        return result;
//    }
//
//    /** Return a new Table whose columns are COLUMNNAMES, selected
//     *  from pairs of rows from this table and from TABLE2 that match
//     *  on all columns with identical names and satisfy CONDITIONS. */
//    Table select(Table table2, List<String> columnNames,
//                 List<Condition> conditions) {
//
//        Table result = new Table(columnNames);
//        ArrayList<String> common = new ArrayList<>();
//        for (String name: _titles) {
//            int a = table2.findColumn(name);
//            if (a >= 0) {
//                common.add(name);
//            }
//        }
//        ArrayList<Column> collection = new ArrayList<>();
//        for (String col : columnNames) {
//            Column toAdd = new Column(col, this, table2);
//            collection.add(toAdd);
//        }
//
//        ArrayList<Column> one = new ArrayList<>();
//        ArrayList<Column> two = new ArrayList<>();
//        for (String name : common) {
//            one.add(new Column(name, this));
//            two.add(new Column(name, table2));
//        }
//
//        for (int i = 0; i < _size; i += 1) {
//            for (int j = 0; j < table2.size(); j += 1) {
//                if (equijoin(one, two, i, j)) {
//                    if (conditions == null) {
//                        result.add(collection, i, j);
//                    } else {
//                        if (Condition.test(conditions, i, j)) {
//                            result.add(collection, i, j);
//                        }
//                    }
//                }
//            }
//        }
//
//        return result;
//
//    }

    /** Return a new Table whose columns are COLUMNNAMES, selected from
     *  rows of this table that satisfy CONDITIONS. */
    Table select(List<String> columnNames, List<Condition> conditions) {
        Table result;

        /** First select the columns */
        List<Column> newColumns = new ArrayList<>();
        List<Integer> indexColumns = new ArrayList<>();
        List<Integer> indexRows = new ArrayList<>();

        for (String wantedCol : columnNames) {
            if (findColumn(wantedCol) < 0) {
                throw error("Invalid column name");
            }
            Column col = new Column(wantedCol, this);
            newColumns.add(col);
            indexColumns.add(findColumn(wantedCol));
        }
        result = new Table(columnNames);

        /** The select based on conditions */
        for (int row = 0; row < size(); row++) {
            List<String> newRow = new ArrayList<>(newColumns.size());

            for (int col = 0; col < columns(); col++) {
                String item = _columns[col].get(row);
                newRow.add(item);
            }
            String[] theNewRow = newRow.toArray(new String[newRow.size()]);

            /** Go over the conditions */
            if (conditions == null) {
//                result.add(theNewRow);
                indexRows.add(row);
            } else {
                if (Condition.test(conditions, row)) {
//                    result.add(theNewRow);
                    indexRows.add(row);
                }
            }
        }

        for (int row : indexRows) {
            int index = 0;
            String[] insertRow = new String[columnNames.size()];
            for (int col : indexColumns) {
                insertRow[index] = this.get(row, col);
                index += 1;
            }
            result.add(insertRow);
        }
        return result;
    }

    /** Return a new Table whose columns are COLUMNNAMES, selected
     *  from pairs of rows from this table and from TABLE2 that match
     *  on all columns with identical names and satisfy CONDITIONS. */
    Table select(Table table2, List<String> columnNames,
                 List<Condition> conditions) {

        Table result = new Table(columnNames);
        List<Column> newColumns = new ArrayList<Column>();
        List<Column> recordSame1 = new ArrayList<Column>();
        List<Column> recordSame2 = new ArrayList<Column>();
        List<Integer> colIndex = new ArrayList<Integer>();
        int tableIndex;
        int rowIndex;

        /** First select the columns */
        for (String wantedCol : columnNames) {
            Column col = new Column(wantedCol, this, table2);
            newColumns.add(col);
        }

        /** Record the same cols in the tables */
        int findSame;
        Column sameColumn1, sameColumn2;
        for (String title : mytitles()) {
            findSame = table2.findColumn(title);
            if (findSame >= 0) {
                sameColumn1 = new Column(title, this);
                sameColumn2 = new Column(title, table2);
                recordSame1.add(sameColumn1);
                recordSame2.add(sameColumn2);
            }
        }

        for (String col : columnNames) {
            if (this.findColumn(col) >= 0) {
                colIndex.add(1);
                colIndex.add(this.findColumn(col));
            } else if (table2.findColumn(col) >= 0) {
                colIndex.add(2);
                colIndex.add(table2.findColumn(col));
            } else {
                throw error("Both table don't have the selection.");
            }
        }

        Iterator indexTrack = colIndex.iterator();

        /** Apply conditions on the results and ignore the same */
        int tableRows = _columns[0].size();
        int tableTwoRows = table2._columns[0].size();

        for (int i = 0; i < tableTwoRows; i++) {
            for (int j = 0; j < tableRows; j++) {
                boolean cond1 = equijoin(recordSame1, recordSame2, j, i);
                boolean cond2 = Condition.test(conditions, j, i);
                if (cond1 && cond2) {
                    String[] fromTable = this.getrow(j);
                    String[] fromTableSecond = table2.getrow(i);

                    List<String> newRow = new ArrayList<String>();
                    while (indexTrack.hasNext()) {
                        tableIndex = (int) indexTrack.next();
                        if (tableIndex == 1) {
                            rowIndex = (int) indexTrack.next();
                            newRow.add(fromTable[rowIndex]);
                        } else if (tableIndex == 2) {
                            rowIndex = (int) indexTrack.next();
                            newRow.add(fromTableSecond[rowIndex]);
                        }
                    }
                    String[] newStringRow = newRow.toArray(new String[newRow.size()]);
                    result.add(newStringRow);

                }
            }
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
