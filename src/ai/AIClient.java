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
     * @return Move to make (1-6)
     */
    public int getMove(GameState currentBoard)
    {
        boolean isMaxPlayer = false;
        int depth = 6;

        /* Player 1 is always maximizing player */
        if (player == 1) {
            isMaxPlayer = true;
        }

        return findBestMove(currentBoard, depth, isMaxPlayer);
    }

    /**
     *
     * This method implements the MiniMaxi algorithm for finding the best move.
     *
     * @param currentBoard The current board state.
     * @param depth Max depth of MiniMaxi search.
     * @param isMaxPlayer Boolean stating whether or not current player is the maximizing player.
     * @return Returns the best move determined by the MiniMaxi search.
     */

    public int findBestMove(GameState currentBoard, int depth, boolean isMaxPlayer) {
        int[] bestMove;
        bestMove = findBestMoveHelper(currentBoard, depth, isMaxPlayer);
        return bestMove[1];
    }

    /**
     *
     * This function is a helper function for performing the recursive DFS used in the MiniMaxi algorithm.
     *
     * @param currentBoard The current board state.
     * @param depth Current depth of DFS.
     * @param isMaxPlayer Boolean stating whether or not current player is the maximizing player.
     * @return Returns an int array with the best score and move for the player
     * Index 0 corresponds to score, index 1 corresponds to move.
     */
    public int[] findBestMoveHelper(GameState currentBoard, int depth, boolean isMaxPlayer) {
        GameState newBoard = currentBoard.clone();
        int[] bestMove = {-1, -1};
        int[] tempMove;

        if (currentBoard.gameEnded() && isMaxPlayer) {
            addText("hey");
            bestMove[0] = Integer.MIN_VALUE;
            return bestMove;
        }
        else if (currentBoard.gameEnded() && !isMaxPlayer) {
            bestMove[0] = Integer.MAX_VALUE;
            return bestMove;
        }

        if (depth == 0) {
            bestMove[0] = heuristicCalc(newBoard);
            return bestMove;
        }

        for (int i = 1; i <= 6; i++) {
            if (newBoard.makeMove(i)) {
                tempMove = findBestMoveHelper(newBoard, depth - 1, !isMaxPlayer);

                if (isMaxPlayer && tempMove[0] > Integer.MIN_VALUE) {
                    tempMove[1] = i;
                    bestMove = tempMove;
                    //addText("Maxplayer score:" + Integer.toString(bestMove[0]) + ", move: " + Integer.toString(bestMove[1]));
                }
                else if (!isMaxPlayer && tempMove[0] < Integer.MAX_VALUE) {
                    tempMove[1] = i;
                    bestMove = tempMove;
                    //addText("Minplayer score:" + Integer.toString(bestMove[0]) + ", move: " + Integer.toString(bestMove[1]));
                }
            }
        }

        return bestMove;
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
        for (int i = 1; i <= 6; i++) {
            player1Seeds += currentBoard.getSeeds(i, 1);
        }

        for (int i = 1; i <= 6; i++) {
            player2Seeds += currentBoard.getSeeds(i, 2);
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