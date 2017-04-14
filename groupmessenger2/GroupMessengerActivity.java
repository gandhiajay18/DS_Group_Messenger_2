package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;

import edu.buffalo.cse.cse486586.groupmessenger2.R;

import static java.lang.Thread.sleep;

// Reference: Content Values - https://developer.android.com/reference/android/content/ContentProvider.html#insert(android.net.Uri,%20android.content.ContentValues)
// Reference: Some suggestions from Piazza posts! & Code from previous (PA1 and PA2A)
// Reference: Socket Programming - https://developer.android.com/reference/java/net/Socket.html
// Reference: Priority Queue - https://developer.android.com/reference/java/util/PriorityQueue.html
// Reference: Priority Queue Iterator - https://developer.android.com/reference/java/util/Iterator.html
// Reference: List - https://docs.oracle.com/javase/tutorial/collections/interfaces/list.html
// Reference: Timer (to ping) - http://stackoverflow.com/questions/13121885/run-code-every-second-by-using-system-currenttimemillis


/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    private int seqNo = 0;                            //Sequence number
    List<Integer> myPortsList = new ArrayList<Integer>();
    int SequenceNo=0;
    private int portnumber = 0;
    boolean portremoved=true;
    boolean removeflag = false;
    boolean check = false;
    boolean mcast = false;
    boolean removeport = false;
    int port2remove = 0;
    boolean portfail = false;
    boolean portfound = false;
    HashMap<String,String> map_Deliverable = new HashMap<String, String>();
    HashMap<String,String> map_Queue = new HashMap<String, String>();
    Comparator<String> comparator = new ComparePriority();
    PriorityQueue<String> queue = new PriorityQueue<String>(100, comparator);
//    Iterator it = queue.iterator();
    static final int SERVER_PORT = 10000;           //Server Port Number - Similar to PA1
    static final String TAG = GroupMessengerActivity.class.getSimpleName();         //For Logging Purposes
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);
        Timer timer = new Timer();
        timer.schedule(new CheckIfPortDown(), 8000, 100);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */

        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        portnumber = Integer.parseInt(myPort);

        myPortsList.add(11108);
        myPortsList.add(11112);
        myPortsList.add(11116);
        myPortsList.add(11120);
        myPortsList.add(11124);
        System.out.println("My ports list is: "+ myPortsList);

        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(10000);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat pr100000ints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket"+myPort);
            return;
        }

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
        //Log.e(TAG, "Before button creation");
