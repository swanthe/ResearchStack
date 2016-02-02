package co.touchlab.researchstack.core.ui.step.body;

import android.content.res.Resources;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import co.touchlab.researchstack.core.R;
import co.touchlab.researchstack.core.answerformat.IntegerAnswerFormat;
import co.touchlab.researchstack.core.result.StepResult;
import co.touchlab.researchstack.core.step.QuestionStep;
import co.touchlab.researchstack.core.step.Step;
import co.touchlab.researchstack.core.utils.ViewUtils;

public class IntegerQuestionBody implements StepBody
{
    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
    // Constructor Fields
    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
    private QuestionStep        step;
    private StepResult<Integer> result;
    private IntegerAnswerFormat format;

    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
    // View Fields
    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
    private int viewType;
    private NumberPicker numberPicker;
    private EditText     editText;

    public IntegerQuestionBody(Step step, StepResult result)
    {
        this.step = (QuestionStep) step;
        this.result = result == null ? new StepResult<>(step.getIdentifier()) : result;
        this.format = (IntegerAnswerFormat) this.step.getAnswerFormat();
    }

    @Override
    public View getBodyView(int viewType, LayoutInflater inflater, ViewGroup parent)
    {
        this.viewType = viewType;

        View view = getViewForType(viewType, inflater, parent);

        Resources res = parent.getResources();
        LinearLayout.MarginLayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = res.getDimensionPixelSize(R.dimen.rsc_margin_left);
        layoutParams.rightMargin = res.getDimensionPixelSize(R.dimen.rsc_margin_right);
        view.setLayoutParams(layoutParams);

        return view;
    }

    private View getViewForType(int viewType, LayoutInflater inflater, ViewGroup parent)
    {
        if(viewType == VIEW_TYPE_DEFAULT)
        {
            return initViewDefault(inflater, parent);
        }
        else if(viewType == VIEW_TYPE_COMPACT)
        {
            return initViewCompact(inflater, parent);
        }
        else
        {
            throw new IllegalArgumentException("Invalid View Type");
        }
    }

    private View initViewDefault(LayoutInflater inflater, ViewGroup parent)
    {
        numberPicker = (NumberPicker) inflater.inflate(R.layout.item_number_picker, parent, false);
        numberPicker.setMinValue(format.getMinValue());

        // if max and min are equal, don't set a max (it's 0/0 if they don't set min/max)
        if(format.getMaxValue() != format.getMinValue())
        {
            numberPicker.setMaxValue(format.getMaxValue());
        }

        //pre-fill if we have a result
        if(result.getResult() != null)
        {
            numberPicker.setValue(result.getResult());
        }

        return numberPicker;
    }

    private View initViewCompact(LayoutInflater inflater, ViewGroup parent)
    {
        View formItemView = inflater.inflate(R.layout.item_edit_text, parent, false);

        editText = (EditText) formItemView.findViewById(R.id.value);
        editText.setSingleLine(true);
        editText.setHint(step.getTitle());
        editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);

        if(result.getResult() != null)
        {
            editText.setText(String.valueOf(result.getResult()));
        }

        String minStr = Integer.toString(format.getMinValue());
        String maxStr = Integer.toString(format.getMaxValue());
        int maxLength = maxStr.length() >= minStr.length() ? maxStr.length() : minStr.length();
        InputFilter.LengthFilter maxLengthFilter = new InputFilter.LengthFilter(maxLength);
        InputFilter[] newFilters = ViewUtils.addFilter(editText.getFilters(), maxLengthFilter);
        editText.setFilters(newFilters);

        // TODO This will crash when typing not digit char (i.e. '.' or '-')
        // If we have a range, set a range filter
        if(format.getMaxValue() - format.getMinValue() > 0)
        {
            InputFilter rangeFilter = (source, start, end, dest, dstart, dend) -> {

                // If the source its empty, just continue, its probably a backspace
                if(TextUtils.isEmpty(source.toString()))
                {
                    return source;
                }

                // If the dest is empty and the incoming char isn't a digit, let it pass. Its
                // probably a negative sign
                if(dest.length() == 0 && ! TextUtils.isDigitsOnly(source))
                {
                    return source;
                }

                // Append source to dest and check the range.
                String valueStr = new StringBuilder(dest).append(source).toString();
                int value = Integer.parseInt(valueStr.toString());

                if(value > format.getMaxValue() || value < format.getMinValue())
                {
                    return "";
                }
                else
                {
                    return source;
                }
            };

            newFilters = ViewUtils.addFilter(editText.getFilters(), rangeFilter);
            editText.setFilters(newFilters);
        }

        return formItemView;
    }

    @Override
    public StepResult getStepResult()
    {
        if(viewType == VIEW_TYPE_DEFAULT)
        {
            result.setResult(numberPicker.getValue());
        }
        else if(viewType == VIEW_TYPE_COMPACT)
        {
            String numString = editText.getText().toString();
            if(! TextUtils.isEmpty(numString))
            {
                result.setResult(Integer.valueOf(editText.getText().toString()));
            }
        }
        else
        {
            throw new IllegalArgumentException("Invalid View Type");
        }

        return result;
    }

    @Override
    public boolean isAnswerValid()
    {
        Integer result = null;

        if(numberPicker != null)
        {
            result = numberPicker.getValue();
        }
        else if(editText != null && editText.getText().length() > 0)
        {
            result = Integer.valueOf(editText.getText().toString());
        }

        if(result == null)
        {
            return false;
        }

        return result >= format.getMinValue() && result <= format.getMaxValue();
    }

}
