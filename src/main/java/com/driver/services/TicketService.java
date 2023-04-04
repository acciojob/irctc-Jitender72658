package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;

    @Autowired
    TrainService trainService;


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
        String stations[] = train.getRoute().split(",");
        int startingStationNo = Arrays.asList(stations).indexOf(String.valueOf(bookTicketEntryDto.getFromStation()));
        int endingStationNo =Arrays.asList(stations).indexOf(String.valueOf(bookTicketEntryDto.getToStation()));

        if(startingStationNo<0 || endingStationNo<0){
            throw new Exception("Invalid stations");
        }

        List<Ticket> tickets = train.getBookedTickets();
        int seatAlreadyBooked = 0;
        for(Ticket ticket: tickets){
            int startingStationIndexOfTicket = Arrays.asList(stations).indexOf(String.valueOf(ticket.getFromStation()));
            int endingStationIndexOfTicket = Arrays.asList(stations).indexOf(String.valueOf(ticket.getToStation()));
            if( endingStationIndexOfTicket>startingStationNo || startingStationIndexOfTicket<endingStationNo ){
                seatAlreadyBooked+=ticket.getPassengersList().size();
            }
        }

        int availableSeats = train.getNoOfSeats()-seatAlreadyBooked;
        if(availableSeats<bookTicketEntryDto.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }
//        String fromStation = String.valueOf(bookTicketEntryDto.getFromStation());
//        String toStation = String.valueOf(bookTicketEntryDto.getToStation());
//        int startingStationNo = 0;
//        int endingStationNo = 0;
//        boolean isTrainPassStartingStation = false;
//        boolean isTrainPassEndingStation = false;
//        String stations [] = train.getRoute().split(",");
//        for(int i =0;i<stations.length;i++){
//            if(fromStation.equals(stations[i])){
//                isTrainPassStartingStation = true;
//                startingStationNo = i;
//            }
//            if(toStation.equals(stations[i])){
//                isTrainPassEndingStation = true;
//                endingStationNo = i;
//            }
//        }

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
        List<Ticket> ticketList = passenger.getBookedTickets();
        ticketList.add(ticket);
        passenger.setBookedTickets(ticketList);

        ticketRepository.save(ticket);


       return ticket.getTicketId();

    }
}
