/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.orolle.ft.crawler;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author muhaaa
 */
public class Excel {

  protected final Workbook wb;

  public Excel() {
    wb = new XSSFWorkbook();
  }

  public void addCell() {
    CreationHelper createHelper = wb.getCreationHelper();
    Sheet sheet = wb.createSheet("hello world");

    // Create a row and put some cells in it. Rows are 0 based.
    Row row = sheet.createRow(0);

    // Create a cell and put a date value in it.  The first cell is not styled
    // as a date.
    Cell cell = row.createCell(0);
    cell.setCellValue(new Date());

    // we style the second cell as a date (and time).  It is important to
    // create a new cell style from the workbook otherwise you can end up
    // modifying the built in style and effecting not only this cell but other cells.
    CellStyle cellStyle = wb.createCellStyle();
    cellStyle.setDataFormat(
      createHelper.createDataFormat().getFormat("m/d/yy h:mm"));
    cell = row.createCell(1);
    cell.setCellValue(new Date());
    cell.setCellStyle(cellStyle);

    //you can also set date as java.util.Calendar
    cell = row.createCell(2);
    cell.setCellValue(Calendar.getInstance());
    cell.setCellStyle(cellStyle);
  }

  public byte[] toBytes() {
    // Write the output to a file
    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    try {
      wb.write(bos);
      bos.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return bos.toByteArray();
  }
}
