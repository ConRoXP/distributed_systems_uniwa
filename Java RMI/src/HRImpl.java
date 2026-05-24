import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class HRImpl extends UnicastRemoteObject implements HRInterface{
    private final Map<String, Integer> availableRooms;
    private final Map<String, Integer> prices;
    private final Map<String, Map<String, Integer>> reservations;
    private final Map<String, Map<String, ClientCallback>> notificationLists;

    public HRImpl() throws RemoteException{
        super();

        availableRooms= new HashMap<>();
        prices= new HashMap<>();
        reservations= new HashMap<>();
        notificationLists= new HashMap<>();

        availableRooms.put("A", 60);
        availableRooms.put("B", 50);
        availableRooms.put("C", 40);
        availableRooms.put("D", 30);
        availableRooms.put("E", 20);

        prices.put("A", 90);
        prices.put("B", 120);
        prices.put("C", 150);
        prices.put("D", 180);
        prices.put("E", 225);

        notificationLists.put("A", new HashMap<>());
        notificationLists.put("B", new HashMap<>());
        notificationLists.put("C", new HashMap<>());
        notificationLists.put("D", new HashMap<>());
        notificationLists.put("E", new HashMap<>());
    }

    //Room type validation
    private boolean isValidType(String type){
        return prices.containsKey(type);
    }

    //Room type to uppercase
    private String normalizeType(String type){
        if(type== null) return "";
        return type.toUpperCase();
    }

    @Override
    //java HRClient list <hostname>
    public synchronized String listRooms() throws RemoteException{
        StringBuilder sb= new StringBuilder();

        sb.append("Available rooms:\n");
        for(String type : new String[]{"A", "B", "C", "D", "E"}){
            sb.append(availableRooms.get(type))
            .append(" room(s) of type ")
            .append(type)
            .append(" - price: ")
            .append(prices.get(type))
            .append(" Euros/night\n");
        }

        return sb.toString();
    }

    //java HRClient book <hostname> <type> <number> <name>
    @Override
    public synchronized BookingResult book(String type, int number, String name, boolean allowPartial) throws RemoteException{
        type= normalizeType(type);

        if(!isValidType(type))
            return new BookingResult(false, false, 0, 0, 0, "Booking error: invalid room type.");
        if(number<= 0)
            return new BookingResult(false, false, 0, 0, 0, "Booking error: invalid room number.");
        if(name== null || name.trim().isEmpty())
            return new BookingResult(false, false, 0, 0, 0, "Booking error: invalid client name.");

        int available= availableRooms.get(type);
        int roomsToBook;

        if(available>= number)
            roomsToBook= number;
        else if(available> 0 && allowPartial)
            roomsToBook= available;
        else if(available> 0)
            return new BookingResult(false, true, available, 0, 0, "Only " + available + " type " + type + " room(s) available.");
        else
            return new BookingResult(false, false, 0, 0, 0, "Booking error: No available rooms.");

        availableRooms.put(type, available - roomsToBook);
        
        //reservations -> <Map<String, customerReservations<String, Integer>>
        //If client exists we update their reservations,
        //otherwise we create a new empty map for their reservations
        Map<String, Integer> customerReservations= reservations.get(name);
        if(customerReservations== null){
            customerReservations= new HashMap<>();
            reservations.put(name, customerReservations);
        }
        
        //Check for previous reservations
        int previous= customerReservations.getOrDefault(type, 0);
        customerReservations.put(type, previous + roomsToBook);

        int totalCost= roomsToBook * prices.get(type);

        return new BookingResult(true, false, available - roomsToBook, roomsToBook, totalCost,
            "Booking successful: " + roomsToBook + " room(s) of type " + type + "\nTotal cost: " + totalCost + " Euros");
    }

    //java HRClient guests <hostname>
    @Override
    public synchronized String listGuests() throws RemoteException{
        if(reservations.isEmpty()){
            return "No reservations found.";
        }

        StringBuilder sb= new StringBuilder();
        sb.append("Customers & Reservations:\n");
        for(Map.Entry<String, Map<String, Integer>> guestEntry : reservations.entrySet()){
            String name= guestEntry.getKey();
            Map<String, Integer> guestReservations= guestEntry.getValue();

            int guestTotal= 0;
            sb.append(name).append(":\n");

            for(String type : new String[]{"A", "B", "C", "D", "E"}){
                int rooms= guestReservations.getOrDefault(type, 0);

                if(rooms> 0){
                    int cost= rooms * prices.get(type);
                    guestTotal+= cost;

                    sb.append("  ").append(rooms).append(" room(s) of type ").append(type).append("\n")
                    .append("    Cost: ").append(cost).append(" Euros\n");
                }
            }

            sb.append("  Total reservation cost: ").append(guestTotal).append(" Euros\n");
        }

        return sb.toString();
    }

    //java HRClient cancel <hostname> <type> <number> <name>
    @Override
    public synchronized String cancel(String type, int number, String name) throws RemoteException{
        type= normalizeType(type);

        if(!isValidType(type))
            return "Cancellation error: Invalid room type.";
        if(number<= 0)
            return "Cancellation error: Invalid number of rooms.";
        if(name== null || name.trim().isEmpty())
            return "Cancellation error: Invalid client name.";

        Map<String, Integer> customerReservations= reservations.get(name);
        if(customerReservations== null)
            return "Cancellation error: No reservations found for client.";

        int bookedType= customerReservations.getOrDefault(type, 0);
        if(bookedType< number)
            return "Cancellation error: Client has not enough reservations for room type " + type + ".\n";
        if(bookedType== number)
            customerReservations.remove(type);
        else
            customerReservations.put(type, bookedType - number);

        availableRooms.put(type, availableRooms.get(type) + number);
        notifyClients(type);

        if(customerReservations.isEmpty()){
            reservations.remove(name);
            return "Cancellation successful: Client has no remaining reservations.\n";
        }

        StringBuilder sb= new StringBuilder();
        sb.append("Cancellation successful. Remaining client reservations:\n");
        int remainingTotal= 0;

        for(String roomType : new String[]{"A", "B", "C", "D", "E"}){
            int rooms= customerReservations.getOrDefault(roomType, 0);

            if(rooms> 0){
                int cost= rooms*prices.get(roomType);
                remainingTotal+= cost;
                sb.append(" " + roomType + ": " + rooms + " room(s).\n");
            }
        }

        sb.append("Total cost: " + remainingTotal + " Euros.\n");
        return sb.toString();
    }

    @Override
    public synchronized void registerForNotification(String type, String name, ClientCallback callback) throws RemoteException{
        type= normalizeType(type);

        if(!isValidType(type))
            throw new RemoteException("Invalid room type");
        if(name== null || name.trim().isEmpty())
            throw new RemoteException("Client name is empty.");

        notificationLists.get(type).put(name, callback);
        System.out.println(name + " is now registered for notifications about room type " + type);
    }

    private void notifyClients(String type){
        Map<String, ClientCallback> clients= notificationLists.get(type);
        if(clients== null | clients.isEmpty())
            return;

        String message= "" + availableRooms.get(type) + " room(s) of type " + type + " are now available.";

        for(Map.Entry<String, ClientCallback> entry : clients.entrySet()){
            try{
                entry.getValue().messageClient(message);
            }
            catch(RemoteException e){
                System.out.println("Could not notify client " + entry.getKey());
            }
        }

        clients.clear();
    }
}
