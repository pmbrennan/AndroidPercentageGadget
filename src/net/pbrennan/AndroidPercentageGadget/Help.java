package net.pbrennan.AndroidPercentageGadget;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Help extends Activity
	implements OnClickListener
{
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        
        Button b = (Button)findViewById(R.id.ButtonOK);
        b.setOnClickListener(this);
        
    }

	@Override
	public void onClick(View v) {
		finish();		
	}

}
