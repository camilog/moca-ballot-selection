package three_part_vote.ballotselection;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.concurrent.locks.Lock;

public class CandidatesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidates);

        final Button[] candidatesButtons = new Button[getResources().getInteger(R.integer.number_of_candidates)];
        LinearLayout buttonsLayout = (LinearLayout)findViewById(R.id.buttons_layout);
        for(int i = 0; i < candidatesButtons.length; i++){
            candidatesButtons[i] = new Button(this);
            candidatesButtons[i].setText(getResources().getText(R.string.candidate1 + i));
            buttonsLayout.addView(candidatesButtons[i]);
            final int j = i;
            candidatesButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(CandidatesActivity.this, BallotConfirmationActivity.class);
                    CharSequence candidateSelected = candidatesButtons[j].getText();
                    intent.putExtra(BallotConfirmationActivity.EXTRA_SELECTED_CANDIDATE, candidateSelected);
                    startActivity(intent);
                }
            });
        }

        final Button blankVoteButton = new Button(this);
        blankVoteButton.setText(R.string.blank_vote);
        buttonsLayout.addView(blankVoteButton);
        blankVoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CandidatesActivity.this, BallotConfirmationActivity.class);
                CharSequence candidateSelected = blankVoteButton.getText();
                intent.putExtra(BallotConfirmationActivity.EXTRA_SELECTED_CANDIDATE, candidateSelected);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.candidates, menu);
        return false;
    }

    @Override
    public void onBackPressed(){
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
