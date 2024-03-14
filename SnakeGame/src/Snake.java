//https://codereview.stackexchange.com/questions/147629/simple-snake-clone-in-java

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class Snake extends JFrame {

    Tiles tiles = new Tiles();
    private int delay = 100; //changed

    /* The coordinates of the snake. */
    private int[] x = new int[tiles.PXs];
    private int[] y = new int[tiles.PXs];

    /* Coordinates for apple. */
    private int apple_x, apple_y;
    private int poison_x, poison_y; // New poison coordinates
    private boolean playOn = true;

    private int snakeSize = 3;

    /* Pressed Key. */
    private int pressedKey = KeyEvent.VK_DOWN;
    private int oldPressedKey;

    private Timer t;
    private Board board = new Board();
    private SoundBoard snd = new SoundBoard();

    // Hash table to track previous scores
    private Map<String, Integer> allScores = new HashMap<>();
    private static final String SCORES_FILE = "scores.txt";

    private static Clip backgroundMusic;
    private static void sound() {
        try {
            File audioFile = new File("SnakeGame/Sounds/around_the_world-atc.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat format = audioStream.getFormat();

            DataLine.Info info = new DataLine.Info(Clip.class, format);
            backgroundMusic = (Clip) AudioSystem.getLine(info);
            backgroundMusic.open(audioStream);
            backgroundMusic.start();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
    }

    public Snake() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(tiles.BOARD_WIDTH, tiles.BOARD_HEIGHT);
        setResizable(false);
        setLocation(50, 50);

        sound();


        addKeyListener(board);
        add(board);
        setVisible(true);

        // Load previous scores from the file
        board.loadPreviousScores();

        t = new Timer(delay, board);
        t.start();

        SwingUtilities.invokeLater(() -> board.repaint());
    }

    public class Board extends JPanel implements KeyListener, ActionListener {

        Board() {

            setBackground(Color.black);

            // Set snake starting coordinates.
            for (int i = 0; i < snakeSize; i++) {
                y[i] = 140 - (i * 30);
                x[i] = 140;
            }

            rndApple();
            rndPoison();// New: Generate poison coordinates

        }

        private void stopBackgroundMusicOnRestart() {
            Snake.stopBackgroundMusic();
        }

        public void actionPerformed(ActionEvent e) {
            checkTile();
            moveSnake();
            repaint();
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (playOn) {
                /* Draw apple. */
                g.setColor(Color.red);
                g.fillRect(apple_x, apple_y, tiles.PX, tiles.PX);

                /* Draw apple. */
                g.setColor(Color.green); // New: Draw poison
                g.fillRect(poison_x, poison_y, tiles.PX, tiles.PX);

                /* Draw snake. */
                for (int i = 0; i < snakeSize; i++) {
                    if (i == 0) g.setColor(Color.WHITE);
                    else g.setColor(Color.cyan);

                    g.fillRect(x[i], y[i], tiles.PX, tiles.PX);
                }

                /* Draw score */
                g.setFont(new Font("Sans serif", Font.BOLD, 20));
                g.drawString(getScore(), 550, 30);

                displayPreviousScores(g); // New: Display previous scores
            } else {
                gameOver(g);
            }
        }

        public void displayPreviousScores(Graphics g) {
            // Display previous scores on the screen
            g.setColor(Color.white);
            g.setFont(new Font("Sans serif", Font.BOLD, 15));
            g.drawString("Previous Scores:", 550, 100);

            int i = 1;
            for (Map.Entry<String, Integer> entry : allScores.entrySet()) {
                g.drawString(i + ". " + entry.getKey() + ": " + entry.getValue(), 550, 120 + i * 20);
                i++;
            }
        }

        public void keyPressed(KeyEvent e) {
            oldPressedKey = pressedKey;
            pressedKey = e.getKeyCode();
        }

        public void keyReleased(KeyEvent e) {
        }

        public void keyTyped(KeyEvent e) {
        }

        private void checkTile() {
            /* Check if outside of wall. */
            /*changed below*/
            if (y[0] > tiles.BOARD_HEIGHT) y[0] = 0;
            if (y[0] < 0) y[0] = tiles.BOARD_HEIGHT;
            if (x[0] < 0) x[0] = tiles.BOARD_WIDTH;
            if (x[0] > tiles.BOARD_WIDTH) x[0] = 0;

            for (int i = 1; i < x.length; i++) {
                if (x[0] == x[i] && y[0] == y[i]) {
                    playOn = false;
                    saveScore(); // New: Save score when the game ends
                }
            }
            /* Check for collisions.  Can remove at first */
            if ((x[0] == apple_x) && (y[0] == apple_y)) {
                snakeSize++;
                snd.play();
                rndApple();
                delay -= (delay > 80 ? 10 : -1);
                t.setDelay(delay);
            }

            // New: Check if snake hits poison
            if ((x[0] == poison_x) && (y[0] == poison_y)) {
                snakeSize--;
                snd.die();
                rndPoison();
            }
        }

        /** Generates random coordinates for apple. */
        private void rndApple() {
            int rnd = (int) (Math.random() * Math.sqrt(tiles.PXs) - 1);
            apple_x = ((rnd * tiles.PX));

            rnd = (int) (Math.random() * Math.sqrt(tiles.PXs) - 1);
            apple_y = ((rnd * tiles.PX));
        }

        // New: Generate random poison coordinates
        private void rndPoison() {
            Random rand = new Random();
            poison_x = rand.nextInt(tiles.BOARD_WIDTH / tiles.PX) * tiles.PX;
            poison_y = rand.nextInt(tiles.BOARD_HEIGHT / tiles.PX) * tiles.PX;

            // Make sure poison doesn't overlap with the snake or apple
            for (int i = 0; i < snakeSize; i++) {
                if (poison_x == x[i] && poison_y == y[i]) {
                    rndPoison();
                    return;
                }
            }

            if (poison_x == apple_x && poison_y == apple_y) {
                rndPoison();
            }
        }

        /** Simply prints a gameOver-message to screen when called. */
        private void gameOver(Graphics g) {
            g.setColor(Color.white);
            g.setFont(new Font("Sans serif", Font.BOLD, 20));
            g.drawString(("Game Over! You ate " + (getScore()) + " apples!"),
                    tiles.BOARD_WIDTH / 4, tiles.BOARD_HEIGHT / 2);
            g.drawString("Press space to restart",
                    tiles.BOARD_WIDTH / 4 + 20, tiles.BOARD_HEIGHT / 2 + 30);

            /* Restart game if space is pressed. */
            if (pressedKey == KeyEvent.VK_SPACE){
                playOn = true;
                pressedKey = KeyEvent.VK_DOWN;
                setVisible(false);
                dispose();
                // Stop background music when restarting
                stopBackgroundMusicOnRestart();
                Snake s = new Snake();

            }
        }

        private void moveSnake() {
            for (int i = snakeSize; i > 0; i--) {
                x[i] = x[(i - 1)];
                y[i] = y[(i - 1)];
            }

            switch (pressedKey) {
                case KeyEvent.VK_DOWN:
                    y[0] += tiles.PX;
                    break;
                case KeyEvent.VK_UP:
                    y[0] -= tiles.PX;
                    break;
                case KeyEvent.VK_LEFT:
                    x[0] -= tiles.PX;
                    break;
                case KeyEvent.VK_RIGHT:
                    x[0] += tiles.PX;
                    break;
            }
        }

        private String getScore() {
            return "Score: " + (snakeSize - 3);
        }
        public void displayAllScores(Graphics g) {
            g.setColor(Color.white);
            g.setFont(new Font("Sans serif", Font.BOLD, 15));
            g.drawString("All Scores:", 550, 100);

            int i = 1;
            for (Map.Entry<String, Integer> entry : allScores.entrySet()) {
                g.drawString(i + ". " + entry.getKey() + ": " + entry.getValue(), 550, 120 + i * 20);
                i++;
            }
        }

        private void saveScore() {
            String playerName = "Player";
            int score = snakeSize - 3;

            // Add the new score to the existing scores
            allScores.put(playerName, score);

            // Save all scores to the file
            saveScoresToFile();
        }

        // Load previous scores from a file
        private void loadPreviousScores() {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SCORES_FILE))) {
                // Read all scores from the file
                allScores = (HashMap<String, Integer>) ois.readObject();
            } catch (FileNotFoundException e) {
                // Ignore if the file is not found (first run, no scores yet)
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            // Repaint the board to display all scores
            repaint();
        }



        // Save scores to a file
        private void saveScoresToFile() {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SCORES_FILE))) {
                oos.writeObject(allScores);
                System.out.println("Scores saved to file.");
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error saving scores to file.");
            }
        }
    }
}