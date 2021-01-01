package AutoSyntaxAnalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * @author JwZheng
 */
public class CreateTable {

    /**
     * 开始符、结束符、空字
     */
    public static final Character START = 'E';
    public static final Character END = '#';
    public static final Character EMP = 'ε';
    /**
     * 读取的LL1文法
     */
    private File grammarFile;
    /**
     * 非终结符
     */
    private HashSet<Character> nonTerminal;

    /**
     * 终结符
     */
    private HashSet<Character> terminal;

    /**
     * 预测分析表
     */
    private String[][] table;

    /**
     * 产生式
     */
    private HashMap<Character, ArrayList<String>> production;

    /**
     * first集
     */
    private HashMap<Character, HashSet<Character>> first;
    private HashMap<String, HashSet<Character>> strFirst;

    /**
     * follow集
     */
    private HashMap<Character, HashSet<Character>> follow;


    public CreateTable(String filePath) {
        this.nonTerminal = new HashSet<>();
        this.terminal = new HashSet<>();
        this.production = new HashMap<>();

        this.first = new HashMap<>();
        this.strFirst = new HashMap<>();
        this.follow = new HashMap<>();

        this.grammarFile = new File(filePath);

        setProductions();
        convertToLL1();
        setFirst();
        setStrFirst();
        setFollow();
        createPredictTable();
        printGrammar();
        System.out.println();
    }

    public HashSet<Character> getNonTerminal() {
        return nonTerminal;
    }

    public HashSet<Character> getTerminal() {
        return terminal;
    }

    public String[][] getTable() {
        return table;
    }

