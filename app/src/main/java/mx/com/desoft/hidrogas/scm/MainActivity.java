package mx.com.desoft.hidrogas.scm;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import mx.com.desoft.hidrogas.scm.dto.Data;

public class MainActivity extends AppCompatActivity{

        /**
         * Controles
         * */
        private Button button;
        private EditText editText;
        private EditText editText2;
        private Context context = this;

        /**
         * Puerto
         * */
        private static final int SERVERPORT = 5000;
        /**
         * HOST
         * */
        private static final String ADDRESS = "192.168.100.7";

        private Thread thread = null;
        private Socket socket;

        private DataOutputStream bufferDeSalida = null;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);


            button = ((Button) findViewById(R.id.button));
            editText = ((EditText) findViewById(R.id.editText));
            editText2 = ((EditText) findViewById(R.id.editText2));
            //LeerDirectorio();
            //MyATaskCliente myATaskYW = new MyATaskCliente();
            //myATaskYW.execute("");


             thread = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(5000);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MyATaskCliente myATaskYW = new MyATaskCliente();
                                    myATaskYW.execute("");
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            button.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View view) {
                            thread.start();
                        }
                    });
        }//end:onCreate


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
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setTitle("Conectando con el servidor");
                progressDialog.setMessage("Por favor espere...");
                progressDialog.show();

            }

            /**
             * Se conecta al servidor y trata resultado
             * */
            @Override
            protected ArrayList<Data> doInBackground(String... values) {
                try {
                    //Se conecta al servidor
                    InetAddress serverAddr = InetAddress.getByName(ADDRESS);
                    Log.i("I/TCP Client", "Connecting...");
                    socket = new Socket(serverAddr, SERVERPORT);
                    if (socket.isConnected()) {

                        /*Log.i("I/TCP Client", "Connected to server");
                        Log.i("I/TCP Client", "Send data to server");
                        PrintStream output = new PrintStream(socket.getOutputStream());
                        String request = values[0];
                        output.println(request);*/

                        bufferDeSalida = new DataOutputStream(socket.getOutputStream());
                        bufferDeSalida.writeUTF("--I");
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

                        return listaPedidos;
                    } else{
                        Toast.makeText(getApplicationContext(),"No se tienen permisos para enviar el mensaje", Toast.LENGTH_LONG).show();

                    }
                    return null;
                }catch (UnknownHostException ex) {
                    Log.e("E/TCP Client", "" + ex.getMessage());
                    return null;
                } catch (IOException ex) {
                    Log.e("E/TCP Client", "" + ex.getMessage());
                    return null;
                }
            }

            /**
             * Oculta ventana emergente y muestra resultado en pantalla
             * */
            @Override
            protected void onPostExecute(ArrayList<Data> listaPedidos){
                if (listaPedidos != null) {
                    try {

                        for (Data pedidos : listaPedidos) {
                            Toast.makeText(getApplicationContext(), "nombre de  archivos." + pedidos.getNombreArchivo() , Toast.LENGTH_LONG).show();
                            if(EnviarMensaje(pedidos)) {
                                bufferDeSalida.writeUTF(pedidos.getNombreArchivo());
                                bufferDeSalida.flush();

                            }
                        }
                        bufferDeSalida.writeUTF("--T");
                        bufferDeSalida.flush();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    /*if (EnviarMensaje(listaPedidos)) {
                        progressDialog.dismiss();
                    }*/
                } else{
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "El servidor no se encuentra arriba, favor de validar con el area de Sistemas." , Toast.LENGTH_LONG).show();
                }
                if (isCancelled()){
                    MyATaskCliente.this.cancel(true);
                }

            }
            @Override
            protected void onCancelled() {
                Toast.makeText(MainActivity.this, "Tarea cancelada!",
                        Toast.LENGTH_SHORT).show();
            }
        }



        public boolean EnviarMensaje(Data pedido){
            /*boolean envioSMS = true;
            try{
                int permisoCheck = ContextCompat.checkSelfPermission(
                        this, android.Manifest.permission.SEND_SMS);
                if (permisoCheck != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"No se tiene permisos para enviar el mensaje", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 225);
                }

                for (Data pedidos : listaPedidos) {
                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage(pedidos.getNumeroCelular().toString()
                            , null, pedidos.getMensaje().toString(), null, null);
                }
                Toast.makeText(this, "Se han enviado " + listaPedidos.size() +" mensajes con éxito" , Toast.LENGTH_LONG).show();
            }
            catch (Exception e){
                Toast.makeText(this, "Mensaje no enviado, datos incorrectos." + e.getMessage().toString(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
                envioSMS = false;
            }
            return envioSMS;*/
            boolean envioSMS = true;
            try{
                int permisoCheck = ContextCompat.checkSelfPermission(
                        this, android.Manifest.permission.SEND_SMS);
                if (permisoCheck != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"No se tiene permisos para enviar el mensaje", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 225);
                }

                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(pedido.getNumeroCelular().toString()
                     , null, pedido.getMensaje().toString(), null, null);
            }
            catch (Exception e){
                Toast.makeText(this, "Mensaje no enviado, datos incorrectos." + e.getMessage().toString(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
                envioSMS = false;
            }
            return envioSMS;
        }
    }
