package io.sim;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Excel {
    private static boolean singleExcel=false;
    private static final Semaphore semaphore = new Semaphore(1);
    private static final String fileName = "C:\\Users\\Usuario\\OneDrive\\Documentos\\UFLA\\11 periodo\\AutomacaoAvancada\\AdvancedAutomation_AV2\\data\\Relatorio.xlsx";
    private static XSSFWorkbook workbook;
    private static Sheet sheet;

    private Excel() {
        workbook = new XSSFWorkbook();
        singleExcel = true;
        sheet = workbook.createSheet("0");
        cabecalho();
    }

    private static void cabecalho(){
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
            System.out.println("{EXCEL:41} Excel iniciado");
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
            int rownum = sheet.getLastRowNum() + 1;

            Row row = sheet.createRow(rownum);
            
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
            workbook.close();
            System.out.println("Arquivo Excel fechado com sucesso!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void newSheet(int numSheet){
        try {
            semaphore.acquire();
            sheet = workbook.createSheet(Integer.toString(numSheet));
            cabecalho();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            semaphore.release();
        }
    }
}
