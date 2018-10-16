package guilherme.url.ph.stocktracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import guilherme.url.ph.stocktracker.Adapter.StockAdapter;
import guilherme.url.ph.stocktracker.Entidades.StockClass;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String userID;

    private ListView listView;
    private ArrayAdapter<StockClass> adapter;
    private ArrayList<StockClass> stocks;
    private DatabaseReference firebase;
    private ValueEventListener listener;
    private StockClass stockEsclhida;

    TextView info, add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stocks = new ArrayList<>();
        listView = (ListView) findViewById(R.id.listview);
        adapter = new StockAdapter(this, stocks);
        listView.setAdapter(adapter);

        info = (TextView) findViewById(R.id.info);
        add = (TextView) findViewById(R.id.add);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        if (user != null) {

            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            firebase = rootRef.child("users").child(userID);

            listener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    stocks.clear();

                    for (DataSnapshot dados : dataSnapshot.getChildren()) {
                        StockClass sc = dados.getValue(StockClass.class);
                        stocks.add(sc);
                    }

                    if (stocks.size() == 0) {
                        info.setText("Não há nenhuma ação adicionada!");
                        add.setText("Adicione as suas ações através do botão.");
                    }


                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    stockEsclhida = adapter.getItem(i);

                    Intent intentCheck = new Intent(MainActivity.this, CheckStockActivity.class);
                    intentCheck.putExtra("ticker", stockEsclhida.getTicker());
                    intentCheck.putExtra("qtd", stockEsclhida.getQtd());
                    startActivity(intentCheck);

                }
            });


        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(MainActivity.this, AddActivity.class);
                startActivity(i);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebase.removeEventListener(listener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebase.addValueEventListener(listener);
    }
}
