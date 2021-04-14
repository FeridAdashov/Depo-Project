package texel.com.depoproject.HelperClasses;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

public class TableGenerator {

    private final int PADDING_SIZE = 2;

    public String generateTable(ArrayList<String> headersArrayList, ArrayList<ArrayList<String>> rowsArrayList, int... overRiddenHeaderHeight) {
        StringBuilder stringBuilder = new StringBuilder();

        int rowHeight = overRiddenHeaderHeight.length > 0 ? overRiddenHeaderHeight[0] : 1;

        Map<Integer, Integer> columnMaxWidthMapping = getMaximumWidhtofTable(headersArrayList, rowsArrayList);

        String NEW_LINE = "\n";
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(NEW_LINE);
        createRowLine(stringBuilder, headersArrayList.size(), columnMaxWidthMapping);
        stringBuilder.append(NEW_LINE);

        for (int headerIndex = 0; headerIndex < headersArrayList.size(); headerIndex++) {
            fillCell(stringBuilder, headersArrayList.get(headerIndex), headerIndex, columnMaxWidthMapping);
        }

        stringBuilder.append(NEW_LINE);

        createRowLine(stringBuilder, headersArrayList.size(), columnMaxWidthMapping);

        for (ArrayList<String> row : rowsArrayList) {

            for (int i = 0; i < rowHeight; i++) {
                stringBuilder.append(NEW_LINE);
            }

            for (int cellIndex = 0; cellIndex < row.size(); cellIndex++) {
                fillCell(stringBuilder, row.get(cellIndex), cellIndex, columnMaxWidthMapping);
            }
        }

        stringBuilder.append(NEW_LINE);
        createRowLine(stringBuilder, headersArrayList.size(), columnMaxWidthMapping);
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(NEW_LINE);

        return stringBuilder.toString();
    }

    private void fillSpace(StringBuilder stringBuilder, int length) {
        for (int i = 0; i < length; i++) stringBuilder.append(" ");
    }

    private void createRowLine(StringBuilder stringBuilder, int headersArrayListSize, Map<Integer, Integer> columnMaxWidthMapping) {
        for (int i = 0; i < headersArrayListSize; i++) {
            String TABLE_JOINT_SYMBOL = "+";
            if (i == 0) {
                stringBuilder.append(TABLE_JOINT_SYMBOL);
            }

            for (int j = 0; j < columnMaxWidthMapping.get(i) + PADDING_SIZE * 2; j++) {
                String TABLE_H_SPLIT_SYMBOL = "-";
                stringBuilder.append(TABLE_H_SPLIT_SYMBOL);
            }
            stringBuilder.append(TABLE_JOINT_SYMBOL);
        }
    }


    private Map<Integer, Integer> getMaximumWidhtofTable(ArrayList<String> headersArrayList, ArrayList<ArrayList<String>> rowsArrayList) {
        HashMap<Integer, Integer> columnMaxWidthMapping = new HashMap<>();

        for (int columnIndex = 0; columnIndex < headersArrayList.size(); columnIndex++) {
            columnMaxWidthMapping.put(columnIndex, 0);
        }

        for (int columnIndex = 0; columnIndex < headersArrayList.size(); columnIndex++) {

            if (headersArrayList.get(columnIndex).length() > columnMaxWidthMapping.get(columnIndex)) {
                columnMaxWidthMapping.put(columnIndex, headersArrayList.get(columnIndex).length());
            }
        }

        for (ArrayList<String> row : rowsArrayList) {

            for (int columnIndex = 0; columnIndex < row.size(); columnIndex++) {

                if (row.get(columnIndex).length() > columnMaxWidthMapping.get(columnIndex)) {
                    columnMaxWidthMapping.put(columnIndex, row.get(columnIndex).length());
                }
            }
        }

        for (int columnIndex = 0; columnIndex < headersArrayList.size(); columnIndex++) {

            if (columnMaxWidthMapping.get(columnIndex) % 2 != 0) {
                columnMaxWidthMapping.put(columnIndex, columnMaxWidthMapping.get(columnIndex) + 1);
            }
        }

        return columnMaxWidthMapping;
    }

    private int getOptimumCellPadding(int cellIndex, int datalength, Map<Integer, Integer> columnMaxWidthMapping, int cellPaddingSize) {
        if (datalength % 2 != 0) {
            datalength++;
        }

        if (datalength < columnMaxWidthMapping.get(cellIndex)) {
            cellPaddingSize = cellPaddingSize + (columnMaxWidthMapping.get(cellIndex) - datalength) / 2;
        }

        return cellPaddingSize;
    }

    private void fillCell(StringBuilder stringBuilder, String cell, int cellIndex, Map<Integer, Integer> columnMaxWidthMapping) {
        int cellPaddingSize = getOptimumCellPadding(cellIndex, cell.length(), columnMaxWidthMapping, PADDING_SIZE);

        String TABLE_V_SPLIT_SYMBOL = "|";
        if (cellIndex == 0) {
            stringBuilder.append(TABLE_V_SPLIT_SYMBOL);
        }

        fillSpace(stringBuilder, cellPaddingSize);
        stringBuilder.append(cell);
        if (cell.length() % 2 != 0) {
            stringBuilder.append(" ");
        }

        fillSpace(stringBuilder, cellPaddingSize);

        stringBuilder.append(TABLE_V_SPLIT_SYMBOL);
    }
}
