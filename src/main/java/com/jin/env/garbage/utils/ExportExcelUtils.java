package com.jin.env.garbage.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

public class ExportExcelUtils {
    public static void main(String[] args) throws Exception {

    }

    public static CellStyle createCellStyle(Workbook wb, Font font){
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFont(font);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        cellStyle.setWrapText(true);
        return cellStyle;
    }

    private static void createCell(Row row, int column, String value, CellStyle cellStyle) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(cellStyle);
    }

    public static void  createTitle(Sheet sheet, String value, int firstRow, int lastRow, int firstCol, int lastCol, CellStyle cellStyle){
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(30);
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
        createCell(titleRow, 0, value, cellStyle);
    }
    public static void  createHeader(Sheet sheet,  List<String> exportHeaders, CellStyle cellStyle){
        Row headerRow = sheet.createRow(1);
        headerRow.setHeightInPoints(30);
        for (int i = 0; i < exportHeaders.size(); i++) {
            createCell(headerRow, i, exportHeaders.get(i), cellStyle);
        }
    }

    public static void createContent(Sheet sheet, List<Object[]> data, CellStyle cellStyle){
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i +2 );
            row.setHeightInPoints(30);
            Object[] o = data.get(i);
            for (int j = 0; j < o.length; j++) {
                 Object oo = o[j] == null? "":o[j];
                createCell(row, j, oo.toString(), cellStyle);
            }
        }
    }


    public static void exportExcel(HttpServletResponse response, List<String> headers, String fileName , String showTitle, Integer[] cellStart, List<Object[]> data)  {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
            Workbook wb = new XSSFWorkbook(); //or new HSSFWorkbook();
            Sheet sheet = wb.createSheet();
            sheet.setDefaultColumnWidth(13);
            Font font = wb.createFont();
            font.setFontName("黑体");
            int firstRow = cellStart[0],  lastRow = cellStart[1], firstCol = cellStart[2], lastCol = cellStart[3];
            CellStyle cellStyle = createCellStyle(wb, font);
            createTitle(sheet, showTitle, firstRow, lastRow, firstCol, lastCol, cellStyle);
            createHeader(sheet, headers, cellStyle);
            createContent(sheet, data, cellStyle);
            ServletOutputStream outputStream = response.getOutputStream();
            wb.write(outputStream);
            outputStream.close();
            wb.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
