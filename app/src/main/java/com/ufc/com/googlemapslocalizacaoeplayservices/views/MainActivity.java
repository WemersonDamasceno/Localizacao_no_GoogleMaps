package com.ufc.com.googlemapslocalizacaoeplayservices.views;

import android.Manifest;
import android.app.IntentService;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.ufc.com.googlemapslocalizacaoeplayservices.Constants;
import com.ufc.com.googlemapslocalizacaoeplayservices.FetchAddressService;
import com.ufc.com.googlemapslocalizacaoeplayservices.R;
import com.ufc.com.googlemapslocalizacaoeplayservices.TesteActivity;

public class MainActivity extends AppCompatActivity {
    FusedLocationProviderClient client;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    ResultReceiver resultReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //PEGAR EVENTOS DA LOCALIZAÇÃO DO CLIENTE
        client = LocationServices.getFusedLocationProviderClient(this);
        resultReceiver = new AddressResultReceiver(null);



        checarPermissaoClient();







    }

    //Faz a pergunta para o usuario da PERMISSAO
    private void checarPermissaoClient() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        0);
            }
        }
    }
    //O código abaixo faz o tratamento da resposta do usuário sobre a PERMISSAO
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // A permissão foi concedida. Pode continuar
                Toast.makeText(this, "Sucess | Permissão concedida", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Fail | Aceite a permissão para usar o app", Toast.LENGTH_LONG).show();
                //O programa necessita disso
                finish();
                // A permissão foi negada. Precisa ver o que deve ser desabilitado
            }
            return;
        }
    }
    @Override
    protected void onResume() {
        //Question of location request
        createLocationRequest();
        askForLocationChange();

        //saber se o google services está atualizado
        int gPlayServices = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if((gPlayServices == ConnectionResult.SERVICE_MISSING)
                || (gPlayServices == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED)
                || (gPlayServices == ConnectionResult.SERVICE_DISABLED)) {
            Toast.makeText(this, "Fail | Baixe/ Ative/ Atualize o Google Play Services", Toast.LENGTH_SHORT).show();
            GoogleApiAvailability.getInstance()
                    .getErrorDialog(this, gPlayServices, 0, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            //matar a activity, pois o programa nao pode rodar sem o google play
                            finish();
                        }
                    }).show();
        }
        if(gPlayServices == ConnectionResult.SUCCESS){
            //Toast.makeText(this, "Sucess | Google services", Toast.LENGTH_SHORT).show();
        }
        //Pegando a ultima localização
        client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    Log.i("Sucess_Location","Lat "+location.getLatitude()+" Long "+location.getLongitude());
                }else {
                    Log.i("Erro_Location", "Location is Null");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("teste","Falha ao pegar a localização"+e);
            }
        });

        client.requestLocationUpdates(locationRequest, locationCallback, null);


        super.onResume();
    }
    private void askForLocationChange() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(builder.build())
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        //verificar se o GPS esta ativo e outras coisas
                        // locationSettingsResponse.getLocationSettingsStates().isGpsPresent();
                        Log.i("Teste", locationSettingsResponse.
                                getLocationSettingsStates().isNetworkLocationPresent()+" que tem net");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ResolvableApiException){
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,10);
                    }catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

       locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(locationResult == null){
                    Log.i("teste2", "Location is null");
                    return;
                }
                //tenho as localizações que estão sendo pegas a cada X segundos
                for ( Location location : locationResult.getLocations()) {
                    Log.i("teste2", location.getLatitude()+" essa é a Latitude");
                    if(!Geocoder.isPresent()){
                        return;
                    }else {
                        starIntentService(location);
                    }
                }
            }
            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                //se a localização esta disponivel ou nao
                Log.i("teste2", locationAvailability.isLocationAvailable()+"");
                super.onLocationAvailability(locationAvailability);
            }
        };

    }
    //iniciar o servico
    private void starIntentService(Location location) {
        Intent intent = new Intent(this, FetchAddressService.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }
    private void createLocationRequest() {
        //Pegar posicao mais precisa com o FINE
        locationRequest = LocationRequest.create();
        //Intervalo de vezes que o app vai procurar a loxalização do usuario defini 15 segundos
        locationRequest.setInterval(15 * 1000);
        //Localizacao vindas de outros App's
        locationRequest.setFastestInterval(5 * 1000);
        //Prioridade de precisao e bateria
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }
    private class AddressResultReceiver extends ResultReceiver{
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }
        //Aqui é onde recebemos os valores do FetchAddress
        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData) {
            if (resultData == null){
                finish();
            }
            final String enderecoSaida = resultData.getString(Constants.RESULT_DATA_KEY);

            if(enderecoSaida != null){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //agora posso colocar esse resultado na view
                        Bundle bundle = new Bundle();
                        bundle.putString("location", enderecoSaida);
                        //TROCAR PELA ACTIVITY DO MAPA
                        Intent intent = new Intent(MainActivity.this, TesteActivity.class);
                        intent.putExtra("location", bundle);
                        startActivity(intent);
                    }
                });
            }

        }
    }

}