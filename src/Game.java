import java.awt.image.BufferStrategy;
import java.util.*;
import java.io.*;
import java.awt.*;

public class Game extends Canvas implements Runnable{

    @Serial
    private static final long serialVersionUID = -1442798787354930462L;

    public static final int WIDTH = 1200, HEIGHT = WIDTH/16*9;

    private Thread thread;
    private boolean running = false;

    private Random r = new Random();
    private Handler handler;
    private HUD hud;
    private Stats stats;
    private Player player;

    public Game() {
        handler = new Handler();
        hud = new HUD();
        stats = new Stats();

        this.addKeyListener(new KeyInput(handler));
        new Window(WIDTH, HEIGHT, "HORIZON 極速狂飆", this);

        player = new Player(WIDTH/2-16, HEIGHT/2+200, ID.Player, handler);
        for (int i = 1; i <= 10; i++) {
            new BasicObstacle(
                    r.nextInt(-1000, Game.WIDTH+1000),
                    r.nextInt(-500, 300)-300,
                    r.nextInt(50, 200),
                    r.nextInt(200, 400),
                    ID.Obstacle, handler);
        }
    }

    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
    public static int reverseClamp(int val, int min, int max) {
        if (val < min || val > max)
            return val;
        else if (Math.abs(val - min) > Math.abs(max - val))
            return max;
        else if (Math.abs(val - min) < Math.abs(max - val))
            return min;
        return val;
    }
    public static boolean isOut(int val, int min, int max) {
        return (Math.max(min, Math.min(max, val)) == min || Math.max(min, Math.min(max, val)) == max);
    }

    private void tick() {
        handler.tick();
        hud.tick();

    }

    private void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();
        float[] HSB = Color.RGBtoHSB(59, 56, 53, null);

        g.setColor(Color.getHSBColor(HSB[0], HSB[1], HSB[2]));
        g.fillRect(0,0,WIDTH, HEIGHT);

        handler.render(g);
        hud.render(g);

        g.dispose();
        bs.show();
    }

    public synchronized void start() {
        thread = new Thread(this);
        thread.start();
        running = true;
    }

    public synchronized void stop() {
        try {
            thread.join();
            running = false;
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        this.requestFocus();
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        while (running) {
            long now = System.nanoTime();
            delta += (now-lastTime) / ns;
            lastTime = now;
            while (delta >= 1) {
                tick();
                delta--;
            }
            if (running)
                render();
            frames++;

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println("FPS: " + frames);
                frames = 0;
            }
        }
        stop();
    }

    public static void main(String[] args) {
        new Game();
    }
}
