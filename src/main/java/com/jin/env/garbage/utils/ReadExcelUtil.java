package com.jin.env.garbage.utils;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ReadExcelUtil {
    // 去读Excel的方法readExcel，该方法的入口参数为一个File对象
    public static List readExcel(InputStream inputStream, String suffix){
        //1.读取Excel文档对象
        List<NameValuePair> list = new ArrayList<>();
        Workbook workbook = null;
        try {
            if ("xls".equals(suffix)) {
                workbook = new HSSFWorkbook(inputStream);
            } else if ("xlsx".equals(suffix)) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                throw new RuntimeException("请上传excel文件！");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("文件流有问题");
        }
        //2.获取要解析的表格（第一个表格）
        Sheet sheet = workbook.getSheetAt(0);
        //获得最后一行的行号
        int lastRowNum = sheet.getLastRowNum();
        Row titleRow = sheet.getRow(0);
        for (int i = 1; i <= lastRowNum; i++) {//遍历每一行
            Row row = sheet.getRow(i);
            //单元格的数量
            final int physicalNumberOfCells = row.getPhysicalNumberOfCells();
            for (int j = 0; j <= physicalNumberOfCells; j++) {
                NameValuePair nameValuePair = new BasicNameValuePair(getCellFormatValue(titleRow.getCell(j)).toString(), getCellFormatValue(row.getCell(j)).toString());
                list.add(nameValuePair);
            }
        }
        return list;
    }

    public static Object getCellFormatValue(Cell cell){
        Object cellValue = null;
        if(cell!=null){
            //判断cell类型
            switch(cell.getCellTypeEnum()){
                case _NONE:
                    cellValue = "";
                    break;
                case NUMERIC:{
                    double numericCellValue = cell.getNumericCellValue();
                    cellValue = String.valueOf((long) numericCellValue);
                    break;
                }
                case STRING:{
                    cellValue = cell.getRichStringCellValue().getString();
                    break;
                }
                case FORMULA:
                    cellValue = "";
                    break;
                case BLANK:
                    cellValue = "";
                    break;
                case BOOLEAN:
                    cellValue = cell.getBooleanCellValue();
                    break;
                case ERROR:
                    break;
                default:
                    cellValue = "";
            }
        }else{
            cellValue = "";
        }
        return cellValue;
    }
}
