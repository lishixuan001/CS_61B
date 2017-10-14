package db61b;

import static org.junit.Assert.*;
import org.junit.Test;
import ucb.junit.textui;

import java.util.ArrayList;

/** The suite of all JUnit tests for the qirkat package.
 *  @author P. N. Hilfinger
 */
public class UnitTest {

    @Test
    public void testColumns() {

        int result = newTable.columns();
        assertEquals(4, result);
    }

    @Test
    public void testGetTitle() {
        String result = newTable.getTitle(1);
        assertEquals("T2", result);
    }


    @Test
    public void testFindColumn() {
        int result = newTable.findColumn("T2");
        assertEquals(1, result);
        int result2 = newTable.findColumn("T5");
        assertEquals(-1, result2);
    }

    @Test
    public void testSize() {
        int result = newTable.size();
        assertEquals(2, result);
    }

    @Test
    public void testAdd() {
        boolean shouldFalse = newTable.add(rowTwo);
        assertEquals(true, addOne);
        assertEquals(true, addTwo);
        assertEquals(false, shouldFalse);
    }

    @Test
    public void testDatabase() {
        Database database = new Database();
        database.put("newTable", newTable);
        database.put("newTable2", newTable2);
        Table get1 = database.get("newTable");
        int result = get1.findColumn("T2");
        assertEquals(1, result);
        Table get2 = database.get("newTable2");
        int result2 = get2.findColumn("C2");
        assertEquals(1, result2);
    }

    @Test
    public void testMyTitle() {
        String[] title = newTable.mytitles();
        assertArrayEquals(title, newTitles);
    }

    @Test
    public void testPrint() {
        newTable.print();
        System.out.println("");
        newTable2.print();
    }

    @Test
    public void testWriteTable() {
        newTable.writeTable("saveNewTable");
        assertEquals(true, true);
    }

    @Test
    public void testReadTable() {
        Table tempTable = copyNewTable.readTable("saveNewTable");
        int result = tempTable.findColumn("T2");
        assertEquals(1, result);
    }

    @Test
    public void testGetRow() {
        String[] result = newTable.getrow(0);
        assertArrayEquals(result, rowOne);
    }

    @Test
    public void testSelectOneTable() {
        ArrayList<String> columns = new ArrayList<>();
        columns.add("T1");
        columns.add("T3");

        ArrayList<Condition> conditions = new ArrayList<>();
        Column col1 = new Column("T3", newTable);
        Column col2 = new Column("T4", newTable);
        Condition condition = new Condition(col1, ">", "B");
        Condition condition2 = new Condition(col2, "=", "2");
        conditions.add(condition);
        conditions.add(condition2);

        Table result = newTable.select(columns, conditions);
        result.print();
    }

    @Test
    public void testSelectTwoTable() {
        ArrayList<String> selectFrom = new ArrayList<>();
        selectFrom.add("SemEnter");
        selectFrom.add("Grades");
        selectFrom.add("SID");

        ArrayList<Condition> condList = new ArrayList<>();
        Column col = new Column("Grades", newSelecTable1, newSelecTable2);
        Column col2 = new Column("SID", newSelecTable1, newSelecTable2);
        Condition condition = new Condition(col, ">=", "B");
        Condition condition2 = new Condition(col2, "<", "104");
        condList.add(condition);
        condList.add(condition2);

        Table result;
        result = newSelecTable1.select(newSelecTable2, selectFrom, condList);
        System.out.print(result.columns());
        result.print();
    }

    /** Initialize copyNewTable */
    Table copyNewTable;

    /** Initialize newTable */
    String[] newTitles = {"T1", "T2", "T3", "T4"};
    Table newTable = new Table(newTitles);
    String[] rowOne = {"row1-T1", "row1-T2", "A", "1"};
    String[] rowTwo = {"row2-T1", "row2-T2", "B", "2"};
    boolean addOne = newTable.add(rowOne);
    boolean addTwo = newTable.add(rowTwo);

    /** Initialize newTable2 */
    String[] newTitles2 = {"C1", "C2", "C3", "C4"};
    Table newTable2 = new Table(newTitles2);
    String[] rowOne2 = {"row1-C1", "row1-C2", "A", "1"};
    String[] rowTwo2 = {"row2-C1", "row2-C2", "B", "2"};
    boolean addOne2 = newTable2.add(rowOne2);
    boolean addTwo2 = newTable2.add(rowTwo2);

    /** Initialize selecTable1 */
    String[] newSelecTitles1 = {"SID", "Fname", "Lname", "Grades"};
    Table newSelecTable1 = new Table(newSelecTitles1);
    String[] rowSelecOne1 = {"101", "Jason", "Knowles", "B"};
    String[] rowSelecOne2 = {"102", "Valerie", "Chan", "B+"};
    boolean addSelecOne1 = newSelecTable1.add(rowSelecOne1);
    boolean addSelecOne2 = newSelecTable1.add(rowSelecOne2);

    /** Initialize selecTable2 */
    String[] newSelecTitles2 = {"Lname", "SemEnter", "Grades"};
    Table newSelecTable2 = new Table(newSelecTitles2);
    String[] rowSelecTwo1 = {"Knowles", "F", "B"};
    String[] rowSelecTwo2 = {"Chan", "S", "B+"};
    boolean addSelecTwo1 = newSelecTable2.add(rowSelecTwo1);
    boolean addSelecTwo2 = newSelecTable2.add(rowSelecTwo2);

    /** Run the JUnit tests in this package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        /* textui.runClasses(); */
        System.exit(textui.runClasses(UnitTest.class));
    }

}
