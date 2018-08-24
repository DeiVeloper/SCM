package mx.com.desoft.hidrogas.scm;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import mx.com.desoft.hidrogas.scm.dto.Data;

public class MainActivity extends AppCompatActivity{

    /**
     * Controles
     * */
    private Context context = this;
    private TextView textView;
    private TextView titulo;

    /**
     * Puerto
     * */
    private static final int SERVERPORT = 5000;
    /**
     * HOST
     * */
    private static final String ADDRESS = "192.168.127.240";

    private Thread thread = null;
    private Socket socket;
    private Switch simpleSwitch;

    private DataOutputStream bufferDeSalida = null;
    private MyATaskCliente myATaskYW;
    private boolean detenerThread = true;
    private boolean bandera = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        textView = findViewById(R.id.etiquetaServidor);
        titulo = findViewById(R.id.tituloSCM);
        titulo.setText("Sistema de Control de Mensajería");
        simpleSwitch = findViewById(R.id.switch1);
        simpleSwitch.setText("Iniciar Servicio...");
        simpleSwitch.setChecked(false);
        simpleSwitch.setHighlightColor(Color.RED);
        simpleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    detenerThread = true;
                    thread = new Thread() {
                        @Override
                        public void run() {
                            while (detenerThread) {
                                try {
                                    Thread.sleep(5000);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            myATaskYW = new MyATaskCliente();
                                            myATaskYW.execute("");
                                        }
                                    });
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    thread.start();
                    simpleSwitch.setText("Activado!");

                }else{
                    detenerThread = false;
                    myATaskYW.onCancelled();
                    simpleSwitch.setText("Desactivado!");
                }
            }
        });
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        myATaskYW.cancel(true);
        detenerThread = false;
    }

    /**
     * Clase para interactuar con el servidor
     * */
    class MyATaskCliente extends AsyncTask<String,Void,ArrayList<Data>>{

        /**
         * Ventana que bloqueara la pantalla del movil hasta recibir respuesta del servidor
         * */
        ProgressDialog progressDialog;

        /**
         * muestra una ventana emergente
         * */

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.setTitle("Conectando con el servidor");
            progressDialog.setMessage("Por favor espere...");
            if (detenerThread) {
                progressDialog.show();
            }
        }

        /**
         * Se conecta al servidor y trata resultado
         * */
        @Override
        protected ArrayList<Data> doInBackground(String... values) {
            try {
                InetAddress serverAddr = InetAddress.getByName(ADDRESS);
                socket = new Socket(serverAddr, SERVERPORT);
                if (socket.isConnected()) {
                    bufferDeSalida = new DataOutputStream(socket.getOutputStream());
                    bufferDeSalida.writeUTF("INICIO");
                    bufferDeSalida.flush();

                    Log.i("I/TCP Client", "Received data to server");
                    ObjectInputStream stream = new ObjectInputStream(socket.getInputStream());
                    ArrayList<Data> listaPedidos = null;
                    try {
                        listaPedidos = (ArrayList<Data>) stream.readObject();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    Log.i("lista", "" + listaPedidos.size());
                    if (!listaPedidos.isEmpty()) {
                        try {
                            for (Data pedidos : listaPedidos) {
                                Log.i("espera", "entra");
                                esperar(3);
                                if(EnviarMensaje(pedidos)) {
                                    bufferDeSalida.writeUTF(pedidos.getNombreArchivo());
                                    bufferDeSalida.flush();
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    bufferDeSalida.writeUTF("FIN");
                    bufferDeSalida.flush();
                    socket.close();
                    progressDialog.dismiss();
                    return listaPedidos;
                }
                return null;
            }catch (UnknownHostException ex) {
                Log.e("UnknownHostException", "" + ex.getMessage());
                return null;
            } catch (IOException ex) {
                bandera = true;
                Log.e("IOException", "" + ex.getMessage());
                return null;
            }
        }

        /**
         * Oculta ventana emergente y muestra resultado en pantalla
         * */
        @Override
        protected void onPostExecute(ArrayList<Data> listaPedidos){
            progressDialog.dismiss();
            if (bandera){
               Toast.makeText(getApplicationContext(),"Servicio no disponible, favor de contactar al administrador.", Toast.LENGTH_LONG).show();
            }
            if (listaPedidos != null && listaPedidos.isEmpty()) {
                Toast.makeText(getApplicationContext(), "No se encontraron archivos para procesar.", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            myATaskYW.cancel(true);
            Toast.makeText(MainActivity.this, "Servicio Terminado!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public boolean EnviarMensaje(Data pedido){
        Log.i("envia", "tiempo de envio");
        boolean envioSMS = true;
        try{
            int permisoCheck = ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.SEND_SMS);
            if (permisoCheck != PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this,"No se tiene permisos para enviar el mensaje", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 225);
            }

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(pedido.getNumeroCelular().toString()
                    , null, pedido.getMensaje().toString(), null, null);
        }
        catch (Exception e){
            //Toast.makeText(this, "Mensaje no enviado, datos incorrectos." + e.getMessage().toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            envioSMS = false;
        }
        return envioSMS;
    }

    /**
     * Pausa la ejecución durante X segundos.
     * @param segundos El número de segundos que se quiere esperar.
     */
    public static void esperar(int segundos){
        try {
            Thread.sleep(segundos * 1000);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}