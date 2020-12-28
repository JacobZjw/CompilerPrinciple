package AutoSyntaxAnalyzer;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author JwZheng
 */
public class CreateTable {

    /**
     * 开始符、结束符、空字
     */
    public static final Character START = 'E';
    public static final Character END = '#';
    public static final Character EMP = '$';
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
        setFirst();
        setStrFirst();
        setFollow();
        createPredictTable();
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
        try (RandomAccessFile randomFile = new RandomAccessFile(grammarFile, "r")) {
            String line;
            while ((line = randomFile.readLine()) != null) {
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
//        //全部置初始值
//        for (int i = 0; i < vnArray.length; i++) {
//            for (int j = 0; j < vtArray.length; j++) {
//                table[i + 1][j + 1] = "";
//            }
//        }

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
}
