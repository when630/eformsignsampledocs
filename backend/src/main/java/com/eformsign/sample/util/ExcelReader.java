package com.eformsign.sample.util;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelReader {

    public List<List<String>> readExcel(InputStream inputStream) throws Exception {
        List<List<String>> result = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            List<String> rowData = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                rowData.add(cell.toString().trim());
            }
            result.add(rowData);
        }

        workbook.close();
        return result;
    }
}