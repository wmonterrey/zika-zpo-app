package ni.org.ics.zpo.appmovil.activities.nuevos;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import ni.org.ics.zpo.appmovil.AbstractAsyncActivity;
import ni.org.ics.zpo.appmovil.MainActivity;
import ni.org.ics.zpo.appmovil.MyZpoApplication;
import ni.org.ics.zpo.appmovil.R;
import ni.org.ics.zpo.appmovil.database.ZpoAdapter;
import ni.org.ics.zpo.domain.Zpo07dInfantBayleyScales;
import ni.org.ics.zpo.appmovil.parsers.Zpo07dInfantBayleyScalesXml;
import ni.org.ics.zpo.appmovil.preferences.PreferencesActivity;
import ni.org.ics.zpo.appmovil.utils.Constants;
import ni.org.ics.zpo.appmovil.utils.FileUtils;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.Date;

/**
 * Created by ics on 23/6/2017.
 * V1.0
 */
public class NewZpo07dInfantBayleyScalesActivity extends AbstractAsyncActivity {
    protected static final String TAG = NewZpo07dInfantBayleyScalesActivity.class.getSimpleName();

    private ZpoAdapter zipA;
    private static Zpo07dInfantBayleyScales mInfantAssessment = null;

    public static final int ADD_ZP07_ODK = 1;
    public static final int EDIT_ZP07_ODK = 2;

