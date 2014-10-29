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
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;

import static android.widget.Toast.LENGTH_SHORT;
import static three_part_vote.ballotselection.R.string;


public class BallotConfirmationActivity extends Activity {

    public static final String EXTRA_SELECTED_CANDIDATE = "three_part_vote.ballotselection.selected_candidate";

    private TextView selectedCandidateView;
    private CharSequence selectedCandidateText;

    private Button confirmate;
    private Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ballot_confirmation);

        selectedCandidateText = getIntent().getCharSequenceExtra(EXTRA_SELECTED_CANDIDATE);

        selectedCandidateView = (TextView)findViewById(R.id.selected_candidate);
        selectedCandidateView.setText(selectedCandidateText);

        confirmate = (Button)findViewById(R.id.confirmation_button);
        confirmate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Paillier p = new Paillier();
                BigInteger n, nSquare, g;
                AssetManager assetManager = getApplicationContext().getAssets();
                try {
                    ObjectInputStream oin_g = new ObjectInputStream(new BufferedInputStream(assetManager.open("publicKey_g.key")));
                    ObjectInputStream oin_n = new ObjectInputStream(new BufferedInputStream(assetManager.open("publicKey_n.key")));
                    ObjectInputStream oin_nSquare = new ObjectInputStream(new BufferedInputStream(assetManager.open("publicKey_nSquare.key")));

                    g = (BigInteger)oin_g.readObject();
                    n = (BigInteger)oin_n.readObject();
                    nSquare = (BigInteger)oin_nSquare.readObject();

                    p.setG(g);
                    p.setN(n);
                    p.setNsquare(nSquare);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // TODO: Agrandar ballot para admitir más de 255 votos
                // TODO: Aceptar voto blanco

                // Los dos extras son para voto blanco y primer resultado como suma total de los votos
                byte[] ballot_byteArray = new byte[getResources().getInteger(R.integer.number_of_candidates) + 2];
                ballot_byteArray[0] = 1;
                int candidateSelectedNumber = Integer.parseInt((String) selectedCandidateText.subSequence(0, 2));
                ballot_byteArray[candidateSelectedNumber] = 1;
                BigInteger ballot = new BigInteger(ballot_byteArray);

                byte[] encryptedBallot = p.Encryption(ballot).toByteArray();
                byte[] randomUsed = p.getR().toByteArray();

                Intent intent = new Intent(BallotConfirmationActivity.this, GenerateQRCodeActivity.class);
                intent.putExtra(GenerateQRCodeActivity.EXTRA_ENCRYPTED_BALLOT, encryptedBallot);
                intent.putExtra(GenerateQRCodeActivity.EXTRA_PLAIN_BALLOT, selectedCandidateText);
                intent.putExtra(GenerateQRCodeActivity.EXTRA_RANDOMNESS, randomUsed);
                startActivity(intent);
            }
        });

        cancel = (Button)findViewById(R.id.cancel_confirmation_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Al apretar botón cancel, ir hacia atrás
            }
        });
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
