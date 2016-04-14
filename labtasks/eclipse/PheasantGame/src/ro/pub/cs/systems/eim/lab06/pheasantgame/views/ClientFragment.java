package ro.pub.cs.systems.eim.lab06.pheasantgame.views;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import ro.pub.cs.systems.eim.lab06.pheasantgame.R;
import ro.pub.cs.systems.eim.lab06.pheasantgame.general.Constants;


public class ClientFragment extends Fragment {

    private EditText wordEditText;
    private Button sendButton;
    private TextView clientHistoryTextView;

    private Handler handler;

    private Socket socket;

    private String mostRecentWordSent = new String();
    private String mostRecentValidPrefix = new String();

    private ButtonClickListener buttonClickListener = new ButtonClickListener();
    private class ButtonClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {
            CommunicationThread communicationThread = new CommunicationThread(socket);
            communicationThread.start();
        }
        
    }

    
    private class CommunicationThread extends Thread {

        private Socket socket = null;
        private String word;

        
        public CommunicationThread(Socket socket) {
            this.socket = socket;
            if (socket == null) {
                try {
                    socket = new Socket(Constants.SERVER_HOST, Constants.SERVER_PORT);
                }
                catch (UnknownHostException unknownHostException) {
                    Log.e(Constants.TAG, "An exception has occurred: " + unknownHostException.getMessage());
                    if (Constants.DEBUG)
                        unknownHostException.printStackTrace();
                }
                catch (IOException ioException) {
                    Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG)
                        ioException.printStackTrace();
                }
            }
            Log.d(Constants.TAG, "[CLIENT] Created communication thread with: " + socket.getInetAddress() + ":" + socket.getLocalPort());
        }

        
        @Override
        public void run() {
            InputStream responseStream = null;
            OutputStream requestStream = null;
            try {
                requestStream = socket.getOutputStream();
            }
            catch (IOException ioException) {
                Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG)
                    ioException.printStackTrace();
            }
            
            PrintStream requestPrintWriter = new PrintStream(requestStream);
            try {
                responseStream = socket.getInputStream();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(responseStream));

            word = wordEditText.getText().toString();
            //trimitem cuvantul din EditText la server
            if (word.length() > 2) {
            	Log.d(Constants.TAG, "[CLIENT] Sent \"" + word + "\" on socket " + socket);
            	requestPrintWriter.println(word);
                mostRecentWordSent = word;
                clientHistoryTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        clientHistoryTextView.setText("Client sent word " + word + " to server\n" + clientHistoryTextView.getText().toString());
                    }
                });
            }
            else {
            	handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Word must be at least 2 characters long!", Toast.LENGTH_LONG).show();
                    }
                });
            }
            
            //vedem ce primim de la server
            try {
            	word = responseReader.readLine();
            	Log.d(Constants.TAG, "[CLIENT] Received \"" + word + "\", most recent word was \"" + mostRecentWordSent + "\" on socket " + socket);
                clientHistoryTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        clientHistoryTextView.setText("Client received word " + word + " from server\n" + clientHistoryTextView.getText().toString());
                    }
                });
                
                if (Constants.END_GAME.equals(word)) {
                	handler.post(new Runnable() {
                        @Override
                        public void run() {
                            wordEditText.setText("");
                            wordEditText.setEnabled(false);
                            sendButton.setEnabled(false);
                            clientHistoryTextView.setText("Communication ended!\n" + clientHistoryTextView.getText().toString());
                        }
                    });
                }
                //daca am primit ceva diferit de cuvantul trimis la server
                else if (mostRecentWordSent.isEmpty() || !mostRecentWordSent.equals(word)) {
                	mostRecentValidPrefix = word.substring(word.length() - 2, word.length());
                    wordEditText.post(new Runnable() {
                        @Override
                        public void run() {
                            wordEditText.setText(mostRecentValidPrefix);
                            wordEditText.setSelection(2);
                        }
                    });
                }
                //daca am primit acelasi cuvant pe care l-am trimis
                else {
                	wordEditText.post(new Runnable() {
                        @Override
                        public void run() {
                            wordEditText.setText(mostRecentValidPrefix);
                            if ((mostRecentValidPrefix != null) && (mostRecentValidPrefix.length() == 2)) 
                                wordEditText.setSelection(2);
                        }
                    });
                }
            }
            catch (IOException ioException) {
            	Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG)
                    ioException.printStackTrace();
            }
        }
    }

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle state) {
        return inflater.inflate(R.layout.fragment_client, parent, false);
    }

    
    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);

        wordEditText = (EditText)getActivity().findViewById(R.id.word_edit_text);
        sendButton = (Button)getActivity().findViewById(R.id.send_button);
        sendButton.setOnClickListener(buttonClickListener);
        clientHistoryTextView = (TextView)getActivity().findViewById(R.id.client_history_text_view);

        handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(Constants.SERVER_HOST, Constants.SERVER_PORT);
                }
                catch (UnknownHostException unknownHostException) {
                    Log.e(Constants.TAG, "An exception has occurred: "+unknownHostException.getMessage());
                    if (Constants.DEBUG)
                        unknownHostException.printStackTrace();
                }
                catch (IOException ioException) {
                    Log.e(Constants.TAG, "An exception has occurred: "+ioException.getMessage());
                    if (Constants.DEBUG)
                        ioException.printStackTrace();
                }
            }
        }).start();
    }

}