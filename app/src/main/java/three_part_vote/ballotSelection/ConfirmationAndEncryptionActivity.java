package three_part_vote.ballotSelection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

import paillierp.Paillier;
import paillierp.key.PaillierKey;

/**
 * Modified by diego diaz on 26-01-16.
 */
public class ConfirmationAndEncryptionActivity extends Activity {

    // EXTRA to store candidate selected (CharSequence)
    public static final String EXTRA_SELECTED_CANDIDATE = "three_part_vote.ballotselection.selected_candidate";

    private CharSequence selectedCandidateText;

    // Elements of the view
    private TextView selectedCandidateView;
    private Button confirmate, cancel;

    // Variables to store and pass to the next activity regarding the encryption, randomness and signature
    private byte[] encryptedBallot, randomUsed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation_and_encryption);

        // Retrieve the EXTRA and store it for later use
        selectedCandidateText = getIntent().getCharSequenceExtra(EXTRA_SELECTED_CANDIDATE);

        // Retrieve the TextView and set the text with the candidate selected
        selectedCandidateView = (TextView)findViewById(R.id.selected_candidate);
        selectedCandidateView.setText(selectedCandidateText);

        // Retrieve confirmation (OK) button and add Listener to the action
        confirmate = (Button)findViewById(R.id.confirmation_button);
        confirmate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Encryption procedure of the candidate selected and store of the randomness used
                BigInteger random = null;
                try {
                    random = encryptionProcedure();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                randomUsed = random.toByteArray();

                // Create intent to initialize the next activity (ShowQR)
                Intent intent = new Intent(ConfirmationAndEncryptionActivity.this, DisplayQREncryptedVoteActivity.class);

                // Pass the values of the select4edCandidate, encryption and randomness to the next activity
                intent.putExtra(DisplayQREncryptedVoteActivity.EXTRA_ENCRYPTED_VOTE, encryptedBallot);
                intent.putExtra(DisplayQREncryptedVoteActivity.EXTRA_PLAIN_BALLOT, selectedCandidateText);
                intent.putExtra(DisplayQREncryptedVoteActivity.EXTRA_RANDOMNESS, randomUsed);

                // Start ShowQR
                startActivity(intent);
            }
        });

        // Retrieve cancel button and add listener to the action (go back to the previous activity)
        cancel = (Button)findViewById(R.id.cancel_confirmation_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // Function that has all the procedure needed to get an encryption
    private BigInteger encryptionProcedure() throws IOException, ClassNotFoundException {
        // Variable to store publicKey that will be retrieved from local storage of the app
        BigInteger publicKeyN = null;
        File publicKeyDir = getApplicationContext().getDir("publicKeyN", Context.MODE_PRIVATE);
        File publicKeyFile = new File(publicKeyDir, "publicKeyN.key");
        String publicKeyString = "";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(publicKeyFile));
            publicKeyString = reader.readLine();
        } catch (IOException e) {}

        publicKeyN = new BigInteger(publicKeyString);

        // Total number of candidates in the election
        int numberOfCandidates = createCandidatesList().number_of_candidates;

        // Get the position of the selectedCandidate, which has to be as the first characters of the name. If it isn't (throws exception) is because is a blank vote
        int candidateSelectedNumber;
        try {
            candidateSelectedNumber = Integer.parseInt((String) selectedCandidateText.subSequence(0, 2));
        } catch (NumberFormatException e) {
            candidateSelectedNumber = numberOfCandidates + 1;
        }

        // Create the object Plain Vote
        PlainVote plainVote = new PlainVote(numberOfCandidates, candidateSelectedNumber);

        // Transform Plain Vote to Big Integer
        BigInteger ballot = plainVote.toBigInteger();

        // Creating the publicKey with the Paillier scheme, and having as basis the publicKey retrieved before
        PaillierKey publicKey = new PaillierKey(publicKeyN, new SecureRandom());

        // Create random number to encrypt, using the K bits of the publicKey
        BigInteger random = new BigInteger(publicKey.getPublicKey().getK(), new SecureRandom());

        // Set-up the scheme with the publicKey created before
        Paillier p = new Paillier(publicKey);

        // Encrypt the ballot, trying different random numbers, catching the exceptions and retaking the process
        boolean v = true;
        while (v) {
            try{
                // Function that actually encrypts, leaving in encryptedVote the final encryption
                encryptBallot(p, random, ballot);
                v = false;
            } catch (Exception e)
            {
                random = new BigInteger(publicKey.getPublicKey().getK(), new SecureRandom());
                v = true;
            }
        }

        // Return the randomness used to encrypt to verify it later
        return random;
    }

    //Function now gets candidate list stored in local storage of the app
    private CandidatesList createCandidatesList() throws IOException, ClassNotFoundException {
        File candidateListDir = getApplicationContext().getDir("candidateList", Context.MODE_PRIVATE);
        File candidateListFile = new File(candidateListDir, "candidateList.json");

        BufferedReader reader = new BufferedReader(new FileReader(candidateListFile));
        String candidatesListJson = reader.readLine();
        Gson gson = new Gson();

        CandidatesList candidatesList = gson.fromJson(candidatesListJson, CandidatesList.class);

        return candidatesList;
    }

    // Function to directly encrypt the Ballot, using the parameters of the scheme, the randomness and the ballot itself
    private void encryptBallot(Paillier p, BigInteger random, BigInteger ballot) {
        // Stores in encryptedVote the encryption made
        encryptedBallot = p.encrypt(ballot, random).toByteArray();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ballot_confirmation, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
