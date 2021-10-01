package ai;

import ai.Global;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import kalaha.*;

/**
 * This is the main class for your Kalaha AI bot. Currently
 * it only makes a random, valid move each turn.
 * 
 * @author Johan Hagelbäck
 */
public class AIClient implements Runnable
{
    private int player;
    private JTextArea text;
    
    private PrintWriter out;
    private BufferedReader in;
    private Thread thr;
    private Socket socket;
    private boolean running;
    private boolean connected;
    	
    /**
     * Creates a new client.
     */
    public AIClient()
    {
	player = -1;
        connected = false;
        
        //This is some necessary client stuff. You don't need
        //to change anything here.
        initGUI();
	
        try
        {
            addText("Connecting to localhost:" + KalahaMain.port);
            socket = new Socket("localhost", KalahaMain.port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            addText("Done");
            connected = true;
        }
        catch (Exception ex)
        {
            addText("Unable to connect to server");
            return;
        }
    }
    
    /**
     * Starts the client thread.
     */
    public void start()
    {
        //Don't change this
        if (connected)
        {
            thr = new Thread(this);
            thr.start();
        }
    }
    
    /**
     * Creates the GUI.
     */
    private void initGUI()
    {
        //Client GUI stuff. You don't need to change this.
        JFrame frame = new JFrame("My AI Client");
        frame.setLocation(Global.getClientXpos(), 445);
        frame.setSize(new Dimension(420,250));
        frame.getContentPane().setLayout(new FlowLayout());
        
        text = new JTextArea();
        JScrollPane pane = new JScrollPane(text);
        pane.setPreferredSize(new Dimension(400, 210));
        
        frame.getContentPane().add(pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setVisible(true);
    }
    
    /**
     * Adds a text string to the GUI textarea.
     * 
     * @param txt The text to add
     */
    public void addText(String txt)
    {
        //Don't change this
        text.append(txt + "\n");
        text.setCaretPosition(text.getDocument().getLength());
    }
    
    /**
     * Thread for server communication. Checks when it is this
     * client's turn to make a move.
     */
    public void run()
    {
        String reply;
        running = true;
        
        try
        {
            while (running)
            {
                //Checks which player you are. No need to change this.
                if (player == -1)
                {
                    out.println(Commands.HELLO);
                    reply = in.readLine();

                    String tokens[] = reply.split(" ");
                    player = Integer.parseInt(tokens[1]);
                    
                    addText("I am player " + player);
                }
                
                //Check if game has ended. No need to change this.
                out.println(Commands.WINNER);
                reply = in.readLine();
                if(reply.equals("1") || reply.equals("2") )
                {
                    int w = Integer.parseInt(reply);
                    if (w == player)
                    {
                        addText("I won!");
                    }
                    else
                    {
                        addText("I lost...");
                    }
                    running = false;
                }
                if(reply.equals("0"))
                {
                    addText("Even game!");
                    running = false;
                }

                //Check if it is my turn. If so, do a move
                out.println(Commands.NEXT_PLAYER);
                reply = in.readLine();
                if (!reply.equals(Errors.GAME_NOT_FULL) && running)
                {
                    int nextPlayer = Integer.parseInt(reply);

                    if(nextPlayer == player)
                    {
                        out.println(Commands.BOARD);
                        String currentBoardStr = in.readLine();
                        boolean validMove = false;
                        while (!validMove)
                        {
                            long startT = System.currentTimeMillis();
                            //This is the call to the function for making a move.
                            //You only need to change the contents in the getMove()
                            //function.
                            GameState currentBoard = new GameState(currentBoardStr);
                            int cMove = getMove(currentBoard);
                            
                            //Timer stuff
                            long tot = System.currentTimeMillis() - startT;
                            double e = (double)tot / (double)1000;
                            
                            out.println(Commands.MOVE + " " + cMove + " " + player);
                            reply = in.readLine();
                            if (!reply.startsWith("ERROR"))
                            {
                                validMove = true;
                                addText("Made move " + cMove + " in " + e + " secs");
                            }
                        }
                    }
                }
                
                //Wait
                Thread.sleep(100);
            }
	}
        catch (Exception ex)
        {
            running = false;
        }
        
        try
        {
            socket.close();
            addText("Disconnected from server");
        }
        catch (Exception ex)
        {
            addText("Error closing connection: " + ex.getMessage());
        }
    }
    
    /**
     * This is the method that makes a move each time it is your turn.
     * Here you need to change the call to the random method to your
     * Minimax search.
     * 
     * @param currentBoard The current board state
     * @return The best move to make (1-6) based on the minimax algorithm
     */
    public int getMove(GameState currentBoard)
    {
        int score;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        int bestScore;
        int bestMove = -1;

        long startTime = System.currentTimeMillis();
        long maxSearchTime = 5000; /* N.B. this is time in ms */

        boolean isMaxPlayer = false;
        int curDepth = 0;

        GameState newBoard;

        /* Player 1 is always maximizing player */
        if (player == 1) {
            isMaxPlayer = true;
        }

        /* Initialization of best score for minimizing/maximizing player */
        if (isMaxPlayer) {
            bestScore = Integer.MIN_VALUE;
        }
        else {
            bestScore = Integer.MAX_VALUE;
        }

        /* While the search has not been carried out for more than maximum search time */
        while ((System.currentTimeMillis() - startTime) < maxSearchTime) {
            /* Increase depth by 1 (initialized to 0) */
            curDepth++;

            /* Spawning 6 nodes to find the best move in each respective subtree */
            for (int ambo = 1; ambo <= 6; ambo++) {
                newBoard = currentBoard.clone();

                if (isMaxPlayer) {
                    score = Integer.MIN_VALUE;
                } else {
                    score = Integer.MAX_VALUE;
                }
                /* Check if move is possible, and if so...*/

                if (newBoard.makeMove(ambo)) {
                    /* This check is implemented to account for when any player gets multiple turns */
                    if (newBoard.getNextPlayer() == 1) {
                        score = miniMaxi(newBoard, curDepth - 1, isMaxPlayer, alpha, beta, startTime, maxSearchTime);
                    }
                    if (newBoard.getNextPlayer() == 2) {
                        score = miniMaxi(newBoard, curDepth - 1, !isMaxPlayer, alpha, beta, startTime, maxSearchTime);
                    }
                }

                /* If it's the maximizing player's turn and the current score is better than the previous - we have found a better branch (ambo) */
                if (isMaxPlayer && score > bestScore) {
                    bestScore = score;
                    bestMove = ambo;
                } /* If it's the minimizing player's turn and the current score is better than the previous - we have found a better branch (ambo) */ else if (!isMaxPlayer && score < bestScore) {
                    bestScore = score;
                    bestMove = ambo;
                }
            }
        }

        /* addText("Best score: " + Integer.toString(bestScore)); */
        /* addText("Best move: " + Integer.toString(bestMove)); */
        /* addText("Reached depth of: " + Integer.toString(curDepth)); */
        return bestMove;
    }

    /**
     *
     * This function implements the means for performing the recursive DFS with alfa-beta pruning used in the MiniMaxi algorithm.
     * The DFS search is carried out in an iterative deepening fashion.
     *
     * @param currentBoard The current board state.
     * @param depth Current depth of DFS.
     * @param isMaxPlayer Boolean stating whether or not current player is the maximizing player.
     * @param alpha The current highest score. Used for pruning when compared to beta.
     * @param beta The current lowest score. Used for pruning when compared to alpha.
     * @param startTime The time when the player initiated their move. Used to know how much time has elapsed.
     * @param maxSearchTime The maximum time the algorithm can search for a solution. The best solution that was found during this time will be picked as the optimal solution.
     * @return Returns the best score of the node sub-tree.
     *
     */
    public int miniMaxi(GameState currentBoard, int depth, boolean isMaxPlayer, int alpha, int beta, long startTime, long maxSearchTime) {
        int bestScore;
        int score;

        /* Cloning the current game board */
        GameState newBoard = currentBoard.clone();

        /* Initialization of best score for minimizing/maximizing player */
        if (isMaxPlayer) {
            bestScore = Integer.MIN_VALUE;
        }
        else {
            bestScore = Integer.MAX_VALUE;
        }

        /* If the leaf node has been reached, return heuristic score */
        if (depth == 0 || newBoard.gameEnded() || (System.currentTimeMillis() - startTime) >= maxSearchTime) {
            bestScore = heuristicCalc(newBoard);
            return bestScore;
        }

        /* Recursively traverse each respective node (game move) tree */
        for (int ambo = 1; ambo <= 6; ambo++) {
            if (isMaxPlayer) {
                score = Integer.MIN_VALUE;
            }
            else {
                score = Integer.MAX_VALUE;
            }

            /* Check if move is possible, and if so...*/
            if (newBoard.makeMove(ambo)) {
                /* This check is implemented to account for when any player gets multiple turns */
                if (newBoard.getNextPlayer() == 1) {
                    score = miniMaxi(newBoard, depth - 1, isMaxPlayer, alpha, beta, startTime, maxSearchTime);
                }
                if (newBoard.getNextPlayer() == 2) {
                    score = miniMaxi(newBoard, depth - 1, !isMaxPlayer, alpha, beta, startTime, maxSearchTime);
                }
            }

            /* Calculate best score for the current player */
            if (isMaxPlayer) {
                bestScore = Integer.max(score, bestScore);
                alpha = Integer.max(score, alpha);
            }
            else {
                bestScore = Integer.min(score, bestScore);
                beta = Integer.min(score, beta);
            }
            
            /* Prune away the unnecessary branch */
            if (beta <= alpha) {
            	break;
            }
        }

        return bestScore;
    }

    /**
     *
     * This function calculates the heuristic value for each respective node.
     *
     * @param currentBoard The current board state.
     * @return Heuristic value based on current scores and number of seeds in the ambos.
     * High values for player 1 (maximizing player), low values for player 2 (minimizing player).
     */

    public int heuristicCalc(GameState currentBoard) {
        int player1Seeds = 0;
        int player2Seeds = 0;

        /* Summing up the number of seeds in each player's ambos */
        for (int ambo = 1; ambo <= 6; ambo++) {
        	/* 
        	 * If the player owns an ambo that has 0 pebbles in it
        	 * And the ambo on the opposite side has more than 0 pebbles
        	 */
	       	if ((currentBoard.getSeeds(ambo, 1) == 0) && (currentBoard.getSeeds(7 - ambo, 2) > 0)) {
	       		/* For each ambo up until the current ambo, search for if there are any ambos
	       		 * with enough pebbles to reach this current empty ambo
	       		 * */
	       		for (int prevAmbo = 1; prevAmbo < ambo; prevAmbo++) {
	       			// If so, add the opponents pebbles to your own and decrease the number of pebbles owned by the opponent.
	       			if (prevAmbo + currentBoard.getSeeds(prevAmbo, 1) == ambo) {
	       				player1Seeds += currentBoard.getSeeds(7 - ambo, 2);
	       				player2Seeds -= currentBoard.getSeeds(7 - ambo, 2);
	       				/* As the pebbles are only possible to be captured once,
	       				 * we break the for-loop as we don't need to check the other previous ambos.
	       				 */
	       				break;
	       			}
	       		}
	       	}
        	player1Seeds += currentBoard.getSeeds(ambo, 1);
        }

        for (int ambo = 1; ambo <= 6; ambo++) {
        	// Same as with the previous player but the other way around --^
        	if ((currentBoard.getSeeds(ambo, 2) == 0) && (currentBoard.getSeeds(7 - ambo, 1) > 0)) {
        		for (int prevAmbo = 1; prevAmbo < ambo; prevAmbo++) {
        			if (prevAmbo + currentBoard.getSeeds(prevAmbo, 2) == ambo) {
        				player1Seeds += currentBoard.getSeeds(7 - ambo, 1);
        				player2Seeds -= currentBoard.getSeeds(7 - ambo, 1);
        				break;
        			}
        		}
        	}
            player2Seeds += currentBoard.getSeeds(ambo, 2);
        }

        /* Adding the number of seeds in each player's house (these seeds are worth twice as much) */
        player1Seeds += (currentBoard.getScore(1) * 2);
        player2Seeds += (currentBoard.getScore(2) * 2);

        /* Returning the difference in seed values between player 1 and player 2 */
        return player1Seeds - player2Seeds;
    }
    
    /**
     * Returns a random ambo number (1-6) used when making
     * a random move.
     * 
     * @return Random ambo number
     */
    public int getRandom()
    {
        return 1 + (int)(Math.random() * 6);
    }
}