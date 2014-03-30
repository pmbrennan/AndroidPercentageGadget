package net.pbrennan.AndroidPercentageGadget;

import android.animation.AnimatorSet;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

public class AndroidPercentageGadget 
	extends Activity
{
    //private static final String TAG = AndroidPercentageGadget.class.getSimpleName();

    // A stack which maintains the LRU state of the fields.
    // m_modStack[0] == the last id modified by the user.
    // m_modStack[1] == the next to last.
    private int m_modStack[];

    // m_targetedField is the id of the field which is
    // to be recomputed.
    private int m_targetedField;

    // m_ignoreTargetedFieldChange indicates that the targeted field has been
    // changed, so we will expect a text change event on this, the first incidence
    // of which we need to ignore.
    private boolean m_ignoreTargetedFieldChange = false;

    /**
     * Push a view id on to the mod stack, and select a new target view.
     * @param id - the view to push on to the stack.
     * @return true if the view id was pushed on to the stack, false otherwise
     */
    private boolean pushViewId(int id) {

        // If the view being changed is the target view and the modify flag is set,
        // ignore.
        if (m_ignoreTargetedFieldChange && id == m_targetedField) {
            m_ignoreTargetedFieldChange = false;
            return false;
        }

        // Write the stack changes.
        if (id != m_modStack[0]) {
            m_modStack[1] = m_modStack[0];
            m_modStack[0] = id;
        }

        if (m_modStack[1] == 0) {
            return false;
        }

        // Now see if we can determine where we
        // are targeting the result of our calculation.
        switch (m_modStack[0])
        {
            case R.id.PercentField:
                switch (m_modStack[1])
                {
                    case R.id.ValueField:
                        m_targetedField = R.id.ProductField;
                        m_ignoreTargetedFieldChange = true;
                        break;
                    case R.id.ProductField:
                        m_targetedField = R.id.ValueField;
                        m_ignoreTargetedFieldChange = true;
                        break;
                    default:
                        m_targetedField = 0;
                        break;
                }
                break;
            case R.id.ValueField:
                switch (m_modStack[1])
                {
                    case R.id.PercentField:
                        m_targetedField = R.id.ProductField;
                        m_ignoreTargetedFieldChange = true;
                        break;
                    case R.id.ProductField:
                        m_targetedField = R.id.PercentField;
                        m_ignoreTargetedFieldChange = true;
                        break;
                    default:
                        m_targetedField = 0;
                        break;
                }
                break;
            case R.id.ProductField:
                switch (m_modStack[1])
                {
                    case R.id.ValueField:
                        m_targetedField = R.id.PercentField;
                        m_ignoreTargetedFieldChange = true;
                        break;
                    case R.id.PercentField:
                        m_targetedField = R.id.ValueField;
                        m_ignoreTargetedFieldChange = true;
                        break;
                    default:
                        m_targetedField = 0;
                        break;
                }
                break;
            default:
                break;
        }
        return (m_targetedField != 0);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Set up the key listeners.
        TextView tv = (TextView)findViewById(R.id.PercentField);
        tv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                onViewChanged(R.id.PercentField);
            }
        });
        
        tv = (TextView)findViewById(R.id.ValueField);
        tv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                onViewChanged(R.id.ValueField);
            }
        });
        
        tv = (TextView)findViewById(R.id.ProductField);
        tv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                onViewChanged(R.id.ProductField);
            }
        });
        
        // Initialize the modStack. This will keep track of
        // which fields have been touched. The last two fields
        // modified by the user will determine which field changes.
        m_modStack = new int[2];
        m_modStack[0] = m_modStack[1] = 0;
        
        m_targetedField = 0;
        FixResultIndicator();
    }

    private void onViewChanged(int viewid) {
        if (pushViewId(viewid)) {
            FixResultIndicator();
            DoComputation();
        }
    }

    /**
     * Give the user a hint as to where the results of the computation will land.
     */
	private void FixResultIndicator()
	{
        TextView targetedView = (TextView)findViewById(m_targetedField);
        addAnimation(targetedView);

		ImageView percentLight = (ImageView)findViewById(R.id.PercentTargeted);
		ImageView valueLight = (ImageView)findViewById(R.id.ValueTargeted);
		ImageView productLight = (ImageView)findViewById(R.id.ProductTargeted);

//		if (m_targetedField == R.id.PercentField) {
//            percentLight.setImageResource(R.drawable.active);
//        } else {
//            percentLight.setImageResource(R.drawable.inactive);
//        }
//
//		if (m_targetedField == R.id.ValueField) {
//            valueLight.setImageResource(R.drawable.active);
//        } else {
//            valueLight.setImageResource(R.drawable.inactive);
//        }
//
//		if (m_targetedField == R.id.ProductField) {
//            productLight.setImageResource(R.drawable.active);
//        } else {
//            productLight.setImageResource(R.drawable.inactive);
//        }
	}

    /**
     * Perform the model computation.
     */
	private void DoComputation()
	{
		double percentage;
		double value;
		double product;
		
		boolean percentageOK = true;
		boolean valueOK = true;
		boolean productOK = true;
		
		TextView percentageTextView = (TextView)findViewById(R.id.PercentField);
		
		try
		{
			percentage = Double.parseDouble(percentageTextView.getText().toString());
		}
		catch (Exception e)
		{
			percentage = 0.0;
			percentageOK = false;
		}
		
		TextView valueTextView = (TextView)findViewById(R.id.ValueField);
		
		try
		{
			value = Double.parseDouble(valueTextView.getText().toString());
		}
        catch (NullPointerException e) {
            value = 0.0;
            valueOK = false;
        }
		catch (Exception e)
		{
			value = 0.0;
			valueOK = false;
		}
		
		TextView productTextView = (TextView)findViewById(R.id.ProductField);
		
		try
		{
			product = Double.parseDouble(productTextView.getText().toString());
		}
		catch (Exception e)
		{
			product = 0.0;
			productOK = false;
		}
		
		if (m_targetedField == R.id.ProductField)
		{
			if ((valueOK)&&(percentageOK))
			{
				product = (percentage * value)/100.0;
				String outProduct = Double.toString(product);
				productTextView.setText(outProduct);
			}
			else
			{
				productTextView.setText("");
			}
		}
		else if (m_targetedField == R.id.PercentField)
		{
			if ((valueOK)&&(productOK))
			{
				percentage = (100.0 * product) / value;
				String outPercentage = Double.toString(percentage);
				percentageTextView.setText(outPercentage);
			}
			else
			{
				percentageTextView.setText("");
			}
		}
		else if (m_targetedField == R.id.ValueField)
		{
			if ((percentageOK)&&(productOK))
			{
				value = (100.0 * product) / percentage;
				String outValue = Double.toString(value);
				valueTextView.setText(outValue);
			}
			else
			{
				valueTextView.setText("");
			}
		}
	}
	
	/** create the menu item(s) */
	public void populateMenu(Menu menu) {

	  menu.setQwertyMode(true);

	  MenuItem m1 = menu.add(R.string.HelpMe);
	  m1.setIcon(android.R.drawable.ic_menu_help);
	  MenuItem m2 = menu.add(R.string.AboutThisApp);
	  m2.setIcon(android.R.drawable.ic_menu_info_details);

	}
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {
	  populateMenu(menu);
	  return super.onCreateOptionsMenu(menu);
	}

	/** when menu button option selected */
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		if (item.getTitle() == getString(R.string.HelpMe))
		{
			Intent myIntent = new Intent();
			myIntent.setClassName("net.pbrennan.AndroidPercentageGadget", "net.pbrennan.AndroidPercentageGadget.Help");
			startActivity(myIntent);
			return true;
		}
		else if (item.getTitle() == getString(R.string.AboutThisApp))
		{
			Intent myIntent = new Intent();
			myIntent.setClassName("net.pbrennan.AndroidPercentageGadget", "net.pbrennan.AndroidPercentageGadget.About");
			startActivity(myIntent);
			return true;
		}
		else
		{
			return super.onOptionsItemSelected(item);
		}
	}

    private void addAnimation(View v) {
        AnimatorSet set = new AnimatorSet();

        if (v == null) {
            return;
        }

        FloatEvaluator eval = new FloatEvaluator() {
            /**
             * This function returns the result of linearly interpolating the start and end values, with
             * <code>fraction</code> representing the proportion between the start and end values. The
             * calculation is a simple parametric calculation: <code>result = x0 + t * (v1 - v0)</code>,
             * where <code>x0</code> is <code>startValue</code>, <code>x1</code> is <code>endValue</code>,
             * and <code>t</code> is <code>fraction</code>.
             *
             * @param fraction   The fraction from the starting to the ending values
             * @param startValue The start value; should be of type <code>float</code> or
             *                   <code>Float</code>
             * @param endValue   The end value; should be of type <code>float</code> or <code>Float</code>
             * @return A linear interpolation between the start and end values, given the
             * <code>fraction</code> parameter.
             */
            @Override
            public Float evaluate(float fraction, Number startValue, Number endValue) {
                //return super.evaluate(fraction, startValue, endValue);
                double sine = Math.sin(Math.PI * fraction) * 0.2;
                return Float.valueOf((float)(startValue.floatValue() * (1.0 + sine)));
            }
        };

        ObjectAnimator animator1 = ObjectAnimator.ofFloat(v, "scaleX", 1f, 1f);
        animator1.setInterpolator(new LinearInterpolator());
        animator1.setEvaluator(eval);
        animator1.setDuration(250);

        ObjectAnimator animator2 = ObjectAnimator.ofFloat(v, "scaleY", 1f, 1f);
        animator2.setInterpolator(new LinearInterpolator());
        animator2.setEvaluator(eval);
        animator2.setDuration(250);

        set.playTogether(animator1, animator2);
        set.start();
    }
}
