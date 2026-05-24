package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;


public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount ) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        long inTime = ticket.getInTime().getTime();
        long outTime = ticket.getOutTime().getTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct
        double duration = (outTime - inTime) / 3_600_000.0;

        double rate;
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: rate = Fare.CAR_RATE_PER_HOUR; break;
            case BIKE: rate = Fare.BIKE_RATE_PER_HOUR; break;
            default: throw new IllegalArgumentException("Unknown Parking Type");
        }

        if (duration < 0.5) {
            ticket.setPrice(0);  // Gratuit
        } else if (duration == 0.5) {
            ticket.setPrice(duration * rate);  // Payant, SANS réduction
        } else {  // duration > 0.5
            double price = duration * rate;
            if (discount) {
                price *= 0.95;  // Réduction uniquement si > 30 min
            }
            ticket.setPrice(price);
        }
    }

    public void calculateFare(Ticket ticket){
        calculateFare(ticket, false);
    }


}