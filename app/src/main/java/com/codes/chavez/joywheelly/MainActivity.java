package com.codes.chavez.joywheelly;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ToggleButton;
import android.os.Vibrator;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity{

    //bt connection state msgs and stuff
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final byte END_T = (byte)255;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BTConnectionService mConnService = null;

    private TextView mTitle;
    private ListView mConversationView;
    //private EditText mOutEditText;
    private Button mSendButton;
    private Button clearButton;
    private ToggleButton avoidingToggle;
    private ToggleButton followingToggle;
    private ToggleButton switch1;

    RobotState robotState;
    SensorView distanceView;
    SensorView lightView;

    JoystickView joystickView;

    private static final int NB_MAX = 8;
    //

    private static final String TAG = "JoyWheelly";
    private static final boolean D = true;

    TextView logstr;

    Vibrator v;

    //private JoystickMovedListener moveListener;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        joystickView = (JoystickView) findViewById(R.id.joyview);
        joystickView.setOnJostickMovedListener(new JoystickMovedListener(){
            /*implementations for the joystick interface*/
            /*public void setOnJostickMovedListener(JoystickMovedListener listener) {
                this.moveListener = listener;
            }
            */

            public void OnMoved(int radial, int angle) {
                Log.d(TAG, String.format("moveListener.OnMoved(%d,%d)", (int) radial, (int) angle));
                byte[] cmd = new byte[7];
                //angle = 128;
                //radial = 128;

                cmd[0] = 1;
                cmd[1] = (byte) radial;
                cmd[2] = (byte)(angle >>> 8);
                cmd[3] = (byte)angle;
                cmd[4] = (byte)0;
                cmd[5] = (byte)191;
                cmd[6] = (byte)189;

                String s1 = String.format("%8s", Integer.toBinaryString(cmd[1] & 0xFF)).replace(' ', '0');
                String s2 = String.format("%8s", Integer.toBinaryString(cmd[1])).replace(' ', '0');
                Log.d(TAG, String.format("BIN of radu %s,  rads: %s", s1, s2));

                //String cmd_str = new String(cmd, Charset.forName("UTF-8"));
                if (switch1.isChecked())
                    sendMessage(cmd);
                else
                    logApp("Manual mode needed. \n");
            }

            public void OnReleased() {

            }

            public void OnReturnedToCenter() {

            }
            /**/
        });
        joystickView.setOnJostickClickedListener(new JoystickClickedListener(){
            public void OnClicked(){

            }
            public void OnReleased(){

            }
        });

        logstr = (TextView) findViewById(R.id.textView);
        //make the textview scrollable
        logstr.setMovementMethod(new ScrollingMovementMethod());
        //logstr.append("\n");
        logApp("\n");

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Put an action for Wheelly here", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */


        avoidingToggle = (ToggleButton) findViewById(R.id.button1);
        avoidingToggle.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                //logstr.append("Click on b1\n");
                //logApp("To do...\n");
                stopRecevingStateRobot();
                byte[] cmd = new byte[5];
                if (avoidingToggle.isChecked()){
                    //str = "Checked";
                    cmd[0] = 0;
                    cmd[1] = 2;
                    cmd[2] = 1;
                    cmd[3] = 0;
                    cmd[4] = END_T;
                    String cmd_str = new String(cmd, Charset.forName("UTF-8"));
                    sendMessage(cmd_str);
                }
                else{
                    //str = "Not checked";
                    cmd[0] = 0;
                    cmd[1] = 2;
                    cmd[2] = 2;
                    cmd[3] = 0;
                    cmd[4] = END_T;
                    String cmd_str = new String(cmd, Charset.forName("UTF-8"));
                    sendMessage(cmd_str);
                    //sendMessage("1");
                }
                startRecevingStateRobot();
            }
        });

        followingToggle = (ToggleButton) findViewById(R.id.button2);
        followingToggle.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                //Log.d(TAG, String.format("Click 2"));
                //TextView logtext = (TextView) MainActivity.this.findViewById(R.id.textView);
                //logstr.append("Click on b2\n");
                //logApp("To do...\n");
                stopRecevingStateRobot();
                byte[] cmd = new byte[5];
                if (followingToggle.isChecked()){
                    //str = "Checked";
                    cmd[0] = 0;
                    cmd[1] = 2;
                    cmd[2] = 0;
                    cmd[3] = 1;
                    cmd[4] = END_T;
                    String cmd_str = new String(cmd, Charset.forName("UTF-8"));
                    sendMessage(cmd_str);
                }
                else{
                    //str = "Not checked";
                    cmd[0] = 0;
                    cmd[1] = 2;
                    cmd[2] = 0;
                    cmd[3] = 2;
                    cmd[4] = END_T;
                    String cmd_str = new String(cmd, Charset.forName("UTF-8"));
                    sendMessage(cmd_str);
                    //sendMessage("1");
                }
                startRecevingStateRobot();

            }
        });

        switch1 = (ToggleButton) findViewById(R.id.toggleButton);
        switch1.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                stopRecevingStateRobot();
                String str;
                char eot='\n';
                byte[] cmd = new byte[3];
                Log.d(TAG, String.format("Switch 2"));
                TextView logtext = (TextView) MainActivity.this.findViewById(R.id.textView);
                if (switch1.isChecked()){
                    str = "Checked";
                    cmd[0] = 0;
                    cmd[1] = 1;
                    cmd[2] = END_T;
                    String cmd_str = new String(cmd, Charset.forName("UTF-8"));
                    sendMessage(cmd_str);
                }
                else{
                    str = "Not checked";
                    cmd[0] = 0;
                    cmd[1] = 0;
                    cmd[2] = END_T;
                    String cmd_str = new String(cmd, Charset.forName("UTF-8"));
                    sendMessage(cmd_str);
                    //sendMessage("1");
                }
                //logtext.append(str.toString()+"\n");
                //logApp(str +"\n");
                startRecevingStateRobot();
            }
        });

        clearButton = (Button) findViewById(R.id.buttonclear);
        clearButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                logstr.setText("");
            }
        });


        /*BT connection setup*/
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Set up the custom title: device + connection status
        //mTitle = (TextView) findViewById(R.id.title_left_text);
        //mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);

        robotState= new RobotState();
        distanceView = (SensorView) findViewById(R.id.sensorviewdist);
        distanceView.setTitle("Distance");
        lightView = (SensorView) findViewById(R.id.sensorviewlight);
        lightView.setTitle("Light");


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void logApp(String str){
        String str_tmp;
        str_tmp = logstr.getText().toString();
        str_tmp = str+str_tmp;
        logstr.setText(str_tmp);
    }

    public void analyzeReadMessage(byte[] msg, int lenght){
        String strt = new String("Unkown");
        byte type;
        type = msg[0];
        switch(type){
            case 0:// robot mode and module state
                robotState.setMode((msg[1]==1)?true:false);
                robotState.setAvoiding((msg[2]==1)?true:false);
                robotState.setFollowing((msg[3]==1)?true:false);
                Log.d(TAG, String.format("String: %d",msg[4]));
                switch(msg[4]){ // byte for message template
                    case 0:
                        strt = "Normal";
                        break;
                    case 1:
                        strt = "Collission";
                        logApp("App:  " + strt + "\n");
                        v.vibrate(100);
                        break;
                }
                displayRobotState();
                break;
            case 1:// message string (from a set of predefined strings)
                // NOTE: we can use the extra bits from the robot mode case 1

                break;
            case 2:// distance sensor info
                byte[] tmpd =new byte[8];
                for (int i=0; i<8; i++)
                    tmpd[i] = msg[1+i];
                robotState.setDistanceValues(tmpd);
                displayRobotState();
                break;
            case 3:// light sensor info
                byte[] tmpl =new byte[8];
                for (int i=0; i<8; i++)
                    tmpl[i] = msg[1+i];
                robotState.setLightValues(tmpl);
                displayRobotState();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        /*BT start process*/
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mConnService == null) setupConnection();
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.codes.chavez.joywheelly/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mConnService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mConnService.getState() == BTConnectionService.STATE_NONE) {
                // Start the Bluetooth chat services
                mConnService.start();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.codes.chavez.joywheelly/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth services
        if (mConnService != null) mConnService.stop();
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void setupConnection() {
        Log.d(TAG, "setupConnection() ");
        // Initialize the array adapter for the conversation thread
        //mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        //mConversationView = (ListView) findViewById(R.id.in);
        //mConversationView.setAdapter(mConversationArrayAdapter);
        // Initialize the compose field with a listener for the return key


        // Put a send button to send customized text to through the connection
        /*
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);
        // Initialize the send button with a listener that for click events
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });
        */


        // Initialize the BluetoothChatService to perform bluetooth connections
        mConnService = new BTConnectionService(this, mHandler);
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
        checkUIElementsWheelly();
    }

    private void displayRobotState(){
        if (robotState.getOnline() == false){
            switch1.setEnabled(false);
            avoidingToggle.setEnabled(false);
            followingToggle.setEnabled(false);
            joystickView.setVisibility(View.INVISIBLE);
        }else{
            switch1.setEnabled(true);
            avoidingToggle.setEnabled(true);
            if (robotState.getMode()==true)
                followingToggle.setEnabled(false);
            else
                followingToggle.setEnabled(true);
            joystickView.setVisibility(View.VISIBLE);

            switch1.setChecked(robotState.getMode());
            avoidingToggle.setChecked(robotState.getAvoiding());
            followingToggle.setChecked(robotState.getFollowing());

            int[] distance = new int[8];
            int[] light = new int[8];
            robotState.getDistanceValues(distance);

            robotState.getLightValues(light);

            lightView.setSensorValues(light);
            lightView.refreshView();
            distanceView.setSensorValues(distance);
            distanceView.refreshView();

            //logApp("D=" + Arrays.toString(distance)+"\n");
            //logApp("L=" + Arrays.toString(light)+"\n");
        }
    }

    private void requestRobotState(byte type){
        //request robot state
        byte[] cmd = new byte[5];
        cmd[0] = 0;
        cmd[1] = 3;
        cmd[2] = type;
        cmd[3] = 0;
        cmd[4] = END_T;
        String cmd_str = new String(cmd, Charset.forName("UTF-8"));
        sendMessage(cmd_str);

    }

    private void stopRecevingStateRobot(){
        requestRobotState((byte)0);
        SystemClock.sleep(80);
    }

    private void startRecevingStateRobot(){

        requestRobotState((byte)1);
    }


    private void checkUIElementsWheelly(){
        if (mConnService.getState() != BTConnectionService.STATE_CONNECTED) {
            robotState.setOnline(false);
        }
        else{
            robotState.setOnline(true);
        }
        displayRobotState();
    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mConnService.getState() != BTConnectionService.STATE_CONNECTED) {
            Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show();
            checkUIElementsWheelly();
            return;
        }
        // Check that there's actually something to send
        //Log.i(TAG,"number of bytes" + message.length());
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes(Charset.forName("UTF-8"));
            Log.i(TAG, "Sending bytes: "+message.length());
            for (int i=0;i< message.length();i++){
                Log.i(TAG, String.format("byte  %d: %8s", i, Integer.toBinaryString(send[i] & 0xFF)).replace(' ', '0'));
            }
            mConnService.write(send);
            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            //mOutEditText.setText(mOutStringBuffer);
        }
    }

    private void sendMessage(byte message[]) {
        // Check that we're actually connected before trying anything
        if (mConnService.getState() != BTConnectionService.STATE_CONNECTED) {
            Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show();
            checkUIElementsWheelly();
            return;
        }
        // Check that there's actually something to send
        //Log.i(TAG,"number of bytes" + message.length());
        if (message.length > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            //byte[] send = message.getBytes(Charset.forName("UTF-8"));
            Log.i(TAG, "Sending bytes: "+message.length);
            for (int i=0;i< message.length;i++){
                Log.i(TAG, String.format("byte %d: %8s", i, Integer.toBinaryString(message[i] & 0xFF)).replace(' ', '0'));
            }
            mConnService.write(message);
            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            //mOutEditText.setText(mOutStringBuffer);
        }
    }


    // The action listener for the EditText widget, to listen for the return key
    /*
    private TextView.OnEditorActionListener mWriteListener =
            new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                    // If the action is a key-up event on the return key, send the message
                    if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                        String message = view.getText().toString();
                        sendMessage(message);
                    }
                    if(D) Log.i(TAG, "END onEditorAction");
                    return true;
                }
            };
    */
    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        private String buffer_tmp;
        private final int CURRENT_CMD_MAX_SIZE = 1024;
        private byte []current_cmd = new byte[CURRENT_CMD_MAX_SIZE];
        private int idx_current = 0;
        private final byte EOT_C1=(byte)191;
        private final byte EOT_C2=(byte)189;
        private final int VALID_MSG_LENGHT=11; // used to identify real messages parsed by the system
        private int eot_flag = 0;
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BTConnectionService.STATE_CONNECTED:
                            mTitle.setText(R.string.title_connected_to);
                            mTitle.append(" "+mConnectedDeviceName);
                            //mConversationArrayAdapter.clear();
                            startRecevingStateRobot(); // send request to start updating state proccess
                            break;
                        case BTConnectionService.STATE_CONNECTING:
                            mTitle.setText(R.string.title_connecting);
                            break;
                        case BTConnectionService.STATE_LISTEN:
                        case BTConnectionService.STATE_NONE:
                            mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("App:  " + writeMessage);
                    //logstr.append("App:  " + writeMessage + "\n");
                    if (writeBuf[0]==1){
                        int tmp_angle = (writeBuf[2]<<8)&0x0000ff00|(writeBuf[3]<<0)&0x000000ff;

                        logApp("App:  (" + (writeBuf[1] & 0xFF) +", "+ tmp_angle  + ")\n");
                    }
                    else if (writeBuf[0]==0){
                        String tmp_state = "auto mode";
                        switch(writeBuf[1]){
                            case 0:
                                tmp_state = "Set Auto mode.";
                                break;
                            case 1:
                                tmp_state = "Set Manual mode.";
                                break;
                            case 2:
                                tmp_state = "Module setting: ";
                                if (writeBuf[2]==1)
                                    tmp_state+= "Avoiding(true) ";
                                else if (writeBuf[2]!=0)
                                    tmp_state+= "Avoiding(false) ";
                                if (writeBuf[3]==1)
                                    tmp_state+= "Following(true) ";
                                else if (writeBuf[3]!=0)
                                    tmp_state+= "Following(false) ";
                                break;
                            case 3:
                                tmp_state = "Request Robot state: ";
                                if (writeBuf[2]==1)
                                    tmp_state+= "true";
                                else
                                    tmp_state+= "false";
                                break;
                            default:
                                tmp_state = "Unknown command.";
                                break;

                        }
                        //tmp_state = (writeBuf[1]==0)? "auto mode":"manual mode";
                        logApp("App:  " + tmp_state + "\n");
                    }
                    else
                        logApp("App:  " + writeMessage + "\n");
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    //mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                    //logstr.append(mConnectedDeviceName+":  " + readMessage + "\n");
                    //Log.i(TAG,"current read: " + readMessage.toCharArray() + " : "+ msg.arg1 +" : " +msg.arg2);//+" : "+msg.toString());
                    String tmp = new String("");
                    for (int j=0; j<msg.arg1 ; j++)
                        tmp = tmp + Integer.toString(readBuf[j] & 0xff) + ", ";
                    //Log.i(TAG,"current bytes readings:" + tmp);
                    for (int i=0; i<msg.arg1 ;i++){
                        current_cmd[idx_current] = readBuf[i];
                        idx_current++;
                        if (idx_current==CURRENT_CMD_MAX_SIZE)
                            idx_current--;

                        if (readBuf[i]==EOT_C1){
                            eot_flag = 1;
                            //logApp(mConnectedDeviceName+" EOTC1: \n");
                        }
                        else{
                            if (readBuf[i]==EOT_C2 && eot_flag ==1){
                                //logApp(mConnectedDeviceName+" EOTC2:  \n");
                                eot_flag = 2;
                                //logApp(mConnectedDeviceName+": data size " + Integer.toString(idx_current) + "\n");
                                //logApp(mConnectedDeviceName+":  " + (Arrays.toString(Arrays.copyOfRange(current_cmd,0,idx_current))) + "\n");
                                if (idx_current == VALID_MSG_LENGHT){ // send the message to be analyzed
                                    analyzeReadMessage(current_cmd,idx_current);
                                }
                                cleanCurrentCmd();
                                eot_flag = 0;
                            }
                            else
                                eot_flag = 0;
                        }
                    }
                    //logApp(mConnectedDeviceName+":  " + readMessage + "\n");
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    checkUIElementsWheelly();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    checkUIElementsWheelly();
                    break;
            }
        }
        public void cleanCurrentCmd(){
            for (int i=0; i< CURRENT_CMD_MAX_SIZE; i++)
                current_cmd[i] =0;
            idx_current = 0;
        }

    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mConnService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupConnection();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan:
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
            //case R.id.discoverable:
                // Ensure this device is discoverable by others
            //    ensureDiscoverable();
            //    return true;
        }
        return false;
    }

    /*Class to manipulate robot state and variables*/

    public class RobotState{
        private boolean online;
        private boolean mode;
        private boolean avoiding_enable;
        private boolean following_enable;
        private int [] distance_sensor;
        private int [] light_sensor;
        public RobotState (){
            online = false;
            mode = false;
            avoiding_enable = false;
            following_enable = false;
            distance_sensor = new int[8];
            light_sensor = new int[8];
        }
        public void setOnline(boolean value){
            online = value;
        }
        public void setMode(boolean value){
            mode = value;
        }
        public void setAvoiding(boolean value){
            avoiding_enable = value;
        }
        public void setFollowing(boolean value){
            following_enable = value;
        }
        public void setDistanceValues(byte[] values){
            for (int i=0; i < values.length; i++){
                distance_sensor [i] = values[i];
                distance_sensor [i] = distance_sensor [i] & 0xFF;
            }
        }
        public void setLightValues(byte[] values){
            for (int i=0; i < values.length; i++){
                light_sensor [i] = values[i];
                light_sensor [i] = light_sensor [i] & 0xFF;
            }
        }
        public boolean getOnline(){
            return online;
        }
        public boolean getMode(){
            return mode;
        }
        public boolean getAvoiding(){
            return avoiding_enable;
        }
        public boolean getFollowing(){
            return following_enable;
        }
        public void getDistanceValues(int[] values){
            for (int i=0; i < distance_sensor.length; i++)
                values[i] = distance_sensor[i];
        }
        public void getLightValues(int[] values){
            for (int i=0; i < light_sensor.length; i++)
                values[i] = light_sensor [i];
        }

    };


}

