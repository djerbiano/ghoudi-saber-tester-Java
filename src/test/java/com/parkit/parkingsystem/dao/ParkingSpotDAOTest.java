package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingSpotDAOTest {

    @Mock
    private DataBaseConfig dataBaseConfig;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private ParkingSpotDAO parkingSpotDAO;

    @BeforeEach
    void setUp() {
        parkingSpotDAO.dataBaseConfig = dataBaseConfig;
    }

    // getNextAvailableSlot

    @Test
    void getNextAvailableSlot_shouldReturnSlotNumber_whenAvailableSlotExists() throws Exception {
        ParkingType parkingType = ParkingType.CAR;
        int expectedSlot = 5;

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(expectedSlot);

        int result = parkingSpotDAO.getNextAvailableSlot(parkingType);

        assertEquals(expectedSlot, result);
        verify(preparedStatement).setString(1, parkingType.toString());
        verify(preparedStatement).executeQuery();
        verify(dataBaseConfig).closeResultSet(resultSet);
        verify(dataBaseConfig).closePreparedStatement(preparedStatement);
        verify(dataBaseConfig).closeConnection(connection);
    }

    @Test
    void getNextAvailableSlot_shouldReturnMinusOne_whenNoAvailableSlotExists() throws Exception {
        ParkingType parkingType = ParkingType.BIKE;

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        int result = parkingSpotDAO.getNextAvailableSlot(parkingType);

        assertEquals(-1, result);
        verify(dataBaseConfig).closeResultSet(resultSet);
        verify(dataBaseConfig).closePreparedStatement(preparedStatement);
        verify(dataBaseConfig).closeConnection(connection);
    }

    @Test
    void getNextAvailableSlot_shouldReturnMinusOne_whenExceptionOccurs() throws Exception {
        ParkingType parkingType = ParkingType.CAR;

        when(dataBaseConfig.getConnection()).thenThrow(new RuntimeException("Database connection error"));

        int result = parkingSpotDAO.getNextAvailableSlot(parkingType);

        assertEquals(-1, result);
        verify(dataBaseConfig, never()).closeResultSet(any());
        verify(dataBaseConfig, never()).closePreparedStatement(any());
        verify(dataBaseConfig).closeConnection(any());
    }

    @Test
    void getNextAvailableSlot_shouldNotCloseResources_whenExceptionOccursDuringQuery() throws Exception {
        ParkingType parkingType = ParkingType.CAR;

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenThrow(new RuntimeException("Query execution error"));

        int result = parkingSpotDAO.getNextAvailableSlot(parkingType);

        assertEquals(-1, result);
        // Le code réel ne ferme PAS ResultSet et PreparedStatement en cas d'exception
        verify(dataBaseConfig, never()).closeResultSet(any());
        verify(dataBaseConfig, never()).closePreparedStatement(any());
        verify(dataBaseConfig).closeConnection(connection);
    }

    // updateParking

    @Test
    void updateParking_shouldReturnTrue_whenUpdateIsSuccessful() throws Exception {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        assertTrue(result);
        verify(preparedStatement).setBoolean(1, parkingSpot.isAvailable());
        verify(preparedStatement).setInt(2, parkingSpot.getId());
        verify(preparedStatement).executeUpdate();
        verify(dataBaseConfig).closePreparedStatement(preparedStatement);
        verify(dataBaseConfig).closeConnection(connection);
    }

    @Test
    void updateParking_shouldReturnFalse_whenUpdateAffectsZeroRows() throws Exception {
        ParkingSpot parkingSpot = new ParkingSpot(99, ParkingType.CAR, true);

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        assertFalse(result);
        verify(preparedStatement).executeUpdate();
        verify(dataBaseConfig).closePreparedStatement(preparedStatement);
        verify(dataBaseConfig).closeConnection(connection);
    }

    @Test
    void updateParking_shouldReturnFalse_whenExceptionOccurs() throws Exception {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

        when(dataBaseConfig.getConnection()).thenThrow(new RuntimeException("Database connection error"));

        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        assertFalse(result);
        verify(dataBaseConfig, never()).closePreparedStatement(any());
        verify(dataBaseConfig).closeConnection(any());
    }

    @Test
    void updateParking_shouldNotClosePreparedStatement_whenExceptionOccursDuringUpdate() throws Exception {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(new RuntimeException("Update execution error"));

        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        assertFalse(result);
        // Le code réel ne ferme PAS PreparedStatement en cas d'exception
        // car closePreparedStatement est dans le bloc try après executeUpdate
        verify(dataBaseConfig, never()).closePreparedStatement(any());
        verify(dataBaseConfig).closeConnection(connection);
    }

    @Test
    void updateParking_shouldHandleAvailableTrue() throws Exception {
        ParkingSpot parkingSpot = new ParkingSpot(3, ParkingType.BIKE, true);

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        assertTrue(result);
        verify(preparedStatement).setBoolean(1, true);
        verify(preparedStatement).setInt(2, 3);
    }

    @Test
    void updateParking_shouldHandleAvailableFalse() throws Exception {
        ParkingSpot parkingSpot = new ParkingSpot(7, ParkingType.CAR, false);

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        assertTrue(result);
        verify(preparedStatement).setBoolean(1, false);
        verify(preparedStatement).setInt(2, 7);
    }

    // TYPES DE PARKING

    @Test
    void getNextAvailableSlot_shouldWorkForCarParkingType() throws Exception {
        ParkingType parkingType = ParkingType.CAR;
        int expectedSlot = 2;

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(expectedSlot);

        int result = parkingSpotDAO.getNextAvailableSlot(parkingType);

        assertEquals(expectedSlot, result);
        verify(preparedStatement).setString(1, "CAR");
    }

    @Test
    void getNextAvailableSlot_shouldWorkForBikeParkingType() throws Exception {
        ParkingType parkingType = ParkingType.BIKE;
        int expectedSlot = 10;

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(expectedSlot);

        int result = parkingSpotDAO.getNextAvailableSlot(parkingType);

        assertEquals(expectedSlot, result);
        verify(preparedStatement).setString(1, "BIKE");
    }
}