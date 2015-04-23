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

    // EXTRA to store the encryption (byte[]), the randomness used (byte[]) and the selectedCandidate (CharSequence)
    public static final String EXTRA_ENCRYPTED_BALLOT = "three_part_vote.ballotselection.showqr.encrypted_ballot",
                               EXTRA_RANDOMNESS = "three_part_vote.ballotselection.showqr.randomness",
                               EXTRA_PLAIN_BALLOT = "three_part_vote.ballotselection.showqr.selected_candidate";
    byte[] encryptedBallot,randomness;
    CharSequence selectedCandidateText;

    // byte[] to store the signature retrieved from the other device
    byte[] sigBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qr);

        // Variables to store the data retrieved from the previous activity (encryptedBallot, randomness and selectedCandidate)
        encryptedBallot = getIntent().getByteArrayExtra(EXTRA_ENCRYPTED_BALLOT);
        randomness = getIntent().getByteArrayExtra(EXTRA_RANDOMNESS);
        selectedCandidateText = getIntent().getStringExtra(EXTRA_PLAIN_BALLOT);

        // Retrieve elements of the view
        ImageView qrImageView = (ImageView) findViewById(R.id.qr_imageView);
        Button receiveButton = (Button) findViewById(R.id.receive_button);

        // Create the QR-Code of the encryption and place it in the imageView
        try {
            qrImageView.setImageBitmap(generateQRCodeBitmap(encryptedBallot.toString()));
        } catch (WriterException e) {
            e.printStackTrace();
        }

        // Set listener on the receive button
        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Initialize the SCAN application to retrieve the signature from the other device
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                intent.putExtra("SCAN_CAMERA_ID", 0);

                // Start SCAN activity
                startActivityForResult(intent, 0);
            }
        });

    }

    // Handle the result of the SCAN activity
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Id of the SCAN activity initialize before
        if (requestCode == 0) {

            // Handle successful scan
            if (resultCode == RESULT_OK) {
                // Retrieve signature from the SCAN, and store it in a String
                String stringSignature = intent.getStringExtra("SCAN_RESULT");

                // Create intent to initialize next activity (GenerateQRCode)
                Intent intent2 = new Intent(this, GenerateQRCodeActivity.class);

                // Pass the values of encryptedBallot, randomness and selectedCandidate to the next activity
                intent2.putExtra(GenerateQRCodeActivity.EXTRA_ENCRYPTED_BALLOT, encryptedBallot);
                intent2.putExtra(GenerateQRCodeActivity.EXTRA_PLAIN_BALLOT, selectedCandidateText);
                intent2.putExtra(GenerateQRCodeActivity.EXTRA_RANDOMNESS, randomness);

                // Transform the String of the signature to a byte[]
                sigBytes = stringSignature.getBytes();

                // Pass the signature value (byte[]) to the next activity
                intent2.putExtra(GenerateQRCodeActivity.EXTRA_SIGNATURE, sigBytes);

                // Start GenerateQRCode
                startActivity(intent2);

            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancelled scan
                Toast toast = Toast.makeText(this, "Scan was Cancelled!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 25, 400);
                toast.show();
            }

        }
    }

    // Function to generate QRCode Bitmap from a String
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
