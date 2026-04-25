package org.cmda.management.dtos;

import java.util.List;
import org.cmda.management.dtos.UserDTO;

public class UserProvincialDTO extends UserDTO {
    private ProvinceDTO province;

    // Getters and Setters
    public ProvinceDTO getProvince() {
        return province;
    }

    public void setProvince(ProvinceDTO province) {
        this.province = province;
    }
}