    /**
     * 设置终结符、非终结符、产生式
     */
    public void setProductions() {
        try (BufferedReader reader = new BufferedReader(new FileReader(grammarFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replace(" ", "");

                String[] strings = line.split("->")[1].split("\\|");
                //非终结符
                char nonChr = line.charAt(0);

                //产生式
                ArrayList<String> list = production.containsKey(nonChr) ? production.get(nonChr) : new ArrayList<>();
                Collections.addAll(list, strings);

                nonTerminal.add(nonChr);
                production.put(nonChr, list);
            }
            //终结符
            for (Character nonChr : nonTerminal) {
                ArrayList<String> list = production.get(nonChr);
                for (String string : list) {
                    for (Character chr : string.toCharArray()) {
                        if (!nonTerminal.contains(chr)) {
                            terminal.add(chr);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成first集
     */
    public void setFirst() {
        for (Character ch : nonTerminal) {
            setFirst(ch);
        }
    }

    public void setFirst(Character ch) {
        ArrayList<String> chProduction = production.get(ch);
        HashSet<Character> set = first.containsKey(ch) ? first.get(ch) : new HashSet<>();

        //终结符，则加入
        if (terminal.contains(ch)) {
            set.add(ch);
            first.put(ch, set);
            return;
        }

        //非终结符，寻找其first集
        for (String str : chProduction) {
            int i = 0;
            while (i < str.length()) {
                char chr = str.charAt(i);
                setFirst(chr);
                HashSet<Character> chrSet = first.get(chr);

                //若非空，将其first集加入
                for (Character tmp : chrSet) {
                    if (!tmp.equals(EMP)) {
                        set.add(tmp);
                    }
                }
                //若包含空串，处理下一个符号
                if (chrSet.contains(EMP)) {
                    ++i;
                } else {
                    break;
                }
            }
            //所有的符号的first集都包含空，则把空串加入
            if (i == str.length()) {
                set.add(EMP);
            }
        }
        first.put(ch, set);
    }

    public void setStrFirst() {
        for (Character ch : nonTerminal) {
            ArrayList<String> chProduction = production.get(ch);
            for (String s : chProduction) {
                setStrFirst(s);
            }
        }
    }

    public void setStrFirst(String str) {
        HashSet<Character> set = strFirst.containsKey(str) ? strFirst.get(str) : new HashSet<>();
        int i = 0;
        //从左到右扫描
        while (i < str.length()) {
            Character chr = str.charAt(i);
            if (!first.containsKey(chr)) {
                setFirst(chr);
            }
            HashSet<Character> chrSet = first.get(chr);
            //若非空，将其first集加入
            for (Character ch : chrSet) {
                if (!ch.equals(EMP)) {
                    set.add(ch);
                }
            }
            //若包含空串，处理下一个符号
            if (chrSet.contains(EMP)) {
                ++i;
            } else {
                break;
            }
            //所有的符号的first集都包含空，则把空串加入
            if (i == str.length()) {
                set.add(EMP);
            }
        }
        strFirst.put(str, set);
    }

    /**
     * 生成follow集
     */
    public void setFollow() {
        for (int i = 0; i < 5; i++) {
            for (Character ch : nonTerminal) {
                setFollow(ch);
            }
        }
    }

    public void setFollow(Character ch) {
        HashSet<Character> set = follow.containsKey(ch) ? follow.get(ch) : new HashSet<>();
        //开始符，添加 #
        if (ch.equals(START)) {
            set.add(END);
        }

        //查找所有产生式，确定ch的后跟终结符
        for (Character chr : nonTerminal) {
            ArrayList<String> chrProduction = production.get(chr);
            for (String str : chrProduction) {
                for (int i = 0; i < str.length(); i++) {
                    if (str.charAt(i) == ch && i + 1 < str.length() && terminal.contains(str.charAt(i + 1))) {
                        set.add(str.charAt(i + 1));
                    }
                }
            }
        }
        follow.put(ch, set);

        ArrayList<String> chProduction = production.get(ch);
        for (String str : chProduction) {
            int i = str.length() - 1;
            while (i >= 0) {
                Character chr = str.charAt(i);

                //只处理非终结符
                if (nonTerminal.contains(chr)) {

                    if (str.length() - i - 1 > 0) {
                        String right = str.substring(i + 1);
                        HashSet<Character> tmpSet;
                        if (right.length() == 1) {
                            if (!first.containsKey(right.charAt(0))) {
                                setFirst(right.charAt(0));
                            }
                            tmpSet = first.get(right.charAt(0));
                        } else {
                            if (!strFirst.containsKey(right)) {
                                setStrFirst(right);
                            }
                            tmpSet = strFirst.get(right);
                        }
                        HashSet<Character> tmpSet2 = follow.containsKey(chr) ? follow.get(chr) : new HashSet<>();
                        for (Character tmpChr : tmpSet) {
                            if (!tmpChr.equals(EMP)) {
                                tmpSet2.add(tmpChr);
                            }
                        }
                        follow.put(chr, tmpSet2);

                        if (tmpSet.contains(EMP)) {
                            if (!chr.equals(ch)) {
                                HashSet<Character> tmpSet3 = follow.containsKey(chr) ? follow.get(chr) : new HashSet<>();
                                tmpSet3.addAll(set);
                                follow.put(chr, tmpSet3);
                            }
                        }
                    } else {
                        if (!chr.equals(ch)) {
                            HashSet<Character> tmpSet3 = follow.containsKey(chr) ? follow.get(chr) : new HashSet<>();
                            tmpSet3.addAll(set);
                            follow.put(chr, tmpSet3);
                        }
                    }

                }
                --i;
            }
        }
    }

    /**
     * 生成预测分析表
     */
    public void createPredictTable() {
        Object[] vnArray = nonTerminal.toArray();
        Object[] vtArray = terminal.toArray();
        //初始化预测分析表
        table = new String[vnArray.length + 1][vtArray.length + 1];
        table[0][0] = "Vn/Vt";
        //首行、首列
        for (int i = 0; i < vtArray.length; i++) {
            table[0][i + 1] = (vtArray[i].toString().charAt(0) == EMP) ? String.valueOf(END) : vtArray[i].toString();
        }
        for (int i = 0; i < vnArray.length; i++) {
            table[i + 1][0] = vnArray[i].toString();
        }

        //插入生成式
        for (Character ch : nonTerminal) {
            ArrayList<String> chProduction = production.get(ch);
            for (String s : chProduction) {
                HashSet<Character> set = strFirst.get(s);
                for (Character chr : set) {
                    insertToTable(ch, chr, s);
                }
                if (set.contains(EMP)) {
                    HashSet<Character> chFollow = follow.get(ch);
                    if (chFollow.contains(END)) {
                        insertToTable(ch, END, s);
                    }
                    for (Character chr : chFollow) {
                        insertToTable(ch, chr, s);
                    }
                }
            }
        }


    }

    public void insertToTable(Character a, Character b, String str) {
        if (b.equals(EMP)) {
            b = END;
        }
        for (int i = 0; i < nonTerminal.size() + 1; i++) {
            if (table[i][0].charAt(0) == a) {
                for (int j = 0; j < terminal.size() + 1; j++) {
                    if (table[0][j].charAt(0) == b) {
                        table[i][j] = str;
                        return;
                    }
                }
            }
        }
    }

    /**
     * 转换为LL(1)文法
     */
    public HashMap<Character, ArrayList<String>> convertToLL1() {
        List<Character> newVn = new ArrayList<>(nonTerminal);
        List<Character> newVt = new ArrayList<>(terminal);
        HashMap<Character, ArrayList<String>> newProductions = new HashMap<>(production);

        for (int i = 0; i < newVn.size(); ++i) {
            Character chVn = newVn.get(i);
            ArrayList<String> chProductions = new ArrayList<>(newProductions.get(chVn));

            for (int j = 0; j < i; j++) {
                Character chVnPre = newVn.get(j);
                ArrayList<String> tempProductions = new ArrayList<>(chProductions);
                for (String s : tempProductions) {
                    /* 如果规则形如P_i->P_jγ */
                    if (s.indexOf(chVnPre) == 0) {
                        /* 如果规则形如P_i->P_jγ */
                        chProductions.remove(s);
                        String subStr = s.substring(1);
                        for (String production : newProductions.get(chVnPre)) {
                            chProductions.add(production + subStr);
                        }
                    }
                }

            }
            /* 消除P_i的新规则中的所有左递归 */
            ArrayList<String> newProductionsHasLR = new ArrayList<>();
            ArrayList<String> newProductionHasNoLR = new ArrayList<>();

            for (String s : chProductions) {
                if (s.indexOf(chVn) == 0) {
                    newProductionsHasLR.add(s.substring(1));
                } else {
                    newProductionHasNoLR.add(s);
                }
            }

            ArrayList<String> newSet = new ArrayList<>();

            if (newProductionsHasLR.size() != 0) {
                Character newSymbol = findNonTermialNotInSet(newVn);
                for (String beta : newProductionHasNoLR) {
                    newSet.add(beta + newSymbol);
                }
                ArrayList<String> newSymbolSet = new ArrayList<>();
                newProductions.put(newSymbol, newSymbolSet);
                for (String alpa : newProductionsHasLR) {
                    newSymbolSet.add(alpa + newSymbol);
                }
                newSymbolSet.add(String.valueOf(EMP));
                newVn.add(newSymbol);
            } else {
                /* 如果规则都不含左递归 */
                newSet.addAll(chProductions);
            }
            newProductions.replace(chVn, newSet);
        }

        /*化简现在的文法*/
        Set<Character> newVnSet = new HashSet<>();
        newVnSet.add(START);
        boolean flag;
        do {
            flag = false;
            for (int i = 0; i < newVnSet.size(); ++i) {
                Character vnCh = (Character) newVnSet.toArray()[i];
                ArrayList<String> production = newProductions.get(vnCh);
                for (String s : production) {
                    for (Character c : s.toCharArray()) {
                        if (Character.isUpperCase(c)) {
                            int fsLen = newVnSet.size();
                            newVnSet.add(c);
                            flag |= (fsLen != newVnSet.size());
                        }
                    }
                }
            }
        } while (flag);

        HashMap<Character, ArrayList<String>> tempProductions = new HashMap<>();
        for (Character tmpCh : newVnSet) {
            tempProductions.put(tmpCh, newProductions.get(tmpCh));
        }

        /* 提左因子，直到没有左因子停止 */
        do {
            flag = false;
            List<Character> curVn = new ArrayList<>(newVnSet);
            for (Character c : newVnSet) {
                ArrayList<String> productions = tempProductions.get(c);
                HashMap<Character, ArrayList<String>> counter = new HashMap<>();
                for (String production : productions) {
                    Character first = production.charAt(0);
                    if (counter.containsKey(first)) {
                        counter.get(first).add(production);
                    } else {
                        counter.put(first, new ArrayList<>());
                        counter.get(first).add(production);
                    }
                }
                HashMap<Character, ArrayList<String>> counterFilter = new HashMap<>();
                for (Character key : counter.keySet()) {
                    if (counter.get(key).size() >= 2) {
                        counterFilter.put(key, counter.get(key));
                    }
                }

                /* 看看是否真的没有左因子 */
                flag |= (counterFilter.size() > 0);
                /* 如果有左因子，将A->δβ_1|δβ_2|...|δβ_n|γ_1|γ_2|...|γ_m
                 * 改写为A->δA'|γ_1|γ_2|...|γ_m
                 * A'->β_1|β_2|...|β_n
                 */
                if (counterFilter.size() > 0) {
                    for (Character delta : counterFilter.keySet()) {
                        Character newSymbol = findNonTermialNotInSet(curVn);
                        curVn.add(newSymbol);
                        ArrayList<String> production = counterFilter.get(delta);
                        productions.removeAll(production);
                        production.add(String.valueOf(delta) + newSymbol);
                        ArrayList<String> production2 = new ArrayList<>();
                        for (String s : production) {
                            if (s.length() < 2) {
                                production2.add(s.substring(0));
                            } else {
                                production2.add(s.substring(1));
                            }
                        }
                        tempProductions.put(newSymbol, production2);
                    }
                }
            }
            newVnSet.addAll(curVn);
        } while (flag);
        generateLL1(tempProductions);
        return tempProductions;
    }

    private Character findNonTermialNotInSet(List<Character> newVn) {
        Character res;
        for (char c = 'A'; c <= 'Z'; ++c) {
            if (!newVn.contains(c)) {
                res = new Character(c);
                return res;
            }
        }
        return null;
    }

    private void generateLL1(HashMap<Character, ArrayList<String>> productions) {
        this.terminal.clear();
        this.nonTerminal.clear();
        for (Character ch : productions.keySet()) {
            nonTerminal.add(ch);
        }
        for (Character vnCh : nonTerminal) {
            ArrayList<String> list = productions.get(vnCh);
            for (String s : list) {
                for (Character c : s.toCharArray()) {
                    if (!nonTerminal.contains(c)) {
                        terminal.add(c);
                    }
                }
            }
        }
        this.production = productions;
    }


    /**
     * 打印LL(1)文法
     **/
    public void printGrammar() {
        System.out.println("转换后的LL(1)文法G[E]：");
        for (Character ch : production.keySet()) {
            for (String str : production.get(ch)) {
                System.out.printf("%s -> %s\n", ch, str);
            }
        }
    }


}