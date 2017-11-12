package qirkat;

import org.junit.Test;

import static org.junit.Assert.*;

/** Tests of the Board class.
 *  @author Shixuan (Wayne) Li
 */
public class BoardTest {

    private static final String INIT_BOARD =
        "  b b b b b\n  b b b b b\n  b b - w w\n  w w w w w\n  w w w w w";

    private static final String[] GAME1 =
    { "c2-c3", "c4-c2",
      "c1-c3", "a3-c1",
      "c3-a3", "c5-c4",
      "a3-c5-c3",
    };

    private static final String GAME1_BOARD =
        "  b b - b b\n  b - - b b\n  - - w w w\n  w - - w w\n  w w b w w";

    private static void makeMoves(Board b, String[] moves) {
        for (String s : moves) {
            b.makeMove(Move.parseMove(s));
        }
    }

    private String getString(String str) {
        str = str.replaceAll("\\s", "");
        str = str.replaceAll(" ", "");
        return str;
    }

    @Test
    public void testInit1() {
        Board b0 = new Board();
        assertEquals(INIT_BOARD, b0.toString());
    }

    @Test
    public void testMoves1() {
        Board b0 = new Board();
        makeMoves(b0, GAME1);
        assertEquals(getString(GAME1_BOARD), b0.board());
    }

    private static final String[] GAME2 = { "c2-c3", "c4-c2", "c1-c3"};
    @Test
    public void testJumpPossible() {
        Board b0 = new Board();
        makeMoves(b0, GAME2);
        assertTrue(b0.jumpPossible('b', '2'));
        makeMoves(b0, new String[]{"a3-c1"});
        assertTrue(b0.jumpPossible('b', '3'));

        b0.setPieces("-b--- --w-- ----- ----- -----", PieceColor.WHITE);
        assertFalse(b0.jumpPossible('c', '2'));

        b0.setPieces("----- -b--- --w-- ----- -----", PieceColor.WHITE);
        assertTrue(b0.jumpPossible('c', '3'));

    }

    @Test
    public void testUndo() {
        Board b0 = new Board();
        Board b1 = new Board(b0);
        makeMoves(b0, GAME1);
        Board b2 = new Board(b0);

        for (int i = 0; i < GAME1.length; i += 1) {
            b0.undo();
        }
        assertEquals("'testUndo' problem", b1.toString(), b0.toString());
        assertEquals("'testUndo' problem", b1.whoseMove(), b0.whoseMove());
        assertEquals("'testUndo' problem", b1.board(), b0.board());
        assertEquals("'testUndo' problem", b1.gameOver(), b0.gameOver());

        makeMoves(b0, GAME1);
        assertEquals("'testUndo' problem", b2.toString(), b0.toString());
        assertEquals("'testUndo' problem", b2.whoseMove(), b0.whoseMove());
        assertEquals("'testUndo' problem", b2.board(), b0.board());
        assertEquals("'testUndo' problem", b2.gameOver(), b0.gameOver());
    }

//    private static final String[] GAME3 = { "c2-c3", "c4-c2"};
//    @Test
//    public void testUndo2() {
//        Board b0 = new Board();
//        makeMoves(b0, GAME3);
//        System.out.println(b0.toString());
//        b0.undo();
//        System.out.println();
//        System.out.println(b0.toString());
//    }


//    @Test
//    public void test() {
//        Board b0 = new Board();
//        b0.setPieces("--b-w ----w ---ww b---- bbbbb", PieceColor.BLACK);
//        System.out.println(b0.staticScore());
//    }

}
