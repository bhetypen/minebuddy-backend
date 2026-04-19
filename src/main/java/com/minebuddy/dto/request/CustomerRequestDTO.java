package com.minebuddy.dto.request;

import java.util.UUID;

public record CustomerRequestDTO(String fName,
                                 String lName,
                                 String handle,
                                 String platform,
                                 String phone, UUID addressId) {
}