    Dialog dialogInit;
    private SharedPreferences settings;
    private String username;
    private String mRecordId = "";
    private Integer accion = 0;
    private String event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!FileUtils.storageReady()) {
            Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.error, R.string.storage_error),Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        settings =
                PreferenceManager.getDefaultSharedPreferences(this);
        username =
                settings.getString(PreferencesActivity.KEY_USERNAME,
                        null);
        String mPass = ((MyZpoApplication) this.getApplication()).getPassApp();
        zipA = new ZpoAdapter(this.getApplicationContext(),mPass,false,false);
        mRecordId = getIntent().getExtras().getString(Constants.RECORDID);
        mInfantAssessment = (Zpo07dInfantBayleyScales) getIntent().getExtras().getSerializable(Constants.OBJECTO_ZP07D);
        event = getIntent().getExtras().getString(Constants.EVENT);
        createInitDialog();
    }

    /**
     * Presenta dialogo inicial
     */

    private void createInitDialog() {
        dialogInit = new Dialog(this, R.style.FullHeightDialog);
        dialogInit.setContentView(R.layout.yesno);
        dialogInit.setCancelable(false);

        //to set the message
        TextView message = (TextView) dialogInit.findViewById(R.id.yesnotext);
        if (mInfantAssessment != null) {
            message.setText(getString(R.string.edit) + " " + getString(R.string.infant_b_8) + "?");

        } else {
            message.setText(getString(R.string.add) + " " + getString(R.string.infant_b_8) + "?");
        }

        //add some action to the buttons

        Button yes = (Button) dialogInit.findViewById(R.id.yesnoYes);
        yes.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                dialogInit.dismiss();
                addZpo07dInfantBayleyScales();
            }
        });

        Button no = (Button) dialogInit.findViewById(R.id.yesnoNo);
        no.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Cierra
                dialogInit.dismiss();
                finish();
            }
        });
        dialogInit.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.general, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.MENU_BACK){
            finish();
            return true;
        }
        else if(item.getItemId()==R.id.MENU_HOME){
            Intent i = new Intent(getApplicationContext(),
                    MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
            return true;
        }
        else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if(requestCode == ADD_ZP07_ODK ||requestCode == EDIT_ZP07_ODK) {
            if(resultCode == RESULT_OK) {
                Uri instanceUri = intent.getData();
                //Busca la instancia resultado
                String[] projection = new String[] {
                        "_id","instanceFilePath", "status","displaySubtext"};
                Cursor c = getContentResolver().query(instanceUri, projection,
                        null, null, null);
                c.moveToFirst();
                //Captura la id de la instancia y la ruta del archivo para agregarlo al participante
                Integer idInstancia = c.getInt(c.getColumnIndex("_id"));
                String instanceFilePath = c.getString(c.getColumnIndex("instanceFilePath"));
                String complete = c.getString(c.getColumnIndex("status"));
                //cierra el cursor
                if (c != null) {
                    c.close();
                }
                if (complete.matches("complete")){
                    //Parsear el resultado obteniendo un tamizaje si esta completo
                    parseZpo07dInfantBayleyScales(idInstancia, instanceFilePath, accion);
                }
                else{
                    Toast.makeText(getApplicationContext(),	getString(R.string.err_not_completed), Toast.LENGTH_LONG).show();
                }
            }
            else{
            	finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void addZpo07dInfantBayleyScales() {
        try {
            Uri formUri;
            if (mInfantAssessment == null) {
                //campos de proveedor de collect
                String[] projection = new String[]{
                        "_id", "jrFormId", "displayName"};
                //cursor que busca el formulario
                Cursor c = getContentResolver().query(Constants.CONTENT_URI, projection,
                        "jrFormId = 'zpo07d_infant_bayley_scales' and displayName = 'Estudio ZPO Escala de Bayley De Desarrollo Infantil'", null, null);
                c.moveToFirst();
                //captura el id del formulario
                Integer id = Integer.parseInt(c.getString(0));
                //cierra el cursor
                if (c != null) {
                    c.close();
                }
                //forma el uri para ODK Collect
                formUri = ContentUris.withAppendedId(Constants.CONTENT_URI, id);
                accion = ADD_ZP07_ODK;
            } else {

                Integer id = mInfantAssessment.getIdInstancia();
                formUri = ContentUris.withAppendedId(Constants.CONTENT_URI_I, id);
                accion = EDIT_ZP07_ODK;
            }
            //Arranca la actividad ODK Collect en busca de resultado
            Intent odkA = new Intent(Intent.ACTION_EDIT, formUri);
            startActivityForResult(odkA, accion);
        } catch (Exception e) {
            //No existe el formulario en el equipo
            Log.e(TAG, e.getMessage(), e);
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void parseZpo07dInfantBayleyScales(Integer idInstancia, String instanceFilePath, Integer accion) {
        Serializer serializer = new Persister();
        File source = new File(instanceFilePath);
        try {
            Zpo07dInfantBayleyScalesXml zp07dXml = serializer.read(Zpo07dInfantBayleyScalesXml.class, source);
            if (accion== ADD_ZP07_ODK) mInfantAssessment = new Zpo07dInfantBayleyScales();
            mInfantAssessment.setRecordId(mRecordId);
            mInfantAssessment.setEventName(event);
            mInfantAssessment.setInfantVisitdt(zp07dXml.getInfantVisitdt());
            mInfantAssessment.setInfantDone(zp07dXml.getInfantDone());
            mInfantAssessment.setInfantReaNot(zp07dXml.getInfantReaNot());
            mInfantAssessment.setInfantNreaOther(zp07dXml.getInfantNreaOther());
            mInfantAssessment.setInfantPerfdt(zp07dXml.getInfantPerfdt());
            mInfantAssessment.setInfantEnglish(zp07dXml.getInfantEnglish());
            mInfantAssessment.setInfantPrilanguage(zp07dXml.getInfantPrilanguage());
            mInfantAssessment.setInfantParentlan(zp07dXml.getInfantParentlan());
            mInfantAssessment.setInfantBayenglish(zp07dXml.getInfantBayenglish());
            mInfantAssessment.setInfantMed(zp07dXml.getInfantMed());
            mInfantAssessment.setInfantMedDay(zp07dXml.getInfantMedDay());
            mInfantAssessment.setInfantTypMed(zp07dXml.getInfantTypMed());
            mInfantAssessment.setInfantCoguAttem(zp07dXml.getInfantCoguAttem());
            mInfantAssessment.setInfantCograScore(zp07dXml.getInfantCograScore());
            mInfantAssessment.setInfantCogscScore(zp07dXml.getInfantCogscScore());
            mInfantAssessment.setInfantCogcoScore(zp07dXml.getInfantCogcoScore());
            mInfantAssessment.setInfantCogValid(zp07dXml.getInfantCogValid());
            mInfantAssessment.setInfantReaInvali(zp07dXml.getInfantReaInvali());
            mInfantAssessment.setInfantInvaOther(zp07dXml.getInfantInvaOther());
            mInfantAssessment.setInfantResAtte(zp07dXml.getInfantResAtte());
            mInfantAssessment.setInfantRetoScore(zp07dXml.getInfantRetoScore());
            mInfantAssessment.setInfantRescScore(zp07dXml.getInfantRescScore());
            mInfantAssessment.setInfantExsuAtte(zp07dXml.getInfantExsuAtte());
            mInfantAssessment.setInfantExtoScore(zp07dXml.getInfantExtoScore());
            mInfantAssessment.setInfantExscScore(zp07dXml.getInfantExscScore());
            mInfantAssessment.setInfantSuScore(zp07dXml.getInfantSuScore());
            mInfantAssessment.setInfantSucomScore(zp07dXml.getInfantSucomScore());
            mInfantAssessment.setInfantLangValid(zp07dXml.getInfantLangValid());
            mInfantAssessment.setInfantRelanInvalid(zp07dXml.getInfantRelanInvalid());
            mInfantAssessment.setInfantRelanOther(zp07dXml.getInfantRelanOther());
            mInfantAssessment.setInfantFmsAtte(zp07dXml.getInfantFmsAtte());
            mInfantAssessment.setInfantFmtoScore(zp07dXml.getInfantFmtoScore());
            mInfantAssessment.setInfantFmscScore(zp07dXml.getInfantFmscScore());
            mInfantAssessment.setInfantGmsuattm(zp07dXml.getInfantGmsuattm());
            mInfantAssessment.setInfantGmtoScore(zp07dXml.getInfantGmtoScore());
            mInfantAssessment.setInfantGmscScore(zp07dXml.getInfantGmscScore());
            mInfantAssessment.setInfantMssuScore(zp07dXml.getInfantMssuScore());
            mInfantAssessment.setInfantMscoscore(zp07dXml.getInfantMscoscore());
            mInfantAssessment.setInfantMtValid(zp07dXml.getInfantMtValid());
            mInfantAssessment.setInfantMtInvalid(zp07dXml.getInfantMtInvalid());
            mInfantAssessment.setInfantMtinvOther(zp07dXml.getInfantMtinvOther());
            mInfantAssessment.setInfantSesAtte(zp07dXml.getInfantSesAtte());
            mInfantAssessment.setInfantSetoSore(zp07dXml.getInfantSetoSore());
            mInfantAssessment.setInfantSescScre(zp07dXml.getInfantSescScre());
            mInfantAssessment.setInfantSecoScre(zp07dXml.getInfantSecoScre());
            mInfantAssessment.setInfantSemoValid(zp07dXml.getInfantSemoValid());
            mInfantAssessment.setInfantSemoInvalid(zp07dXml.getInfantSemoInvalid());
            mInfantAssessment.setInfantSemoinvOther(zp07dXml.getInfantSemoinvOther());
            mInfantAssessment.setInfantCog76(zp07dXml.getInfantCog76());
            mInfantAssessment.setInfantCircuEvalu(zp07dXml.getInfantCircuEvalu());
            mInfantAssessment.setInfantExplain(zp07dXml.getInfantExplain());
            mInfantAssessment.setInfantBaidCom(username);
            mInfantAssessment.setInfantBadtCom(new Date());
            mInfantAssessment.setInfantBaeyeIdRevi(username);
            mInfantAssessment.setInfantBadtRevi(new Date());
            mInfantAssessment.setInfantBaidEntry(username);
            mInfantAssessment.setInfantBadtEnt(new Date());

            mInfantAssessment.setIdInstancia(idInstancia);
            mInfantAssessment.setRecordDate(new Date());
            mInfantAssessment.setRecordUser(username);
            mInfantAssessment.setInstancePath(instanceFilePath);
            mInfantAssessment.setEstado(Constants.STATUS_NOT_SUBMITTED);
            mInfantAssessment.setStart(zp07dXml.getStart());
            mInfantAssessment.setEnd(zp07dXml.getEnd());
            mInfantAssessment.setDeviceid(zp07dXml.getDeviceid());
            mInfantAssessment.setSimserial(zp07dXml.getSimserial());
            mInfantAssessment.setPhonenumber(zp07dXml.getPhonenumber());
            mInfantAssessment.setToday(zp07dXml.getToday());

            new SaveDataTask().execute(accion);

        } catch (Exception e) {
            // Presenta el error al parsear el xml
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    // ***************************************
    // Private classes
    // ***************************************
    private class SaveDataTask extends AsyncTask<Integer, Void, String> {
        private Integer accionaRealizar = null;
        @Override
        protected void onPreExecute() {
            // before the request begins, show a progress indicator
            showLoadingProgressDialog();
        }

        @Override
        protected String doInBackground(Integer... values) {
            try {
                accionaRealizar = values[0];
                try {
                    zipA.open();
                    if (accionaRealizar == ADD_ZP07_ODK){
                        zipA.crearZpo07dInfantBayleyScales(mInfantAssessment);
                    }
                    else{
                        zipA.editarZpo07dInfantBayleyScales(mInfantAssessment);
                    }
                    zipA.close();
                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);
                    return "error";
                }
                return "exito";
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
                return "error";
            }
        }

        protected void onPostExecute(String resultado) {
            // after the network request completes, hide the progress indicator
            dismissProgressDialog();
            showResult(resultado);
        }

    }

    // ***************************************
    // Private methods
    // ***************************************
    private void showResult(String resultado) {
        Toast.makeText(getApplicationContext(),	resultado, Toast.LENGTH_LONG).show();
        finish();
    }
}
