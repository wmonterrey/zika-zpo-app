package ni.org.ics.zpo.appmovil.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import ni.org.ics.zpo.appmovil.R;
import ni.org.ics.zpo.domain.Zpo01StudyEntrySectionDtoF;
import ni.org.ics.zpo.domain.Zpo08StudyExit;
import ni.org.ics.zpo.domain.ZpoEstadoInfante;
import ni.org.ics.zpo.domain.ZpoInfantData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MenuInfantesAdapter extends ArrayAdapter<String> {

    private final String[] values;
    private final ZpoInfantData mZpInfante;
    private final ZpoEstadoInfante mZpEstado;
    private final Zpo08StudyExit mZpSalida;
    private final Zpo01StudyEntrySectionDtoF entryD;
    private final Calendar fechaIngreso;
    private final Context context;
    private Date fechaEvento;
    private Date todayDate;
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

    public MenuInfantesAdapter(Context context, int textViewResourceId,
                               String[] values, ZpoInfantData zp00, ZpoEstadoInfante zpEstado, Zpo08StudyExit zpSalida, Zpo01StudyEntrySectionDtoF entryD) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
        this.mZpInfante = zp00;
        this.mZpEstado = zpEstado;
        this.mZpSalida = zpSalida;
        this.entryD = entryD;
        try {
            this.todayDate = formatter.parse(formatter.format(new Date()));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.fechaIngreso = Calendar.getInstance();
        if (mZpInfante.getInfantBirthDate()!=null)
            fechaIngreso.setTime(mZpInfante.getInfantBirthDate());
    }


    @Override
    public boolean isEnabled(int position) {
        // Disable the first item of GridView
        boolean habilitado = true;
        if(mZpSalida!= null){
            return false;
        }

        if(position == 6){
            if (entryD != null){
                if(entryD.getSeaFirstPreg().equals("0")){
                    return  false;
                }
            }

        }
        return habilitado;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.menu_item_2, null);
        }
        TextView textView = (TextView) v.findViewById(R.id.label);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setText(values[position]);
        // Change icon based on position
        Drawable img = null;
        switch (position){
            case 0:
                fechaEvento = mZpInfante.getRecordDate();
                if(String.valueOf(mZpEstado.getIngreso()).equals("0")){
                    textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.pending));
                    textView.setTextColor(Color.BLUE);
                    long dif = getDateDiff(fechaEvento,todayDate,TimeUnit.DAYS);
                    if(dif>15){
                        textView.setTextColor(Color.RED);
                        textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.delayed));
                    }
                    else if(dif<=3){
                        textView.setTextColor(Color.BLUE);
                        textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.ontime));
                    }
                    else {
                        textView.setTextColor(Color.RED);
                        textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.delayed));
                    }
                }
                else{
                    textView.setTextColor(Color.BLACK);
                    textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.done)+"\n\n");
                }
                img=getContext().getResources().getDrawable( R.drawable.ic_enroll);
                textView.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
                break;
            case 1:
                fechaIngreso.add(Calendar.DATE, 365);
                fechaEvento = fechaIngreso.getTime();
                if(String.valueOf(mZpEstado.getMes12()).equals("0")){
                    textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.pending));
                    textView.setTextColor(Color.BLUE);
                    long dif = getDateDiff(fechaEvento,todayDate,TimeUnit.DAYS);
                    if(dif<-7){
                        textView.setTextColor(Color.GRAY);
                        textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.programmed)+": "+ formatter.format(fechaEvento));
                    }
                    else if(dif>7){
                        textView.setTextColor(Color.RED);
                        textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.delayed));
                    }
                    else if(dif<=0){
                        textView.setTextColor(Color.BLUE);
                        textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.ontime));
                    }
                    else{
                        textView.setTextColor(Color.RED);
                        textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.delayed));
                    }
                }
                else{
                    textView.setTextColor(Color.BLACK);
                    textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.done)+"\n\n");
                }
                img=getContext().getResources().getDrawable( R.drawable.ic_12m);
                textView.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
                fechaIngreso.add(Calendar.DATE, -365);
                break;
            case 2:
                fechaIngreso.add(Calendar.DATE, 730);
                fechaEvento = fechaIngreso.getTime();
                if(String.valueOf(mZpEstado.getMes24()).equals("0")){
                    textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.pending));
                    textView.setTextColor(Color.BLUE);
                    long dif = getDateDiff(fechaEvento,todayDate,TimeUnit.DAYS);
                    if(dif<-7){
                        textView.setTextColor(Color.GRAY);
                        textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.programmed)+": "+ formatter.format(fechaEvento));
                    }
                    else if(dif>7){
                        textView.setTextColor(Color.RED);
                        textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.delayed));
                    }
                    else if(dif<=0){
                        textView.setTextColor(Color.BLUE);
                        textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.ontime));
                    }
                    else{
                        textView.setTextColor(Color.RED);
                        textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.delayed));
                    }
                }
                else{
                    textView.setTextColor(Color.BLACK);
                    textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.done)+"\n\n");
                }
                img=getContext().getResources().getDrawable( R.drawable.ic_24m);
                textView.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
                fechaIngreso.add(Calendar.DATE, -730);
                break;

            case 3:
                textView.setTextColor(Color.BLACK);
                textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.available)+"\n\n");
                img=getContext().getResources().getDrawable( R.drawable.ic_addvisit);
                textView.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
                break;

            case 4:
                textView.setTextColor(Color.BLACK);
                textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.available)+"\n\n");
                img=getContext().getResources().getDrawable( R.drawable.ic_addvisit);
                textView.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
                break;

            case 5:
                textView.setTextColor(Color.BLACK);
                textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.available)+"\n\n");
                img=getContext().getResources().getDrawable( R.drawable.ic_addvisit);
                textView.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
                break;

            case 6:
                if(entryD != null){
                    if(entryD.getSeaAnemia()== null){
                        textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.available)+"\n\n");
                        textView.setTextColor(Color.BLACK);

                    }else{
                        textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.notavailable)+"\n\n");
                        textView.setTextColor(Color.GRAY);

                    }
                }

                img=getContext().getResources().getDrawable( R.drawable.ic_pregnancy);
                textView.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
                break;

            case 7:
                textView.setTextColor(Color.BLACK);
                if(mZpSalida!=null){
                    textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.inf_retired)+"\n\n");
                }
                else{
                    textView.setText(textView.getText()+"\n"+ context.getResources().getString(R.string.available)+"\n\n");
                }
                img=getContext().getResources().getDrawable( R.drawable.ic_exit);
                textView.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
                break;
            default:
                img=getContext().getResources().getDrawable( R.drawable.logo);
                textView.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
                break;
        }

        return v;
    }


    /**
     * Get a diff between two dates
     * @param date1 the oldest date
     * @param date2 the newest date
     * @param timeUnit the unit in which you want the diff
     * @return the diff value, in the provided unit
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }
}
