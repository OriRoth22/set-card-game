package bguspl.set.ex;

import bguspl.set.Env;
import java.util.Set;
import java.time.Clock;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Vector;

import java.util.LinkedList;

/**
 * This class manages the dealer's threads and data
 */
public class Dealer implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;
    private final Player[] players;

    /**
     * The list of card ids that are left in the dealer's deck.
     */
    private final List<Integer> deck;
    private final List<Integer> randomPlacesOfCards;

    /**
     * True iff game should be terminated due to an external event.
     */
    private volatile boolean terminate;

    /**
     * The time when the dealer needs to reshuffle the deck due to turn timeout.
     */
    private long reshuffleTime = Long.MAX_VALUE;
    private long time = System.currentTimeMillis()+60000;
    /**
    private Thread[] playersT= new Thread[players.length];


        for (int i=0;i< players.length;i++) {
        playersT[i] = new Thread(players[i], "player");

    }*/
    private Queue <Set>  queue;
    private Queue <Integer>  PlayersQueue;

    private Thread[] playersT;



    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());
        Object lock = new Object();
        this.queue = new ArrayDeque<Set>();
        this.PlayersQueue = new ArrayDeque<Integer>();

        //yuval added
        this.randomPlacesOfCards = new ArrayList<>();
        for (int i = 0; i <= 11; i++)
            randomPlacesOfCards.add(i);
    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
        // todo:  change to yuvals
         Thread[] playersT= new Thread[players.length];
        for (int i=0;i< players.length;i++) {
            playersT[i] = new Thread(players[i],""+(i));
           playersT[i].start();
        }
        env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " starting.");
        while (!shouldFinish()) {
            placeCardsOnTable();
            timerLoop();
//            updateTimerDisplay(false);
//            removeAllCardsFromTable();
        }
        announceWinners();
        env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did not time out.
     */
    private void timerLoop() {
        while (!terminate && System.currentTimeMillis() < reshuffleTime) {
            updateTimerDisplay(true);
            sleepUntilWokenOrTimeout();
            removeCardsFromTable();
            if (!terminate) {
                placeCardsOnTable();
            }

        }
    }

    /**
     * Called when the game should be terminated due to an external event.
     */
    public void terminate() {
        // TODO implement

           // announceWinners();
            terminate=true;
        for (int i=0;i< players.length;i++) {
            players[i].terminate();
        }
        }



    public void addToQueue(Set a) {
        queue.add(a);
    }

    public void addToPlayersQueue(Integer a) {
        PlayersQueue.add(a);
    }


    /**
     * Check if the game should be terminated or the game end conditions are met.
     *
     * @return true iff the game should be finished.
     */
    private boolean shouldFinish() {
        return terminate || env.util.findSets(deck, 1).size() == 0;
    }

    /**
     * Checks cards should be removed from the table and removes them.
     */
    private void removeCardsFromTable() {
        // TODO implement

        Collections.shuffle(randomPlacesOfCards);
        for (int i=0;i<12;i++) {
            if (table.slotToCard[randomPlacesOfCards.get(i)] != null) {
                deck.add(table.slotToCard[randomPlacesOfCards.get(i)]);
                if (table.slotToCard[randomPlacesOfCards.get(i)]!=null){
                table.removeCard(randomPlacesOfCards.get(i));}
            }
        }
        if (shouldFinish()){
            terminate();
        }
    }

    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    private void placeCardsOnTable() {
        System.out.println("get in to the juice");
            Collections.shuffle(randomPlacesOfCards);
            Collections.shuffle(deck);
            for (int i = 0; i < 12; i++) {
                if (deck.size()>0){
                table.placeCard(deck.get(0), randomPlacesOfCards.get(i));
                deck.remove(0);}
            }
    }

    /**
     * Sleep for a fixed amount of time or until the thread is awakened for some purpose.
     */
    private synchronized void sleepUntilWokenOrTimeout() {
        while (time>System.currentTimeMillis())
        {
           try{Thread.sleep(100);}catch(InterruptedException e){System.out.println(e);}
                env.ui.setCountdown(time-System.currentTimeMillis(),false);
                if (!(queue.isEmpty())) {
                    checkSet();}

//            if (deck.size()<20){
//                terminate();
//                break;
//            }
        }
    }

    /**
     * Reset and/or update the countdown and the countdown display.
     */
    private void updateTimerDisplay(boolean reset) {
        // TODO implement
        time=System.currentTimeMillis()+60000;
    }

    /**
     * Returns all the cards from the table to the deck.
     */
    private void removeAllCardsFromTable() {
        // TODO implement
        for (int f=0; f<12;f++)
        {
            deck.add(table.slotToCard[f]);
            table.removeCard(f);
        }

    }

    /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners() {

        // TODO implement

        LinkedList <Integer> winners=new LinkedList<Integer>();
        int max = 0;


        for (Player x : players) {
            if (x.getScore() > max) {
                max = x.getScore();
            }
        }
        for (Player y : players) {
            if (y.getScore() == max) {
                winners.add(y.id);
            }
        }
        int[] arr = new int[winners.size()];
        for (int j = 0; j < winners.size(); j++) {
            arr[j] = winners.get(j);
        }


//            x.terminate();

        System.out.println(arr[0]);
        env.ui.announceWinner(arr);

    }
    private void checkSet(){
        System.out.println("point or not");

        Set <Integer> playerChoices = queue.peek();
        queue.remove();
        int pl=PlayersQueue.peek();
        PlayersQueue.remove();

        int[] arr = new int[3];
        int[] arr2 = new int[3];
        int i = 0;
        for (int x:playerChoices){
            arr[i] = x;
            if (table.slotToCard[x]!=null)
            arr2[i]= table.slotToCard[x];
            i++;

        }
        if (this.env.util.testSet(arr2)==true)
        {
            for (int l = 0; l < arr.length; l++) {
                table.removeToken(pl, arr[l]);
                table.removeCard(arr[l]);
                if (deck.size()>0){
                    table.placeCard(deck.get(0), arr[l]);
                    deck.remove(0);}
            }
//            removeSets(playerChoices);
            synchronized (players[pl].playerThread){
            players[pl].playerThread.notify();
            }
//            removeSets(playerChoices);
            players[pl].point();
            System.out.println("point");
            updateTimerDisplay(true);
         // to do - delete sets that not straingers
        }
        else {
            for (int l = 0; l < arr.length; l++) {
                table.removeToken(pl, arr[l]);
            }
            synchronized (players[pl].playerThread) {
                players[pl].playerThread.notify();
            }
            players[pl].penalty();

        }
    }

        public void removeSets(Set<Integer> mainSet) {
            // Iterate through the queue
            int i=0;
            for (Set<Integer> set : queue) {
                // Check if the set contains any elements from the main set
                for (Integer element : set) {
                    if (mainSet.contains(element)) {
                        // If the set contains an element from the main set, remove it from the queue
                        for (int x:set){
                        table.removeToken(0,x);
                        }
                        queue.remove(set);

                        PlayersQueue.remove(getValueAtIndex(i));

                        break;
                    }

                }
                i++;
            }
        }
    public   int getValueAtIndex( int index) {
        // Check if the index is out of bounds
        if (index < 0 || index >= queue.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }

        // Iterate through the queue until the desired index is reached
        int currentIndex = 0;
        for (int value : PlayersQueue) {
            if (currentIndex == index) {
                return value;
            }
            currentIndex++;
        }

        // This code should not be reached, but is included to satisfy the compiler
        return 0;
    }
}
