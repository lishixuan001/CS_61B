package db61b;

import java.util.HashMap;


/** A collection of Tables, indexed by name.
 *  @author Shixuan (Wayne) Li*/
class Database {
    /** An empty database. */
    public Database() {
        tableCollection = new HashMap<String, Table>();
    }

    /** Return the Table whose name is NAME stored in this database, or null
     *  if there is no such table. */
    public Table get(String name) {
        return tableCollection.get(name);
    }

    /** Set or replace the table named NAME in THIS to TABLE.  TABLE and
     *  NAME must not be null, and NAME must be a valid name for a table. */
    public void put(String name, Table table) {
        if (name == null || table == null) {
            throw new IllegalArgumentException("null argument");
        }
        if (!Character.isDigit(name.charAt(0))) {
            tableCollection.put(name, table);
        }
    }

    /** Define the tableCollection. */
    private HashMap<String, Table> tableCollection;
}
