package io.sim;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Excel extends Thread{

    private static final String fileName = "\\data\\Relatorio.xls";
    private static XSSFWorkbook workbook;
    private Sheet sheet;
    private DrivingData repport;

    public Excel(DrivingData repport){
        this.repport = repport;
        workbook = new XSSFWorkbook();
        this.start();
    }

    @Override
    public void run(){

        try {
            this.sheet = workbook.getSheet(repport.getAutoID());
            if (this.sheet == null) {
                this.sheet = workbook.createSheet(repport.getAutoID());
            }
        } catch (Exception e) {}

        int rownum = 0;{
            Row row = sheet.createRow(rownum++);
            int cellnum = 0;
            Cell cellTime = row.createCell(cellnum++);
            cellTime.setCellValue(repport.getTimeStamp());

            Cell cellAuto = row.createCell(cellnum++);
            cellAuto.setCellValue(repport.getAutoID());

            Cell cellRoute = row.createCell(cellnum++);
            cellRoute.setCellValue(repport.getRouteIDSUMO());

            Cell cellSpeed = row.createCell(cellnum++);
            cellSpeed.setCellValue(repport.getSpeed());

            Cell cellDistance = row.createCell(cellnum++);
            cellDistance.setCellValue(repport.getOdometer());

            Cell cellMedia = row.createCell(cellnum++);
            cellMedia.setCellValue(repport.getFuelConsumption());

            Cell cellType = row.createCell(cellnum++);
            cellType.setCellValue(repport.getFuelType());   

            Cell cellCo2 = row.createCell(cellnum++);
            cellCo2.setCellValue(repport.getCo2Emission());

            Cell cellLongitude = row.createCell(cellnum++);
            cellLongitude.setCellValue(repport.getLongitude());

            Cell cellLatitude = row.createCell(cellnum++);
            cellLatitude.setCellValue(repport.getLatitude());
        }

        try (FileOutputStream out = new FileOutputStream(Excel.fileName)) {
            workbook.write(out);
            System.out.println("Arquivo Excel criado com sucesso!");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(){
        DrivingData repport = new DrivingData("TEST", "TEST", (long)1.0, 1.0, 1.0, "1", "1", 1.0, 1.0, 1.0, 1.0, 1, 1.0,1.0,1.0,1,1);
        Excel excel = new Excel(repport);
    }
}