package dk.cphbusiness.flightdemo;

import dk.cphbusiness.utils.Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Purpose:
 *
 * @author: Thomas Hartmann
 */
public class FlightReader {

    public static void main(String[] args) {
        FlightReader flightReader = new FlightReader();
        try {
            List<DTOs.FlightDTO> flightList = flightReader.getFlightsFromFile("flights.json");
            List<DTOs.FlightInfo> flightInfoList = flightReader.getFlightInfoDetails(flightList);
            flightInfoList.forEach(f -> {
                System.out.println("\n" + f);
            });

            // Example: Calculate the average flight time for a specific airline, e.g., "Lufthansa"
            String airlineName = "Lufthansa"; // You can change this to any airline
            double averageFlightTime = flightReader.averageDuration(flightInfoList, airlineName);
            System.out.println("Average flight time for " + airlineName + ": " + averageFlightTime + " minutes");

            String airlineNameTotal="Lufthansa";
            double totalFlightTime = flightReader.totalFlightTime(flightInfoList, airlineNameTotal);
            System.out.println("Total flight time for " + airlineNameTotal + ": " + totalFlightTime + " minutes");

            // Feature: Get flights departing before a specific time for a specific airline
            LocalTime specificTime = LocalTime.of(8, 0); // 08:00 AM
            String specificAirline = "Lufthansa"; // Change this to any airline you want to filter by
            List<DTOs.FlightInfo> earlyFlights = flightReader.flightSpecifick(flightInfoList, specificTime, specificAirline);
            System.out.println("Flights departing before " + specificTime + " for " + specificAirline + ":");
            earlyFlights.forEach(f -> System.out.println(f));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    public List<FlightDTO> jsonFromFile(String fileName) throws IOException {
//        List<FlightDTO> flights = getObjectMapper().readValue(Paths.get(fileName).toFile(), List.class);
//        return flights;
//    }


    public List<DTOs.FlightInfo> getFlightInfoDetails(List<DTOs.FlightDTO> flightList) {
        List<DTOs.FlightInfo> flightInfoList = flightList.stream().map(flight -> {
            Duration duration = Duration.between(flight.getDeparture().getScheduled(), flight.getArrival().getScheduled());
            DTOs.FlightInfo flightInfo = DTOs.FlightInfo.builder()
                    .name(flight.getFlight().getNumber())
                    .iata(flight.getFlight().getIata())
                    .airline(flight.getAirline().getName())
                    .duration(duration)
                    .departure(flight.getDeparture().getScheduled().toLocalDateTime())
                    .arrival(flight.getArrival().getScheduled().toLocalDateTime())
                    .origin(flight.getDeparture().getAirport())
                    .destination(flight.getArrival().getAirport())
                    .build();

            return flightInfo;
        }).toList();
        return flightInfoList;
    }

    public List<DTOs.FlightDTO> getFlightsFromFile(String filename) throws IOException {
        DTOs.FlightDTO[] flights = new Utils().getObjectMapper().readValue(Paths.get(filename).toFile(), DTOs.FlightDTO[].class);

        List<DTOs.FlightDTO> flightList = Arrays.stream(flights).toList();
        return flightList;


    }

    // Calculate the average flight time for a specific airline
    public double averageDuration(List<DTOs.FlightInfo> flightList, String airlineName) {
        double avg = flightList.stream()
                .filter(flightInfo -> flightInfo.getAirline() != null ? flightInfo.getAirline().equalsIgnoreCase(airlineName) : false)
                .collect(Collectors.averagingDouble(info -> info.getDuration().toMinutes()));
        return avg;
    }

//    double averageAmount = transactions.stream()
//            .collect(Collectors.averagingDouble(Transaction::getAmount));
//        System.out.println("Average transaction amount: " + averageAmount);
    public double totalFlightTime(List<DTOs.FlightInfo> flightList, String airlineNameTotal) {
        double total = flightList.stream()
                .filter((flightInfo -> flightInfo.getAirline() != null ? flightInfo.getAirline().equalsIgnoreCase(airlineNameTotal) : false))
                .collect(Collectors.summingDouble(info -> info.getDuration().toMinutes()));
        return total;

    }

    public List<DTOs.FlightInfo> flightSpecifick(List<DTOs.FlightInfo> flightList, LocalTime time, String airline) {
        return flightList.stream()
                .filter(flightInfo -> flightInfo.getAirline() != null && flightInfo.getAirline().equalsIgnoreCase(airline))
                .filter(flightInfo -> flightInfo.getDeparture().toLocalTime().isBefore(time))
                .toList();
    }
}
