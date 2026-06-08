package org.cmda.management.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.cmda.management.entities.*;
import org.cmda.management.enums.MemberStatus;
import org.cmda.management.enums.Role;
import org.cmda.management.repositories.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class MemberSecureExportService {
    private static final String[] HEADERS = {
            "Identifiant", "Prenom", "Nom", "Email", "Telephone", "Date de bapteme",
            "Profession", "Talents / Competences", "Etat de vie", "Cheminement",
            "Statut", "Ville", "Fraternite", "Region", "Province",
            "Groupes actifs", "Services actifs", "Responsabilites actives"
    };

    private final CmdaMemberRepository memberRepository;
    private final MemberGroupAssignmentRepository groupAssignmentRepository;
    private final MemberServiceAssignmentRepository serviceAssignmentRepository;
    private final MemberResponsibilityRepository responsibilityRepository;
    private final CurrentUserService currentUserService;
    private final int maxExportRows;

    public MemberSecureExportService(
            CmdaMemberRepository memberRepository,
            MemberGroupAssignmentRepository groupAssignmentRepository,
            MemberServiceAssignmentRepository serviceAssignmentRepository,
            MemberResponsibilityRepository responsibilityRepository,
            CurrentUserService currentUserService,
            @Value("${cmda.api.export.max-rows:5000}") int maxExportRows
    ) {
        this.memberRepository = memberRepository;
        this.groupAssignmentRepository = groupAssignmentRepository;
        this.serviceAssignmentRepository = serviceAssignmentRepository;
        this.responsibilityRepository = responsibilityRepository;
        this.currentUserService = currentUserService;
        this.maxExportRows = maxExportRows;
    }

    @Transactional(readOnly = true)
    public String exportCsv(String keyword, Long fraternityId, Long regionId, Long provinceId, String profession,
                            String talentsAndSkills, String status, String missing) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.join(";", HEADERS)).append('\n');
        for (ExportRow row : findRows(keyword, fraternityId, regionId, provinceId, profession, talentsAndSkills, status, missing)) {
            builder.append(String.join(";", row.values().stream().map(this::csv).toList())).append('\n');
        }
        return builder.toString();
    }

    @Transactional(readOnly = true)
    public byte[] exportExcel(String keyword, Long fraternityId, Long regionId, Long provinceId, String profession,
                              String talentsAndSkills, String status, String missing) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Membres");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = header.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (ExportRow exportRow : findRows(keyword, fraternityId, regionId, provinceId, profession, talentsAndSkills, status, missing)) {
                Row row = sheet.createRow(rowIndex++);
                List<String> values = exportRow.values();
                for (int i = 0; i < values.size(); i++) row.createCell(i).setCellValue(values.get(i));
            }

            for (int i = 0; i < HEADERS.length; i++) sheet.autoSizeColumn(i);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to export members to Excel", exception);
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportPdf(String keyword, Long fraternityId, Long regionId, Long provinceId, String profession,
                            String talentsAndSkills, String status, String missing) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDocument = new PdfDocument(writer);

        try (Document document = new Document(pdfDocument)) {
            document.add(new Paragraph("Export membres CMDA DEV").setBold().setFontSize(14));
            document.add(new Paragraph("Vue courante securisee selon le perimetre utilisateur.").setFontSize(9));

            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 2, 2, 2, 2, 2, 2}))
                    .useAllAvailableWidth();
            String[] pdfHeaders = {"Prenom", "Nom", "Telephone", "Email", "Statut", "Fraternite", "Region", "Province"};
            for (String header : pdfHeaders) table.addHeaderCell(new Cell().add(new Paragraph(header).setBold()));

            for (ExportRow row : findRows(keyword, fraternityId, regionId, provinceId, profession, talentsAndSkills, status, missing)) {
                table.addCell(row.firstName());
                table.addCell(row.lastName());
                table.addCell(row.phoneNumber());
                table.addCell(row.email());
                table.addCell(row.status());
                table.addCell(row.fraternityName());
                table.addCell(row.regionName());
                table.addCell(row.provinceName());
            }
            document.add(table);
        }
        return outputStream.toByteArray();
    }

    private List<ExportRow> findRows(String keyword, Long fraternityId, Long regionId, Long provinceId, String profession,
                                     String talentsAndSkills, String status, String missing) {
        PageRequest exportPage = PageRequest.of(0, maxExportRows, Sort.by(Sort.Direction.ASC, "lastName", "firstName", "id"));
        return memberRepository.findAll(specification(keyword, fraternityId, regionId, provinceId, profession, talentsAndSkills, status, missing), exportPage)
                .stream()
                .map(this::toRow)
                .toList();
    }

    private Specification<CmdaMember> specification(String keyword, Long fraternityId, Long regionId, Long provinceId,
                                                    String profession, String talentsAndSkills, String status, String missing) {
        User user = currentUserService.getCurrentUser();
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(criteriaBuilder.notEqual(root.get("status"), MemberStatus.ARCHIVED));

            switch (user.getRole()) {
                case ADMIN -> { }
                case PROVINCIAL -> predicates.add(criteriaBuilder.equal(root.get("fraternity").get("region").get("province").get("id"), requiredProvinceId(user)));
                case REGIONAL -> predicates.add(criteriaBuilder.equal(root.get("fraternity").get("region").get("id"), requiredRegionId(user)));
                case BERGER -> predicates.add(criteriaBuilder.equal(root.get("fraternity").get("id"), requiredFraternityId(user)));
            }

            if (provinceId != null) predicates.add(criteriaBuilder.equal(root.get("fraternity").get("region").get("province").get("id"), provinceId));
            if (regionId != null) predicates.add(criteriaBuilder.equal(root.get("fraternity").get("region").get("id"), regionId));
            if (fraternityId != null) predicates.add(criteriaBuilder.equal(root.get("fraternity").get("id"), fraternityId));
            if (status != null && !status.isBlank()) predicates.add(criteriaBuilder.equal(root.get("status"), parseStatus(status)));
            if (profession != null && !profession.isBlank()) predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("profession")), contains(profession)));
            if (talentsAndSkills != null && !talentsAndSkills.isBlank()) predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("talentsAndSkills")), contains(talentsAndSkills)));
            addMissingPredicate(missing, root, criteriaBuilder, predicates);
            if (keyword != null && !keyword.isBlank()) {
                String pattern = keyword.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), pattern)
                ));
            }
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private ExportRow toRow(CmdaMember member) {
        Fraternity fraternity = member.getFraternity();
        Region region = fraternity != null ? fraternity.getRegion() : null;
        Province province = region != null ? region.getProvince() : null;
        return new ExportRow(
                value(member.getId()), value(member.getFirstName()), value(member.getLastName()),
                value(member.getEmail()), value(member.getPhoneNumber()), value(member.getBaptismDate()),
                value(member.getProfession()), value(member.getTalentsAndSkills()),
                member.getLifeState() != null ? member.getLifeState().getLabel() : "",
                member.getCurrentJourneyStage() != null ? member.getCurrentJourneyStage().getLabel() : "",
                member.getStatus() != null ? member.getStatus().name() : "", value(member.getCity()),
                fraternity != null ? value(fraternity.getName()) : "",
                region != null ? value(region.getName()) : "",
                province != null ? value(province.getName()) : "",
                activeGroups(member.getId()), activeServices(member.getId()), activeResponsibilities(member.getId())
        );
    }

    private String activeGroups(Long memberId) {
        return groupAssignmentRepository.findByMemberIdOrderByStartDateAsc(memberId).stream()
                .filter(assignment -> assignment.getEndDate() == null)
                .map(assignment -> assignment.getGroup().getLabel())
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private String activeServices(Long memberId) {
        return serviceAssignmentRepository.findByMemberIdOrderByStartDateAsc(memberId).stream()
                .filter(assignment -> assignment.getEndDate() == null)
                .map(assignment -> assignment.getService().getLabel())
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private String activeResponsibilities(Long memberId) {
        return responsibilityRepository.findByMemberIdOrderByStartDateAsc(memberId).stream()
                .filter(responsibility -> responsibility.getEndDate() == null)
                .map(MemberResponsibility::getTitle)
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private String csv(String value) {
        String safe = value(value).replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    private String value(Object value) {
        return value != null ? value.toString() : "";
    }

    private String contains(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }

    private MemberStatus parseStatus(String status) {
        try {
            return MemberStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown member status.");
        }
    }

    private void addMissingPredicate(
            String missing,
            jakarta.persistence.criteria.Root<CmdaMember> root,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            List<jakarta.persistence.criteria.Predicate> predicates
    ) {
        if (missing == null || missing.isBlank()) return;

        switch (missing.trim()) {
            case "email" -> predicates.add(blankText(root, criteriaBuilder, "email"));
            case "phone" -> predicates.add(blankText(root, criteriaBuilder, "phoneNumber"));
            case "photo" -> predicates.add(blankText(root, criteriaBuilder, "photoReference"));
            case "baptismDate" -> predicates.add(criteriaBuilder.isNull(root.get("baptismDate")));
            case "journeyStage" -> predicates.add(criteriaBuilder.isNull(root.get("currentJourneyStage")));
            case "lifeState" -> predicates.add(criteriaBuilder.isNull(root.get("lifeState")));
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown missing member filter.");
        }
    }

    private jakarta.persistence.criteria.Predicate blankText(
            jakarta.persistence.criteria.Root<CmdaMember> root,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            String field
    ) {
        return criteriaBuilder.or(
                criteriaBuilder.isNull(root.get(field)),
                criteriaBuilder.equal(criteriaBuilder.trim(root.get(field)), "")
        );
    }

    private Long requiredProvinceId(User user) {
        if (user.getProvince() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PROVINCIAL user has no province.");
        return user.getProvince().getId();
    }

    private Long requiredRegionId(User user) {
        if (user.getRegion() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "REGIONAL user has no region.");
        return user.getRegion().getId();
    }

    private Long requiredFraternityId(User user) {
        if (user.getFraternity() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "BERGER user has no fraternity.");
        return user.getFraternity().getId();
    }

    private record ExportRow(
            String id, String firstName, String lastName, String email, String phoneNumber, String baptismDate,
            String profession, String talentsAndSkills, String lifeState, String journeyStage, String status,
            String city, String fraternityName, String regionName, String provinceName,
            String activeGroups, String activeServices, String activeResponsibilities
    ) {
        List<String> values() {
            return List.of(id, firstName, lastName, email, phoneNumber, baptismDate, profession, talentsAndSkills,
                    lifeState, journeyStage, status, city, fraternityName, regionName, provinceName,
                    activeGroups, activeServices, activeResponsibilities);
        }
    }
}
