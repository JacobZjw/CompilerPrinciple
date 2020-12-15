package SyntaxAnalyzer;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author JwZheng
 */
public class Production {

    /**
     * 非终结符
     */
    private String nonTerminal;

    /**
     * 终结符
     */
    private String terminal;

    /**
     * 结果
     */
    private String[] result;

    public Production(String nonTerminal, String terminal, String[] result) {
        this.nonTerminal = nonTerminal;
        this.terminal = terminal;
        this.result = result;
    }

    public String[] getResult() {
        return result;
    }

    @Override
    public String toString() {
        String temp = "";
        for (String i : result) {
            temp += i;
        }
        return String.format("p['%-2s']['%-2s']  :  %-2s  ->  %-10s", nonTerminal, terminal, nonTerminal, temp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Production that = (Production) o;
        return nonTerminal.equals(that.nonTerminal) && Arrays.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        int result1 = Objects.hash(nonTerminal);
        result1 = 31 * result1 + Arrays.hashCode(result);
        return result1;
    }
}
