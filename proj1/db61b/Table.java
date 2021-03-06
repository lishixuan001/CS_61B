package db61b;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static db61b.Utils.*;

/** A single table in a database.
 *  @author P. N. Hilfinger & Shixuan (Wayne) Li
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

    /** Redundant but helpful mytitles.
     * @return */
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

        if (size() != 0) {
            for (int j = 0; j < _size; j += 1) {
                ArrayList<String> checkRow = new ArrayList<>();
                for (int i = 0; i < _rowSize; i += 1) {
                    if (values[i].equals(this.get(j, i))) {
                        checkRow.add(values[i]);
                    }
                }
                if (checkRow.size() == _rowSize) {
                    return false;
                }
            }
        }

        _size += 1;
        for (int index = 0; index < _rowSize; index += 1) {
            _columns[index].add(values[index]);
        }

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
        String[] newRow = new String[_rowSize];
        int index = 0;
        for (Column column : columns) {
            newRow[index] = column.getFrom(rows);
            index += 1;
        }
        return add(newRow);
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

    /** A method writeTable.
     * @param name -- the String input */
    void writeTable(String name) {
        PrintStream output;
        output = null;
        try {
            String sep;
            sep = "";
            output = new PrintStream(name + ".db");

            for (int index = 0; index < _rowSize; index++) {
                if (index != _rowSize - 1) {
                    sep = sep + _titles[index] + ",";
                } else {
                    sep = sep + _titles[index];
                }
            }
            output.println(sep);

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
        for (int row = 0; row < _size; row += 1) {
            String printMaterial = new String();
            for (int col = 0; col < _rowSize; col++) {
                if (col == 0) {
                    printMaterial = "  " + get(_index.indexOf(row), col);
                } else {
                    printMaterial = printMaterial + " "
                                    + get(_index.indexOf(row), col);
                }
            }
            System.out.println(printMaterial);
        }
    }


    /** Return a new Table whose columns are COLUMNNAMES, selected from
     *  rows of this table that satisfy CONDITIONS. */
    Table select(List<String> columnNames, List<Condition> conditions) {
        Table result;

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

        for (int row = 0; row < size(); row++) {
            List<String> newRow = new ArrayList<>(newColumns.size());

            for (int col = 0; col < columns(); col++) {
                String item = _columns[col].get(row);
                newRow.add(item);
            }
            String[] theNewRow = newRow.toArray(new String[newRow.size()]);

            if (conditions == null) {
                indexRows.add(row);
            } else {
                if (Condition.test(conditions, row)) {
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
        List<Column> newColumns = new ArrayList<>();
        List<Column> recordSame1 = new ArrayList<>();
        List<Column> recordSame2 = new ArrayList<>();
        List<String> sameColumns = new ArrayList<>();
        List<Integer> colIndex = new ArrayList<Integer>();
        int tableIndex;
        int rowIndex;

        for (String wantedCol : columnNames) {
            Column col = new Column(wantedCol, this, table2);
            newColumns.add(col);
        }

        int findSame;
        for (String title : _titles) {
            findSame = table2.findColumn(title);
            if (findSame >= 0) {
                sameColumns.add(title);
            }
        }

        for (String title : sameColumns) {
            recordSame1.add(new Column(title, this));
            recordSame2.add(new Column(title, table2));
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

        for (int i = 0; i < _size; i += 1) {
            for (int j = 0; j < table2.size(); j += 1) {
                boolean cond1 = equijoin(recordSame1, recordSame2, i, j);
                if (cond1) {
                    if (conditions == null) {
                        result.add(newColumns, i, j);
                    } else {
                        boolean cond2 = Condition.test(conditions, i, j);
                        if (cond2) {
                            result.add(newColumns, i, j);
                        }
                    }
                }
            }
        }
        return result;
    }

    /** Return the row that wanted.
     * @param row -- an int input */
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
        for (int i = 0; i < limit; i += 1) {
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
