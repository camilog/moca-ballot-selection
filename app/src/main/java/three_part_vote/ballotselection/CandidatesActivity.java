package three_part_vote.ballotselection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;

public class CandidatesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidates);

        ListView listView = (ListView) findViewById(R.id.candidates_listview);
        final String[] candidates = new String[getResources().getInteger(R.integer.number_of_candidates)];

        for (int i = 0; i < candidates.length; i++)
            candidates[i] = (String) getResources().getText(R.string.candidate01 + i);

        final ArrayList<String> list = new ArrayList<String>();

        for (int i = 0; i < candidates.length; ++i) {
            list.add(candidates[i]);
        }
        list.add(getResources().getString(R.string.blank_vote));

        final CandidatesArrayAdapter adapter = new CandidatesArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

    }

    private class CandidatesArrayAdapter extends ArrayAdapter<String> {

        List<String> list = new ArrayList<String>();
        Context context;

        public CandidatesArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
            this.context = context;
            this.list = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Activity activity = (Activity) this.context;
            LayoutInflater inflater = activity.getLayoutInflater();
            View view = inflater.inflate(R.layout.candidates_list_item_layout, parent, false);
            final Button button = (Button) view.findViewById(R.id.candidates_button);
            String candidate = list.get(position);
            button.setText(candidate);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(CandidatesActivity.this, BallotConfirmationActivity.class);
                    CharSequence candidateSelected = button.getText();
                    intent.putExtra(BallotConfirmationActivity.EXTRA_SELECTED_CANDIDATE, candidateSelected);
                    startActivity(intent);
                }
            });

            return view;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.candidates, menu);
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
