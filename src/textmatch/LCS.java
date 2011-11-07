package textmatch;

import static java.lang.Math.max;
import static textmatch.GCollectionUtils.*;
import static textmatch.GStringUtils.*;
import static textmatch.CharTree.*;

public class LCS {

    public static int[][] LCSMatrix(final char[] X, final char[] Y) {
        final int m = X.length + 1;
        final int n = Y.length + 1;
        final int[][] C = new int[m][n];
        for (int i = 1; i < m; ++i) {
            for (int j = 1; j < n; ++j) {
                if (X[i-1] == Y[j-1]) {
                    C[i][j] = C[i-1][j-1] + 1;
                } else {
                    C[i][j] = max(C[i][j-1], C[i-1][j]);
                }
            }
        }
        return C;
    }
    
    public static double LCSScore(final char[] X, final char[] Y) {
        return ((double)lastElem(LCSMatrix(X, Y))) / max(X.length, Y.length);
    }
    
    public static Pair<double[][], CharTree[][]> LCSMatrixTemplated(final char[] X, final char[] Y) {
        // X is the templated string
        // % is the substitution character
        final int m = X.length + 1;
        final int n = Y.length + 1;
        final double[][] M = new double[m][n];
        final CharTree[][] T = new CharTree[m][n];
        for (int i = 1; i < m; ++i) {
            for (int j = 1; j < n; ++j) {
                if (X[i-1] == Y[j-1]) {
                    // character match
                    M[i][j] = M[i-1][j-1] + 1;
                    T[i][j] = T[i-1][j-1];
                } else {
                    if (M[i-1][j] > M[i][j-1]) {
                        // Template is missing a character
                        M[i][j] = M[i-1][j] - 0.2;
                        T[i][j] = T[i-1][j];
                        if (X[i-1] == '%') {
                            M[i][j] = M[i-1][j];
                            T[i][j] = new CharTree(T[i][j], '^');
                        }
                    } else {
                        if (X[i-1] == '%') {
                            // Template substitution
                            M[i][j] = M[i][j-1];
                            T[i][j] = new CharTree(T[i][j-1], Y[j-1]);
                        } else {
                            // Target is missing a character
                            M[i][j] = M[i][j-1] - 0.2;
                            T[i][j] = T[i][j-1];
                        }
                    }
                }
            }
        }
        return makePair(M, T);
    }
    
    public static String substituteIntoTemplate(String templatedString, String[] substitutions) {
        StringBuilder b = new StringBuilder();
        int substitutionIndex = 0;
        for (char c : templatedString.toCharArray()) {
            if (c == '%') {
                b.append(substitutions[substitutionIndex]);
                ++substitutionIndex;
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }
    
    public static String substituteIntoTemplateMarked(String templatedString, String[] substitutions) {
        StringBuilder b = new StringBuilder();
        int substitutionIndex = 0;
        for (char c : templatedString.toCharArray()) {
            if (c == '%') {
                b.append("#{");
                b.append(substitutions[substitutionIndex]);
                b.append("}");
                ++substitutionIndex;
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }
    
    public static String[] getSubstitutedStrings(String str) {
        if (str.length() == 0)
            return new String[0];
        return split(str.substring(1), '^');
    }
    
    public static String[] getSubstitutedStrings(CharTree[][] charTree) {
        String str = charTreeToString(lastElem(charTree));
        if (str.length() == 0)
            return new String[0];
        return split(str.substring(1), '^');
    }
    
    public static double LCSTemplatedScore(final char[] X, final char[] Y) {
        final Pair<double[][], CharTree[][]> p = LCSMatrixTemplated(X, Y);
        return LCSTemplatedScore(X, Y, p.Item1, p.Item2);
    }
    
    public static double LCSTemplatedScore(final char[] X, final char[] Y, final double[][] M, CharTree[][] T) {
        final double matchedTemplateChars = lastElem(M);
        final int numTemplateChars = X.length - count(X, '%');
        final String templateWords = charTreeToString(lastElem(T));
        final int templateWordsLength = templateWords.length() - count(templateWords, '^');
        final int rawTargetTextLength = Y.length - templateWordsLength;
        return ((double)matchedTemplateChars) / max(numTemplateChars, rawTargetTextLength);
    }
}
