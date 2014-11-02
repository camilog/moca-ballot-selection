package three_part_vote.ballotselection;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

public class BallotConfirmationActivity extends Activity {

    public static final String EXTRA_SELECTED_CANDIDATE = "three_part_vote.ballotselection.selected_candidate";

    private TextView selectedCandidateView;
    private CharSequence selectedCandidateText;

    private Button confirmate;
    private Button cancel;

    private byte[] encryptedBallot;
    private byte[] randomUsed;
    private byte[] sigBytes;

    String stringPrivateKey_1;

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

                encryptedBallot = p.Encryption(ballot).toByteArray();
                randomUsed = p.getR().toByteArray();

                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                intent.putExtra("SCAN_CAMERA_ID", 1);

                startActivityForResult(intent, 0);

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

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Intent intent2 = new Intent("com.google.zxing.client.android.SCAN");
                intent2.putExtra("SCAN_MODE", "QR_CODE_MODE");
                intent2.putExtra("SCAN_CAMERA_ID", 1);

                stringPrivateKey_1 = intent.getStringExtra("SCAN_RESULT");

                startActivityForResult(intent2, 1);
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
                Toast toast = Toast.makeText(this, "Scan was Cancelled!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 25, 400);
                toast.show();
            }
        }
        else if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String stringPrivateKey_2 = intent.getStringExtra("SCAN_RESULT");
                String stringPrivateKey = stringPrivateKey_1.concat(stringPrivateKey_2);

                // Handle successful scan
                Intent intent3 = new Intent(this, GenerateQRCodeActivity.class);
                intent3.putExtra(GenerateQRCodeActivity.EXTRA_ENCRYPTED_BALLOT, encryptedBallot);
                intent3.putExtra(GenerateQRCodeActivity.EXTRA_PLAIN_BALLOT, selectedCandidateText);
                intent3.putExtra(GenerateQRCodeActivity.EXTRA_RANDOMNESS, randomUsed);

                try {
                    byte[] privateKeyBytes = Base64.decode(stringPrivateKey.getBytes("utf-8"), Base64.DEFAULT);
                    PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                    KeyFactory privateKeyFactory = KeyFactory.getInstance("RSA");
                    PrivateKey privateKey = privateKeyFactory.generatePrivate(privateSpec);

                    Signature signature = Signature.getInstance("SHA1withRSA");
                    signature.initSign(privateKey, new SecureRandom());

                    signature.update(encryptedBallot);
                    sigBytes = signature.sign();

                    intent3.putExtra(GenerateQRCodeActivity.EXTRA_SIGNATURE, sigBytes);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                startActivity(intent3);

            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
                Toast toast = Toast.makeText(this, "Scan was Cancelled!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 25, 400);
                toast.show();

            }
        }
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
