package org.cmda.management.services;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.cmda.management.dtos.CmdaMemberDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class CmdaMemberExcelExportService {

    @Autowired
    private CmdaMemberService cmdaMemberService;

    public byte[] exportMembersToExcel(String keyword, Long fraternityId, Long regionId, Long provinceId, String firstName, String lastName, String profession, String status) {
        List<CmdaMemberDTO> members = cmdaMemberService.getMembersForCurrentUserExport(
                keyword,
                fraternityId,
                regionId,
                provinceId,
                firstName,
                lastName,
                profession,
                status
        );

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Members");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] headers = {
                    "ID",
                    "First Name",
                    "Last Name",
                    "Email",
                    "Phone Number",
                    "Birthday",
                    "Profession",
                    "Status",
                    "Fraternity Id",
                    "Fraternity Name",
                    "Region Id",
                    "Region Name",
                    "Province Id",
                    "Province Name"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (CmdaMemberDTO member : members) {
                Row row = sheet.createRow(rowIndex++);
                writeMemberRow(row, member);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Unable to export members to Excel", e);
        }
    }

    private void writeMemberRow(Row row, CmdaMemberDTO member) {
        row.createCell(0).setCellValue(member.getId() != null ? member.getId() : 0);
        row.createCell(1).setCellValue(valueOrEmpty(member.getFirstName()));
        row.createCell(2).setCellValue(valueOrEmpty(member.getLastName()));
        row.createCell(3).setCellValue(valueOrEmpty(member.getEmail()));
        row.createCell(4).setCellValue(valueOrEmpty(member.getPhoneNumber()));
        row.createCell(5).setCellValue(member.getBirthday() != null ? member.getBirthday().toString() : "");
        row.createCell(6).setCellValue(valueOrEmpty(member.getProfession()));
        row.createCell(7).setCellValue(valueOrEmpty(member.getStatus()));
        row.createCell(8).setCellValue(member.getFraternityId() != null ? member.getFraternityId() : 0);
        row.createCell(9).setCellValue(valueOrEmpty(member.getFraternityName()));
        row.createCell(10).setCellValue(member.getRegionId() != null ? member.getRegionId() : 0);
        row.createCell(11).setCellValue(valueOrEmpty(member.getRegionName()));
        row.createCell(12).setCellValue(member.getProvinceId() != null ? member.getProvinceId() : 0);
        row.createCell(13).setCellValue(valueOrEmpty(member.getProvinceName()));
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }
}
