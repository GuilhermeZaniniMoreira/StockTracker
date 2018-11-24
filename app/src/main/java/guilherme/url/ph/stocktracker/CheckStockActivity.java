package guilherme.url.ph.stocktracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import guilherme.url.ph.stocktracker.Entidades.StockClass;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class CheckStockActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String userID;
    private DatabaseReference firebase;
    private AlertDialog alerta;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;

    private StockClass s;

    DecimalFormat df = new DecimalFormat("#.00");

    TextView qtdTextView, tickerTextView, valor, valorTotal, change, nome;
    Button update, delete, updateQtdButton;
    String concat;
    BigDecimal priceBD;
    BigDecimal changeBD;
    Integer qtdStock;
    String nomeString = "";
    EditText updateQtd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_stock);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        tickerTextView = findViewById(R.id.ticker);
        qtdTextView = findViewById(R.id.qtd);
        valor = findViewById(R.id.valor);
        valorTotal = findViewById(R.id.valorTotal);
        change = findViewById(R.id.change);
        nome = findViewById(R.id.nome);
        delete = findViewById(R.id.delete);
        update = findViewById(R.id.update);
        updateQtd = findViewById(R.id.editTextUpdateQtd);
        updateQtdButton = findViewById(R.id.updateQtdBtn);

        Intent intent = getIntent();

        final String ticker = String.valueOf(intent.getExtras().getString("ticker"));
        String qtd = String.valueOf(intent.getExtras().getInt("qtd"));

        tickerTextView.setText("Ticker: " + ticker);
        qtdTextView.setText("Quantidade: " + qtd);

        concat = ticker + ".SA";
        Stock stock = null;

        try {
            stock = YahooFinance.get(concat);
            priceBD = stock.getQuote(true).getPrice();
            changeBD = stock.getQuote().getChangeInPercent();
            nomeString = stock.getName();

        } catch (IOException e) {
            e.printStackTrace();
        }

        valor.setText(String.valueOf("Valor atual: R$ " + df.format(priceBD)));

        qtdStock = Integer.valueOf(qtd);
        nome.setText(nomeString);

        int i = qtdStock.intValue();
        BigDecimal temp = new BigDecimal(i);
        BigDecimal result = priceBD.multiply(temp);

        valorTotal.setText(String.valueOf("Valor total: R$ " + df.format(result)));

        double changeDouble = changeBD.doubleValue();

        if (changeDouble < 0) {
            change.setTextColor(getResources().getColor(R.color.negativeChange));
            change.setText(String.valueOf("Variação: " + changeDouble + "%"));
        } else if (changeDouble > 0) {
            change.setTextColor(getResources().getColor(R.color.positiveChange));
            change.setText(String.valueOf("Variação: " + changeDouble + "%"));
        } else {
            change.setText(String.valueOf("Variação: " + changeDouble + "%"));
        }

        delete.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CheckStockActivity.this);
                builder.setMessage("Você deseja excluir " + ticker + "?");

                builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                        firebase = rootRef.child("users").child(userID);

                        firebase.child(ticker).removeValue();

                        Intent intent1 = new Intent(CheckStockActivity.this, MainActivity.class);
                        startActivity(intent1);;
                        Toast.makeText(getApplicationContext(), "Ativo excluido", Toast.LENGTH_LONG).show();
                    }
                });

                builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(CheckStockActivity.this, "Exclusão cancelada!", Toast.LENGTH_LONG).show();
                    }
                });

                //criar o alertdialog
                alerta = builder.create();

                //exibe alertdialog
                alerta.show();
            }
        });

        update.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update.setVisibility(View.GONE);
                updateQtd.setVisibility(View.VISIBLE);
                updateQtdButton.setVisibility(View.VISIBLE);
                delete.setVisibility(View.GONE);
            }
        });

        updateQtdButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAuth = FirebaseAuth.getInstance();
                mFirebaseDatabase = FirebaseDatabase.getInstance();
                myRef = mFirebaseDatabase.getReference();
                FirebaseUser user = mAuth.getCurrentUser();
                userID = user.getUid();

                s = new StockClass();
                s.setTicker(ticker.toUpperCase());

                Integer qtd = Integer.valueOf(updateQtd.getText().toString());

                if (qtd <= 0) {
                    Intent i = new Intent(CheckStockActivity.this, MainActivity.class);
                    startActivity(i);
                    Toast.makeText(getApplicationContext(), "Quantidade não pode ser igual ou inferior a 0!", Toast.LENGTH_LONG).show();
                    return;
                }

                s.setQtd(qtd);

                myRef.child("users").child(userID).child(ticker).setValue(s);

                Intent intent2 = new Intent(CheckStockActivity.this, MainActivity.class);
                startActivity(intent2);
                Toast.makeText(getApplicationContext(), "Quantidade de "+ ticker +" alterada!", Toast.LENGTH_LONG).show();
            }
        });
    }
}