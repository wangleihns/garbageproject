package com.jin.env.garbage.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

public class ExportExcelUtils {
    public static void main(String[] args) throws Exception {

    }

    private static void createCell(Workbook wb, Row row, int column, Font font, String value) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
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
        cell.setCellStyle(cellStyle);
    }

    public static void  createTitle(Workbook wb, Sheet sheet, Font font, String value, int firstRow, int lastRow, int firstCol, int lastCol){
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(30);
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
        createCell(wb, titleRow, 0, font, value);
    }
    public static void  createHeader(Workbook wb, Sheet sheet, Font font, List<String> exportHeaders){
        Row headerRow = sheet.createRow(1);
        headerRow.setHeightInPoints(30);
        for (int i = 0; i < exportHeaders.size(); i++) {
            createCell(wb, headerRow, i, font, exportHeaders.get(i));
        }
    }

    public static void createContent(Workbook wb,Sheet sheet, Font font, List<Object[]> data){
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i +2 );
            row.setHeightInPoints(30);
            Object[] o = data.get(i);
            for (int j = 0; j < o.length; j++) {
                 Object oo = o[j] == null? "":o[j];
                createCell(wb, row, j, font, oo.toString());
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
            createTitle(wb, sheet, font, showTitle, firstRow, lastRow, firstCol, lastCol);
            createHeader(wb,sheet,font, headers);
            createContent(wb, sheet, font, data);
            wb.write(response.getOutputStream());
            wb.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
