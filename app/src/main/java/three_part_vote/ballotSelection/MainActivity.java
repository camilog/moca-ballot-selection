package three_part_vote.ballotSelection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;

import java.io.File;

import three_part_vote.ballotSelection.R;
/**
 * Created by diego diaz on 26-01-16.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean hasInfo = checkInfo();

        //Based on if the information has been configured previously or not, decide which activity to start
        if(!hasInfo){
            Intent intent = new Intent(MainActivity.this, ConfigurationActivity.class);
            startActivity(intent);
        }

        else {
            Intent intent2 = new Intent(MainActivity.this, DisplayCandidatesActivity.class);
            startActivity(intent2);
        }
    }

    //Check whether the public information has been previously configured or not
    public boolean checkInfo(){
        File candidateListDir = getApplicationContext().getDir("candidateList", Context.MODE_PRIVATE);
        File publicKeyDir = getApplicationContext().getDir("publicKeyN",Context.MODE_PRIVATE);
        File publicKeyFile = new File(publicKeyDir, "publicKeyN.key");
        File candidateListFile = new File(candidateListDir, "candidateList.json");

        if(!publicKeyFile.exists() || !candidateListFile.exists()){
            return false;
        }

        return true;
    }

}
