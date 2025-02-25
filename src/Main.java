import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Запуск игры в отдельном потоке GUI (Event Dispatch Thread)
        EventQueue.invokeLater(() -> {
            Game game = new Game();
            game.setVisible(true);
        });
    }
}