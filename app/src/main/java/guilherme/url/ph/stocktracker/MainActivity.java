package guilherme.url.ph.stocktracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import guilherme.url.ph.stocktracker.Adapter.StockAdapter;
import guilherme.url.ph.stocktracker.Entidades.StockClass;
import guilherme.url.ph.stocktracker.Formatter.MyFormatter;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import static android.R.layout.simple_list_item_1;

public class MainActivity extends AppCompatActivity {

    DecimalFormat df = new DecimalFormat("##.##");

    private FirebaseAuth mAuth;
    private String userID;

    private ListView listView;
    private ArrayAdapter<StockClass> adapter;

    private ArrayList<StockClass> stocks;
    private ArrayList<StockClass> qtdStocks;

    private DatabaseReference firebase;
    private ValueEventListener listener;
    private StockClass stockEscolhida;

    ArrayList<Integer> qtd = new ArrayList<>();
    ArrayList<String> ticker = new ArrayList<>();

    TextView info, add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stocks = new ArrayList<>();
        qtdStocks = new ArrayList<>();

        final TextView tv = (TextView) findViewById(R.id.valorTotal);

        final PieChart pieChart = (PieChart) findViewById(R.id.piechart);
        pieChart.setNoDataText("");

        listView = (ListView) findViewById(R.id.listview);
        adapter = new StockAdapter(this, stocks);
        listView.setAdapter(adapter);

        final LinearLayout linear = findViewById(R.id.LinearLayout);
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
                        add.setText("Adicione as suas ações através do botão.");
                        pieChart.setVisibility(View.GONE);
                        listView.setVisibility(View.GONE);
                        linear.setVisibility(View.GONE);

                    } else {
                        add.setVisibility(View.GONE);
                    }

                    qtd.clear();
                    ticker.clear();

                    adapter.notifyDataSetChanged();

                    for (DataSnapshot dados : dataSnapshot.getChildren()) {
                        ticker.add(dados.getKey());
                        qtd.add(Integer.parseInt(dados.child("qtd").getValue().toString()));
                    }

                    if (stocks.size() > 0) {
                        setupPieChart();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    stockEscolhida = adapter.getItem(i);
                    Intent intentCheck = new Intent(MainActivity.this, CheckStockActivity.class);
                    intentCheck.putExtra("ticker", stockEscolhida.getTicker());
                    intentCheck.putExtra("qtd", stockEscolhida.getQtd());
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

    private void setupPieChart() {

        ArrayList<Float> multi = new ArrayList<>();
        List<PieEntry> PieEntries = new ArrayList<>();

        Integer[] arrQtd = qtd.toArray(new Integer[qtd.size()]);
        String[] arrTickerSA = ticker.toArray(new String[ticker.size()]);
        String[] arrTicker = ticker.toArray(new String[ticker.size()]);

        for (int i = 0; i < arrTickerSA.length; i++) {
            arrTickerSA[i] = arrTickerSA[i] + ".SA";
        }

        Float valorFloat = null;
        Float qtdFloat = null;
        BigDecimal valor;
        double total = 0;

        try {
            Map<String, Stock> stocks = YahooFinance.get(arrTickerSA); // single request

            for (int i = 0; i < arrQtd.length; i++) {
                Stock atual = stocks.get(arrTickerSA[i]);
                valor = atual.getQuote().getPrice();
                valorFloat = Float.valueOf(df.format(valor.floatValue()));
                qtdFloat = (float) arrQtd[i];
                multi.add(qtdFloat * valorFloat);
            }

            Float[] arrValor = multi.toArray(new Float[multi.size()]);


            for (int i = 0; i < arrQtd.length; i++) {
                PieEntries.add(new PieEntry(arrValor[i], arrTicker[i]));
                total += arrValor[i].doubleValue();
            }

            PieDataSet dataSet = new PieDataSet(PieEntries, "");
            dataSet.setSliceSpace(1f);
            dataSet.setColors(ColorTemplate.LIBERTY_COLORS);
            PieData data = new PieData(dataSet);
            data.setValueTextSize(10f);
            data.setValueFormatter(new MyFormatter());

            PieChart pieChart = (PieChart) findViewById(R.id.piechart);
            pieChart.setCenterTextSize(15f);
            pieChart.getLegend().setEnabled(false);
            Description des = pieChart.getDescription();
            des.setEnabled(false);
            pieChart.setData(data);
            int colorBlack = Color.parseColor("#000000");
            pieChart.setEntryLabelColor(colorBlack);
            pieChart.setCenterText("R$ " + df.format(total));
            pieChart.setCenterTextSize(20f);
            pieChart.invalidate();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}