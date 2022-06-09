import javax.swing.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

public class Menu implements MouseListener {
    private Handler handler;
    private Game game;
    private Leaderboard leaderboard;
    private Button[][] buttons; //menuButtons, gameOverButtons, leaderboardButtons, optionsButtons, pauseButtons, gameButtons;
    private int focus, lastFocus, menuScreen = menuCode.get(Game.gameState);
    private TextField textField;
    private int tempSeed, tempDistance, tempDeathMethod;
    private LeaderboardEntry tempLeaderboardEntry;
    private boolean isInvalid;

    private int lmx, lmy;
    private boolean[] lastKeyboardState = new boolean[4]; // 0 for up, 1 for down, 2 for enter, 4 for esc.
    private static HashMap<Game.state, Integer> menuCode = new HashMap<>() {
        {
            put(Game.state.Menu, 0);
            put(Game.state.GameOver, 1);
            put(Game.state.Leaderboard, 2);
            put(Game.state.Options, 3);
            put(Game.state.LeaderboardEntry, 4);
            put(Game.state.Pause, 5);
            put(Game.state.Game, 6);
        }
    };

    public Menu(Game game, Leaderboard leaderboard, Handler handler) {
        focus = 0;
        lastFocus = 0;
        lmx = 0;
        lmy = 0;
        this.game = game;
        this.leaderboard = leaderboard;
        this.handler = handler;
        textField = new TextField("Player");
        game.setLayout(null);
        game.add(textField);
        textField.setBounds(262, Game.HEIGHT - 220, 400, 25);
        textField.setBackground(new Color(255, 255, 255));
        textField.setFont(new Font("Monospaced", Font.BOLD, 18));
        textField.setVisible(false);

        isInvalid = false;

        buttons = new Button[7][];
        buttons[0] = new Button[]{
                new Button("Start", 90, Game.HEIGHT - 220 + 21, 200, 27, game),
                new Button("Options", 90, Game.HEIGHT - 180 + 21, 200, 27, game),
                new Button("Leaderboard", 90, Game.HEIGHT - 140 + 21, 200, 27, game),
                new Button("Quit", 90, Game.HEIGHT - 100 + 21, 200, 27, game),
        };
        buttons[1] = new Button[]{
                new Button("Reload", 110, Game.HEIGHT - 240, 200, 27, game),
                new Button("Menu", 110, Game.HEIGHT - 200, 200, 27, game)
        };
        buttons[2] = new Button[]{
                new Button("Back", 60, Game.HEIGHT - 45, 200, 27, game),
        };
        buttons[3] = new Button[]{
                new Button("Back", 60, Game.HEIGHT - 50, 200, 27, game),
        };
        buttons[4] = new Button[]{
                new Button("Enter", 850, Game.HEIGHT - 220, 200, 27, game),
                new Button("Skip", 850, Game.HEIGHT - 180, 200, 27, game),
        };
        buttons[5] = new Button[]{
                new Button("Resume", 850, Game.HEIGHT - 220, 200, 27, game),
                new Button("Menu", 850, Game.HEIGHT - 180, 200, 27, game),
        };
        buttons[6] = new Button[]{
                new Button("Pause", 960, Game.HEIGHT - 40, 200, 27, game),
        };
    }

    public void setTempLeaderboard(int distance, int seed, int deathMethod) {
        tempLeaderboardEntry = new LeaderboardEntry(distance, seed, deathMethod, 150, Game.HEIGHT - 270);
        textField.setVisible(true);
    }

