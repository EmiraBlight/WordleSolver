import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

class Main {

    public static void main(String[] args) {
        wordleSolver wordle = new wordleSolver("possible.txt");
        JFrame frame = new JFrame("Wordle Solver");
        JTextField textbox = new JTextField();
        JPanel panel = new JPanel();
        JButton button = new JButton("Get Best Word");
        String[] colors = { "Grey", "Yellow", "Green" };
        textbox.setPreferredSize(new Dimension(250, 40));
        panel.add(textbox);
        JPanel panel1AndAHalf = new JPanel();
        JPanel panel3 = new JPanel();
        JPanel panel4 = new JPanel();
        panel1AndAHalf.add(button);
        JButton button2 = new JButton("Reset");

        JPanel panel2 = new JPanel();
        JLabel messageLabel = new JLabel("Best Guess: " + wordle.getBestWord());
        JList<String>[] colorLists = new JList[5];
        for (int i = 0; i < 5; i++) {
            colorLists[i] = new JList<>(colors);
            panel2.add(colorLists[i]);
        }
        panel3.add(messageLabel);

        button2.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    wordle.initScore();
                    wordle.scoreChars();
                    wordle.scoreWords();
                }
            }
        );
        panel4.add(button2);

        button.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String text = textbox.getText();
                    if (text.length() != 5) {
                        textbox.setBackground(Color.RED); // highlight red if not 5 characters
                    } else {
                        textbox.setBackground(Color.WHITE); // reset to white if valid
                        int[] selectedColors = new int[5];
                        try {
                            for (int i = 0; i < 5; i++) {
                                if (
                                    colorLists[i].getSelectedValue()
                                        .equals("Grey")
                                ) {
                                    selectedColors[i] = 0;
                                }
                                if (
                                    colorLists[i].getSelectedValue()
                                        .equals("Yellow")
                                ) {
                                    selectedColors[i] = 2;
                                }
                                if (
                                    colorLists[i].getSelectedValue()
                                        .equals("Green")
                                ) {
                                    selectedColors[i] = 1;
                                }
                            }
                        } catch (NullPointerException a) {
                            messageLabel.setText(
                                "Not all colors selected, solver not effected!"
                            );
                            return;
                        }
                        feedback uIFeedback = new feedback(
                            text,
                            selectedColors
                        );
                        try {
                            wordle.giveFeedback(uIFeedback);
                            messageLabel.setText(wordle.getBestWord());
                        } catch (IllegalStateException error) {
                            messageLabel.setText(
                                "Illegal input! Wordle solver reset to empty wordle!"
                            );
                            wordle.initScore();
                            wordle.scoreChars();
                            wordle.scoreWords();
                        }
                    }
                }
            }
        );

        // Use a main panel with vertical layout to contain both sub-panels
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(panel);
        mainPanel.add(panel1AndAHalf);
        mainPanel.add(panel2);
        mainPanel.add(panel3);
        mainPanel.add(panel4);

        frame.add(mainPanel);
        frame.setSize(500, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

class wordleSolver {

    String pathToFile;
    HashMap<String, Integer> mapping;
    HashMap<Character, Integer> freq;

    wordleSolver(String pathToFile) {
        this.pathToFile = pathToFile;
        this.initScore();
        this.freq = new HashMap<Character, Integer>();
        this.scoreChars();
        this.scoreWords();
    }

    public void initScore() {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        try {
            File file = new File(this.pathToFile);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.put(line, 0);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }
        this.mapping = result;
    }

    public void scoreChars() {
        this.freq = new HashMap<Character, Integer>(); //easier than reseting all of the scores to 0
        for (String word : this.mapping.keySet()) {
            for (int i = 0; i < 5; i++) {
                try {
                    this.freq.put(
                            word.charAt(i),
                            this.freq.get(word.charAt(i)) + 1
                        );
                } catch (NullPointerException e) {
                    this.freq.put(word.charAt(i), 1);
                }
            }
        }
    }

    public void scoreWords() {
        for (String word : this.mapping.keySet()) {
            int score = 0;
            HashSet<Character> seen = new HashSet<Character>();
            for (int i = 0; i < 5; i++) {
                if (!seen.contains(word.charAt(i))) {
                    score += this.freq.get(word.charAt(i));
                    seen.add(word.charAt(i));
                }
            }
            this.mapping.put(word, score);
        }
    }

    public void giveFeedback(feedback result) {
        for (int index = 0; index < 5; index++) {
            if (result.colors[index] == 0) {
                this.removeChar(result.guess[index]);
            }
            if (result.colors[index] == 1) {
                this.charIsAt(result.guess[index], index);
            }
            if (result.colors[index] == 2) {
                this.charIsInWord(result.guess[index], index);
            }
        }
    }

    public void removeChar(Character toBeRemoved) {
        HashSet<String> toRemove = new HashSet<String>();
        this.freq.put(toBeRemoved, -1000);
        for (String word : this.mapping.keySet()) {
            if (word.contains(toBeRemoved.toString())) {
                toRemove.add(word);
            }
        }

        for (String word : toRemove) {
            this.mapping.remove(word);
        }
    }

    public void removeCharAt(Character toBeRemoved, int index) {
        HashSet<String> toRemove = new HashSet<String>();
        for (String word : this.mapping.keySet()) {
            if (word.charAt(index) == toBeRemoved) {
                toRemove.add(word);
            }
        }
        for (String word : toRemove) {
            this.mapping.remove(word);
        }
    }

    public void charIsAt(Character locatedAt, int index) {
        HashSet<String> toRemove = new HashSet<String>();
        for (String word : this.mapping.keySet()) {
            if (word.charAt(index) != locatedAt) {
                toRemove.add(word);
            }
        }
        for (String word : toRemove) {
            this.mapping.remove(word);
        }
    }

    public void charIsInWord(char inWord, int index) {
        HashSet<String> toRemove = new HashSet<String>();
        for (String word : this.mapping.keySet()) {
            if (word.indexOf(inWord) == -1 || word.charAt(index) == inWord) {
                toRemove.add(word);
            }
        }
        for (String word : toRemove) {
            this.mapping.remove(word);
        }
        this.freq.put(inWord, this.freq.get(inWord) * 2);
    }

    public String getBestWord() {
        int bestScore = Integer.MIN_VALUE;
        String result = "";
        for (String word : this.mapping.keySet()) {
            if (bestScore < this.mapping.get(word)) {
                bestScore = this.mapping.get(word);
                result = word;
            }
        }
        if (result.isEmpty()) {
            throw new IllegalStateException();
        }
        return result;
    }
}

class feedback {

    char[] guess;
    int[] colors;

    /*
    0 == grey
    1 == green
    2 ==  yellow
    */

    feedback(String word, int[] colors) {
        guess = word.toCharArray();
        this.colors = colors;
    }
}
