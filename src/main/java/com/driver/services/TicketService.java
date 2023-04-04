package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db
        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
        if(train.getNoOfSeats()<bookTicketEntryDto.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }
        String fromStation = String.valueOf(bookTicketEntryDto.getFromStation());
        String toStation = String.valueOf(bookTicketEntryDto.getToStation());
        int startingStationNo = 0;
        int endingStationNo = 0;
        boolean isTrainPassStartingStation = false;
        boolean isTrainPassEndingStation = false;
        String stations [] = train.getRoute().split(",");
        for(int i =0;i<stations.length;i++){
            if(fromStation.equals(stations[i])){
                isTrainPassStartingStation = true;
                startingStationNo = i;
            }
            if(toStation.equals(stations[i])){
                isTrainPassEndingStation = true;
                endingStationNo = i;
            }
        }
        if(!isTrainPassEndingStation || !isTrainPassEndingStation){
            throw new Exception("Invalid stations");
        }
        int totalFare = (endingStationNo-startingStationNo)*300*bookTicketEntryDto.getPassengerIds().size();

        Ticket ticket = new Ticket();
        List<Integer> passengerIds = bookTicketEntryDto.getPassengerIds();
        List<Passenger> passengerList = new ArrayList<>();
        for(Integer passengerId : passengerIds){
            Passenger passenger = passengerRepository.findById(passengerId).get();
            passengerList.add(passenger);
        }
        ticket.setPassengersList(passengerList);
        ticket.setTrain(train);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTotalFare(totalFare);

        train.getBookedTickets().add(ticket);
//        int updatedSeats = train.getNoOfSeats()-bookTicketEntryDto.getNoOfSeats();
//        train.setNoOfSeats(updatedSeats);
        Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        List<Ticket> tickets = passenger.getBookedTickets();
        tickets.add(ticket);
        passenger.setBookedTickets(tickets);

        ticketRepository.save(ticket);


       return ticket.getTicketId();

    }
}