    public void tick() throws IOException {
        Point p = MouseInfo.getPointerInfo().getLocation();
        int mx = (int) (p.getX() - game.getLocationOnScreen().getX());
        int my = (int) (p.getY() - game.getLocationOnScreen().getY());
        menuScreen = menuCode.get(Game.gameState);
        //If mouse moved
        if (lmx != mx || lmy != my) {
            for (int i = 0; i < buttons[menuScreen].length; i++) {
                if (buttons[menuScreen][i].isOver(mx, my)) {
                    focus = i;
                }
            }
            lmx = mx;
            lmy = my;
        }
        //If a key is pressed
        if ((lastKeyboardState[0] != Stats.getKeyPress()[0][3] || lastKeyboardState[1] != Stats.getKeyPress()[0][4])
                || lastKeyboardState[2] != Stats.getKeyPress()[0][5] || lastKeyboardState[3] != Stats.getKeyPress()[0][6]) {
            //If ENTER is either pressed or lifted && is currently pressed
            if (lastKeyboardState[2] != Stats.getKeyPress()[0][5] && Stats.getKeyPress()[0][5]) {
                buttonEntered();
            }
            //If UP is either pressed or lifted && is currently pressed
            else if (lastKeyboardState[0] != Stats.getKeyPress()[0][3] && Stats.getKeyPress()[0][3]) {
                focus = (focus + buttons[menuScreen].length - 1) % buttons[menuScreen].length;
            }
            //If DOWN is either pressed or lifted && is currently pressed
            else if (lastKeyboardState[1] != Stats.getKeyPress()[0][4] && Stats.getKeyPress()[0][4]) {
                focus = (focus + 1) % buttons[menuScreen].length;
            } else if (lastKeyboardState[3] != Stats.getKeyPress()[0][6] && Stats.getKeyPress()[0][6] && Game.gameState == Game.state.Game) {
                Game.gameState = Game.state.Pause;
            }
            //Must check if state changed for computer to read the actively changing key
            //Update all keyboard states
            lastKeyboardState[0] = Stats.getKeyPress()[0][3];
            lastKeyboardState[1] = Stats.getKeyPress()[0][4];
            lastKeyboardState[2] = Stats.getKeyPress()[0][5];
            lastKeyboardState[3] = Stats.getKeyPress()[0][6];
        }

        //Require reset of focus for each menu change
        for (int i = 0; i < buttons[menuScreen].length; i++) {
            buttons[menuScreen][i].setFocused(false);
        }
        buttons[menuScreen][focus].setFocused(true);

        if (Game.gameState == Game.state.LeaderboardEntry) {
            //A few spaces added to avoid StringIndexOutOfBounds when cutting the string as it is edited
            tempLeaderboardEntry.setName(removeSpaces((removeSpaces(textField.getText()) + "   ").substring(0, Math.min(16, removeSpaces(textField.getText()).length()))));
        }
    }

