package com.futureskyltd.app.helper;


import android.text.InputFilter;
import android.text.Spanned;

/**
 * Filter for controlling maximum new lines in EditText.
 */
public class MaxLinesInputFilter implements InputFilter {

    private final int mMax;

    public MaxLinesInputFilter(int max) {
        mMax = max;
    }

    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        int newLinesToBeAdded = countOccurrences(source.toString(), '\n');
        int newLinesBefore = countOccurrences(dest.toString(), '\n');
        if (newLinesBefore >= mMax - 1 && newLinesToBeAdded > 0) {
            // filter
            return "";
        }

        // do nothing
        return null;
    }

    /**
     * @return the maximum lines enforced by this input filter
     */
    public int getMax() {
        return mMax;
    }

    /**
     * Counts the number occurrences of the given char.
     *
     * @param string the string
     * @param charAppearance the char
     * @return number of occurrences of the char
     */
    public static int countOccurrences(String string, char charAppearance) {
        int count = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == charAppearance) {
                count++;
            }
        }
        return count;
    }
}