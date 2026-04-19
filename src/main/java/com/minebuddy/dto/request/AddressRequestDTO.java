package com.minebuddy.dto.request;

public record AddressRequestDTO(
        String line1, String line2, String barangay,
        String city, String province, String region,
        String zip, String landmark
) { }