    public void render(Graphics g) {
        Font courier = new Font("Courier New", Font.PLAIN, 18);
        Font courier2 = new Font("Courier New", Font.BOLD, 60);

        if (game.gameState == Game.state.Menu) {
            g.setColor(Color.white);
            g.setFont(courier);
            g.drawImage(Toolkit.getDefaultToolkit().getImage("appdata/pics/mainBackground2.png"), 0, 0, 400 * 3, 225 * 3, game);
            g.drawImage(Toolkit.getDefaultToolkit().getImage("appdata/pics/title.png"), (Game.WIDTH - 310 * 3) / 2, 270, 310 * 3, 24 * 3, game);
        }
        if (game.gameState == Game.state.GameOver) {
            g.setFont(courier2);
            g.setColor(new Color(219, 198, 191, 50));
            g.fillRect(70, 70, Game.WIDTH - 140, Game.HEIGHT - 140);
            g.drawImage(Toolkit.getDefaultToolkit().getImage("appdata/pics/gameOver.png"), 110, 110, 138 * 3, 42 * 3, game);
            g.setColor(Color.white);
            g.drawImage(Toolkit.getDefaultToolkit().getImage("appdata/pics/distance.png"), 110, 300, 83 * 3, 5 * 3, game);
            g.drawString(String.valueOf(Stats.speederDistance), 110, 380);
        }
        if (game.gameState == Game.state.Leaderboard) {
            g.setFont(courier2);
            g.setColor(new Color(0, 0, 0, 200));
            g.drawImage(Toolkit.getDefaultToolkit().getImage("appdata/pics/leaderboardBackground.png"), 0, 0, Game.WIDTH, Game.HEIGHT, game);
            g.drawImage(Toolkit.getDefaultToolkit().getImage("appdata/pics/leaderboard.png"), 60, 40, 318 * 3, 15 * 3, game);
            leaderboard.render(g);
        }
        if (game.gameState == Game.state.Options) {
            g.drawImage(Toolkit.getDefaultToolkit().getImage("appdata/pics/optionsBackground.png"), 0, 0, Game.WIDTH, Game.HEIGHT, game);
        }
        if (game.gameState == Game.state.LeaderboardEntry) {
            g.setColor(new Color(219, 198, 191, 50));
            g.fillRect(120, 140, Game.WIDTH - 240, Game.HEIGHT - 280);
            g.drawImage(Toolkit.getDefaultToolkit().getImage("appdata/pics/gameOver.png"), 155, 170, 138 * 3, 42 * 3, game);
            g.setFont(new Font("Courier New", Font.PLAIN, 25));
            g.setColor(Color.white);
            g.drawString("Congratulations! You have entered the leaderboard!", 150, Game.HEIGHT - 320);
            g.setFont(new Font("Courier New", Font.PLAIN, 18));
            g.drawString("Enter your name below (16 character max): ", 150, Game.HEIGHT - 290);
            if (isInvalid) {
                g.setColor(Color.red);
                g.setFont(new Font("Courier New", Font.BOLD, 18));
                g.drawString("<E> Invalid Name.", 270, Game.HEIGHT - 170);
            }
            tempLeaderboardEntry.render(g);
        }
        if (game.gameState == Game.state.Pause) {
            g.setColor(new Color(35, 35, 35, 199));
            g.fillRect(120, 140, Game.WIDTH - 240, Game.HEIGHT - 280);
            g.drawImage(Toolkit.getDefaultToolkit().getImage("appdata/pics/gamePaused.png"), 155, 170, 189 * 3, 40 * 3, game);
            g.drawImage(Toolkit.getDefaultToolkit().getImage("appdata/pics/distance.png"), 155, 360, 83 * 3, 5 * 3, game);
            g.setColor(Color.white);
            g.setFont(courier2);
            g.drawString(String.valueOf(Stats.speederDistance), 155, 440);
        }
        if (game.gameState == Game.state.Game) {
            g.setColor(new Color(35, 35, 35, 199));
            g.fillRect(943, Game.HEIGHT-45, 234, 37);
        }
        for (Button b : buttons[menuScreen]) {
            b.render(g);
        }
    }

    public void buttonEntered() throws IOException {
        if (game.gameState == Game.state.Menu) {
            if (focus == 0) {
                game.gameState = Game.state.Game;
                game.reset();
                System.out.println("Game Reset");
            } else if (focus == 1) {
                focus = 0;
                game.gameState = Game.state.Options;
            } else if (focus == 2) {
                focus = 0;
                game.gameState = Game.state.Leaderboard;
            } else if (focus == 3) {
                System.exit(0);
            }
        } else if (game.gameState == Game.state.GameOver) {
            if (focus == 0) {
                game.gameState = Game.state.Game;
                game.reset();
                System.out.println("Game Reset");
            } else if (focus == 1) {
                focus = 0;
                game.gameState = Game.state.Menu;
            }
        } else if (game.gameState == Game.state.Leaderboard) {
            if (focus == 0) {
                game.gameState = Game.state.Menu;
            }
        } else if (game.gameState == Game.state.Options) {
            if (focus == 0) {
                game.gameState = Game.state.Menu;
            }
        } else if (game.gameState == Game.state.LeaderboardEntry) {
            if (focus == 0) {
                tempLeaderboardEntry.setName(removeSpaces((textField.getText() + "   ").substring(0, Math.min(16, textField.getText().length()))));
                if (tempLeaderboardEntry.getName().equals("")) {
                    isInvalid = true;
                } else {
                    leaderboard.add(tempLeaderboardEntry);
                    textField.setVisible(false);
                    textField.setText("Player");
                    isInvalid = false;
                    game.gameState = Game.state.GameOver;
                }
            } else if (focus == 1) {
                focus = 0;
                textField.setVisible(false);
                textField.setText("Player");
                isInvalid = false;
                game.gameState = Game.state.GameOver;
            }
        } else if (game.gameState == Game.state.Pause) {
            if (focus == 0) {
                focus = 0;
                Game.gameState = Game.state.Game;
            } else if (focus == 1) {
                focus = 0;
                Game.gameState = Game.state.Menu;
            }
        } else if (game.gameState == Game.state.Game) {
            if (focus == 0) {
                focus = 0;
                Game.gameState = Game.state.Pause;
            }
        }
    }


