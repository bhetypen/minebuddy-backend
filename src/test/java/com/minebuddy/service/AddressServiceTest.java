package com.minebuddy.service;

import com.minebuddy.dto.request.AddressRequestDTO;
import com.minebuddy.dto.response.AddressResponseDTO;
import com.minebuddy.model.Address;
import com.minebuddy.repository.AddressRepository;
import com.minebuddy.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressService addressService;

    private UUID storeId;
    private UUID addressId;
    private Address address;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        addressId = UUID.randomUUID();
        TenantContext.setStoreId(storeId);

        address = new Address();
        address.setAddressId(addressId);
        address.setStoreId(storeId);
        address.setLine1("123 Main St");
        address.setLine2("Apt 4");
        address.setBarangay("Brgy 1");
        address.setCity("Cityville");
        address.setProvince("Provinciashire");
        address.setRegion("Region X");
        address.setZip("1234");
        address.setLandmark("Near the park");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createAddress_ShouldReturnResponseDTO() {
        // Arrange
        AddressRequestDTO requestDTO = new AddressRequestDTO(
                "123 Main St", "Apt 4", "Brgy 1", "Cityville",
                "Provinciashire", "Region X", "1234", "Near the park"
        );

        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address savedAddress = invocation.getArgument(0);
            savedAddress.setAddressId(addressId);
            savedAddress.setStoreId(storeId);
            return savedAddress;
        });

        // Act
        AddressResponseDTO responseDTO = addressService.createAddress(requestDTO);

        // Assert
        assertNotNull(responseDTO);
        assertEquals(addressId.toString(), responseDTO.addressId());
        assertEquals("123 Main St, Apt 4, Brgy 1, Cityville, Provinciashire", responseDTO.summary());
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void createAddress_WithoutLine2_ShouldReturnResponseDTOWithoutLine2InSummary() {
        // Arrange
        AddressRequestDTO requestDTO = new AddressRequestDTO(
                "123 Main St", "", "Brgy 1", "Cityville",
                "Provinciashire", "Region X", "1234", "Near the park"
        );

        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address savedAddress = invocation.getArgument(0);
            savedAddress.setAddressId(addressId);
            savedAddress.setStoreId(storeId);
            return savedAddress;
        });

        // Act
        AddressResponseDTO responseDTO = addressService.createAddress(requestDTO);

        // Assert
        assertNotNull(responseDTO);
        assertEquals("123 Main St, Brgy 1, Cityville, Provinciashire", responseDTO.summary());
    }

    @Test
    void listAll_ShouldReturnListOfResponseDTOs() {
        // Arrange
        when(addressRepository.findAllByStoreId(storeId)).thenReturn(List.of(address));

        // Act
        List<AddressResponseDTO> results = addressService.listAll();

        // Assert
        assertEquals(1, results.size());
        assertEquals(addressId.toString(), results.get(0).addressId());
        verify(addressRepository, times(1)).findAllByStoreId(storeId);
    }

    @Test
    void searchAddresses_WithValidTerm_ShouldReturnResults() {
        // Arrange
        String searchTerm = "City";
        when(addressRepository.findByStoreIdAndCityContainingIgnoreCaseOrStoreIdAndProvinceContainingIgnoreCaseOrStoreIdAndBarangayContainingIgnoreCaseOrStoreIdAndLine1ContainingIgnoreCase(
                eq(storeId), eq(searchTerm), eq(storeId), eq(searchTerm), eq(storeId), eq(searchTerm), eq(storeId), eq(searchTerm)
        )).thenReturn(List.of(address));

        // Act
        List<AddressResponseDTO> results = addressService.searchAddresses(searchTerm);

        // Assert
        assertEquals(1, results.size());
        assertEquals(addressId.toString(), results.get(0).addressId());
    }

    @Test
    void searchAddresses_WithBlankTerm_ShouldReturnEmptyList() {
        // Act
        List<AddressResponseDTO> results = addressService.searchAddresses("  ");

        // Assert
        assertTrue(results.isEmpty());
        verifyNoInteractions(addressRepository);
    }

    @Test
    void searchAddresses_WithNullTerm_ShouldReturnEmptyList() {
        // Act
        List<AddressResponseDTO> results = addressService.searchAddresses(null);

        // Assert
        assertTrue(results.isEmpty());
        verifyNoInteractions(addressRepository);
    }

    @Test
    void existsById_WhenExists_ShouldReturnTrue() {
        // Arrange
        when(addressRepository.existsByAddressIdAndStoreId(addressId, storeId)).thenReturn(true);

        // Act
        boolean exists = addressService.existsById(addressId.toString());

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsById_WhenNotExists_ShouldReturnFalse() {
        // Arrange
        when(addressRepository.existsByAddressIdAndStoreId(addressId, storeId)).thenReturn(false);

        // Act
        boolean exists = addressService.existsById(addressId.toString());

        // Assert
        assertFalse(exists);
    }

    @Test
    void getAddressById_WhenFound_ShouldReturnResponseDTO() {
        // Arrange
        when(addressRepository.findByAddressIdAndStoreId(addressId, storeId)).thenReturn(Optional.of(address));

        // Act
        AddressResponseDTO responseDTO = addressService.getAddressById(addressId.toString());

        // Assert
        assertNotNull(responseDTO);
        assertEquals(addressId.toString(), responseDTO.addressId());
    }

    @Test
    void getAddressById_WhenNotFound_ShouldThrowException() {
        // Arrange
        when(addressRepository.findByAddressIdAndStoreId(addressId, storeId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            addressService.getAddressById(addressId.toString())
        );
        assertTrue(exception.getMessage().contains("Address not found"));
    }
}
