package AutoSyntaxAnalyzer;

/**
 * @author JwZheng
 */
public class Production {

    /**
     * 非终结符
     */
    private String nonTerminal;

    /**
     * 结果
     */
    private String[] result;

    public Production(String nonTerminal, String[] result) {
        this.nonTerminal = nonTerminal;
        this.result = result;
    }

    public String getNonTerminal() {
        return nonTerminal;
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
        return String.format("%-2s  ->  %-10s", nonTerminal, temp);
    }

}
