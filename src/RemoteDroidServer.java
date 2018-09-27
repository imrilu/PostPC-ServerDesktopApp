import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class RemoteDroidServer {
    private static int x,y;
    private static int MAX_X;
    private static int MAX_Y;
    private static int MIN_X;
    private static int MIN_Y;
    private static ServerSocket server = null;
    private static Socket client = null;
    private static BufferedReader in = null;
    private static String line;
    private static boolean isConnected=true;
    private static Robot robot;
    protected static final int SERVER_PORT = 8998;

    private static void processSensorData(float movex,float movez,float movey){
        float magnitude=(float)Math.sqrt(movex*movex+movey*movey+movez*movez);
        if (magnitude<0.10){
            //         System.out.println("MAGNITUDE LESS THAN 0.25");

            return;
        }
        final float sens =35.0f;//15.0f
        int newx=(int)(movex*sens);
        int newy=(int)(movey*sens);
        move(newx,newy);

    }
    public static void move(int movex,int movey){
        x=(x+movex)>MAX_X?MAX_X:((x+movex)<MIN_X?MIN_X:x+movex);
        y=(y+movey)>MAX_Y?MAX_Y:((y+movey)<MIN_Y?MIN_Y:y+movey);
        robot.mouseMove(x,y);
    }
    public static String getLocalIp() {

        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            String ip = socket.getLocalAddress().getHostAddress();
            return ip;
        } catch (SocketException e) {
            System.out.println("Problem with datagram socket...");
            e.printStackTrace();
        } catch (UnknownHostException e) {
            System.out.println("Problem with unknown host...");
            e.printStackTrace();
        }
        return null;
    }
    public static void main() {
        System.out.println("new main thread started...");
        boolean leftpressed=false;
        boolean rightpressed=false;
        Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();
        MAX_X=(int)screen.getWidth();
        MAX_Y=(int)screen.getHeight();
        MIN_X=0;
        MIN_Y=0;
        x=MAX_X/2;
        y=MAX_Y/2;
        boolean isRightClickPressed = false;
        boolean isLeftClickPressed = false;
        try{
            robot = new Robot();
            server = new ServerSocket(SERVER_PORT); //Create a server socket on port 8998
            System.out.println(getLocalIp());
            client = server.accept(); //Listens for a connection to be made to this socket and accepts it
            in = new BufferedReader(new InputStreamReader(client.getInputStream())); //the input stream where data will come from client
        }catch (SocketException e) {
            System.out.println("Connection Closed.");
        } catch (IOException e) {
            System.out.println("Error in opening Socket");
            System.exit(-1);
        }catch (AWTException e) {
            System.out.println("Error in creating robot instance");
            System.exit(-1);
        }
        int counter = 0;
        //read input from client while it is connected
        while(isConnected){
            try{

                line = in.readLine(); //read input from client

                if (line != null && !line.contains(",")) {
                    System.out.println(line); //print whatever we get from client
                }
                while (line == null) {
                    client = server.accept(); //Listens for a connection to be made to this socket and accepts it
                    in = new BufferedReader(new InputStreamReader(client.getInputStream())); //the input stream where data will come from client
                    line = in.readLine();
                }

                //if user clicks on next
                if(line.equalsIgnoreCase("n")){
                    //Simulate press and release of key 'n'
                    robot.keyPress(KeyEvent.VK_N);
                    robot.keyRelease(KeyEvent.VK_N);
                }
                //if user clicks on previous
                else if(line.equalsIgnoreCase("p")){
                    //Simulate press and release of key 'p'
                    robot.keyPress(KeyEvent.VK_P);
                    robot.keyRelease(KeyEvent.VK_P);
                }
                //if user clicks on play/pause
                else if(line.equalsIgnoreCase("space")){
                    //Simulate press and release of spacebar
                    robot.keyPress(KeyEvent.VK_SPACE);
                    robot.keyRelease(KeyEvent.VK_SPACE);
                }
                //input will come in x,y format if user moves mouse on mousepad
                else if(line.contains(",")){
                    float movex=-Float.parseFloat(line.split(",")[2]);//extract movement in x direction
                    float movez=Float.parseFloat(line.split(",")[1]);//extract movement in y direction
                    float movey=-Float.parseFloat(line.split(",")[0]);
                    Point point = MouseInfo.getPointerInfo().getLocation(); //Get current mouse position
                    float nowx=point.x;
                    float nowy=point.y;
                    processSensorData(movex,movez,movey);
//                    robot.mouseMove((int)(nowx+movex),(int)(nowy+movey));//Move mouse pointer to new location
                }
                //if user taps on mousepad to simulate a left click
                else if(line.contains("left_click")){
                    //Simulate press and release of mouse button 1(makes sure correct button is pressed based on user's dexterity)
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                }
                else if(line.contains("right_click")){
                    //Simulate press and release of mouse button 2(makes sure correct button is pressed
                    // based on user's dexterity)
                    robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                    robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                } else if (line.contains("right_click_long") && !isRightClickPressed) {
                    System.out.println("right click pressed");
                    robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                    isRightClickPressed = true;
                } else if (line.contains("left_click_long") && !isLeftClickPressed) {
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    System.out.println("left click pressed");
                    isLeftClickPressed = true;
                } else if (line.contains("right_click_stop")) {
                    System.out.println("right click stopped");
                    robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                    isRightClickPressed = false;
                } else if (line.contains("left_click_stop")) {
                    System.out.println("left click stopped");
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    isLeftClickPressed = false;
                } else if (line.equalsIgnoreCase("enter")) {
                    //Simulate press and release of key 'q'
                    robot.keyPress(KeyEvent.VK_ENTER);
                    robot.keyRelease(KeyEvent.VK_ENTER);
                } else if (line.equalsIgnoreCase("q")) {
                    //Simulate press and release of key 'q'
                    robot.keyPress(KeyEvent.VK_Q);
                    robot.keyRelease(KeyEvent.VK_Q);
                } else if (line.equalsIgnoreCase("w")) {
                    //Simulate press and release of key 'w'
                    robot.keyPress(KeyEvent.VK_W);
                    robot.keyRelease(KeyEvent.VK_W);
                } else if (line.equalsIgnoreCase("e")) {
                    //Simulate press and release of key 'e'
                    robot.keyPress(KeyEvent.VK_E);
                    robot.keyRelease(KeyEvent.VK_E);
                } else if (line.equalsIgnoreCase("r")) {
                    //Simulate press and release of key 'r'
                    robot.keyPress(KeyEvent.VK_R);
                    robot.keyRelease(KeyEvent.VK_R);
                } else if (line.equalsIgnoreCase("t")) {
                    //Simulate press and release of key 't'
                    robot.keyPress(KeyEvent.VK_T);
                    robot.keyRelease(KeyEvent.VK_T);
                } else if (line.equalsIgnoreCase("y")) {
                    //Simulate press and release of key 'y'
                    robot.keyPress(KeyEvent.VK_Y);
                    robot.keyRelease(KeyEvent.VK_Y);
                } else if (line.equalsIgnoreCase("u")) {
                    //Simulate press and release of key 'u'
                    robot.keyPress(KeyEvent.VK_U);
                    robot.keyRelease(KeyEvent.VK_U);
                } else if (line.equalsIgnoreCase("i")) {
                    //Simulate press and release of key 'p'
                    robot.keyPress(KeyEvent.VK_I);
                    robot.keyRelease(KeyEvent.VK_I);
                } else if (line.equalsIgnoreCase("o")) {
                    //Simulate press and release of key 'o'
                    robot.keyPress(KeyEvent.VK_O);
                    robot.keyRelease(KeyEvent.VK_O);
                } else if (line.equalsIgnoreCase("a")) {
                    //Simulate press and release of key 'a'
                    robot.keyPress(KeyEvent.VK_A);
                    robot.keyRelease(KeyEvent.VK_A);
                } else if (line.equalsIgnoreCase("s")) {
                    //Simulate press and release of key 's'
                    robot.keyPress(KeyEvent.VK_S);
                    robot.keyRelease(KeyEvent.VK_S);
                } else if (line.equalsIgnoreCase("d")) {
                    //Simulate press and release of key 'd'
                    robot.keyPress(KeyEvent.VK_D);
                    robot.keyRelease(KeyEvent.VK_D);
                } else if (line.equalsIgnoreCase("f")) {
                    //Simulate press and release of key 'f'
                    robot.keyPress(KeyEvent.VK_F);
                    robot.keyRelease(KeyEvent.VK_F);
                } else if (line.equalsIgnoreCase("g")) {
                    //Simulate press and release of key 'g'
                    robot.keyPress(KeyEvent.VK_G);
                    robot.keyRelease(KeyEvent.VK_G);
                } else if (line.equalsIgnoreCase("h")) {
                    //Simulate press and release of key 'h'
                    robot.keyPress(KeyEvent.VK_H);
                    robot.keyRelease(KeyEvent.VK_H);
                } else if (line.equalsIgnoreCase("j")) {
                    //Simulate press and release of key 'j'
                    robot.keyPress(KeyEvent.VK_J);
                    robot.keyRelease(KeyEvent.VK_J);
                } else if (line.equalsIgnoreCase("k")) {
                    //Simulate press and release of key 'k'
                    robot.keyPress(KeyEvent.VK_K);
                    robot.keyRelease(KeyEvent.VK_K);
                } else if (line.equalsIgnoreCase("l")) {
                    //Simulate press and release of key 'l'
                    robot.keyPress(KeyEvent.VK_L);
                    robot.keyRelease(KeyEvent.VK_L);
                } else if (line.equalsIgnoreCase("z")) {
                    //Simulate press and release of key 'z'
                    robot.keyPress(KeyEvent.VK_Z);
                    robot.keyRelease(KeyEvent.VK_Z);
                } else if (line.equalsIgnoreCase("x")) {
                    //Simulate press and release of key 'x'
                    robot.keyPress(KeyEvent.VK_X);
                    robot.keyRelease(KeyEvent.VK_X);
                } else if (line.equalsIgnoreCase("c")) {
                    //Simulate press and release of key 'c'
                    robot.keyPress(KeyEvent.VK_C);
                    robot.keyRelease(KeyEvent.VK_C);
                } else if (line.equalsIgnoreCase("v")) {
                    //Simulate press and release of key 'v'
                    robot.keyPress(KeyEvent.VK_V);
                    robot.keyRelease(KeyEvent.VK_V);
                } else if (line.equalsIgnoreCase("b")) {
                    //Simulate press and release of key 'b'
                    robot.keyPress(KeyEvent.VK_B);
                    robot.keyRelease(KeyEvent.VK_B);
                } else if (line.equalsIgnoreCase("n")) {
                    //Simulate press and release of key 'n'
                    robot.keyPress(KeyEvent.VK_N);
                    robot.keyRelease(KeyEvent.VK_N);
                } else if (line.equalsIgnoreCase("m")) {
                    //Simulate press and release of key 'm'
                    robot.keyPress(KeyEvent.VK_M);
                    robot.keyRelease(KeyEvent.VK_M);
                }
                //Exit if user ends the connection
                else if (line.equalsIgnoreCase("exit")) {
                    System.out.println("Exit thru equals, restarting connection");
                    client.close();
                    client = server.accept(); //Listens for a connection to be made to this socket and accepts it
                    in = new BufferedReader(new InputStreamReader(client.getInputStream())); //the input stream where data will come from client
                    line = in.readLine();
                }
            } catch (SocketException e) {
                System.out.println("Connection Closed.");

            } catch (IOException e) {
                System.out.println(e.getMessage());
                System.exit(-1);
            }
        }
    }
}
