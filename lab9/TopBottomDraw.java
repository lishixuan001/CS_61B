import java.lang.Math;

class TopBottomDraw {

    /** An array of all cards in the deck. */
    public int[] deck;

    /** Given some deck, TopBottomDraw can find the
     *  best possible score for the starting player. Assume that our
     *  opponent is playing optimally to minimize our score.
     */
    public TopBottomDraw(int[] deck) {
        this.deck = deck;
    }

    /** Finds the best score, assuming our maximizer is going first.
     */
    public int findBestScore(int i, int j, int person) {
        if (i == j) {
            if (deck.length % 2 == 0) {
                return 0;
            } else {
                return deck[i];
            }
        } else if (person == 0) {
            person = Math.abs(person - 1);
            return Math.max(findBestScore(i + 1, j, person) + deck[i], findBestScore(i, j - 1, person) + deck[j]);
        } else {
            person = Math.abs(person - 1);
            return Math.min(findBestScore(i + 1, j, person), findBestScore(i, j - 1, person));
        }
    }

    /** Test cases for TopBottomDraw.
     */
    public static void main(String[] args) {
        int[] exampleDeck1 = new int[] {1, 3, 45, 6, 7, 8, 9, 9};
        int[] exampleDeck2 = new int[] {1, 3, 45, 6, 7, 8, 9, 9, 2};
        TopBottomDraw tbp1 = new TopBottomDraw(exampleDeck1);
        TopBottomDraw tbp2 = new TopBottomDraw(exampleDeck2);
        System.out.printf("findBestScore returned %d, should be 63\n", tbp1.findBestScore(0, exampleDeck1.length - 1, 0));
        System.out.printf("findBestScore returned %d, should be 27\n", tbp2.findBestScore(0, exampleDeck2.length - 1, 0));
    }

}