    public void buttonEntered(int mx, int my) throws IOException {
        if (game.gameState == Game.state.Menu) {
            if (buttons[0][0].isOver(mx, my)) {
                focus = 0;
                game.gameState = Game.state.Game;
                game.reset();
                System.out.println("Game Reset");
            } else if (buttons[0][1].isOver(mx, my)) {
                focus = 0;
                game.gameState = Game.state.Options;
            } else if (buttons[0][2].isOver(mx, my)) {
                focus = 0;
                game.gameState = Game.state.Leaderboard;
            } else if (buttons[0][3].isOver(mx, my)) {
                System.exit(0);
            }
        } else if (game.gameState == Game.state.GameOver) {
            if (buttons[1][0].isOver(mx, my)) {
                focus = 0;
                game.gameState = Game.state.Game;
                game.reset();
                System.out.println("Game Reset");
            } else if (buttons[1][1].isOver(mx, my)) {
                focus = 0;
                game.gameState = Game.state.Menu;
            }
        } else if (game.gameState == Game.state.Leaderboard) {
            if (buttons[2][0].isOver(mx, my)) {
                focus = 0;
                game.gameState = Game.state.Menu;
            }
        } else if (game.gameState == Game.state.Options) {
            if (buttons[3][0].isOver(mx, my)) {
                focus = 0;
                game.gameState = Game.state.Menu;
            }
        } else if (game.gameState == Game.state.LeaderboardEntry) {
            if (buttons[4][0].isOver(mx, my)) {
                tempLeaderboardEntry.setName(removeSpaces((textField.getText() + "   ").substring(0, Math.min(16, textField.getText().length()))));
                if (tempLeaderboardEntry.getName().equals("")) {
                    isInvalid = true;
                } else {
                    leaderboard.add(tempLeaderboardEntry);
                    textField.setVisible(false);
                    textField.setText("Player");
                    isInvalid = false;
                    focus = 0;
                    game.gameState = Game.state.GameOver;
                }
            } else if (buttons[4][1].isOver(mx, my)) {
                textField.setVisible(false);
                textField.setText("Player");
                isInvalid = false;
                focus = 0;
                game.gameState = Game.state.GameOver;
            }
        } else if (game.gameState == Game.state.Pause) {
            if (buttons[5][0].isOver(mx, my)) {
                focus = 0;
                Game.gameState = Game.state.Game;
            } else if (buttons[5][1].isOver(mx, my)) {
                focus = 0;
                Game.gameState = Game.state.Menu;
            }
        } else if (game.gameState == Game.state.Game) {
            if (buttons[6][0].isOver(mx, my)) {
                focus = 0;
                Game.gameState = Game.state.Pause;
            }
        }
    }

    private String removeSpaces(String s) {
        StringBuilder sb = new StringBuilder();
        String[] temp = s.split(" ");
        for (String tempString : temp) {
            sb.append(tempString);
        }
        System.out.println(Arrays.toString(temp));
        return sb.toString();
    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        try {
            buttonEntered(mx, my);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    private boolean mouseOver(int mx, int my, int x, int y, int width, int height) {
        return (mx >= x && mx <= x + width && my >= y && my <= y + height);
    }

    public void setFocus(int focus) {
        this.focus = focus;
    }
}
