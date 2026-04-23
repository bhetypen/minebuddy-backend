package com.minebuddy.controller;

import com.minebuddy.dto.request.AddressRequestDTO;
import com.minebuddy.dto.response.AddressResponseDTO;
import com.minebuddy.service.AddressService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressControllerTest {

    @Mock
    private AddressService addressService;

    @InjectMocks
    private AddressController addressController;

    @Test
    void createAddress_ShouldReturnResponse() {
        AddressRequestDTO dto = new AddressRequestDTO("Line1", "Line2", "Brgy", "City", "Prov", "Reg", "123", "Landmark");
        AddressResponseDTO expected = new AddressResponseDTO("uuid-123", "Line1, Brgy, City, Prov");
        when(addressService.createAddress(dto)).thenReturn(expected);

        AddressResponseDTO actual = addressController.createAddress(dto);

        assertEquals(expected, actual);
        verify(addressService, times(1)).createAddress(dto);
    }

    @Test
    void getAll_ShouldReturnList() {
        when(addressService.listAll()).thenReturn(List.of(new AddressResponseDTO("1", "Addr1")));

        List<AddressResponseDTO> result = addressController.getAll();

        assertEquals(1, result.size());
        verify(addressService, times(1)).listAll();
    }

    @Test
    void searchAddresses_ShouldReturnList() {
        String query = "Makati";
        when(addressService.searchAddresses(query)).thenReturn(List.of(new AddressResponseDTO("1", "Makati")));

        List<AddressResponseDTO> result = addressController.searchAddresses(query);

        assertEquals(1, result.size());
        verify(addressService, times(1)).searchAddresses(query);
    }

    @Test
    void findById_ShouldReturnAddress() {
        String id = "uuid-123";
        AddressResponseDTO expected = new AddressResponseDTO(id, "Some Address");
        when(addressService.getAddressById(id)).thenReturn(expected);

        AddressResponseDTO actual = addressController.findById(id);

        assertEquals(expected, actual);
        verify(addressService, times(1)).getAddressById(id);
    }

    @Test
    void addressExists_ShouldReturnBoolean() {
        String id = "uuid-123";
        when(addressService.existsById(id)).thenReturn(true);

        boolean exists = addressController.addressExists(id);

        assertTrue(exists);
        verify(addressService, times(1)).existsById(id);
    }
}