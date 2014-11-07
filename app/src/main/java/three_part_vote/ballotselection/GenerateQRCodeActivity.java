package three_part_vote.ballotselection;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.support.v4.print.PrintHelper;
import android.util.Printer;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.math.BigInteger;

public class GenerateQRCodeActivity extends Activity {

    public static final String EXTRA_ENCRYPTED_BALLOT = "three_part_vote.ballotselection.encrypted_ballot";
    public static final String EXTRA_PLAIN_BALLOT = "three_part_vote.ballotselection.plain_ballot";
    public static final String EXTRA_RANDOMNESS = "three_part_vote.ballotselection.randomness";
    public static final String EXTRA_SIGNATURE = "three_part_vote.ballotselection.signature";

    private String plainBallot;
    private BigInteger encryptedBallot;
    private BigInteger signature;
    private BigInteger randomness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qrcode);

        ImageView ballotQrcodeView = (ImageView)findViewById(R.id.ballotQrcode_view);
        ImageView randomnessQrcodeView = (ImageView)findViewById(R.id.randomness_view);
        TextView plainView = (TextView)findViewById(R.id.plain_view);

        plainBallot = getIntent().getStringExtra(EXTRA_PLAIN_BALLOT);
        encryptedBallot = new BigInteger(getIntent().getByteArrayExtra(EXTRA_ENCRYPTED_BALLOT));
        signature = new BigInteger(getIntent().getByteArrayExtra(EXTRA_SIGNATURE));
        randomness = new BigInteger(getIntent().getByteArrayExtra(EXTRA_RANDOMNESS));

        plainView.setText(plainBallot);

        try {
            // QR = Largo del EncryptedBallot (3 caracteres) + EncryptedBallot + Signature
            Bitmap ballotBitmap = generateQRCodeBitmap(encryptedBallot.toString().length() + encryptedBallot.toString() + signature.toString());
            Bitmap randomnessBitmap = generateQRCodeBitmap(randomness.toString());

            ballotQrcodeView.setImageBitmap(ballotBitmap);
            randomnessQrcodeView.setImageBitmap(randomnessBitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }

    }

    private void doPhotoPrint(Bitmap bitmap) {
        PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        photoPrinter.printBitmap("example.jpg - test print", bitmap);
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
