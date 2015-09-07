package three_part_vote.ballotSelection;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.print.PrintHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.math.BigInteger;
import java.util.Random;

public class DisplayQRBallotActivity extends Activity {

    // EXTRA to store the encryption, randomness, signature (byte[]) and selectedCandidate (String)
    public static final String EXTRA_ENCRYPTED_VOTE = "three_part_vote.ballotselection.encrypted_ballot",
                               EXTRA_PLAIN_BALLOT = "three_part_vote.ballotselection.plain_ballot",
                               EXTRA_RANDOMNESS = "three_part_vote.ballotselection.randomness",
                               EXTRA_SIGNATURE = "three_part_vote.ballotselection.signature";
    private String plainBallot;
    private byte[] encryptedVote, signature, randomness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qrcode);

        // Retrieve elements of the View
        TextView plainView = (TextView)findViewById(R.id.plain_view);
        ImageView ballotQrcodeView = (ImageView)findViewById(R.id.ballotQrcode_view);
        ImageView randomnessQrcodeView = (ImageView)findViewById(R.id.randomness_view);

        // Variables to store the data retrieved from the previous activity (encryptedVote, randomness, selectedCandidate and signature)
        plainBallot = getIntent().getStringExtra(EXTRA_PLAIN_BALLOT);
        encryptedVote = getIntent().getByteArrayExtra(EXTRA_ENCRYPTED_VOTE);
        signature = getIntent().getByteArrayExtra(EXTRA_SIGNATURE);
        randomness = getIntent().getByteArrayExtra(EXTRA_RANDOMNESS);

        // Set text to the first part of the view with the selectedCandidate
        plainView.setText(plainBallot);

        // Generation of the 2 QR-Codes
        try {
            // First QR-Code: JSON storing encryptedVote and signature
            String encryptedBallotString = new BigInteger(encryptedVote).toString();
            String signatureString = new BigInteger(signature).toString();

            String ballotJson = "{\"ballot\": {\"enc\":" + encryptedBallotString + ",\"sign\":" + signatureString + "}}";
            Bitmap ballotBitmap = generateQRCodeBitmap(ballotJson, 1);

            // Second QR-Code: randomness used to encrypt the ballot previously shown
            String randomnessString = new BigInteger(randomness).toString();
            Bitmap randomnessBitmap = generateQRCodeBitmap(randomnessString, 0);

            // Set QR-Code in the correspondent ImageView
            ballotQrcodeView.setImageBitmap(ballotBitmap);
            randomnessQrcodeView.setImageBitmap(randomnessBitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }

        // TODO: Print the ballot automatically

        // TODO: Create and set-up of the dialog shown after printing the ballot (describing next steps)

    }

    // Function to generate QRCode Bitmap from a String
    public Bitmap generateQRCodeBitmap(String data, int color) throws WriterException {
        BitMatrix bitMatrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, 400, 400);

        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++)
        {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                if (color > 0)
                    pixels[offset + x] = bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
                else
                    pixels[offset + x] = bitMatrix.get(x, y) ? 0xFF0000FF : 0xFFFFFFFF;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
        return bitmap;
    }

    // Function to take an screenshot of the view, necessary to print the ballot shown in the screen
    public Bitmap screenShot(View view) {
        // TODO: Cut button "Print Ballot"
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth() - 50, view.getHeight() - 60, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    // Function to print a Bitmap, in this case, the screenshot taken previously
    private void doPrint(Bitmap bitmap) {
        // TODO: Automatize this and try to print directly to the printer
        Random r = new Random();
        PrintHelper ballotPrinter = new PrintHelper(this);
        ballotPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        ballotPrinter.setColorMode(PrintHelper.COLOR_MODE_MONOCHROME);
        ballotPrinter.printBitmap("ballot" + r.nextInt(1000) + ".jpg", bitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.generate_qrcode, menu);
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
