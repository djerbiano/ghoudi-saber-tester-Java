package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketDAOTest {

    private TicketDAO ticketDAO;

    @Mock
    private DataBaseConfig dataBaseConfig;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private Ticket ticket;

    @BeforeEach
    public void setUp() throws Exception {
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseConfig;

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket = new Ticket();
        ticket.setId(1);
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setPrice(1.5);
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setOutTime(new Date());
    }

    // ---- saveTicket ----

    @Test
    public void saveTicket_shouldReturnFalse_whenTicketIsSaved() throws Exception {
        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.execute()).thenReturn(false);

        boolean result = ticketDAO.saveTicket(ticket);
        assertFalse(result);
        verify(preparedStatement, times(1)).execute();
    }

    @Test
    public void saveTicket_shouldReturnFalse_whenExceptionOccurs() throws Exception {
        when(dataBaseConfig.getConnection()).thenThrow(new SQLException("DB error"));
        boolean result = ticketDAO.saveTicket(ticket);
        assertFalse(result);
    }

    // ---- getTicket ----

    @Test
    public void getTicket_shouldReturnTicket_whenTicketExists() throws Exception {
        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        lenient().when(resultSet.getInt(1)).thenReturn(1);
        lenient().when(resultSet.getInt(2)).thenReturn(1);
        lenient().when(resultSet.getDouble(3)).thenReturn(1.5);
        lenient().when(resultSet.getTimestamp(4)).thenReturn(new Timestamp(System.currentTimeMillis() - (60 * 60 * 1000)));
        lenient().when(resultSet.getTimestamp(5)).thenReturn(new Timestamp(System.currentTimeMillis()));
        lenient().when(resultSet.getString(6)).thenReturn("CAR");

        Ticket result = ticketDAO.getTicket("ABCDEF");

        assertNotNull(result);
        assertEquals("ABCDEF", result.getVehicleRegNumber());
        assertEquals(1, result.getId());
        assertEquals(1.5, result.getPrice());
        assertNotNull(result.getInTime());
        assertNotNull(result.getOutTime());
        assertEquals(ParkingType.CAR, result.getParkingSpot().getParkingType());
    }

    @Test
    public void getTicket_shouldReturnNull_whenTicketDoesNotExist() throws Exception {
        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Ticket result = ticketDAO.getTicket("ABCDEF");
        assertNull(result);
    }

    @Test
    public void getTicket_shouldReturnNull_whenExceptionOccurs() throws Exception {
        when(dataBaseConfig.getConnection()).thenThrow(new SQLException("DB error"));
        Ticket result = ticketDAO.getTicket("ABCDEF");
        assertNull(result);
    }

    // ---- updateTicket ----

    @Test
    public void updateTicket_shouldReturnTrue_whenUpdateSucceeds() throws Exception {
        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.execute()).thenReturn(true);

        boolean result = ticketDAO.updateTicket(ticket);
        assertTrue(result);
        verify(preparedStatement, times(1)).execute();
    }

    @Test
    public void updateTicket_shouldReturnFalse_whenExceptionOccurs() throws Exception {
        when(dataBaseConfig.getConnection()).thenThrow(new SQLException("DB error"));
        boolean result = ticketDAO.updateTicket(ticket);
        assertFalse(result);
    }

    // ---- getNbTicket ----

    @Test
    public void getNbTicket_shouldReturnCount_whenTicketsExist() throws Exception {
        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(3);

        int result = ticketDAO.getNbTicket("ABCDEF");
        assertEquals(3, result);
    }

    @Test
    public void getNbTicket_shouldReturnZero_whenNoTicketsExist() throws Exception {
        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        int result = ticketDAO.getNbTicket("ABCDEF");
        assertEquals(0, result);
    }

    @Test
    public void getNbTicket_shouldReturnZero_whenExceptionOccurs() throws Exception {
        when(dataBaseConfig.getConnection()).thenThrow(new SQLException("DB error"));
        int result = ticketDAO.getNbTicket("ABCDEF");
        assertEquals(0, result);
    }

    // ---- updateInTime ----

    @Test
    public void updateInTime_shouldReturnTrue_whenUpdateSucceeds() throws Exception {
        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.execute()).thenReturn(true);

        boolean result = ticketDAO.updateInTime(ticket);
        assertTrue(result);
        verify(preparedStatement, times(1)).execute();
    }

    @Test
    public void updateInTime_shouldReturnFalse_whenExceptionOccurs() throws Exception {
        when(dataBaseConfig.getConnection()).thenThrow(new SQLException("DB error"));
        boolean result = ticketDAO.updateInTime(ticket);
        assertFalse(result);
    }

    // ---- getTicketWithOutTime ----

    @Test
    public void getTicketWithOutTime_shouldReturnTicket_whenTicketExists() throws Exception {
        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        lenient().when(resultSet.getInt(1)).thenReturn(1);
        lenient().when(resultSet.getInt(2)).thenReturn(1);
        lenient().when(resultSet.getDouble(3)).thenReturn(1.5);
        lenient().when(resultSet.getTimestamp(4)).thenReturn(new Timestamp(System.currentTimeMillis() - (60 * 60 * 1000)));
        lenient().when(resultSet.getTimestamp(5)).thenReturn(new Timestamp(System.currentTimeMillis()));
        lenient().when(resultSet.getString(6)).thenReturn("CAR");

        Ticket result = ticketDAO.getTicketWithOutTime("ABCDEF");

        assertNotNull(result);
        assertEquals("ABCDEF", result.getVehicleRegNumber());
        assertEquals(1.5, result.getPrice());
        assertNotNull(result.getOutTime());
    }

    @Test
    public void getTicketWithOutTime_shouldReturnNull_whenTicketDoesNotExist() throws Exception {
        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Ticket result = ticketDAO.getTicketWithOutTime("ABCDEF");
        assertNull(result);
    }

    @Test
    public void getTicketWithOutTime_shouldReturnNull_whenExceptionOccurs() throws Exception {
        when(dataBaseConfig.getConnection()).thenThrow(new SQLException("DB error"));
        Ticket result = ticketDAO.getTicketWithOutTime("ABCDEF");
        assertNull(result);
    }
}