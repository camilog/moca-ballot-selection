package three_part_vote.ballotselection;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

import paillierp.Paillier;
import paillierp.key.PaillierKey;

public class BallotConfirmationActivity extends Activity {

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
        setContentView(R.layout.activity_ballot_confirmation);

        // Retrieve the EXTRA and store it for later use
        selectedCandidateText = getIntent().getCharSequenceExtra(EXTRA_SELECTED_CANDIDATE);

        // Retrieve the TextView and set the text with the candidate selected
        selectedCandidateView = (TextView)findViewById(R.id.selected_candidate);
        selectedCandidateView.setText(selectedCandidateText);

        /*
        TODO: Check if include dialog now that the Signature is made with another device
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);
        builder.setNeutralButton(R.string.dialog_neutral, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something!
            }
        });
        final AlertDialog dialog = builder.create();
        */

        // Retrieve confirmation (OK) button and add Listener to the action
        confirmate = (Button)findViewById(R.id.confirmation_button);
        confirmate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // dialog.show();

                // Encryption procedure of the candidate selected and store of the randomness used
                BigInteger random = encryptionProcedure();
                randomUsed = random.toByteArray();

                // Create intent to initialize the next activity (ShowQR)
                Intent intent = new Intent(BallotConfirmationActivity.this, ShowEncryptedBallotQRActivity.class);

                // Pass the values of the selectedCandidate, encryption and randomness to the next activity
                intent.putExtra(ShowEncryptedBallotQRActivity.EXTRA_ENCRYPTED_BALLOT, encryptedBallot);
                intent.putExtra(ShowEncryptedBallotQRActivity.EXTRA_PLAIN_BALLOT, selectedCandidateText);
                intent.putExtra(ShowEncryptedBallotQRActivity.EXTRA_RANDOMNESS, randomUsed);

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
    private BigInteger encryptionProcedure() {
        // Variable to store publicKey that will be retrieved from a local file
        BigInteger publicKeyN = null;

        // Retrieve publicKey from local file using an assetManager, and store it in publicKeyN
        AssetManager assetManager = getApplicationContext().getAssets();
        try {
            ObjectInputStream oin_key = new ObjectInputStream(new BufferedInputStream(assetManager.open("publicKeyN")));
            publicKeyN = (BigInteger) oin_key.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // TODO: Agrandar ballot para admitir más de 255 votos
        // Creation of the ballot as a byte[] with size of number of candidates + 2 (one for get the total of votes and other for the blank vote)
        byte[] ballot_byteArray = new byte[getResources().getInteger(R.integer.number_of_candidates) + 2];

        // First position with a 1 to get the total of votes at the end in the first position
        ballot_byteArray[0] = 1;

        // Get the position of the selectedCandidate, which has to be as the first characters of the name. If it isn't (throws exception) is because is a blank vote
        int candidateSelectedNumber;
        try {
            candidateSelectedNumber = Integer.parseInt((String) selectedCandidateText.subSequence(0, 2));
        } catch (NumberFormatException e) {
            candidateSelectedNumber = ballot_byteArray.length - 1;
        }

        // Put a 1 in that position, relative to the selectedCandidate
        ballot_byteArray[candidateSelectedNumber] = 1;

        // Create BigInteger with the ballot, to the later encryption
        BigInteger ballot = new BigInteger(ballot_byteArray);

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
                // Function that actually encrypts, leaving in encryptedBallot the final encryption
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

    // Function to directly encrypt the Ballot, using the parameters of the scheme, the randomness and the ballot itself
    private void encryptBallot(Paillier p, BigInteger random, BigInteger ballot) {
        // Stores in encryptedBallot the encryption made
        encryptedBallot = p.encrypt(ballot, random).toByteArray();
    }

    // Eliminate function of Back Button of the device
    @Override
    public void onBackPressed(){}

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
