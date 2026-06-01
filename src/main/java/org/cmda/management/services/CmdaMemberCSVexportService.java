package org.cmda.management.services;

import com.opencsv.CSVWriter;

import org.cmda.management.dtos.CmdaMemberDTO;
import org.cmda.management.repositories.CmdaMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.cmda.management.services.CmdaMemberService;

import java.io.StringWriter;
import java.util.List;

@Service
public class CmdaMemberCSVexportService {

    @Autowired
    private CmdaMemberService cmdaMemberService;  // Injection de CmdaMemberService

    @Autowired
    private CmdaMemberRepository cmdaMemberRepository;

    // Méthode pour générer le CSV
    public String exportMembersToCSV(String keyword, Long fraternityId, Long regionId, Long provinceId, String firstName, String lastName, String profession, String status) {
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
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer);

        // En-tête CSV
        String[] header = {"ID", "First Name", "Last Name", "Email", "Phone Number", "Birthday", "Profession", "Status", "Fraternity Id", "Fraternity Name"};
        csvWriter.writeNext(header);

        // Contenu du fichier CSV
        for (CmdaMemberDTO member : members) {
            String[] memberData = {
                    member.getId().toString(),
                    member.getFirstName(),
                    member.getLastName(),
                    member.getEmail(),
                    member.getPhoneNumber(),
                    member.getBirthday().toString(),
                    member.getProfession(),
                    member.getStatus(),
                    member.getFraternityId() != null ? member.getFraternityId().toString() : "",
                    member.getFraternityName()
            };
            csvWriter.writeNext(memberData);
        }

        try {
            csvWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return writer.toString();
    }




}
