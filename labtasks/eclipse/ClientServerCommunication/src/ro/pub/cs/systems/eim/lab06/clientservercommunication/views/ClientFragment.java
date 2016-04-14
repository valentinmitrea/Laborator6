package ro.pub.cs.systems.eim.lab06.clientservercommunication.views;

import java.io.BufferedReader;
import java.net.Socket;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import ro.pub.cs.systems.eim.lab06.clientservercommunication.R;
import ro.pub.cs.systems.eim.lab06.clientservercommunication.general.Constants;
import ro.pub.cs.systems.eim.lab06.clientservercommunication.general.Utilities;


public class ClientFragment extends Fragment {

    private EditText serverAddressEditText, serverPortEditText;
    private TextView serverMessageTextView;
    private Button displayMessageButton;

    
    private class ClientAsyncTask extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                // get the connection parameters (serverAddress and serverPort from parameters - on positions 0 and 1)
                // open a socket to the server
                // get the BufferedReader object in order to read from the socket (use Utilities.getReader())
                // while the line that was read is not null (EOF was not sent), append the content to serverMessageTextView
                // by publishing the progress - with the publishProgress(...) method - to the UI thread
                // close the socket to the server
            	Socket socket = new Socket(params[0], Integer.parseInt(params[1]));
            	BufferedReader bufferedReader = Utilities.getReader(socket);
            	
            	String line = bufferedReader.readLine();
            	while (line != null) {
            		publishProgress(line);
            		line = bufferedReader.readLine();
            	}
            	
            	socket.close();
            }
            catch (Exception exception) {
                Log.e(Constants.TAG, "An exception has occurred: " + exception.getMessage());
                if (Constants.DEBUG)
                    exception.printStackTrace();
            }
            
            return null;
        }

        
        @Override
        protected void onPreExecute() {
        	// reset the content of serverMessageTextView
        	serverMessageTextView.setText("");
        }

        
        @Override
        protected void onProgressUpdate(String... progress) {
        	// append the content to serverMessageTextView
        	serverMessageTextView.setText(serverMessageTextView.getText() + progress[0]);
        }

        
        @Override
        protected void onPostExecute(Void result) { }
        
    }

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle state) {
        return inflater.inflate(R.layout.fragment_client, parent, false);
    }

    
    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);

        serverAddressEditText = (EditText)getActivity().findViewById(R.id.server_address_edit_text);
        serverPortEditText = (EditText)getActivity().findViewById(R.id.server_port_edit_text);
        serverMessageTextView = (TextView)getActivity().findViewById(R.id.server_message_text_view);

        displayMessageButton = (Button)getActivity().findViewById(R.id.display_message_button);
        displayMessageButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClientAsyncTask clientAsyncTask = new ClientAsyncTask();
                clientAsyncTask.execute(serverAddressEditText.getText().toString(), serverPortEditText.getText().toString());
            }
        });
    }

}
