package io.sim;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Excel {
    private static boolean singleExcel=false;
    private static double totalDistanceOdometer = 0, totalDistanceCalc = 0;
    private static final Semaphore semaphore = new Semaphore(1);
    private static final String fileName = "C:\\Users\\Usuario\\OneDrive\\Documentos\\UFLA\\11 periodo\\AutomacaoAvancada\\AdvancedAutomation_AV2\\data\\Relatorio.xlsx";
    private static XSSFWorkbook workbook;

    private Excel() {
        workbook = new XSSFWorkbook();
        singleExcel = true;
        Sheet sheet = workbook.createSheet("00");
        Row row = sheet.createRow(0);
        int cellnum = 0;
        row.createCell(cellnum++).setCellValue("TimeStamp");
        row.createCell(cellnum++).setCellValue("AutoID");
        row.createCell(cellnum++).setCellValue("RouteIDSUMO");
        row.createCell(cellnum++).setCellValue("RoadIDSUMO");
        row.createCell(cellnum++).setCellValue("Speed");
        row.createCell(cellnum++).setCellValue("Odometer");
        row.createCell(cellnum++).setCellValue("DistanciaCalculada");
        row.createCell(cellnum++).setCellValue("FuelConsumption");
        row.createCell(cellnum++).setCellValue("FuelType");
        row.createCell(cellnum++).setCellValue("Co2Emission");
        row.createCell(cellnum++).setCellValue("Longitude");
        row.createCell(cellnum++).setCellValue("Latitude");
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            workbook.write(out);
            System.out.println("{EXCEL:37} Excel iniciado");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void writeDataToExcel(DrivingData report) {
        try {
            semaphore.acquire();
            if(!singleExcel){
                new Excel();
            }
            workbook = new XSSFWorkbook();
            
            Sheet sheet = workbook.getSheet(report.getAutoID());
            if (sheet == null) {
                sheet = workbook.createSheet(report.getAutoID());
            }

            int rownum = sheet.getLastRowNum() + 1;
            int lastRow = sheet.getLastRowNum();


            Row row = sheet.createRow(rownum);

            if (lastRow > 0) {
                totalDistanceOdometer = report.getOdometer() + sheet.getRow(lastRow).getCell(5).getNumericCellValue();
                totalDistanceCalc = report.getDistanciaCalculada() + sheet.getRow(lastRow).getCell(6).getNumericCellValue();
            }
            int cellnum = 0;
            row.createCell(cellnum++).setCellValue(report.getTimeStamp());
            row.createCell(cellnum++).setCellValue(report.getAutoID());
            row.createCell(cellnum++).setCellValue(report.getRouteIDSUMO());
            row.createCell(cellnum++).setCellValue(report.getRoadIDSUMO());
            row.createCell(cellnum++).setCellValue(report.getSpeed());
            row.createCell(cellnum++).setCellValue(report.getOdometer());
            row.createCell(cellnum++).setCellValue(report.getDistanciaCalculada());
            row.createCell(cellnum++).setCellValue(report.getFuelConsumption());
            row.createCell(cellnum++).setCellValue(report.getFuelType());
            row.createCell(cellnum++).setCellValue(report.getCo2Emission());
            row.createCell(cellnum++).setCellValue(report.getLongitude());
            row.createCell(cellnum++).setCellValue(report.getLatitude());

            try (FileOutputStream out = new FileOutputStream(fileName)) {
                workbook.write(out);
                //System.out.println("Dados gravados com sucesso no arquivo Excel!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            semaphore.release();
        }
    }

    public static synchronized void closeExcelFile() {
        try {
           Sheet sheet = workbook.getSheet("00");
           Row row = sheet.createRow(sheet.getLastRowNum()+1);
           row.createCell(0).setCellValue(""); 
           try (FileOutputStream out = new FileOutputStream(fileName)) {
                workbook.write(out);
                System.out.println("Separando dados...");
            } catch (IOException e) {
                e.printStackTrace();
            }
            workbook.close();
            System.out.println("Arquivo Excel fechado com sucesso!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void doLine(){
        try {
            semaphore.acquire();
            Sheet sheet = workbook.getSheet("00");
            Row row = sheet.createRow(sheet.getLastRowNum()+1);
            row.createCell(0).setCellValue(""); 
            try (FileOutputStream out = new FileOutputStream(fileName)) {
                 workbook.write(out);
                 System.out.println("Separando dados...");
             } catch (IOException e) {
                 e.printStackTrace();
             }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            semaphore.release();
        }
    }
}
