import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

public class HRClient {
    
    private static void printUsage(){
        System.out.println("Available commands:");
        System.out.println("  java HRClient list <hostname>");
        System.out.println("  java HRClient book <hostname> <type> <number> <name>");
        System.out.println("  java HRClient guests <hostname>");
        System.out.println("  java HRClient cancel <hostname> <type> <number> <name>");
    }

    public static void main(String[] args){
        if(args.length== 0){
            printUsage();
            return;
        }

        try{
            String command= args[0];
            if(command.equals("list")){
                if(args.length!= 2){
                    printUsage();
                    return;
                }

                String hostname= args[1];
                HRInterface hotel= getRemoteObject(hostname);

                System.out.println(hotel.listRooms());
            }
            else if(command.equals("book")){
                if(args.length!= 5){
                    printUsage();
                    return;
                }

                String hostname= args[1];
                String type= args[2];
                int number= Integer.parseInt(args[3]);
                String name= args[4];

                HRInterface hotel= getRemoteObject(hostname);
                BookingResult result= hotel.book(type, number, name, false);
                System.out.println(result.message);

                boolean bookingCompleted= result.success;

                if(!result.success && result.partialAvailable){
                    try (Scanner scanner = new Scanner(System.in)) {
                        System.out.println("Would you like to book all available? (Y/N): ");
                        String ans= scanner.nextLine();

                        if(ans.equalsIgnoreCase("y") || ans.equalsIgnoreCase("yes")){
                            BookingResult partialResult= hotel.book(type, result.available, name, true);
                            System.out.println(partialResult.message);

                            bookingCompleted= partialResult.success;
                        }
                    }
                }

                if(!bookingCompleted){
                    try (Scanner scanner = new Scanner(System.in)) {
                        System.out.println("Would you like to be notified when room type " + type + " is available? (Y/N): ");
                        String ans= scanner.nextLine();

                        if(ans.equalsIgnoreCase("y") || ans.equalsIgnoreCase("yes")){
                            ClientCallback callback= new ClientCallbackImpl();
                            hotel.registerForNotification(type, name, callback);

                            System.out.println("Registration successful.");
                            System.out.println("Keep this window open to receive notifications.");
                            
                            while(true)
                                Thread.sleep(1000);
                        }
                    }
                }
            }
            else if(command.equals("guests")){
                if(args.length!= 2){
                    printUsage();
                    return;
                }

                String hostname= args[1];
                HRInterface hotel= getRemoteObject(hostname);
                System.out.println(hotel.listGuests());
            }
            else if(command.equals("cancel")){
                if(args.length!= 5){
                    printUsage();
                    return;
                }

                String hostname= args[1];
                String type= args[2];
                int number= Integer.parseInt(args[3]);
                String name= args[4];

                HRInterface hotel= getRemoteObject(hostname);
                System.out.println(hotel.cancel(type, number, name));
            }
            else printUsage();
        }
        catch(NumberFormatException e){
            System.out.println("Input error: <number> must be integer.");
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static HRInterface getRemoteObject(String hostname) throws Exception{
        Registry registry= LocateRegistry.getRegistry(hostname, 3338);
        return (HRInterface) registry.lookup("HotelReservation");
    }
}