//Reference: https://developer.android.com/reference/android/widget/Button.html
//Reference: https://developer.android.com/reference/android/view/View.OnClickListener.html

        Button button = (Button) findViewById(R.id.button4);                //Create a Button
        final EditText et = (EditText) findViewById(R.id.editText1);        //Create a editText
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.e(TAG, "Inside OnClick");

               // System.out.println("Test OnClick");                         //Debugging purposes
                String msg = et.getText().toString();                //Get data from editText
                et.setText(""); // This is one way to reset the input box.  //Clear editText
                TextView TextView = (TextView) findViewById(R.id.textView1);//Generate a TextView
                TextView.append(msg);                                  // This is to display msg on TextView


                    /*
                     * Note that the following AsyncTask uses AsyncTask.SERIAL_EXECUTOR, not
                     * AsyncTask.THREAD_POOL_EXECUTOR as the above ServerTask does. To understand
                     * the difference, please take a look at
                     * http://developer.android.com/reference/android/os/AsyncTask.html
                     */
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort); //Send message 'msg'
//                Log.e(TAG, "End OnClick");


            }

        });}
    class CheckIfPortDown extends TimerTask {
        public void run() {
            try {
                int[] checkports = {11108, 11112, 11116, 11120, 11124}; //List of ports to check if any port is dead
                if(!portfound)
                {
                    for (int i : checkports) {                                                    //Iterate over the list
                    Socket checksocket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), i);     //Create sockets
                    PrintStream printer = new PrintStream(checksocket.getOutputStream());             //Write over the socket
                    //System.out.println("Printed: Hi to check");
                    printer.println("Hi to check");                                                 //Message to ping (initial)
                    printer.flush();
                    Thread.sleep(100);
                    check = false;
                    InputStreamReader input = new InputStreamReader(checksocket.getInputStream());   //Read over the socket
                    BufferedReader reader = new BufferedReader(input);
                    String confirm;                                                                    //Reply to ping
                    if ((confirm = reader.readLine()) != null) {

                        //System.out.println("Printed: Inside if" + confirm);
                        if (confirm.equals("Check successful")) {
                            check = true;
                        }

                    }
                    if (!check) {                                                                   //If ping fails
                        portfound=true;

                        System.out.println("Printed: Port failed " + i);                            //Port failed
                        port2remove = i;
                    }

                }
            }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {
        @Override

        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
//            try {
//                serverSocket.setSoTimeout(500);
//            } catch (SocketException e) {
//                e.printStackTrace();
//                System.out.println("Server side exception1 :");
//            }

            while (true) {
                try {
                //    Log.v(TAG, "Server Socket");



                        Socket example = serverSocket.accept();

                    InputStreamReader input = new InputStreamReader(example.getInputStream());
                    BufferedReader reader = new BufferedReader(input);
                    Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
                    String str  = reader.readLine();                    //Read string from client side
//                    Log.v(TAG, "Server Socket2");

                    if(str.equals("Hi to check"))                                           //To check if port is alive
                    {
                        PrintStream writer = new PrintStream(example.getOutputStream());
                        writer.println("Check successful");                                 //If alive respond to ping message
                        writer.flush();
                    }
                    if(str.contains("-r-") && !removeflag)                                  //Remove failed port messages (to unblock queue)
                    {
//                        portfound=true;
                        String[] remove = str.split(":");
                        int port2rem = Integer.parseInt(remove[1]);                         //The port(process) whose messages are to be removed
                        //System.out.println("Test: ENTERING port 2 rem: ="+port2rem);
                        Iterator<String> Queue_it = queue.iterator();                       //Create an iterator to read all messages in the queue
                        while(Queue_it.hasNext())                                           //Terminating Condition
                        {
                            String queue_val = (String) Queue_it.next();
                            String[] que_val_arr = queue_val.split(":");
                            String queue_val_msg = que_val_arr[0];
                            int initial_port = Integer.parseInt(que_val_arr[1].trim());
                            int max_seq = Integer.parseInt(que_val_arr[2]);
                            int port = Integer.parseInt(que_val_arr[3].trim());
                       //     System.out.println("Test: port 2 rem: ="+port2rem+" Initial_port: "+initial_port);
                            if(initial_port==port2rem)                                      //Check if messages of failed port(process) present in the queue
                            {
                                //System.out.println("QUEUE SIZE: port before :"+queue.size());
                                queue.remove(queue_val);                                   //Remove the message from queue
                                //System.out.println("QUEUE SIZE: port after :"+queue.size());
                                //System.out.println("Removed due to port failure :"+queue_val);
                                removeflag=true;
                            }
                        }
                    }
                    if(str.indexOf("-f-")==-1 && (!str.contains("-r-")) && (!str.equals("Hi to check")))         //Initial message
                    {
                        System.out.println("1st time in server task");
                        System.out.println("MY PORT IS : " + portnumber);                                       //Receivers port number
                        SequenceNo++;                                                                               //Sequence number
                        PrintStream initial = new PrintStream(example.getOutputStream());
                      //  System.out.println("Sending 1st time"+str+SequenceNo+":"+portnumber);
                        initial.println(str+":"+SequenceNo+":"+portnumber);                                     //Write message back to client with proposed sequence number
                        initial.flush();
                    //    System.out.println("Initial String is " + str);
                        String[] initialportsplit = str.split(":");
                        String strmsg = initialportsplit[0];
                        int initialport = Integer.parseInt(initialportsplit[1]);
                //        System.out.println("Output msg : "+strmsg);
                 //       System.out.println("Output initial port :"+initialport);
                  //      System.out.println("Output to map: "+strmsg);
                        map_Deliverable.put(strmsg.trim(),"No");                                                           //Map to check if message is deliverable or not
                     //   System.out.println("Output to diff map: "+initialport+":"+SequenceNo+":"+portnumber+":");
                        map_Queue.put(strmsg.trim(),initialport+":"+SequenceNo+":"+portnumber+":");                 //Map to retrieve old message from queue
                        //String insertto = strmsg+":"+SequenceNo+":"+portnumber;
                        queue.add(strmsg.trim()+":"+initialport+":"+SequenceNo+":"+portnumber);                //Add message to queue but undeliverable
                   //     System.out.println("Output queue add : "+strmsg+":"+initialport+":"+SequenceNo+":"+portnumber);
                        Thread.sleep(100);
                        //System.out.println("First time add :"+queue);
                    }
                    else if(str.indexOf("-f-")!=-1) {                                                          //Message comes with final(agreed) sequence number
              //          System.out.println("2nd time in server task");
//                        int index = str.indexOf("f");
//                        int index2 = str.indexOf("--");
//                        int index3 = str.indexOf(" ");
                        String[] SplitStr = str.split(":");
                        String newmsg = SplitStr[1].trim();
                        int initialport2 = Integer.parseInt(SplitStr[2].trim());
                        int maxseq = Integer.parseInt(SplitStr[3].trim());
                        SequenceNo = Math.max(SequenceNo,maxseq);                                           //Set the sequence number to max
                        int maxport = Integer.parseInt(SplitStr[4].trim());
        //                String newmsg = str.substring(index+2,index2).trim();
        //                int maxseq = Integer.parseInt(str.substring(index2+2,index3));
         //               int maxport = Integer.parseInt(str.substring(index3+1));
                        //System.out.println(" Msg :" + newmsg +"Initial port"+initialport2+ " Max Seq :" + maxseq + " Max port :" + maxport);

                        String[] val = map_Queue.get(newmsg).split(":");

//                        System.out.println("Msg 2nd time = "+newmsg+"Value for it = "+val[0]+":"+val[1]);
//                        System.out.println("Before queue remove :"+queue);
//                        System.out.println("Msg to remove : "+newmsg.trim()+Integer.parseInt(val[0])+":"+Integer.parseInt(val[1])+":"+Integer.parseInt(val[2]));
//                        System.out.println("QUEUE SIZE before final: "+queue.size());

                        queue.remove(newmsg.trim()+":"+Integer.parseInt(val[0])+":"+Integer.parseInt(val[1])+":"+Integer.parseInt(val[2]));     //Remove earlier - undeliverable message
                        queue.add(newmsg+":"+initialport2+":"+maxseq+":"+maxport);                                                              //Add new message - deliverable
                        map_Deliverable.put(newmsg,"Yes");                                                                                                 //Set it to deliverable
                    //    System.out.println("second time add :"+queue);
                      //  System.out.println("Added str in queue:"+newmsg+":"+initialport2+":"+maxseq+":"+maxport);
                        Thread.sleep(100);
                    }

//                    String tocheck = queue.peek();
//                    System.out.println("to check: "+tocheck);
//                    String[] tocheckSplit = tocheck.split(":");

                    //System.out.println("Before checking condition :"+tocheck+"      "+tocheckSplit[0]+"    "+map_Deliverable.get(tocheckSplit[0]));
                //    System.out.println("QUEUE SIZE: "+queue.size());
                    Thread.sleep(150);
                    while (queue.peek()!=null && map_Deliverable.get(queue.peek().split(":")[0]).equals("Yes"))                        //If message in queue (top) is deliverable - add it to the content provider
                    {
                     //   String tocheck = queue.peek();
//                        System.out.println("to check: "+tocheck);
                     //   String[] tocheckSplit = tocheck.split(":");

                        //System.out.println("Entering Content Provider " + tocheck);
                        //System.out.println("Actual Message to Save " + tocheckSplit[0]);
                        ContentValues cVal = new ContentValues();                     //Create a contentValues variable 'cVal'
                        cVal.put("key", String.valueOf(seqNo));                   //Insert  key (Sequence Number) into the cVal
                        cVal.put("value",queue.peek().split(":")[0].trim());              //Insert value (Data/msg) into the cVal

                                                                            //Increment Sequence Number
//                        System.out.println("After Incrementing :"+seqNo);

                        Uri uri = getContentResolver().insert(mUri,cVal);
                        seqNo++;

                        publishProgress(str);                                         //Publish the Message
            //            System.out.println("Saved Successfully");
                        queue.poll();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
          //          System.out.println("Server exception1 caused by :");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
            }
        }
        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0];
            TextView TextView = (TextView) findViewById(R.id.textView1);
            TextView.append(strReceived + "\t\n");
            //   TextView localTextView = (TextView) findViewById(R.id.local_text_display);
            //   localTextView.append("\n");

            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */

//            String filename = "SimpleMessengerOutput";
//            String string = strReceived + "\n";
//            FileOutputStream outputStream;
//
//            try {
//                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
//                outputStream.write(string.getBytes());
//                outputStream.close();
//            } catch (Exception e) {
//                Log.e(TAG, "File write failed");
//            }

            return;
        }
    }

    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     * @author stevko
     *
     */
    static class ComparePriority implements Comparator<String>
    {
        @Override
        public int compare(String val1, String val2)
        {
            // Assume neither string is null. Real code should
            // probably be more robust
            // You could also just return x.length() - y.length(),
            // which would be more efficient.

                String[] Split = val1.split(":");
                String msgx = Split[0].trim();
                int seqx = Integer.parseInt(Split[2].trim());
                int portx = Integer.parseInt(Split[3].trim());

        //    System.out.println(" X: "+x);
                String[] Split2 = val2.split(":");
                String msgy = Split2[0].trim();
                int seqy = Integer.parseInt(Split2[2].trim());
                int porty = Integer.parseInt(Split2[3].trim());

        //    System.out.println(" Y: "+y);

                if (seqx != seqy) {
                    if (seqx < seqy) {
                        return -1;
                    }
                    else if (seqx > seqy) {
                        return 1;
                    }
                }
                else if (seqx == seqy) {
                  //  System.out.println("Sequence Numbers same,comparing ports");

                    if (porty < portx) {
                    //    System.out.println("port1>port2");
                        return 1;
                    }else if (porty > portx)
                    { // System.out.println("port1<port2");

                        return -1;
                }
                }

            return 0;
        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {

            //int[] PORTS = {11108,11112,11116,11120,11124};                              //Array of all 5 port values
            HashMap<String,String> map_SequenceNo = new HashMap<String, String>();

            try {


                for (int i : myPortsList ) {                                                    //Loop over all ports to multicast
                    if(i!=port2remove){
                    Log.e(TAG, "Inside ClientTask");
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), i);    //Create a socket for every port

                    Log.e(TAG, "Client Socket Created");

                    String msgToSend = msgs[0].trim();                                                           //Message to be sent
                    PrintStream ps = new PrintStream(socket.getOutputStream());
                //    System.out.println("Msg to send" + msgToSend + "Initial Port Number" + i);
                    msgToSend = msgToSend+":"+portnumber;                                           //Send message and sender portnumber
                //    System.out.println("Initia message to send :" + msgToSend);
                    ps.println(msgToSend);
                //    System.out.println("Msg to send" + msgToSend + "Initial Port Number" + i);
                    ps.flush();
                    Thread.sleep(100);
                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
                    InputStreamReader inp = new InputStreamReader(socket.getInputStream());

                    BufferedReader read = new BufferedReader(inp);                                  //Read response from server side
                    String msgseqport = read.readLine();                                            //Get message+initalport+sequence number+receivers portnumber
                  //  System.out.println("Received 2nd time in client"+msgseqport);
                    if (msgseqport != null) {

//                    System.out.println("MSG: "+msg1);

                        String[] splitall = msgseqport.split(":");
                        String msg1 = splitall[0];
                        String iniPort = splitall[1];
                        String ctr1 = splitall[2];
//                    System.out.println("Counter: "+ctr1);
                        String port1 = splitall[3];
                        //                  System.out.println("Port Number received at sender: "+port1);

                        String valueformap = iniPort+":"+ctr1+":"+port1;                            //Value to insert into map

//                    System.out.println("Printing Map" + map.toString());
//                    System.out.println("Print msg1" + msg1);
                        //System.out.println("Print Split Value :"+Integer.parseInt(valueformap.substring(0,valueformap.indexOf(" "))));
                        if (map_SequenceNo.containsKey(msg1)) {                                                //To agree upon max sequence number
//                        System.out.println("Entering 2nd time into map");
                            String oldvalue = map_SequenceNo.get(msg1);
                            String[] oldvalarr = oldvalue.split(":");
                            int iniport = Integer.parseInt(oldvalarr[0].trim());
                            int ctrmax = Integer.parseInt(oldvalarr[1].trim());
                            int porttosend = Integer.parseInt(oldvalarr[2].trim());
                            String[] valueformaparr = valueformap.split(":");
                            int valiniport = Integer.parseInt(valueformaparr[0].trim());
                            int valctrmax = Integer.parseInt(valueformaparr[1].trim());
                            int valporttosend = Integer.parseInt(valueformaparr[2].trim());
                            //System.out.println("Here old value is:"+oldvalue);
                            if (valctrmax <= ctrmax) {
                 //               System.out.println("not updated");
                            } else if (valctrmax > ctrmax) {
                   //             System.out.println("updated");
                                map_SequenceNo.put(msg1, valueformap);                                         //Update with max sequence number
                            }
                        } else {
                            map_SequenceNo.put(msg1, valueformap);                                             //Enter message and value into map
//                        System.out.println("Inserting into map");
                        }
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }}

                if(portfound && !mcast)                                                             //To broadcast failure message
                {
//                    mcast = true;
                    for (int i : myPortsList) {
                        if(i!=port2remove){
                        Socket socket2convey = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), i);
                        PrintStream ps3 = new PrintStream(socket2convey.getOutputStream());
                        ps3.println("-r-" + ":" + port2remove);                                     //Broadcast failed port number to all processes

                        ps3.flush();
                        }
                    }
                }
                //Send max sequence and msg back to server task - below
                for (int i : myPortsList) {                                                         //To send final message to server
                    if(i!=port2remove){
                    //Loop over all ports to multicast
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), i);

                    PrintStream ps2 = new PrintStream(socket.getOutputStream());

                    String msg1 = msgs[0];
                    msg1 = msg1.trim();
//                    System.out.println("Printing Map" + map.toString());
                    String value1 = null;
                    if (map_SequenceNo.containsKey(msg1))
                        value1 = map_SequenceNo.get(msg1);                                                     //Retrieve agreed sequence number for the message
                    if (value1 != null) {
                        //         System.out.println("Msg to send: " + msg1);
                        //        System.out.println("Value to send: " + value1);
                        ps2.println("-f-" + ":" + msg1 + ":" + value1 + ":");                       //Send message with agreed sequence number to server task
                        ps2.flush();
                    }
//  try {
//                        Thread.sleep(300);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                }


            }catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }
        }




}
