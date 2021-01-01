package SyntaxAnalyzer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

/**
 * @author JwZheng
 */
public class SyntaxAnalyzer {

    private int steps;
    private String inputString;
    /**
     * 符号栈
     */
    private Stack<String> symbolStack;
    /**
     * 输入串
     */
    private LinkedList<String> inputQueue;
    /**
     * LL(1)分析表
     */
    private Production[][] productions;
    /**
     * 非终结符
     */
    private HashMap<String, Integer> nonTerminal;
    /**
     * 终结符
     */
    private HashMap<String, Integer> terminal;


    public SyntaxAnalyzer(String inputString) {
        this.steps = 1;
        this.inputString = inputString.replace(" ", "");
        init();
    }

    public static void main(String[] args) {
        SyntaxAnalyzer s = new SyntaxAnalyzer("i+i*(i+i+i+i*i*i*(i+i*i*(i+i)+i))*i");
        s.printProductions();
        s.analyzer();
    }


    public void init() {
        symbolStack = new Stack<>();
        inputQueue = new LinkedList<>();
        for (Character ch : inputString.toCharArray()) {
            inputQueue.add(String.valueOf(ch));
        }
        inputQueue.offer("#");

        nonTerminal = new HashMap<>(5);
        nonTerminal.put("E", 0);
        nonTerminal.put("E`", 1);
        nonTerminal.put("T", 2);
        nonTerminal.put("T`", 3);
        nonTerminal.put("F", 4);

        terminal = new HashMap<>(6);
        terminal.put("i", 0);
        terminal.put("+", 1);
        terminal.put("*", 2);
        terminal.put("(", 3);
        terminal.put(")", 4);
        terminal.put("#", 5);

        productions = new Production[5][6];
        productions[0][0] = new Production("E", "i", new String[]{"T", "E`"});
        productions[0][3] = new Production("E", "(", new String[]{"T", "E`"});

        productions[1][1] = new Production("E`", "+", new String[]{"+", "T", "E`"});
        productions[1][4] = new Production("E`", ")", new String[]{"ε"});
        productions[1][5] = new Production("E`", "#", new String[]{"ε"});

        productions[2][0] = new Production("T", "i", new String[]{"F", "T`"});
        productions[2][3] = new Production("T", "(", new String[]{"F", "T`"});

        productions[3][1] = new Production("T`", "+", new String[]{"ε"});
        productions[3][2] = new Production("T`", "*", new String[]{"*", "F", "T`"});
        productions[3][4] = new Production("T`", ")", new String[]{"ε"});
        productions[3][5] = new Production("T`", "#", new String[]{"ε"});

        productions[4][0] = new Production("F", "i", new String[]{"i"});
        productions[4][3] = new Production("F", "(", new String[]{"(", "E", ")"});

    }

    public void printProductions() {
        System.out.println("LL(1)分析表如下：");
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 6; j++) {
                if (productions[i][j] != null) {
                    System.out.println(productions[i][j].toString());
                }
            }
        }
    }

    public String getInputQueue() {
        String res = "";
        for (String s : inputQueue) {
            res += s;
        }
        res += "  ";
        return res;
    }

    public String getSymbolStack() {
        String res = "";
        for (String s : symbolStack) {
            res += s;
        }
        res += "  ";
        return res;
    }


    public void analyzer() {
        System.out.println("输入串为：" + inputString + "  开始分析 ：");
        symbolStack.push("#");
        symbolStack.push("E");

        String a = inputQueue.poll();
        String x;
        do {
            System.out.println(String.format("第%-2d步，符号栈：%-30s当前输入符号：%-5s输入串：%-20s", steps, getSymbolStack(), a, getInputQueue()));
            x = symbolStack.pop();
            if (terminal.containsKey(x)) {
                if (x.equals(a)) {
                    if (!"#".equals(a)) {
                        a = inputQueue.poll();
                    } else {
                        System.out.println("ACCEPT !");
                        return;
                    }
                } else {
                    System.out.println("ERROR !");
                    return;
                }
            } else if (nonTerminal.containsKey(x)) {
                if (productions[nonTerminal.get(x)][terminal.get(a)] != null) {
                    String[] temp = productions[nonTerminal.get(x)][terminal.get(a)].getResult();
                    int length = temp.length;
                    for (int i = length - 1; i >= 0; i--) {
                        if (!"ε".equals(temp[i])) {
                            symbolStack.push(temp[i]);
                        }
                    }
                } else {
                    System.out.println("ERROR !");
                    return;
                }
            } else {
                System.out.println("ERROR !");
                return;
            }
            steps++;
        } while (!x.equals("#"));
    }

}

