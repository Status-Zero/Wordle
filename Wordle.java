import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.transform.*;
import javafx.scene.input.*;
import javafx.scene.control.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.event.*;
import javafx.geometry.*;

import java.util.*;
import java.lang.*;
import java.io.*;

public class Wordle extends Application{
    @Override
    public void start(Stage stage) {
        List<String> words = new ArrayList<>();
        List<String> answers = new ArrayList<>();
        try {
            try (Scanner scan = new Scanner(new FileReader("allwords.txt"))) {
                //file size: 12972 lines
                while (scan.hasNextLine()) {
                    words.add(scan.nextLine());
                }
            }
            try (Scanner scan = new Scanner(new FileReader("answers.txt"))) {
                //file size: 2315 lines
                while (scan.hasNextLine()) {
                    answers.add(scan.nextLine());
                }
            }
        } catch (IOException e) {
        }
        
        Collections.shuffle(answers);
        String w = answers.get(0).toUpperCase();
        String[] answer = new String[5];
        for (int i = 0; i < 5; i++) {
            answer[i] = String.valueOf(w.charAt(i));
        }

        Map<String, String> colors = new HashMap<>();
        colors.put("green", "-fx-background-color: #6aaa64;");
        colors.put("yellow", "-fx-background-color: #c9b458;");
        colors.put("gray", "-fx-background-color: #787c7e;");
        colors.put(
            "default", 
            "-fx-background-color: lightgray;" +
            "-fx-background-radius: 5;" +
            "-fx-border-color: black;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 5;"
        );
        
        Pane root = new Pane();
        root.setPrefWidth(600);
        root.setPrefHeight(700);
        root.setFocusTraversable(true);
        
        ArrayList<Button> keys = new ArrayList<>();
        Button[][] tiles = new Button[6][5];
        int[] cords = {0, 0};
        
        
        double layoutXtiles = 147.5;
        double layoutYtiles = 130;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 5; col++) {
                Button b = new Button();
                b.setPrefSize(55, 55);
                b.setLayoutX(layoutXtiles);
                b.setLayoutY(layoutYtiles);
                
                b.setStyle(colors.get("default"));
                b.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
                tiles[row][col] = b;
                root.getChildren().add(b);
                
                layoutXtiles += 60;
            }
            layoutXtiles = 147.5;
            layoutYtiles += 60;
        }
        
        EventHandler<ActionEvent> handler = e -> {
          Button clicked = (Button) e.getSource();
          tiles[cords[0]][cords[1]].setText(clicked.getText());
          if (cords[1] == 4) {
              cords[1] = 0;
              cords[0]++;
          } else {
              cords[1]++;
          }
        };
        
        String[] letters = {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "A", "S", "D", "F", "G",
        "H", "J", "K", "L", "Z", "X", "C", "V", "B", "N", "M"};
        double layoutXkeys = 75.0;
        double layoutYkeys = 500.0;
        for (int x = 0; x < 26; x++) {
            Button b = new Button(letters[x]);
            b.setPrefSize(40, 55);
            b.setStyle(colors.get("default"));
            b.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
            b.setUserData("default");

            b.setLayoutX(layoutXkeys);
            b.setLayoutY(layoutYkeys);
            layoutXkeys += 45;
            if (x == 9) {
                layoutYkeys += 60;
                layoutXkeys = 97.5;
            } else if (x == 18) {
                layoutYkeys += 60;
                layoutXkeys = 142.5;
            }
            
            b.setOnAction(handler);
            b.setFocusTraversable(false);
            keys.add(b);
            root.getChildren().add(b);
        }
        
        Scene scene = new Scene(root);
        root.setFocusTraversable(true);
        root.requestFocus();
        scene.setOnMouseClicked(e -> root.requestFocus());
        
        String[] guess = {"", "", "", "", ""};
        boolean[] last = {false};
        boolean[] won = {false};
        scene.setOnKeyPressed(event -> {
            if (!won[0]) {
                if (event.getCode().isLetterKey()) {
                    if (cords[1] < 5) {
                        String letter = event.getCode().getName().toUpperCase();
                        tiles[cords[0]][cords[1]].setText(letter);
                        guess[cords[1]] = letter;
                        cords[1]++;
                        last[0] = (guess[4] != null && !guess[4].equals(""));
                    }
                }
                if (event.getCode() == KeyCode.BACK_SPACE) {
                    if (cords[0] == 0 && cords[1] == 0) {
                        return;
                    }
                    if (cords[1] > 0) {
                        cords[1]--;
                        tiles[cords[0]][cords[1]].setText("");
                        guess[cords[1]] = "";
                    }
                }
                if ((event.getCode() == KeyCode.ENTER && last[0]) && isGuess(guess, words)) {
                    if (Arrays.deepEquals(guess, answer)) {
                        for (int x = 0; x < 5; x++) {
                            tiles[cords[0]][x].setStyle(colors.get("green"));
                            won[0] = true;
                        }
                        return;
                    }
                    String[] answerColors = compare(guess, answer);
                    int row = cords[0];
                    for (int x = 0; x < 5; x++) {
                        Button tile = tiles[row][x];
                        tile.setStyle(colors.get(answerColors[x]));
                    }
                    List<Button> tempKeys = new ArrayList<>();
                    for (int x = 0; x < guess.length; x++) {
                        for (int y = 0; y < keys.size(); y++) {
                            if (keys.get(y).getText().equals(guess[x])) {
                                tempKeys.add(keys.get(y));
                            }
                        }
                    }
                    for (int x = 0; x < tempKeys.size(); x++) {
                        if (tempKeys.get(x).getUserData().equals("default")) {
                            if (answerColors[x].equals("yellow") || answerColors[x].equals("green")) {
                                tempKeys.get(x).setStyle(colors.get("green"));
                                tempKeys.get(x).setUserData("green");
                            } else {
                                tempKeys.get(x).setStyle(colors.get("gray"));
                                tempKeys.get(x).setUserData("gray");
                            }
                        }
                    }
                
                    cords[0]++;
                    cords[1] = 0;
                    for (int x = 0; x < guess.length; x++) {
                        guess[x] = "";
                    }
                    last[0] = false;
                    int counter = 0;
                }
            }
            if (event.getCode() == KeyCode.ESCAPE) {
                cords[0] = 0;
                cords[1] = 0;
                for (int x = 0; x < tiles.length; x++) {
                    for (int y = 0; y < tiles[x].length; y++) {
                        tiles[x][y].setText("");
                        tiles[x][y].setStyle(colors.get("default"));
                    }
                }
                for (int x = 0; x < guess.length; x++) {
                    guess[x] = "";
                }
                last[0] = false;
                won[0] = false;
                Collections.shuffle(answers);
                String word = answers.get(0).toUpperCase();
                for (int x = 0; x < 5; x++) {
                    answer[x] = String.valueOf(word.charAt(x));
                }
                for(int x = 0; x < keys.size(); x++) {
                    keys.get(x).setStyle(colors.get("default"));
                    keys.get(x).setUserData("default");
                }
            }
        });

        
        stage.setTitle("Wordle");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    public String[] compare(String[] guess, String[] answer) {
        String[] result = new String[5];
        boolean[] used = new boolean[5];
        for (int x = 0; x < 5; x++) {
            if (guess[x].equals(answer[x])) {
                result[x] = "green";
                used[x] = true;
            }
        }
        for (int x = 0; x < 5; x++) {
            if (result[x] != null) {
                continue;
            }
            boolean found = false;
            for (int y = 0; y < 5; y++) {
                if (!used[y] && guess[x].equals(answer[y])) {
                    found = true;
                    used[y] = true;
                    break;
                }
            }
            result[x] = found ? "yellow" : "gray";
        }
        return result;
    }
    public boolean isGuess(String[] guess, List<String> words) {
        String guessWord = String.join("", guess).toLowerCase();
        return words.contains(guessWord);
    }
    public static void main (String[] args) {
        launch(args);
    }
}
