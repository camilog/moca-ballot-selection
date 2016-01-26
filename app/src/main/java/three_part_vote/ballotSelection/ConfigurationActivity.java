package three_part_vote.ballotSelection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
/**
 * Created by diego diaz on 26-01-16.
 */
public class ConfigurationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration_layout);

        Button receiveList = (Button) findViewById(R.id.receive_candidates_info);
        receiveList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                intent.putExtra("SCAN_CAMERA_ID", 1);
                startActivityForResult(intent, 0);
            }
        });

        Button receiveKey = (Button) findViewById(R.id.receive_public_key_info);
        receiveKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                intent.putExtra("SCAN_CAMERA_ID", 1);
                startActivityForResult(intent, 1);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String encryptedCandidatesInfo = intent.getStringExtra("SCAN_RESULT");
                File candidateListDir = getApplicationContext().getDir("candidateList", Context.MODE_PRIVATE);
                File candidateListFile = new File(candidateListDir, "candidateList.json");

                try {
                    if (candidateListFile.exists())
                        candidateListFile.delete();

                    candidateListFile.createNewFile();

                    BufferedWriter writer = new BufferedWriter(new FileWriter(candidateListFile, true));
                    writer.write(encryptedCandidatesInfo);
                    writer.close();

                    Toast toast = Toast.makeText(this, "Candidate List recorded successfully!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 25, 400);
                    toast.show();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                Button receiveList = (Button) findViewById(R.id.receive_candidates_info);
                receiveList.setVisibility(View.GONE);
                TextView candidateInfo = (TextView) findViewById(R.id.user_info_candidates);
                candidateInfo.setVisibility(View.GONE);
                TextView keyInfo = (TextView) findViewById(R.id.user_info_public_key);
                keyInfo.setVisibility(View.VISIBLE);
                Button receiveKey = (Button) findViewById(R.id.receive_public_key_info);
                receiveKey.setVisibility(View.VISIBLE);
            }
        }
        else{
            if (resultCode == RESULT_OK){
                String encryptedPublicKeyInfo = intent.getStringExtra("SCAN_RESULT");
                File publicKeyDir = getApplicationContext().getDir("publicKeyN", Context.MODE_PRIVATE);
                File publicKeyFile = new File(publicKeyDir, "publicKeyN.key");

                try {
                    if (publicKeyFile.exists())
                        publicKeyFile.delete();

                    publicKeyFile.createNewFile();

                    BufferedWriter writer = new BufferedWriter(new FileWriter(publicKeyFile, true));
                    writer.write(encryptedPublicKeyInfo);
                    writer.close();

                    Toast toast = Toast.makeText(this, "Public Key recorded successfully!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 25, 400);
                    toast.show();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent intent1 = new Intent(ConfigurationActivity.this, DisplayCandidatesActivity.class);
                startActivity(intent1);
            }
        }
    }
}
