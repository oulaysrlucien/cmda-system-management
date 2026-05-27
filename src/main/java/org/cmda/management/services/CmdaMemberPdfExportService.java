package org.cmda.management.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.cmda.management.dtos.CmdaMemberDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class CmdaMemberPdfExportService {

    @Autowired
    private CmdaMemberService cmdaMemberService;

    public byte[] exportMembersToPdf(Long fraternityId, String firstName, String lastName, String profession) {
        List<CmdaMemberDTO> members = cmdaMemberService.getMembersForCurrentUserExport(
                fraternityId,
                firstName,
                lastName,
                profession
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDocument = new PdfDocument(writer);

        try (Document document = new Document(pdfDocument)) {
            document.add(new Paragraph("CMDA Members Export").setBold().setFontSize(14));

            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 2, 3, 2, 2, 2, 2, 2, 3}))
                    .useAllAvailableWidth();

            String[] headers = {
                    "ID",
                    "First Name",
                    "Last Name",
                    "Email",
                    "Phone",
                    "Birthday",
                    "Profession",
                    "Status",
                    "Fraternity Id",
                    "Fraternity"
            };

            for (String header : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(header).setBold()));
            }

            for (CmdaMemberDTO member : members) {
                table.addCell(valueOrEmpty(member.getId()));
                table.addCell(valueOrEmpty(member.getFirstName()));
                table.addCell(valueOrEmpty(member.getLastName()));
                table.addCell(valueOrEmpty(member.getEmail()));
                table.addCell(valueOrEmpty(member.getPhoneNumber()));
                table.addCell(member.getBirthday() != null ? member.getBirthday().toString() : "");
                table.addCell(valueOrEmpty(member.getProfession()));
                table.addCell(valueOrEmpty(member.getStatus()));
                table.addCell(valueOrEmpty(member.getFraternityId()));
                table.addCell(valueOrEmpty(member.getFraternityName()));
            }

            document.add(table);
        }

        return outputStream.toByteArray();
    }

    private String valueOrEmpty(Object value) {
        return value != null ? value.toString() : "";
    }
}
