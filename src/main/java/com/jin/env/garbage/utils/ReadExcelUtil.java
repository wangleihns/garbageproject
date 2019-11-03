package com.jin.env.garbage.utils;


import com.jin.env.garbage.dto.user.UserExcelDto;
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
        List<UserExcelDto> list = new ArrayList<>();
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
        for (int i = 1; i <= lastRowNum; i++) {//遍历每一行
            Row row = sheet.getRow(i);
            //单元格的数量
            final int physicalNumberOfCells = row.getPhysicalNumberOfCells();
            UserExcelDto excelDto = new UserExcelDto();
            String name = getCellFormatValue(row.getCell(0)).toString();
            String phone = getCellFormatValue(row.getCell(1)).toString();
            String password = "123456";  //默认密码
            String idCard = getCellFormatValue(row.getCell(2)).toString();
            String sex = getCellFormatValue(row.getCell(3)).toString();
            String eNo = getCellFormatValue(row.getCell(4)).toString();
            String address = getCellFormatValue(row.getCell(5)).toString();
            String dangYuan = getCellFormatValue(row.getCell(6)).toString();
            String cunMinDaiBiao = getCellFormatValue(row.getCell(7)).toString();
            String cunZuLeader = getCellFormatValue(row.getCell(8)).toString();
            String streetCommentDaiBiao = getCellFormatValue(row.getCell(9)).toString();
            String liangDaiBiaoYiWeiYuan = getCellFormatValue(row.getCell(10)).toString();
            String cunLeader = getCellFormatValue(row.getCell(11)).toString();
            String womenExeLeader = getCellFormatValue(row.getCell(12)).toString();
            excelDto.setName(name);
            excelDto.setPhone(phone);
            excelDto.setIdCard(idCard);
            excelDto.setSex(sex);
            excelDto.seteNo(eNo);
            excelDto.setAddress(address);
            excelDto.setPassword(password);
            excelDto.setDangYuan(dangYuan);
            excelDto.setDangYuan(dangYuan);
            excelDto.setCunMinDaiBiao(cunMinDaiBiao);
            excelDto.setCunZuLeader(cunZuLeader);
            excelDto.setCunLeader(cunLeader);
            excelDto.setStreetCommentDaiBiao(streetCommentDaiBiao);
            excelDto.setLiangDaiBiaoYiWeiYuan(liangDaiBiaoYiWeiYuan);
            excelDto.setWomenExeLeader(womenExeLeader);
            list.add(excelDto);
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
