package three_part_vote.ballotselection;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

public class ShowQRActivity extends Activity {

    public static final String EXTRA_ENCRYPTED_BALLOT = "three_part_vote.ballotselection.showqr.encrypted_ballot",
                               EXTRA_RANDOMNESS = "three_part_vote.ballotselection.showqr.randomness",
                               EXTRA_PLAIN_BALLOT = "three_part_vote.ballotselection.showqr.selected_candidate";

    byte[] encryptedBallot,randomness, sigBytes;
    CharSequence selectedCandidateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qr);

        encryptedBallot = getIntent().getByteArrayExtra(EXTRA_ENCRYPTED_BALLOT);
        randomness = getIntent().getByteArrayExtra(EXTRA_RANDOMNESS);
        selectedCandidateText = getIntent().getStringExtra(EXTRA_PLAIN_BALLOT);

        ImageView qrImageView = (ImageView) findViewById(R.id.qr_imageView);
        Button receiveButton = (Button) findViewById(R.id.receive_button);

        try {
            qrImageView.setImageBitmap(generateQRCodeBitmap(encryptedBallot.toString()));
        } catch (WriterException e) {
            e.printStackTrace();
        }

        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                intent.putExtra("SCAN_CAMERA_ID", 0);

                startActivityForResult(intent, 0);
            }
        });


    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String stringSignature = intent.getStringExtra("SCAN_RESULT");

                // Handle successful scan
                Intent intent2 = new Intent(this, GenerateQRCodeActivity.class);
                intent2.putExtra(GenerateQRCodeActivity.EXTRA_ENCRYPTED_BALLOT, encryptedBallot);
                intent2.putExtra(GenerateQRCodeActivity.EXTRA_PLAIN_BALLOT, selectedCandidateText);
                intent2.putExtra(GenerateQRCodeActivity.EXTRA_RANDOMNESS, randomness);

                /*try {
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
                }*/

                sigBytes = stringSignature.getBytes();
                intent2.putExtra(GenerateQRCodeActivity.EXTRA_SIGNATURE, sigBytes);

                startActivity(intent2);

            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
                Toast toast = Toast.makeText(this, "Scan was Cancelled!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 25, 400);
                toast.show();

            }
        }
    }

    public Bitmap generateQRCodeBitmap(String data) throws WriterException {
        BitMatrix bitMatrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, 400, 400);

        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++)
        {
            int offset = y * width;
            for (int x = 0; x < width; x++)
                pixels[offset + x] = bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
        }

        Bitmap bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
        return bitmap;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_qr, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
