package three_part_vote.ballotselection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Base64;
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

import paillierp.Paillier;
import paillierp.key.PaillierKey;

public class BallotConfirmationActivity extends Activity {

    public static final String EXTRA_SELECTED_CANDIDATE = "three_part_vote.ballotselection.selected_candidate";

    private TextView selectedCandidateView;
    private CharSequence selectedCandidateText;

    private Button confirmate, cancel;

    private byte[] encryptedBallot, randomUsed, sigBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ballot_confirmation);

        selectedCandidateText = getIntent().getCharSequenceExtra(EXTRA_SELECTED_CANDIDATE);

        selectedCandidateView = (TextView)findViewById(R.id.selected_candidate);
        selectedCandidateView.setText(selectedCandidateText);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);
        builder.setNeutralButton(R.string.dialog_neutral, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                BigInteger publicKeyN = null;
                AssetManager assetManager = getApplicationContext().getAssets();

                try {
                    ObjectInputStream oin_key = new ObjectInputStream(new BufferedInputStream(assetManager.open("publicKeyN.key")));
                    publicKeyN = (BigInteger) oin_key.readObject();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // TODO: Agrandar ballot para admitir más de 255 votos
                byte[] ballot_byteArray = new byte[getResources().getInteger(R.integer.number_of_candidates) + 2]; // Los dos extras son para voto blanco y primer resultado como suma total de los votos

                ballot_byteArray[0] = 1;

                int candidateSelectedNumber;
                try {
                    candidateSelectedNumber = Integer.parseInt((String) selectedCandidateText.subSequence(0, 2));
                } catch (NumberFormatException e) {
                    candidateSelectedNumber = ballot_byteArray.length - 1;
                }

                ballot_byteArray[candidateSelectedNumber] = 1;
                BigInteger ballot = new BigInteger(ballot_byteArray);

                PaillierKey publicKey = new PaillierKey(publicKeyN, new SecureRandom());
                BigInteger random = new BigInteger(publicKey.getPublicKey().getK(), new SecureRandom());

                Paillier p = new Paillier(publicKey);

                boolean v = true;
                while (v) {
                    try{
                        encryptBallot(p, random, ballot);
                        v = false;
                    } catch (Exception e)
                    {
                        random = new BigInteger(publicKey.getPublicKey().getK(), new SecureRandom());
                        v = true;
                    }
                }

                randomUsed = random.toByteArray();

                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                intent.putExtra("SCAN_CAMERA_ID", 0);

                startActivityForResult(intent, 0);
            }
        });
        final AlertDialog dialog = builder.create();

        confirmate = (Button)findViewById(R.id.confirmation_button);
        confirmate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        cancel = (Button)findViewById(R.id.cancel_confirmation_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void encryptBallot(Paillier p, BigInteger random, BigInteger ballot) {
        encryptedBallot = p.encrypt(ballot, random).toByteArray();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String stringPrivateKey = intent.getStringExtra("SCAN_RESULT");

                // Handle successful scan
                Intent intent2 = new Intent(this, GenerateQRCodeActivity.class);
                intent2.putExtra(GenerateQRCodeActivity.EXTRA_ENCRYPTED_BALLOT, encryptedBallot);
                intent2.putExtra(GenerateQRCodeActivity.EXTRA_PLAIN_BALLOT, selectedCandidateText);
                intent2.putExtra(GenerateQRCodeActivity.EXTRA_RANDOMNESS, randomUsed);

                try {
                    byte[] privateKeyBytes = Base64.decode(stringPrivateKey.getBytes("utf-8"), Base64.DEFAULT);
                    PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                    KeyFactory privateKeyFactory = KeyFactory.getInstance("RSA");
                    PrivateKey privateKey = privateKeyFactory.generatePrivate(privateSpec);

                    Signature signature = Signature.getInstance("SHA1withRSA");
                    signature.initSign(privateKey, new SecureRandom());

                    signature.update(encryptedBallot);
                    sigBytes = signature.sign();

                    intent2.putExtra(GenerateQRCodeActivity.EXTRA_SIGNATURE, sigBytes);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                startActivity(intent2);

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
    public void onBackPressed(){}

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
