package org.example.solver;

import org.chocosolver.solver.Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

import static org.chocosolver.util.tools.MathUtils.pow;


public class equationSolver {
    private static String inputPath = "D:\\eclipse-workspace\\multiStirngMatching\\conf\\input";

    public static void main(String[] args) {
        List<String> eachLine = getEachLine(inputPath);
        List<inType> inTypes = new ArrayList<>();
        List<likeType> likeTypes = new ArrayList<>();
        getInTypeAndLikeType(inTypes, likeTypes, eachLine);
        solve(inTypes, likeTypes);
    }

    public static void solve(List<inType> inTypes, List<likeType> likeTypes) {
        if (inTypes.size() != 0 || likeTypes.size() != 0) {
            //求方程个数
            int n = 0;
            if (inTypes.size() != 0) {
                for (inType currentInType : inTypes) {
                    n += currentInType.paraNum;
                }
            }
            if (likeTypes.size() != 0) {
                //todo
            }

            //构造in类型的方程
            Model model = new Model();
            int len = getMaxPercentage(inTypes);
            List<IntVar[]> allEquation = new ArrayList<>();
            for (int i = 0; i < inTypes.size() + 1; i++) {
                int max;
                if (i == 0) {
                    max = pow(10, len);
                } else {
                    max = 1;
                }
                IntVar[] x = model.intVarArray("x", n, 0, max);
                allEquation.add(x);
            }
            String op = "="; // among ">=", ">", "<=", "<", "="
            List<IntVar[]> vectorMuls = new ArrayList<>();
            for (int i = 0; i < inTypes.size(); i++) {
                IntVar[] vectorMul = new IntVar[n];
                vectorMuls.add(vectorMul);
            }
            for (int i = 0; i < inTypes.size(); i++) {
                for (int j = 0; j < n; j++) {
                    vectorMuls.get(i)[j] = allEquation.get(0)[j].mul(allEquation.get(i + 1)[j]).intVar();
                }
            }
            model.sum(allEquation.get(0), "<=", pow(10, len)).post();
            for (int i = 0; i < inTypes.size(); i++) {
                String strPercentage = inTypes.get(i).getPercentage();
                int percentage = (int) (Double.parseDouble(strPercentage) * pow(10, len));
                model.sum(vectorMuls.get(i), "=", percentage).post();
                //System.out.println(inTypes.get(i).getParaNum());
                model.sum(allEquation.get(i + 1), "=", inTypes.get(i).getParaNum()).post();
            }
            Solver solver = model.getSolver();
            if (solver.solve()) {
                for (IntVar[] intVars : allEquation) {
                    System.out.println(Arrays.toString(intVars));
                }
            }
        }
    }

    public static int getMaxPercentage(List<inType> inTypes) {
        int len = 0;
        for (inType eachInType : inTypes) {
            int num = eachInType.getPercentage().length();
            if (num > len) {
                len = num;
            }
        }
        if (len <= 4) {
            return 2;
        } else {
            return len - 2;
        }
    }

    public static List<String> getEachLine(String intputPath) {
        File file = new File(intputPath);
        List<String> eachLine = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file));) {
            eachLine = bufferedReader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return eachLine;
    }

    public static void getInTypeAndLikeType(List<inType> inTypes, List<likeType> likeTypes, List<String> eachLine) {
        for (String line : eachLine) {
            List<String> allParas = getAllParas(line);
            String probability = getProbability(line);
            if (line.contains("in") || line.contains("IN")) {
                inType currentInType = new inType(allParas.size(), probability);
                inTypes.add(currentInType);
            } else if (line.contains("like") || line.contains("LIKE")) {
                //todo
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    public static List<String> getAllParas(String line) {
        int left = line.indexOf("(");
        int currentRight = line.indexOf(")");
        int right = currentRight;
        while (currentRight != -1) {
            right = currentRight;
            currentRight = line.indexOf(")", currentRight + 1);
        }
        if (right == -1 || left == -1) {
            throw new UnsupportedOperationException();
        }
        String[] allParaString = line.substring(left + 1, right).split(",");
        List<String> allParas = new ArrayList<>();
        for (String s : allParaString) {
            allParas.add(s.trim());
        }
        return allParas;
    }

    public static String getProbability(String line) {
        int currentIndex = line.indexOf("=");
        int index = currentIndex;
        while (currentIndex != -1) {
            index = currentIndex;
            currentIndex = line.indexOf("=", currentIndex + 1);
        }
        return line.substring(index + 1).trim();
    }
}