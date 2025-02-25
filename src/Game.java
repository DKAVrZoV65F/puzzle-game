import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game extends JFrame implements ActionListener {

    private final int DESIRED_WIDTH = 400;
    private final int GRID_ROWS = 4;
    private final int GRID_COLS = 3;
    private ArrayList<Point> solution;
    private ArrayList<PuzzleButton> buttons;
    private JPanel panel;
    private BufferedImage source, resized;
    private int width, height;
    private PuzzleButton lastButton;

    public Game() {
        initUI();
    }

    public void initUI() {
        solution = new ArrayList<>();
        buttons = new ArrayList<>();

        // Создание решения (правильный порядок пазлов)
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                solution.add(new Point(i, j));
            }
        }

        panel = new JPanel();
        panel.setBorder(BorderFactory.createLineBorder(Color.gray));
        panel.setLayout(new GridLayout(GRID_ROWS, GRID_COLS));

        try {
            source = loadImage();
            int height = getNewHeight(source.getWidth(), source.getHeight());
            resized = resizeImage(source, DESIRED_WIDTH, height, BufferedImage.TYPE_INT_ARGB);
        } catch (IOException exception) {
            System.err.println(exception.getMessage());
            System.exit(0);
        }

        width = resized.getWidth();
        height = resized.getHeight();

        add(panel, BorderLayout.CENTER);

        // Разрезаем изображение на кусочки и создаём кнопки
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                Image image = createImage(new FilteredImageSource(resized.getSource(), new CropImageFilter(
                        j * width / GRID_COLS, i * height / GRID_ROWS, width / GRID_COLS, height / GRID_ROWS)));
                PuzzleButton button = new PuzzleButton(image);
                button.putClientProperty("position", new Point(i, j));
                button.addActionListener(this);

                if (i == GRID_ROWS - 1 && j == GRID_COLS - 1) {
                    lastButton = new PuzzleButton();
                    lastButton.setBorderPainted(false);
                    lastButton.setContentAreaFilled(false);
                    lastButton.setLastButton(true);
                    lastButton.putClientProperty("position", new Point(i, j));
                    buttons.add(lastButton);
                } else {
                    buttons.add(button);
                }
            }
        }

        shuffleButtons();
        for (PuzzleButton button : buttons) {
            panel.add(button);
        }

        setTitle("Puzzle Game");
        setSize(DESIRED_WIDTH + 20, DESIRED_WIDTH + 40);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void shuffleButtons() {
        // Перемешиваем кнопки
        do {
            Collections.shuffle(buttons);
        } while (!isSolvable() || isSolved());
    }

    private boolean isSolvable() {
        // Проверка на решаемость пазла
        int inversions = 0;
        List<Point> positions = new ArrayList<>();
        for (PuzzleButton button : buttons) {
            Point position = (Point) button.getClientProperty("position");
            positions.add(position);
        }

        for (int i = 0; i < positions.size(); i++) {
            for (int j = i + 1; j < positions.size(); j++) {
                Point p1 = positions.get(i);
                Point p2 = positions.get(j);

                if (!p1.equals(solution.get(solution.size() - 1)) &&
                        !p2.equals(solution.get(solution.size() - 1)) &&
                        (p1.x * GRID_COLS + p1.y) > (p2.x * GRID_COLS + p2.y)) {
                    inversions++;
                }
            }
        }

        return inversions % 2 == 0;
    }

    private boolean isSolved() {
        // Проверка на завершение игры
        for (int i = 0; i < buttons.size(); i++) {
            Point current = (Point) buttons.get(i).getClientProperty("position");
            Point correct = solution.get(i);
            if (!current.equals(correct)) {
                return false;
            }
        }
        return true;
    }

    private BufferedImage resizeImage(BufferedImage original, int width, int height, int type) {
        BufferedImage resizeImage = new BufferedImage(width, height, type);
        Graphics2D graphics = resizeImage.createGraphics();
        graphics.drawImage(original, 0, 0, width, height, null);
        graphics.dispose();
        return resizeImage;
    }

    private BufferedImage loadImage() throws IOException {
        return ImageIO.read(new File("/Users/kamchatka/Projects/Java/LearningGUI/resources/picture.jpg")); // Укажите путь к вашему изображению
    }

    private int getNewHeight(int width, int height) {
        double ratio = (double) DESIRED_WIDTH / width;
        return (int) (height * ratio);
    }

    private void swapButtons(PuzzleButton button) {
        int emptyIndex = buttons.indexOf(lastButton);
        int buttonIndex = buttons.indexOf(button);

        // Меняем кнопки местами
        Collections.swap(buttons, emptyIndex, buttonIndex);

        panel.removeAll();
        for (PuzzleButton btn : buttons) {
            panel.add(btn);
        }
        panel.validate();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PuzzleButton button = (PuzzleButton) e.getSource();
        int buttonIndex = buttons.indexOf(button);
        int emptyIndex = buttons.indexOf(lastButton);

        // Проверяем, можно ли переместить кнопку (она должна быть рядом с пустой)
        if ((buttonIndex == emptyIndex - 1 && emptyIndex % GRID_COLS != 0) ||
                (buttonIndex == emptyIndex + 1 && buttonIndex % GRID_COLS != 0) ||
                (buttonIndex == emptyIndex - GRID_COLS) ||
                (buttonIndex == emptyIndex + GRID_COLS)) {
            swapButtons(button);
        }

        // Проверяем, завершена ли игра
        if (isSolved()) {
            JOptionPane.showMessageDialog(this, "Поздравляем, вы собрали пазл!");
        }
    }
}