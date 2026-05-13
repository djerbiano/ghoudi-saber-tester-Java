package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @AfterAll
    public static void tearDown() {

    }

    @BeforeEach
    public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    public void testParkingACar() {
        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        Ticket savedTicket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(savedTicket, "Ticket should not be null");
        assertNotNull(savedTicket.getInTime(), "In time should be saved in database ");
        assertNull(savedTicket.getOutTime(), "Out time should be null (not yet exit)");
        assertEquals(0.0, savedTicket.getPrice(), "Price should be 0 (not yet exit)");

        assertFalse(savedTicket.getParkingSpot().isAvailable(), "Parking table should have available = false for this spot");


    }

    @Test
    public void testParkingLotExit() {
        //TODO: check that the fare generated and out time are populated correctly in the database
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticketDAO.updateInTime(ticket);

        parkingService.processExitingVehicle();

        Ticket exitedTicket = ticketDAO.getTicketWithOutTime("ABCDEF");
        assertNotNull(exitedTicket.getOutTime(), "The departure time must be entered in the database");
        assertTrue(exitedTicket.getPrice() > 0, "fare must be greater than 0 for 1 hour of parking");
    }

    @Test
    public void testParkingLotExitRecurringUser() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //first entry
        parkingService.processIncomingVehicle();
        Ticket firstTicket = ticketDAO.getTicket("ABCDEF");
        firstTicket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticketDAO.updateInTime(firstTicket);
        parkingService.processExitingVehicle();

        // second entry
        parkingService.processIncomingVehicle();
        Ticket secondTicket = ticketDAO.getTicket("ABCDEF");
        secondTicket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticketDAO.updateInTime(secondTicket);
        parkingService.processExitingVehicle();

        Ticket exitedTicket = ticketDAO.getTicketWithOutTime("ABCDEF");
        assertNotNull(exitedTicket.getOutTime(), "out time must be saved in database ");

        // Expected price: 1 hour with 5% discount
        double expectedPrice = Fare.CAR_RATE_PER_HOUR * 0.95;
        assertEquals(expectedPrice, exitedTicket.getPrice(), 0.01, "price must include the 5% discount for a recurring user");
    }

}
