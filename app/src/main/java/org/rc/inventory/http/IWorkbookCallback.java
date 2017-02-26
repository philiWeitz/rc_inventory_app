package org.rc.inventory.http;


import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public interface IWorkbookCallback {
    void onFail(FtpResponseCode response);
    void onResponse(XSSFWorkbook workbook);
}
