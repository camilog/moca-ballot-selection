package three_part_vote.ballotselection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.print.PrintHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.math.BigInteger;
import java.util.Random;

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
        Button printingButton = (Button)findViewById(R.id.print_button);

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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_final_message);
        builder.setTitle(R.string.dialog_title);
        builder.setNeutralButton("Finalizar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(GenerateQRCodeActivity.this, CandidatesActivity.class);
                intent.putExtra("needToClose", true);
                startActivity(intent);
            }
        });
        final AlertDialog dialog = builder.create();

        printingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap ballot = screenShot(findViewById(R.id.totalBallot_view));
                doPrint(ballot);
                dialog.show();
            }
        });

    }

    public Bitmap screenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private void doPrint(Bitmap bitmap) {
        Random r = new Random();
        PrintHelper ballotPrinter = new PrintHelper(this);
        ballotPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        ballotPrinter.setColorMode(PrintHelper.COLOR_MODE_MONOCHROME);
        ballotPrinter.printBitmap("ballot" + r.nextInt(1000) + ".jpg", bitmap);
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
    public void onBackPressed() {}

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
