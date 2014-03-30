package net.pbrennan.AndroidPercentageGadget;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Displays the About dialog.
 * 
 * @author Patrick Brennan
 * 
 */
public class About extends Activity
	implements OnClickListener
{
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        String versionNumberFormatString = getResources().getString(R.string.AboutText);
        String versionNumberValue = "<N/A>";
        try {
            versionNumberValue = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String aboutText = String.format(versionNumberFormatString, versionNumberValue);
        TextView v = (TextView)findViewById(R.id.AboutLabel);
        v.setText(aboutText);

        Button b = (Button)findViewById(R.id.ButtonOK);
        b.setOnClickListener(this);
        
    }

    /**
     *  Handler for any button clicks in the view.
     *  @param v : the widget which received the event.
     */
	@Override
	public void onClick(View v) {
		finish();		
	}

}
