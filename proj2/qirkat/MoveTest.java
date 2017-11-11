/* Author: Paul N. Hilfinger.  (C) 2008. */

package qirkat;

import org.junit.Test;
import static org.junit.Assert.*;

import static qirkat.Move.*;

/** Test Move creation.
 *  @author Shixuan (Wayne) Li
 */
public class MoveTest {

    @Test
    public void testMove1() {
        Move m = move('a', '3', 'b', '2');
        assertNotNull(m);
        assertFalse("move should not be jump", m.isJump());
    }

    @Test
    public void testCRI() {
        char c = col(index('d', '2'));
        char r = row(index('d', '2'));
        assertEquals(r, '2');
        assertEquals(c, 'd');
    }

    @Test
    public void testMove2() {
        Move m = move('a', '2', 'a', '4');
        Move n = move('a', '4', 'c', '4');
        Move mov = Move.move(m, n);
        assertTrue(mov.jumpTail() == n);
    }

    @Test
    public void testJump1() {
        Move m = move('a', '3', 'a', '5');
        assertNotNull(m);
        assertTrue("move should be jump", m.isJump());
    }

    @Test
    public void testLeftRightMove() {
        Move m = move('a', '3', 'b', '3');
        Move n = move('b', '3', 'a', '3');
        assertTrue(m.isRightMove());
        assertTrue(n.isLeftMove());
    }

    @Test
    public void testJumpIndexAndChar() {
        Move m = move('a', '3', 'c', '3');
        int k = m.jumpedIndex();
        assertEquals(Move.col(k), 'b');
        assertEquals(Move.row(k), '3');
    }

    @Test
    public void testString() {
        assertEquals("a3-b2", move('a', '3', 'b', '2').toString());
        assertEquals("a3-a5", move('a', '3', 'a', '5').toString());
        assertEquals("a3-a5-c3", move('a', '3', 'a', '5',
                                      move('a', '5', 'c', '3')).toString());
    }

    @Test
    public void testParseString() {
        assertEquals("a3-b2", parseMove("a3-b2").toString());
        assertEquals("a3-a5", parseMove("a3-a5").toString());
        assertEquals("a3-a5-c3", parseMove("a3-a5-c3").toString());
        assertEquals("a3-a5-c3-e1", parseMove("a3-a5-c3-e1").toString());
    }
}
