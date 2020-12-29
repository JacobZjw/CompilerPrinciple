package AutoSyntaxAnalyzer;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

/**
 * @author JwZheng
 */
public class SyntaxAnalyzer {

    private final String inputString;
    private int steps;
    private Stack<Character> symbolStack;
    private LinkedList<Character> inputQueue;

    private String[][] table;
    private HashSet<Character> terminal;
    private HashSet<Character> nonTerminal;


    public SyntaxAnalyzer(String inputString) {
        this.steps = 1;
        this.inputString = inputString.replace(" ", "");
        init();
    }

    public static void main(String[] args) {
        SyntaxAnalyzer s = new SyntaxAnalyzer("accbd");
        s.printTable();
        s.analyzer();
    }

    public void init() {
        CreateTable data = new CreateTable("src/main/resources/grammar1.txt");

        symbolStack = new Stack<>();
        inputQueue = new LinkedList<>();
        for (Character ch : inputString.toCharArray()) {
            inputQueue.add(ch);
        }
        inputQueue.offer(CreateTable.END);

        table = data.getTable();
        terminal = data.getTerminal();
        nonTerminal = data.getNonTerminal();

        terminal.remove(CreateTable.EMP);
        terminal.add(CreateTable.END);
    }


    public void printTable() {
        System.out.println("LL(1)预测分析表如下：");
        for (String[] strings : table) {
            for (String str : strings) {
                System.out.printf("%10s", str);
            }
            System.out.println();
        }
    }


    public String getInputQueue() {
        StringBuilder res = new StringBuilder();
        for (Character ch : inputQueue) {
            res.append(ch);
        }
        res.append("  ");
        return res.toString();
    }

    public String getSymbolStack() {
        StringBuilder res = new StringBuilder();
        for (Character ch : symbolStack) {
            res.append(ch);
        }
        res.append("  ");
        return res.toString();
    }

    public void analyzer() {
        System.out.println("\n输入串为：" + inputString + "  开始分析 ：");
        symbolStack.push(CreateTable.END);
        symbolStack.push(CreateTable.START);

        Character a = inputQueue.poll();
        Character x;
        do {
            System.out.printf("第%-2d步，符号栈：%-30s当前输入符号：%-5s输入串：%-20s%n", steps, getSymbolStack(), a, getInputQueue());
            x = symbolStack.pop();
            if (terminal.contains(x)) {
                if (x.equals(a)) {
                    if (!CreateTable.END.equals(a)) {
                        a = inputQueue.poll();
                    } else {
                        System.out.println("ACCEPT !");
                        return;
                    }
                } else {
                    System.out.println("ERROR !");
                    return;
                }
            } else if (nonTerminal.contains(x)) {
                String temp = searchTable(x, a);
                if (temp != null) {
                    char[] resultArray = temp.toCharArray();
                    int length = resultArray.length;
                    for (int i = length - 1; i >= 0; i--) {
                        if (!CreateTable.EMP.equals(resultArray[i])) {
                            symbolStack.push(resultArray[i]);
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
        } while (!x.equals(CreateTable.END));


    }

    public String searchTable(Character nonChr, Character chr) {
        int i = 0, j = 0;
        for (int k = 1; k < table.length; k++) {
            if (table[k][0].charAt(0) == nonChr) {
                i = k;
                break;
            }
        }

        for (int k = 1; k < table[0].length; k++) {
            if (table[0][k].charAt(0) == chr) {
                j = k;
                break;
            }
        }

        return table[i][j];
    }
}
