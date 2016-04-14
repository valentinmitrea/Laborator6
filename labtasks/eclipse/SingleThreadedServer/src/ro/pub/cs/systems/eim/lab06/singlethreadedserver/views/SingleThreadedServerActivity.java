package ro.pub.cs.systems.eim.lab06.singlethreadedserver.views;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import ro.pub.cs.systems.eim.lab06.singlethreadedserver.R;
import ro.pub.cs.systems.eim.lab06.singlethreadedserver.general.Constants;
import ro.pub.cs.systems.eim.lab06.singlethreadedserver.general.Utilities;


public class SingleThreadedServerActivity extends Activity {
	
    private EditText serverTextEditText;

    private ServerTextContentWatcher serverTextContentWatcher = new ServerTextContentWatcher();
    private class ServerTextContentWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            Log.v(Constants.TAG, "Text changed in edit text: " + charSequence.toString());
            if (Constants.SERVER_START.equals(charSequence.toString())) {
                serverThread = new ServerThread();
                serverThread.startServer();
                Log.v(Constants.TAG, "Starting server...");
            }
            if (Constants.SERVER_STOP.equals(charSequence.toString())) {
                serverThread.stopServer();
                Log.v(Constants.TAG, "Stopping server...");
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }

    }

    private ServerThread serverThread;
    private class ServerThread extends Thread {

        private boolean isRunning;
        private ServerSocket serverSocket;

        
        public void startServer() {
            isRunning = true;
            start();
            Log.v(Constants.TAG, "startServer() method invoked " + serverSocket);
        }

        
        public void stopServer() {
            isRunning = false;
            try {
                if (serverSocket != null)
                    serverSocket.close();
            }
            catch (IOException e) {
                Log.e(Constants.TAG, "An exception has occurred: " + e.getMessage());
                if (Constants.DEBUG)
                    e.printStackTrace();
            }
            Log.v(Constants.TAG, "stopServer() method invoked ");
        }

        
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(Constants.SERVER_PORT);
                while (isRunning) {
                    Socket socket = serverSocket.accept();
                    new CommuncationThread(socket).start();
                }
            }
            catch (IOException e) {
                Log.e(Constants.TAG, "An exception has occurred: " + e.getMessage());
                if (Constants.DEBUG)
                    e.printStackTrace();
            }
        }
    }
    
    
    private class CommuncationThread extends Thread {
    	
    	private Socket socket;
    	
    	
    	public CommuncationThread(Socket socket) {
    		this.socket = socket;
    	}
    	
    	
    	@Override
    	public void run() {
    		try {
    			Log.v(Constants.TAG, "Connection opened with " + socket.getInetAddress() + ":" + socket.getLocalPort());
                PrintWriter printWriter = Utilities.getWriter(socket);
                printWriter.println(serverTextEditText.getText().toString());
                
                try {
					Thread.sleep(10000);
                }
                catch (InterruptedException e) {
                	Log.e(Constants.TAG, e.getMessage());
                	if (Constants.DEBUG)
                		e.printStackTrace();
				}
                
                socket.close();
                Log.v(Constants.TAG, "Connection closed");
    		}
    		catch (IOException e) {
    			Log.e(Constants.TAG, "An exception has occurred: " + e.getMessage());
		    	if (Constants.DEBUG)
		    		e.printStackTrace();
    		}
    	}
    	
    }


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_threaded_server);

        serverTextEditText = (EditText)findViewById(R.id.server_text_edit_text);
        serverTextEditText.addTextChangedListener(serverTextContentWatcher);
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (serverThread != null)
			serverThread.stopServer();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.single_threaded_server, menu);
		
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
			return true;
		
		return super.onOptionsItemSelected(item);
	}
	
}
