package dtancompany.gallerytest;


import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by david on 2015-07-28.
 */
public class KeywordNode {
    boolean isRowStart;
    KeywordNode parent = null;
    KeywordNode childRight = null;
    KeywordNode childBelow = null;
    TextView textView;
    int margin;

    KeywordNode(TextView textView, int margin) {
        this(textView,null,true, margin);
    }

    //this is used if you want to define a parent
    KeywordNode(TextView textView, KeywordNode parent, boolean isRowStart, int margin) {
        this.textView = textView;
        this.parent = parent;
        this.isRowStart = isRowStart;
        this.margin = margin;
        textView.setId(View.generateViewId());
        if (parent != null) {
            if (isRowStart) {
                parent.childBelow = this;
            } else {
                parent.childRight = this;
            }
            parent.update();
        }
        update();
    }

    //copy constructor
    KeywordNode(KeywordNode original) {
        TextView originalTV = original.textView;
        textView = new TextView(originalTV.getContext());
        textView.setText(originalTV.getText());
        textView.setLayoutParams(originalTV.getLayoutParams());
        textView.setPadding(0, 0, 0, 0);
        textView.setId(View.generateViewId()); //id is unique
        parent = original.parent;
        isRowStart = original.isRowStart;
        margin = original.margin;
    }

    //returns replacement
    public KeywordNode destroy() {
        KeywordNode replacement = null;

        // try to replace this node with right first, then below
        if (childRight != null) {
            replacement = childRight;
            //replacement takes the child below
            if (childBelow != null) {
                replacement.childBelow = this.childBelow;
                childBelow.parent = replacement;
                childBelow.update();
            }
        } else if (childBelow != null) {
            replacement = childBelow;
        }
        if (replacement != null) {
            replacement.parent = this.parent;
            replacement.isRowStart = this.isRowStart;
            replacement.update();
        }

        if (parent != null) {
            if (this.isRowStart) {
                parent.childBelow = replacement;
            } else {
                parent.childRight = replacement;
            }
            parent.update();
        }

        RelativeLayout container = (RelativeLayout) textView.getParent();
        container.removeView(textView);

        //remove references just in case (?)
        parent = null;
        childBelow = null;
        childRight = null;
        textView = null;
        return replacement;
    }

    public void update() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //update margins
        params.setMargins(0,0,0, childBelow == null ? 0 : margin);
        params.setMarginEnd(childRight == null ? 0 : margin);

        //update relative placement
        if (isRowStart) {
            params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
            if (parent == null) {
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            } else {
                params.addRule(RelativeLayout.BELOW, parent.textView.getId());
            }
        } else {
            params.addRule(RelativeLayout.END_OF, parent.textView.getId());
            params.addRule(RelativeLayout.ALIGN_BASELINE, parent.textView.getId());
        }

        textView.setLayoutParams(params);
    }
}
