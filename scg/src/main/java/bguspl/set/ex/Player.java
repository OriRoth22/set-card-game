package bguspl.set.ex;
import java.util.*;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.Vector;
import java.util.Random;

import bguspl.set.Env;

/**
 * This class manages the players' threads and data
 *
 * @inv id >= 0
 * @inv score >= 0
 */
public class Player implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;

    /**
     * The id of the player (starting from 0).
     */
    public final int id;

    /**
     * The thread representing the current player.
     */
    protected Thread playerThread;

    /**
     * The thread of the AI (computer) player (an additional thread used to generate key presses).
     */
    protected Thread aiThread;

    /**
     * True iff the player is human (not a computer player).
     */
    private final boolean human;
    private Dealer dealer;
    private Player somelock;
    /**
     * True iff game should be terminated due to an external event.
     */
    private volatile boolean terminate;
    private boolean waiting;
    private boolean onesecSleep;
    /**
     * The current score of the player.
     */
    private int score;
    Set<Integer> playerChoices = new HashSet<Integer>();

    // i added
    /**
     * The class constructor.
     *
     * @param env    - the environment object.
     * @param dealer - the dealer object.
     * @param table  - the table object.
     * @param id     - the id of the player.
     * @param human  - true iff the player is a human player (i.e. input is provided manually, via the keyboard).
     */

    public Player(Env env, Dealer dealer, Table table, int id, boolean human) {
        this.env = env;
        this.table = table;
        this.id = id;
        this.human = human;
        this.dealer= dealer;

    }

    /**
     * The main player thread of each player starts here (main loop for the player thread).
     */
    @Override
    public void run() {
        playerThread = Thread.currentThread();

        Object lock = new Object();
        System.out.println(playerThread.getName());
        env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + "starting.");
        if (!human) createArtificialIntelligence();

         while (!terminate) {
            // TODO implement main player loop

            if (waiting ){
                for (int i=5;i>0;i--){
                    env.ui.setFreeze(id,i*1000);

                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                    System.out.println("got interrupted!");
                }
                }
                env.ui.setFreeze(id,-1);
                waiting=false;
            }
            if (onesecSleep){

                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                    System.out.println("got interrupted!");
                }
                env.ui.setFreeze(id,-1);
                onesecSleep=false;

            }

//             while (!waiting)
//             {

//
//                 env.ui.setFreeze(id,-1);
//
//             }
//            synchronized (lock){
//
//            }
        }
        if (!human) try { aiThread.join(); } catch (InterruptedException ignored) {}
        env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * Creates an additional thread for an AI (computer) player. The main loop of this thread repeatedly generates
     * key presses. If the queue of key presses is full, the thread waits until it is not full.
     */
    private void createArtificialIntelligence() {
        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            System.out.println("got interrupted!");
        }
        // note: this is a very very smart AI (!)
        aiThread = new Thread(() -> {
            env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " starting.");
            while (!terminate) {
                // TODO implement player key press simulator

                if (waiting ){
                    for (int i=5;i>0;i--){
                        env.ui.setFreeze(id,i*1000);

                        try {
                            Thread.sleep(1000);
                        } catch(InterruptedException e) {
                            System.out.println("got interrupted!");
                        }
                    }
                    env.ui.setFreeze(id,-1);
                    waiting=false;
                }
                if (onesecSleep){

                    try {
                        Thread.sleep(1000);
                    } catch(InterruptedException e) {
                        System.out.println("got interrupted!");
                    }
                    env.ui.setFreeze(id,-1);
                    onesecSleep=false;

                }
                else {
                    try {
                        Thread.sleep(2000);
                    } catch(InterruptedException e) {
                        System.out.println("got interrupted!");
                    }

                    Random random = new Random();
                    int randomInt = random.nextInt(12);
                    System.out.println(randomInt);
                    keyPressed(randomInt);


                }


//                try {
//                    synchronized (this) { wait(); }
//                } catch (InterruptedException ignored) {}
            }
            env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " terminated.");
        }, "computer-" + id);
        aiThread.start();
    }

    /**
     * Called when the game should be terminated due to an external event.
     */
    public void terminate() {
        // TODO implement
        this.terminate=true;

    }

    /**
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     */
    public  void keyPressed(int slot) {
        // TODO implement
        if (!waiting && !onesecSleep) {
            if (playerChoices.contains(slot)) {
                playerChoices.remove(slot);
                env.ui.removeToken(Integer.parseInt(playerThread.getName()), slot);
            } else {

                if (table.slotToCard[slot]!=null){
                playerChoices.add(slot);
                env.ui.placeToken(Integer.parseInt(playerThread.getName()), slot);
                }
                if (this.playerChoices.size() == 3) {
                    // to add to dealer queue;

                    dealer.addToQueue(playerChoices);
                    dealer.addToPlayersQueue(this.id);
                    synchronized (playerThread) {
                        try {
                            this.playerThread.wait();
                        } catch (InterruptedException e) {
                            System.out.println(e);
                        }
                    }
//                notify();
                    System.out.println("player sent to the dealer");

//                    try {
//                        this.playerThread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        System.out.println(e);
//                    }
                    playerChoices.clear();
               /*
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Thread Interrupted");
                }
                */
                }

            }
        }
        /*this method is for me only
         * */
    }
    public  void receive() {

        while (true) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread Interrupted");
            }
        }
    }
    /**
     * Award a point to a player and perform other related actions.
     *
     * @post - the player's score is increased by 1.
     * @post - the player's score is updated in the ui.
     */
    public void point() {
        // TODO implement

        int ignored = table.countCards(); // this part is just for demonstration in the unit tests
        this.score++;
        env.ui.setScore(id, score);
        env.ui.setFreeze(id,1000);
        onesecSleep= true;




    }

    /**
     * Penalize a player and perform other related actions.
     */
    public synchronized  void penalty() {

        waiting=true;
        // TODO implement





 }



    public int getScore() {
        return score;
    }
}
