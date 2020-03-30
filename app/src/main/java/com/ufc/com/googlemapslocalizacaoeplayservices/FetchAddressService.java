package com.ufc.com.googlemapslocalizacaoeplayservices;


import android.app.IntentService;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//Trabalhar com localização em background
//Deve ser informado no manifest que será usado um servico
//E tambem definir uma classe constante para passar esses valores
//Esse servico vai buscar os endereços com base na latitude e longitude
public class FetchAddressService extends IntentService {
    protected ResultReceiver receiver;

    public FetchAddressService() {
        super("fetchAddressService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent == null){
            return;
        }
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
        receiver = intent.getParcelableExtra(Constants.RECEIVER);


        List<Address> addressList = null;

        try {
           addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            Log.i("Teste","Servico indisponivel", e);
        }catch (IllegalArgumentException e){
            Log.i("teste", "Latitude e Longitude são invalidas");
        }

        if (addressList == null || addressList.isEmpty()){
            Log.i("teste", "Nenhum endereco encontrado");
            //passa o resultado de falha
            deliverResultToReceiver(Constants.FAILURE_RESULT, "Nenhum endereco encontrado");
        }else{
            Address address = addressList.get(0);
            List<String> enderecoString = new ArrayList<>();

            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                enderecoString.add(address.getAddressLine(i));
            }
            //passa o resultado de sucesso
            deliverResultToReceiver(Constants.SUCESS_RESULT, TextUtils.join("|", enderecoString));
        }



    }

    private void deliverResultToReceiver(int resultCode, String message){
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        receiver.send(resultCode, bundle);
    }

}
