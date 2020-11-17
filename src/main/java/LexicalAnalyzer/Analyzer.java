package LexicalAnalyzer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Jv____
 */
public class Analyzer {
    static Map<String, Integer> keyWord = new HashMap<>();
    private final BufferedReader reader;
    private final List<Binary> binaries;
    private char character;
    private boolean isEnd = false;

    public Analyzer(String fileName) throws FileNotFoundException {
        this.binaries = new ArrayList<>();
        this.reader = new BufferedReader(new FileReader(fileName));
        initKeyWords();
    }

    public static void main(String[] args) {
        try {
            Analyzer analyzer = new Analyzer("C:\\Users\\Jv____\\IdeaProjects\\SimpleLexicalAnalyzer\\src\\main\\resources\\testCase.txt");
            analyzer.printResult();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean readCh(char ch) throws IOException {
        readCh();
        return this.character == ch && !isEnd;
    }

    public void readCh() throws IOException {
        int temp = reader.read();
        if (temp != -1) {
            this.character = (char) temp;
        } else {
            this.isEnd = true;
        }
    }

    public void initKeyWords() {
        keyWord.put("main", 1);
        keyWord.put("int", 2);
        keyWord.put("char", 3);
        keyWord.put("if", 4);
        keyWord.put("else", 5);
        keyWord.put("for", 6);
        keyWord.put("while", 7);
        keyWord.put("return", 8);
    }

    public boolean isLetter() throws IOException {
        if (!Character.isLetter(this.character)) {
            return false;
        }
        StringBuilder tokenBuilder = new StringBuilder();
        tokenBuilder.append(character);
        readCh();
        while (Character.isLetter(this.character) || Character.isDigit(this.character)) {
            tokenBuilder.append(character);
            readCh();
        }
        String token = tokenBuilder.toString();
        if (keyWord.containsKey(token)) {
            //关键字
            int syn = keyWord.get(token);
            binaries.add(new Binary(syn, token));
        } else {
            //标识符
            binaries.add(new Binary(10, token));
        }
        return true;
    }

    public boolean isDigit() throws IOException {
        if (!Character.isDigit(this.character)) {
            return false;
        }
        int sum = 0;
        do {
            sum = sum * 10 + character - '0';
            readCh();
        } while (Character.isDigit(this.character));
        binaries.add(new Binary(20, String.valueOf(sum)));
        return true;
    }

    public boolean isCharacter() throws IOException {
        StringBuilder tokenBuilder = new StringBuilder();
        int syn;
        String token;
        switch (character) {
            case '=':
                tokenBuilder.append(character);
                if (readCh('=')) {
                    //==
                    tokenBuilder.append(character);
                    syn = 29;
                } else {
                    //'='
                    syn = 21;
                }
                token = tokenBuilder.toString();
                binaries.add(new Binary(syn, token));
                return true;
            case '+':
                tokenBuilder.append(character);
                syn = 22;
                token = tokenBuilder.toString();
                binaries.add(new Binary(syn, token));
                readCh();
                return true;
            case '-':
                tokenBuilder.append(character);
                syn = 23;
                token = tokenBuilder.toString();
                binaries.add(new Binary(syn, token));
                readCh();
                return true;
            case '*':
                tokenBuilder.append(character);
                syn = 24;
                token = tokenBuilder.toString();
                binaries.add(new Binary(syn, token));
                readCh();
                return true;
            case '/':
                tokenBuilder.append(character);
                syn = 25;
                token = tokenBuilder.toString();
                binaries.add(new Binary(syn, token));
                readCh();
                return true;
            case '(':
                tokenBuilder.append(character);
                syn = 26;
                token = tokenBuilder.toString();
                binaries.add(new Binary(syn, token));
                readCh();
                return true;
            case ')':
                tokenBuilder.append(character);
                syn = 27;
                token = tokenBuilder.toString();
                binaries.add(new Binary(syn, token));
                readCh();
                return true;
            case '[':
                tokenBuilder.append(character);
                syn = 28;
                token = tokenBuilder.toString();
                binaries.add(new Binary(syn, token));
                readCh();
                return true;
            case ']':
                tokenBuilder.append(character);
                syn = 29;
                token = tokenBuilder.toString();
                binaries.add(new Binary(syn, token));
                readCh();
                return true;
            case '{':
                tokenBuilder.append(character);
                syn = 30;
                token = tokenBuilder.toString();
                binaries.add(new Binary(syn, token));
                readCh();
                return true;
            case '}':
                tokenBuilder.append(character);
                syn = 31;
                token = tokenBuilder.toString();
                binaries.add(new Binary(syn, token));
                readCh();
                return true;
            case ',':
                tokenBuilder.append(character);
                syn = 32;
                token = tokenBuilder.toString();
                binaries.add(new Binary(syn, token));
                readCh();
                return true;
            case ':':
                tokenBuilder.append(character);
                syn = 33;
                token = tokenBuilder.toString();
                binaries.add(new Binary(syn, token));
                readCh();
                return true;
            case ';':
                tokenBuilder.append(character);
                syn = 34;
                token = tokenBuilder.toString();
                binaries.add(new Binary(syn, token));
                readCh();
                return true;
            case '>':
                tokenBuilder.append(character);
                if (readCh('=')) {
                    tokenBuilder.append(character);
                    syn = 37;
                } else {
                    syn = 35;

                }
                token = tokenBuilder.toString();
                binaries.add(new Binary(syn, token));
                return true;
            case '<':
                tokenBuilder.append(character);
                if (readCh('=')) {
                    tokenBuilder.append(character);
                    syn = 38;
                } else {
                    syn = 36;

                }
                token = tokenBuilder.toString();
                binaries.add(new Binary(syn, token));
                return true;
            case '!':
                tokenBuilder.append(character);
                if (readCh('=')) {
                    tokenBuilder.append(character);
                    syn = 40;
                } else {
                    syn = 41;
                }
                token = tokenBuilder.toString();
                binaries.add(new Binary(syn, token));
                return true;
            case '"':
                while (!readCh('"')) {
                    tokenBuilder.append(character);
                }
                readCh();
                syn = 50;
                token = tokenBuilder.toString();
                binaries.add(new Binary(syn, token));
                return true;
            default:
                readCh();
                return false;
        }
    }

    public boolean isEscapeCharacter() throws IOException {
        if (this.character == ' ' || this.character == '\n' || this.character == '\r') {
            readCh();
            return true;
        }
        return false;
    }

    public void analyzer() throws IOException {
        readCh();
        while (!isEnd) {
            //每循环一次,只添加一个二元组
            if (!isEscapeCharacter() && !isLetter() && !isDigit() && !isCharacter()) {
                System.out.println("Get a ERROR !");
            }
        }
    }

    public void printResult() throws IOException {
        analyzer();
        for (Binary binary : binaries) {
            String code = "null";
            if (binary.syn == 20) {
                code = "INT";
            } else if (binary.syn == 10) {
                code = "ID";
            } else if (binary.syn == 50) {
                code = "STRING";
            }
            System.out.println("<" + binary.token + "," + code + ">");
        }
    }

    static class Binary {
        int syn;
        String token;
        public Binary(int syn, String token) {
            this.syn = syn;
            this.token = token;
        }
    }

}
