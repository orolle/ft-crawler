/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.orolle.ft.crawler;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author muhaaa
 */
public class ExcelTest {

  public ExcelTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  // TODO add test methods here.
  // The methods must be annotated with annotation @Test. For example:
  //
  @Test
  public void makeExcelFile() throws Exception {
    Excel e = new Excel();

    e.addCell();
    
    Path path = setupFile();
    writeFile(path, e.toBytes());
    //deleteFile(path);
  }

  public static Path setupFile() throws Exception {
    Path path = FileSystems.getDefault().getPath("/home/muhaaa/tmp/", "test_ExcelTest.xlsx");

    deleteFile(path);

    return path;
  }

  public static void deleteFile(Path path) throws Exception {
    if (Files.exists(path)) {
      Files.delete(path);
    }
  }
  
  public static void writeFile(Path path, byte[] data) throws Exception {
    Files.write(path, data, new OpenOption[]{});
  }
}
