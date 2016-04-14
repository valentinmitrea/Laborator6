package ro.pub.cs.systems.eim.lab06.clientservercommunication.views;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import ro.pub.cs.systems.eim.lab06.clientservercommunication.R;
import ro.pub.cs.systems.eim.lab06.clientservercommunication.general.Constants;
import ro.pub.cs.systems.eim.lab06.clientservercommunication.general.Utilities;


public class ServerFragment extends Fragment {

    private EditText serverTextEditText;

    
    private ServerTextContentWatcher serverTextContentWatcher = new ServerTextContentWatcher();
    private class ServerTextContentWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
        }
        

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            Log.v(Constants.TAG, "Text changed in edit text: " + charSequence.toString());
            if (Constants.SERVER_START.toString().equals(charSequence.toString())) {
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

    
    private class CommunicationThread extends Thread {

        private Socket socket;

        
        public CommunicationThread(Socket socket) {
            this.socket = socket;
        }

        
        @Override
        public void run() {
            try {
                Log.v(Constants.TAG, "Connection opened with " + socket.getInetAddress() + ":" + socket.getLocalPort());
                // get the PrintWriter object in order to write on the socket (use Utilities.getWriter())
                // print a line containing the text in serverTextEditText edit text
                PrintWriter printWriter = Utilities.getWriter(socket);
                printWriter.println(serverTextEditText.getText().toString());
                
                socket.close();
                Log.v(Constants.TAG, "Connection closed");
            }
            catch (IOException ioException) {
                Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG)
                    ioException.printStackTrace();
            }
        }
        
    }

    
    private ServerThread serverThread;
    private class ServerThread extends Thread {

        private boolean isRunning;

        private ServerSocket serverSocket;

        
        public void startServer() {
            isRunning = true;
            start();
            Log.v(Constants.TAG, "startServer() method invoked");
        }

        
        public void stopServer() {
            isRunning = false;
            try {
                serverSocket.close();
            }
            catch(IOException ioException) {
                Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG)
                    ioException.printStackTrace();
            }
            Log.v(Constants.TAG, "stopServer() method invoked");
        }

        
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(Constants.SERVER_PORT);
                while (isRunning) {
                    Socket socket = serverSocket.accept();
                    if (socket != null) {
                        CommunicationThread communicationThread = new CommunicationThread(socket);
                        communicationThread.start();
                    }
                }
            }
            catch (IOException ioException) {
                Log.e(Constants.TAG, "An exception has occurred: "+ioException.getMessage());
                if (Constants.DEBUG)
                    ioException.printStackTrace();
            }
        }
        
    }

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle state) {
        return inflater.inflate(R.layout.fragment_server, parent, false);
    }

    
    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);

        serverTextEditText = (EditText)getActivity().findViewById(R.id.server_text_edit_text);
        serverTextEditText.addTextChangedListener(serverTextContentWatcher);
    }

    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serverThread != null)
            serverThread.stopServer();
    }

}
